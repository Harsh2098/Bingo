package com.hmproductions.bingo.ui

import android.content.*
import android.graphics.Color
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.GridLayoutAnimationController
import butterknife.ButterKnife
import butterknife.OnClick
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.gms.common.util.NumberUtils
import com.hmproductions.bingo.BingoActionServiceGrpc
import com.hmproductions.bingo.BingoStreamServiceGrpc
import com.hmproductions.bingo.R
import com.hmproductions.bingo.actions.*
import com.hmproductions.bingo.actions.ClickGridCell.ClickGridCellRequest
import com.hmproductions.bingo.actions.ClickGridCell.ClickGridCellResponse
import com.hmproductions.bingo.adapter.GameGridRecyclerAdapter
import com.hmproductions.bingo.adapter.LeaderboardRecyclerAdapter
import com.hmproductions.bingo.animations.StrikeAnimation
import com.hmproductions.bingo.dagger.ContextModule
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent
import com.hmproductions.bingo.data.GridCell
import com.hmproductions.bingo.data.LeaderboardPlayer
import com.hmproductions.bingo.data.Player
import com.hmproductions.bingo.datastreams.GameEventUpdate
import com.hmproductions.bingo.models.GameEvent.EventCode.*
import com.hmproductions.bingo.models.GameSubscription
import com.hmproductions.bingo.ui.main.MainActivity
import com.hmproductions.bingo.ui.main.RoomFragment
import com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo
import com.hmproductions.bingo.utils.ConnectionUtils.isReachableByTcp
import com.hmproductions.bingo.utils.Constants
import com.hmproductions.bingo.utils.Constants.*
import com.hmproductions.bingo.utils.Miscellaneous.*
import com.hmproductions.bingo.utils.TimeLimitUtils
import com.hmproductions.bingo.utils.TimeLimitUtils.*
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import io.grpc.stub.StreamObserver
import kotlinx.android.synthetic.main.activity_game.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import java.util.*
import javax.inject.Inject

class GameActivity : AppCompatActivity(), GameGridRecyclerAdapter.GridCellClickListener, RecognitionListener {

    companion object {
        const val PLAYER_ID = "player-id"
        const val ROOM_ID = "room-id"
        const val TIME_LIMIT_ID = "time-limit-id"
        const val ROOM_NAME_EXTRA_KEY = "room-name-extra-key"

        const val PLAYERS_LIST_ID = "players-list-id"
        private const val LEADER_BOARD_LIST_KEY = "leader-board-list-key"

        const val CELL_CLICKED_ID = "cell-clicked-id"
        const val WON_ID = "won-id"
        const val CURRENT_PLAYER_ID = "current-player-id"
        const val EVENT_CODE_ID = "event-code-id"
    }

    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var streamServiceStub: BingoStreamServiceGrpc.BingoStreamServiceStub

    @Inject
    lateinit var actionServiceBlockingStub: BingoActionServiceGrpc.BingoActionServiceBlockingStub

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    @Inject
    lateinit var networkRequest: NetworkRequest

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognitionIntent: Intent

    private lateinit var celebrationSound: MediaPlayer
    private lateinit var popSound: MediaPlayer
    private lateinit var rowCompletedSound: MediaPlayer

    private var gridRecyclerAdapter: GameGridRecyclerAdapter? = null
    private var gameTimer: CountDownTimer? = null

    private var playerId = -1
    private var roomId = -1
    private var currentRoomName = ""
    private var currentTimeLimit: TimeLimitUtils.TIME_LIMIT = TimeLimitUtils.TIME_LIMIT.MINUTE_1

    private var gameCompleted = false
    private var myTurn = false
    private var wasDisconnected = true
    private var gameTimerStarted = false

    private var gameGridCellList = ArrayList<GridCell>()
    private var playersList = ArrayList<Player>()

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var snackBar: Snackbar

    private val gridCellReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (intent.action != null && intent.action == Constants.GRID_CELL_CLICK_ACTION) {

                val currentPlayerId = intent.getIntExtra(CURRENT_PLAYER_ID, -1)
                val cellClicked = intent.getIntExtra(CELL_CLICKED_ID, -1)
                val winnerId = intent.getIntExtra(WON_ID, -1)

                when (intent.getIntExtra(EVENT_CODE_ID, -1)) {

                    GAME_WON_VALUE -> {

                        /*  1. Celebrations begin if player has won
                            2. Sets up leader board recycler view nad hides BINGO linear layout
                            3. Sets turn order text to winner names
                         */

                        if (winnerId == playerId) {
                            if (preferences.getBoolean(getString(R.string.sound_preference_key), true))
                                celebrationSound.start()

                            konfettiView.build()
                                    .addColors(Color.parseColor("#9162e4"), Color.YELLOW, Color.RED)
                                    .setDirection(0.0, 359.0)
                                    .setSpeed(3f, 6f)
                                    .setFadeOutEnabled(true)
                                    .setTimeToLive(3000)
                                    .addShapes(Shape.RECT, Shape.CIRCLE)
                                    .addSizes(Size(12, 5f))
                                    .setPosition(-50f, konfettiView.width + 50f, -50f, -50f)
                                    .streamFor(400, 2000)
                        }

                        gameRecyclerView.isEnabled = false
                        myTurn = false

                        makeBingoLookSmaller()
                        leaderBoardRecyclerView.visibility = View.VISIBLE
                        nextRoundButton.show()
                        startNextRoundButtonTapTargetView()

                        leaderBoardRecyclerView.layoutManager = GridLayoutManager(this@GameActivity, LEADERBOARD_COL_SPAN)
                        leaderBoardRecyclerView.adapter = LeaderboardRecyclerAdapter(this@GameActivity, intent.getParcelableArrayListExtra(LEADER_BOARD_LIST_KEY))
                        leaderBoardRecyclerView.setHasFixedSize(true)

                        if (gameCompleted) {
                            val winnerTextBuilder = StringBuilder(turnOrderTextView.text.toString())
                            winnerTextBuilder.insert(winnerTextBuilder.lastIndexOf(" "), ", ${getNameFromId(playersList, winnerId)!!}")
                            turnOrderTextView.text = winnerTextBuilder.toString()
                        } else {
                            turnOrderTextView.text = if (winnerId == playerId) "You won" else "${getNameFromId(playersList, winnerId)!!} won"
                            gameCompleted = true
                        }
                        gameTimer?.cancel()
                    }

                    PLAYER_QUIT_VALUE -> {
                        // Here winner ID refers to the ID of player who has quit
                        val quitIntent = Intent(this@GameActivity, MainActivity::class.java)
                        quitIntent.action = Constants.QUIT_GAME_ACTION

                        quitIntent.putExtra(RoomFragment.TIME_LIMIT_BUNDLE_KEY, getValueFromEnum(currentTimeLimit))
                        quitIntent.putExtra(MainActivity.PLAYER_LEFT_KEY, playerId == winnerId)
                        quitIntent.putExtra(ROOM_NAME_EXTRA_KEY, currentRoomName)

                        if (currentPlayerId == winnerId) {
                            startActivity(quitIntent)
                            finish()
                            return
                        }

                        quitIntent.putExtra(PLAYER_ID, playerId)
                        quitIntent.putExtra(ROOM_ID, roomId)

                        gameTimer?.cancel()
                        startActivity(quitIntent)
                        finish()
                    }

                    CELL_CLICKED_VALUE -> {

                        if (preferences.getBoolean(getString(R.string.sound_preference_key), true) && cellClicked != TURN_SKIPPED_CODE)
                            popSound.start()

                        for (gridCell in gameGridCellList) {
                            if (gridCell.value == cellClicked) {
                                gridCell.isClicked = true
                                gridCell.color = getColorFromNextPlayerId(playersList, currentPlayerId)
                                gridRecyclerAdapter?.swapData(gameGridCellList, gameGridCellList.indexOf(gridCell))
                                break
                            }
                        }

                        if (numberOfLinesCompleted() == 5 && !gameCompleted)
                            broadcastWinnerAsynchronously()

                        myTurn = currentPlayerId == playerId

                        startGameTimer(myTurn) // TODO : Create separate layout for 4 inch phone

                        if (myTurn) {

                            if (preferences.getBoolean(getString(R.string.tts_preference_key), false)) speechRecognizer.startListening(speechRecognitionIntent)
                            gameRecyclerView.isEnabled = true

                        } else {
                            gameTimer?.cancel()
                            gameRecyclerView.isEnabled = false
                        }

                        turnOrderTextView.text = if (currentPlayerId == playerId) "Your turn" else "${getNameFromId(playersList, currentPlayerId)!!}\'s turn"
                    }

                    GAME_STARTED_VALUE -> {

                        if (!gameCompleted) {
                            myTurn = currentPlayerId == playerId

                            startGameTimer(myTurn)

                            if (myTurn) {

                                if (preferences.getBoolean(getString(R.string.tts_preference_key), false))
                                    speechRecognizer.startListening(speechRecognitionIntent)
                                else
                                    gameRecyclerView.isEnabled = true
                            } else {
                                gameTimer?.cancel()
                                gameRecyclerView.isEnabled = false
                            }

                            nextRoundButton.hide()

                            startMicTapTargetView()

                            turnOrderTextView.text = if (currentPlayerId == playerId) "Your turn" else "${getNameFromId(playersList, currentPlayerId)!!} \'s turn"
                        } else {
                            myTurn = false
                        }
                    }

                    NEXT_ROUND_VALUE -> recreate()

                    else -> toast("Internal server error")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        ButterKnife.bind(this)

        DaggerBingoApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)

        roomId = intent.getIntExtra(ROOM_ID, -1)
        playerId = intent.getIntExtra(PLAYER_ID, -1)
        currentTimeLimit = getEnumFromValue(intent.getIntExtra(TIME_LIMIT_ID, 2))
        currentRoomName = intent.getStringExtra(ROOM_NAME_EXTRA_KEY)
        playersList = intent.getParcelableArrayListExtra(PLAYERS_LIST_ID)

        // Creates an ArrayList made up of random values
        createGameGridArrayList()
        createGameTimer()
        createNetworkCallback()
        snackBar = Snackbar.make(findViewById<View>(android.R.id.content), "Internet connection unavailable", Snackbar.LENGTH_INDEFINITE)

        gridRecyclerAdapter = GameGridRecyclerAdapter(this, GRID_SIZE, gameGridCellList, this)

        with(gameRecyclerView) {
            layoutManager = GridLayoutManager(this@GameActivity, GRID_SIZE)
            layoutAnimation = AnimationUtils.loadLayoutAnimation(this@GameActivity, R.anim.game_grid_animation) as GridLayoutAnimationController
            adapter = gridRecyclerAdapter
            setHasFixedSize(true)
        }

        speechRecognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)
    }

    // Creates an ArrayList of GridCell using int[][] made by CreateRandomGameArray()
    private fun createGameGridArrayList() {

        // Returns an array with numbers 1 to GRID_SIZE * GRID_SIZE randomly placed in it
        val randomArray = CreateRandomGameArray(GRID_SIZE)

        for (i in 0 until GRID_SIZE * GRID_SIZE) {
            gameGridCellList.add(GridCell(randomArray[i], false))
        }
    }

    private fun createGameTimer() {
        val totalTime = getExactValueFromEnum(currentTimeLimit)

        gameTimer = object : CountDownTimer(((totalTime + 1) * 1000).toLong(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeLimitProgressBar.progress = (totalTime - millisUntilFinished / 1000).toInt()
                currentTimeTextView.text = "${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                timeLimitProgressBar.progress = timeLimitProgressBar.max
                currentTimeTextView.text = "${getExactValueFromEnum(currentTimeLimit)}"

                clickCellAsynchronously(TURN_SKIPPED_CODE)
            }
        }
    }

    private fun createNetworkCallback() {

        networkCallback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                doAsync {
                    val metadata = Metadata()
                    val metadataKey: Metadata.Key<String> = Metadata.Key.of(SESSION_ID_KEY, Metadata.ASCII_STRING_MARSHALLER)
                    metadata.put(metadataKey, Constants.SESSION_ID)

                    val data = MetadataUtils.attachHeaders(actionServiceBlockingStub, metadata).reconnect(ReconnectRequest.newBuilder().setSessionsId(Constants.SESSION_ID).build())
                    uiThread {
                        if (data.statusCode == ReconnectResponse.StatusCode.SESSION_ID_NOT_EXIST) {
                            startActivity(Intent(this@GameActivity, SplashActivity::class.java))
                            Constants.SESSION_ID = null
                            finish()
                        }
                    }
                }
                subscribeToGameEventUpdates(playerId, roomId)

                runOnUiThread { showSnackBar(false) }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                if (!getConnectionInfo(this@GameActivity)) wasDisconnected = true
                runOnUiThread { onNetworkDownError() }
            }
        }
    }

    private fun startGameTimer(realTimer: Boolean) {
        timeLimitProgressBar.isIndeterminate = false
        val totalTime = getExactValueFromEnum(currentTimeLimit)
        timeLimitProgressBar.progress = 0
        timeLimitProgressBar.max = totalTime

        if (realTimer) {
            if (gameTimerStarted) {
                gameTimer?.cancel()
            }
            gameTimer?.start()
            gameTimerStarted = true
        }
    }

    // Returns the number of lines completed
    fun numberOfLinesCompleted(): Int {

        var counter = 0

        // Checking for columns
        for (i in 0 until GRID_SIZE) {
            var columnFormed = true
            for (j in 0 until GRID_SIZE)
                if (!gameGridCellList[j * GRID_SIZE + i].isClicked) {
                    columnFormed = false
                    break
                }
            if (columnFormed) counter++
        }

        // Checking for rows
        for (i in 0 until GRID_SIZE) {
            var rowFormed = true
            for (j in 0 until GRID_SIZE) {
                if (!gameGridCellList[i * GRID_SIZE + j].isClicked) {
                    rowFormed = false
                    break
                }
            }
            if (rowFormed) counter++
        }

        // Checking for diagonals
        var mainDiagonalFormed = true
        for (i in 0 until GRID_SIZE) {
            if (!gameGridCellList[i * GRID_SIZE + i].isClicked) {
                mainDiagonalFormed = false
                break
            }
        }
        if (mainDiagonalFormed) counter++

        var secondaryDiagonalFormed = true
        for (i in 0 until GRID_SIZE) {
            if (!gameGridCellList[i * GRID_SIZE + (GRID_SIZE - i - 1)].isClicked) {
                secondaryDiagonalFormed = false
                break
            }
        }
        if (secondaryDiagonalFormed) counter++

        if (counter > GRID_SIZE) counter = GRID_SIZE

        var playRowCompleted = false

        if (counter >= GRID_SIZE) {
            oLetterTextView.textColor = Color.parseColor("#FF0000")
            val animation = StrikeAnimation(oStrikeView)
            animation.duration = 1000
            oStrikeView.startAnimation(animation)
            playRowCompleted = true
        }
        if (counter >= 4 && !gStrikeView.finishedAnimation()) {
            gLetterTextView.textColor = Color.parseColor("#FF0000")
            val animation = StrikeAnimation(gStrikeView)
            animation.duration = 1000
            gStrikeView.startAnimation(animation)
            playRowCompleted = true
        }
        if (counter >= 3 && !nStrikeView.finishedAnimation()) {
            nLetterTextView.textColor = Color.parseColor("#FF0000")
            val animation = StrikeAnimation(nStrikeView)
            animation.duration = 1000
            nStrikeView.startAnimation(animation)
            playRowCompleted = true
        }
        if (counter >= 2 && !iStrikeView.finishedAnimation()) {
            iLetterTextView.textColor = Color.parseColor("#FF0000")
            val animation = StrikeAnimation(iStrikeView)
            animation.duration = 1000
            iStrikeView.startAnimation(animation)
            playRowCompleted = true
        }
        if (counter >= 1 && !bStrikeView.finishedAnimation()) {
            bLetterTextView.textColor = Color.parseColor("#FF0000")
            val animation = StrikeAnimation(bStrikeView)
            animation.duration = 1000
            bStrikeView.startAnimation(animation)
            playRowCompleted = true
        }

        if (playRowCompleted && preferences.getBoolean(getString(R.string.sound_preference_key), true))
            rowCompletedSound.start()

        return counter
    }

    @OnClick(R.id.quitButton)
    fun onQuitButtonClick() {
        AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Confirm Quit")
                .setMessage("Game will be cancelled. Do you want to forfeit ?")
                .setPositiveButton(R.string.quit) { _, _ -> quitPlayerAsynchronously() }
                .setNegativeButton(R.string.no) { dI, _ -> dI.dismiss() }
                .show()
    }

    @OnClick(R.id.nextRoundButton)
    fun onNextRoundButtonClick() {
        toast("Waiting for other players")
        startNextRoundAsynchronously()
    }

    @OnClick(R.id.talkToSpeakImageButton)
    fun onImageButtonClick() {
        if (myTurn && preferences.getBoolean(getString(R.string.tts_preference_key), false))
            speechRecognizer.startListening(speechRecognitionIntent)
        else if (myTurn && !preferences.getBoolean(getString(R.string.tts_preference_key), false))
            toast("Mic is disabled")
        else
            toast("Not your turn")
    }

    override fun onGridCellClick(value: Int) {
        if (preferences.getBoolean(getString(R.string.tts_preference_key), false)) speechRecognizer.stopListening()
        if (myTurn && !valueClicked(gameGridCellList, value)) clickCellAsynchronously(value)
    }

    // Click grid cell request 
    private fun clickCellAsynchronously(value: Int) {
        doAsync {
            if (getConnectionInfo(this@GameActivity) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {
                val data = actionServiceBlockingStub.clickGridCell(ClickGridCellRequest.newBuilder().setRoomId(roomId)
                        .setPlayerId(playerId).setCellClicked(value).build())

                uiThread {
                    if (data.statusCode == ClickGridCellResponse.StatusCode.INTERNAL_SERVER_ERROR || data.statusCode == ClickGridCellResponse.StatusCode.NOT_PLAYER_TURN)
                        toast(data.statusMessage)
                }
            } else {
                uiThread { onNetworkDownError() }
            }
        }
    }

    // Broadcast winner request
    private fun broadcastWinnerAsynchronously() {
        doAsync {
            if (getConnectionInfo(this@GameActivity) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {

                val data = actionServiceBlockingStub.broadcastWinner(BroadcastWinnerRequest.newBuilder().setRoomId(roomId)
                        .setPlayer(com.hmproductions.bingo.models.Player.newBuilder().setName(getNameFromId(playersList, playerId))
                                .setId(playerId).setColor(getColorFromId(playersList, playerId)).setReady(true).build()).build())

                uiThread {
                    if (data.statusCode == BroadcastWinnerResponse.StatusCode.INTERNAL_SERVER_ERROR) toast(data.statusMessage)
                }

            } else {
                uiThread { onNetworkDownError() }
            }
        }
    }

    // Quit player request
    private fun quitPlayerAsynchronously() {
        quitButton.startAnimation(AnimationUtils.loadAnimation(this@GameActivity, R.anim.clockwise_rotate))

        doAsync {
            if (getConnectionInfo(this@GameActivity) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {
                val data = actionServiceBlockingStub.quitPlayer(QuitPlayerRequest.newBuilder().setRoomId(roomId).setPlayer(com.hmproductions.bingo.models.Player.newBuilder().setColor(getColorFromId(playersList, playerId))
                        .setId(playerId).setReady(true).setName(getNameFromId(playersList, playerId)).setWinCount(0).build()).build())

                uiThread {
                    if (data.statusCode == QuitPlayerResponse.StatusCode.SERVER_ERROR) toast(data.statusMessage)
                }

            } else {
                uiThread { onNetworkDownError() }
            }
        }
    }

    // Next Round request 
    private fun startNextRoundAsynchronously() {
        doAsync {
            if (getConnectionInfo(this@GameActivity) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {
                val data = actionServiceBlockingStub.startNextRound(StartNextRoundRequest.newBuilder().setPlayerId(playerId).setRoomId(roomId).build())

                uiThread {
                    if (data.statusCode == StartNextRoundResponse.StatusCode.INTERNAL_SERVER_ERROR) toast(data.statusMessage)
                }

            } else {
                uiThread { onNetworkDownError() }
            }
        }
    }

    private fun startMicTapTargetView() {
        if (preferences.getBoolean(FIRST_TIME_PLAYED_KEY, true)) {
            TapTargetView.showFor(this,
                    TapTarget
                            .forView(findViewById(R.id.talkToSpeakImageButton), "How to use Mic", "Call out your number after the beep. Tap here if mic doesn't recognise number in the first time.")
                            .targetRadius(50)
                            .icon(getDrawable(R.drawable.mic_icon_white))
                            .cancelable(true),
                    object : TapTargetView.Listener() {
                        override fun onOuterCircleClick(view: TapTargetView?) {
                            super.onOuterCircleClick(view)
                            view!!.dismiss(false)
                        }
                    })

            val editor = preferences.edit()
            editor.putBoolean(FIRST_TIME_PLAYED_KEY, false)
            editor.putBoolean(getString(R.string.tutorial_preference_key), false)
            editor.apply()
        }
    }

    private fun startNextRoundButtonTapTargetView() {
        if (preferences.getBoolean(FIRST_TIME_WON_KEY, true)) {
            TapTargetView.showFor(this,
                    TapTarget
                            .forView(findViewById(R.id.nextRoundButton), "Next Round", "Tap to mark yourself ready and start next round")
                            .targetRadius(40)
                            .icon(getDrawable(R.drawable.next_icon))
                            .cancelable(true),
                    object : TapTargetView.Listener() {
                        override fun onTargetClick(view: TapTargetView) {
                            super.onTargetClick(view)
                            onNextRoundButtonClick()
                        }

                        override fun onOuterCircleClick(view: TapTargetView?) {
                            super.onOuterCircleClick(view)
                            view!!.dismiss(false)
                        }
                    })

            val editor = preferences.edit()
            editor.putBoolean(FIRST_TIME_WON_KEY, false)
            editor.apply()
        }
    }

    private fun onNetworkDownError() {
        showSnackBar(true)
        gameTimer?.cancel()

        Handler().postDelayed({
            if (!getConnectionInfo(this)) {
                startActivity(Intent(this, SplashActivity::class.java))
                Constants.SESSION_ID = null
                finish()
            }
        }, 30000) // TODO : What if after 30 secs the user has lost connection 2nd time
    }

    private fun showSnackBar(show: Boolean) = if (show) {
        findViewById<FloatingActionButton>(R.id.quitButton).hide()
        findViewById<FloatingActionButton>(R.id.nextRoundButton).hide()
        snackBar.show()
    } else {
        findViewById<FloatingActionButton>(R.id.quitButton).show()
        findViewById<FloatingActionButton>(R.id.nextRoundButton).show()
        snackBar.dismiss()
    }

    private fun makeBingoLookSmaller() {
        bLetterTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32F)
        iLetterTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32F)
        nLetterTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32F)
        gLetterTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32F)
        oLetterTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32F)
    }

    private fun subscribeToGameEventUpdates(playerId: Int, roomId: Int) {

        val gameSubscription = GameSubscription.newBuilder().setFirstSubscription(true).setWinnerId(-1)
                .setCellClicked(-1).setRoomId(roomId).setPlayerId(playerId).build()

        val metadata = Metadata()

        val metadataKey = Metadata.Key.of(PLAYER_ID_KEY, Metadata.ASCII_STRING_MARSHALLER)
        metadata.put(metadataKey, playerId.toString())

        MetadataUtils.attachHeaders(streamServiceStub, metadata).getGameEventUpdates(gameSubscription, object : StreamObserver<GameEventUpdate> {
            override fun onNext(value: GameEventUpdate) {

                val gameEvent = value.gameEvent
                val leaderboardPlayerArrayList = ArrayList<LeaderboardPlayer>()

                for (currentPlayer in gameEvent.leaderboardList) {
                    leaderboardPlayerArrayList.add(LeaderboardPlayer(currentPlayer.name, currentPlayer.color,
                            currentPlayer.winCount))
                }

                val intent = Intent(Constants.GRID_CELL_CLICK_ACTION)
                with(intent) {
                    putExtra(EVENT_CODE_ID, gameEvent.eventCodeValue)
                    putExtra(CELL_CLICKED_ID, gameEvent.cellClicked)
                    putExtra(CURRENT_PLAYER_ID, gameEvent.currentPlayerId)
                    putExtra(WON_ID, gameEvent.winner)
                    putParcelableArrayListExtra(LEADER_BOARD_LIST_KEY, leaderboardPlayerArrayList)
                }
                LocalBroadcastManager.getInstance(this@GameActivity).sendBroadcast(intent)
            }

            override fun onError(t: Throwable) {}

            override fun onCompleted() {}
        })
    }

    override fun onResume() {
        super.onResume()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        LocalBroadcastManager.getInstance(this).registerReceiver(gridCellReceiver,
                IntentFilter(Constants.GRID_CELL_CLICK_ACTION))
    }

    override fun onPause() {
        super.onPause()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gridCellReceiver)
    }

    override fun onStart() {
        super.onStart()
        celebrationSound = MediaPlayer.create(this, R.raw.tada_celebration)
        popSound = MediaPlayer.create(this, R.raw.pop)
        rowCompletedSound = MediaPlayer.create(this, R.raw.swoosh)
    }

    override fun onStop() {
        super.onStop()
        celebrationSound.release()
        popSound.release()
        rowCompletedSound.release()

        // TODO : Unsubscribe from game stream ?
    }

    // ================================== Speech Recognition Methods Implementations ==================================

    override fun onReadyForSpeech(bundle: Bundle) {}

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(v: Float) {}

    override fun onBufferReceived(bytes: ByteArray) {}

    override fun onEndOfSpeech() {}

    override fun onError(i: Int) {}

    override fun onResults(bundle: Bundle) = parseResults(bundle)

    override fun onPartialResults(bundle: Bundle) = parseResults(bundle)

    override fun onEvent(i: Int, bundle: Bundle) {}

    private fun parseResults(bundle: Bundle) {
        var foundMatch = false

        val speechResult = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

        if (speechResult != null) {
            for (currentWord in speechResult) {
                if (NumberUtils.isNumeric(currentWord) && Integer.parseInt(currentWord) >= 1 && Integer.parseInt(currentWord) <= GRID_SIZE * GRID_SIZE) {
                    onGridCellClick(Integer.parseInt(currentWord))
                    foundMatch = true
                }
            }
        }

        if (!foundMatch)
            speechRecognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH))
    }
}