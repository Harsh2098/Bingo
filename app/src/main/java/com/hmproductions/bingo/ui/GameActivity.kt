package com.hmproductions.bingo.ui

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
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
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.GridLayoutAnimationController
import butterknife.ButterKnife
import butterknife.OnClick
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseAuth.getInstance
import com.google.firebase.database.*
import com.hmproductions.bingo.BingoActionServiceGrpc
import com.hmproductions.bingo.BingoStreamServiceGrpc
import com.hmproductions.bingo.R
import com.hmproductions.bingo.actions.*
import com.hmproductions.bingo.actions.ClickGridCell.ClickGridCellRequest
import com.hmproductions.bingo.actions.ClickGridCell.ClickGridCellResponse
import com.hmproductions.bingo.adapter.ChatRecyclerAdapter
import com.hmproductions.bingo.adapter.GameGridRecyclerAdapter
import com.hmproductions.bingo.adapter.LeaderboardRecyclerAdapter
import com.hmproductions.bingo.animations.StrikeAnimation
import com.hmproductions.bingo.dagger.ContextModule
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent
import com.hmproductions.bingo.data.*
import com.hmproductions.bingo.datastreams.GameEventUpdate
import com.hmproductions.bingo.models.GameEvent.EventCode.*
import com.hmproductions.bingo.models.GameSubscription
import com.hmproductions.bingo.ui.main.MainActivity
import com.hmproductions.bingo.ui.main.RoomFragment
import com.hmproductions.bingo.ui.settings.SettingsFragment
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
import kotlinx.android.synthetic.main.chat_layout.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
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
    private var chatRecyclerAdapter: ChatRecyclerAdapter? = null
    private var gameTimer: CountDownTimer? = null

    private var playerId = -1
    private var roomId = -1
    private var currentRoomName = ""
    private var messageCount = 0
    private var currentTimeLimit: TimeLimitUtils.TIME_LIMIT = TimeLimitUtils.TIME_LIMIT.MINUTE_1

    private var gameCompleted = false
    private var myTurn = false
    private var wasDisconnected = true
    private var gameTimerStarted = false
    private var chatListenerAttached = false

    private var gameGridCellList = ArrayList<GridCell>()
    private var playersList = ArrayList<Player>()

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var snackBar: Snackbar
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CardView>

    private var chatDatabaseReference: DatabaseReference? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseAuthStateListener: AuthStateListener
    private lateinit var firebaseChildEventListener: ChildEventListener

    private lateinit var afterGameInterstitialAd: InterstitialAd

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
                                    .addColors(ContextCompat.getColor(this@GameActivity, R.color.player_not_ready), ContextCompat.getColor(this@GameActivity, R.color.gold_shimmer),
                                            ContextCompat.getColor(this@GameActivity, R.color.neon_blue), ContextCompat.getColor(this@GameActivity, R.color.neon_orange), ContextCompat.getColor(this@GameActivity, R.color.neon_green))
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

                        leaderBoardRecyclerView.adapter = LeaderboardRecyclerAdapter(this@GameActivity, intent.getParcelableArrayListExtra(LEADER_BOARD_LIST_KEY))
                        leaderBoardRecyclerView.layoutManager = GridLayoutManager(this@GameActivity,
                                if (isTablet(this@GameActivity)) LEADERBOARD_TAB_COL_SPAN else LEADERBOARD_COL_SPAN)
                        leaderBoardRecyclerView.setHasFixedSize(true)

                        if (gameCompleted) {
                            val winnerTextBuilder = StringBuilder(turnOrderTextView.text.toString())
                            winnerTextBuilder.insert(winnerTextBuilder.lastIndexOf(" "), ", ${getNameFromId(playersList, winnerId)!!}")
                            turnOrderTextView.text = winnerTextBuilder.toString()

                            if (turnOrderTextView.text.toString().length > 27) {
                                turnOrderTextView.text = shortenName(turnOrderTextView.text.toString(), 27)
                            }
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
                        toast(getNameFromId(playersList, winnerId) + " left")
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

                        startGameTimer(myTurn)

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
        snackBar.view.backgroundColor = ContextCompat.getColor(this@GameActivity, R.color.dark_blue_background)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)

        gridRecyclerAdapter = GameGridRecyclerAdapter(this, GRID_SIZE, gameGridCellList, this)
        chatRecyclerAdapter = ChatRecyclerAdapter(this, null)

        with(gameRecyclerView) {
            adapter = gridRecyclerAdapter
            layoutManager = GridLayoutManager(this@GameActivity, GRID_SIZE)
            layoutAnimation = AnimationUtils.loadLayoutAnimation(this@GameActivity, R.anim.game_grid_animation) as GridLayoutAnimationController
            setHasFixedSize(true)
        }

        setupFirebaseChatEventListener()
        setupChatBottomSheet()

        speechRecognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)

        setupAds()
    }

    // Setting up bottom sheet
    private fun setupChatBottomSheet() {

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        with(chatRecyclerView) {
            layoutManager = LinearLayoutManager(this@GameActivity)
            adapter = chatRecyclerAdapter
            setHasFixedSize(false)
        }

        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
                sendButton.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
            }
        })

        messageEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT))

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HIDDEN) {
                    quitButton.show()
                    if (gameCompleted) nextRoundButton.show()
                    contentView?.hideKeyboard()

                } else {
                    quitButton.hide()
                    nextRoundButton.hide()
                }

                notificationBubbleTextView.visibility = View.GONE
            }
        })

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        chatDateTextView.text = SimpleDateFormat("dd MMM", Locale.US).format(calendar.time)
    }

    // Setting up auth state listener and child event listener for chat
    private fun setupFirebaseChatEventListener() {

        firebaseAuth = getInstance()
        chatDatabaseReference = FirebaseDatabase.getInstance().reference.child("chats").child(roomId.toString())

        firebaseChildEventListener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                showChatRecyclerView(true)
                chatRecyclerAdapter?.addMessage(dataSnapshot.getValue(Message::class.java))
                chatRecyclerView.smoothScrollToPosition(chatRecyclerAdapter?.itemCount ?: 1-1)
                messageCount++

                if (((bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) ||
                                (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)) && messageCount > READ_COUNT) {
                    notificationBubbleTextView.visibility = View.VISIBLE
                    notificationBubbleTextView.text = if (messageCount - READ_COUNT > 9) "9+" else (messageCount - READ_COUNT).toString()
                } else {
                    if (READ_COUNT < messageCount)
                        READ_COUNT = messageCount
                }
            }
        }

        firebaseAuthStateListener = FirebaseAuth.AuthStateListener { _ ->
            if (firebaseAuth.currentUser != null && !chatListenerAttached) {
                chatDatabaseReference?.addChildEventListener(firebaseChildEventListener)
                chatListenerAttached = true
            } else {
                firebaseAuth.signInAnonymously().addOnCompleteListener(this@GameActivity) {
                    if (it.isSuccessful && !chatListenerAttached) {
                        chatDatabaseReference?.addChildEventListener(firebaseChildEventListener)
                        chatListenerAttached = true

                    } else {
                        showChatRecyclerView(false)
                        chatDatabaseReference?.removeEventListener(firebaseChildEventListener)
                        chatListenerAttached = false
                        snackBar.setText("Chat functionality suspended")
                        showSnackBar(true)
                        Handler().postDelayed({
                            showSnackBar(false)
                            snackBar.setText("Internet connection unavailable")
                        }, 2000)
                    }
                }
            }
        }
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

        gameTimer = object : CountDownTimer((totalTime + 1000), 10) {

            override fun onTick(millisUntilFinished: Long) {
                timeLimitProgressBar.progress = (totalTime - millisUntilFinished).toInt()
                currentTimeTextView.text = "${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                timeLimitProgressBar.progress = timeLimitProgressBar.max
                currentTimeTextView.text = "${getExactValueFromEnum(currentTimeLimit) / 1000}"

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

                runOnUiThread {
                    showSnackBar(false)
                    doubleBounceProgressBar.visibility = View.INVISIBLE
                    chatButton.visibility = View.VISIBLE
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                if (!getConnectionInfo(this@GameActivity)) wasDisconnected = true
                runOnUiThread {
                    doubleBounceProgressBar.visibility = View.VISIBLE
                    chatButton.visibility = View.GONE
                    onNetworkDownError()
                }
            }
        }
    }

    private fun startGameTimer(realTimer: Boolean) {
        timeLimitProgressBar.isIndeterminate = false
        val totalTime = getExactValueFromEnum(currentTimeLimit)
        timeLimitProgressBar.progress = 0
        timeLimitProgressBar.max = totalTime.toInt()

        if (realTimer) {
            if (gameTimerStarted) {
                gameTimer?.cancel()
            }
            gameTimer?.start()
            gameTimerStarted = true
        }
    }

    private fun setupAds() {

        // TODO (Release): Interstitial Ad Unit ID
        afterGameInterstitialAd = InterstitialAd(this)

        afterGameInterstitialAd.adUnitId = getString(R.string.sample_interstitial_ad_id)
        afterGameInterstitialAd.loadAd(AdRequest.Builder().build())
        afterGameInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                toast("Waiting for other players")
                Handler().postDelayed({ startNextRoundAsynchronously() }, 500)
            }
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
            val animation = StrikeAnimation(oStrikeView)
            animation.duration = 1000
            oStrikeView.startAnimation(animation)
            playRowCompleted = true
        }
        if (counter >= 4 && !gStrikeView.finishedAnimation()) {
            val animation = StrikeAnimation(gStrikeView)
            animation.duration = 1000
            gStrikeView.startAnimation(animation)
            playRowCompleted = true
        }
        if (counter >= 3 && !nStrikeView.finishedAnimation()) {
            val animation = StrikeAnimation(nStrikeView)
            animation.duration = 1000
            nStrikeView.startAnimation(animation)
            playRowCompleted = true
        }
        if (counter >= 2 && !iStrikeView.finishedAnimation()) {
            val animation = StrikeAnimation(iStrikeView)
            animation.duration = 1000
            iStrikeView.startAnimation(animation)
            playRowCompleted = true
        }
        if (counter >= 1 && !bStrikeView.finishedAnimation()) {
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
        AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setCancelable(false)
                .setTitle("Confirm Quit")
                .setMessage("Game will be cancelled. Do you want to forfeit ?")
                .setPositiveButton(R.string.quit) { _, _ -> quitPlayerAsynchronously() }
                .setNegativeButton(R.string.no) { dI, _ -> dI.dismiss() }
                .show()
    }

    @OnClick(R.id.chatButton)
    fun onChatButtonClick() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        READ_COUNT = messageCount
    }

    @OnClick(R.id.nextRoundButton)
    fun onNextRoundButtonClick() {
        with(afterGameInterstitialAd) {
            if (isLoaded) {
                show()
            } else {
                Log.v(CLASSIC_TAG, "Did not load !")
                adListener.onAdClosed()
            }
        }
    }

    @OnClick(R.id.talkToSpeakImageButton)
    fun onImageButtonClick() {
        if (myTurn && preferences.getBoolean(getString(R.string.tts_preference_key), false))
            speechRecognizer.startListening(speechRecognitionIntent)
        else if (myTurn && !preferences.getBoolean(getString(R.string.tts_preference_key), false))
            AlertDialog.Builder(this, R.style.CustomAlertDialog)
                    .setCancelable(true)
                    .setTitle("Mic is disabled")
                    .setMessage("Do you want to enable text to speech feature ?")
                    .setPositiveButton(R.string.enable) { _, _ ->
                        ActivityCompat.requestPermissions(
                                this, arrayOf(Manifest.permission.RECORD_AUDIO), SettingsFragment.RECORD_AUDIO_RC)
                    }
                    .setNegativeButton(R.string.no) { dI, _ -> dI.dismiss() }
                    .show()
        else
            toast("Not your turn")
    }

    @OnClick(R.id.sendButton)
    fun onSendMessageClick() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val time = SimpleDateFormat("hh:mm a", Locale.US).format(calendar.time)

        val newMessage = Message(messageEditText.text.toString(), getNameFromId(playersList, playerId), time)
        chatDatabaseReference?.push()?.setValue(newMessage)

        messageEditText.setText("")
    }

    @OnClick(R.id.hideChatButton)
    fun onHideChatButtonClick() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun onGridCellClick(value: Int) {
        if (preferences.getBoolean(getString(R.string.tts_preference_key), false)) speechRecognizer.stopListening()
        if (myTurn && !valueClicked(gameGridCellList, value)) clickCellAsynchronously(value)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == SettingsFragment.RECORD_AUDIO_RC && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            preferences.edit().putBoolean(getString(R.string.tts_preference_key), true).apply()
            onImageButtonClick()
        } else {
            toast("Permission required for TTS")
        }
    }

    // Click grid cell request
    private fun clickCellAsynchronously(value: Int) {
        var hide = true

        Handler().postDelayed({
            if (hide) {
                doubleBounceProgressBar.visibility = View.VISIBLE
                chatButton.visibility = View.GONE
            }
        }, 500)

        doAsync {
            if (getConnectionInfo(this@GameActivity) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {
                val data = actionServiceBlockingStub.clickGridCell(ClickGridCellRequest.newBuilder().setRoomId(roomId)
                        .setPlayerId(playerId).setCellClicked(value).build())

                uiThread {
                    hide = false
                    doubleBounceProgressBar.visibility = View.INVISIBLE
                    chatButton.visibility = View.VISIBLE

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
        }, 30000)
    }

    private fun showSnackBar(show: Boolean) = if (show) {
        quitButton.hide()
        nextRoundButton.hide()
        sendButton.hide()
        snackBar.show()
    } else {
        quitButton.show()
        nextRoundButton.show()
        sendButton.show()
        snackBar.dismiss()
    }

    private fun makeBingoLookSmaller() {

        if (resources.configuration.screenHeightDp >= 600) {
            bLetterTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32F)
            iLetterTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32F)
            nLetterTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32F)
            gLetterTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32F)
            oLetterTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32F)
        } else {
            bingoLinearLayout.visibility = View.GONE
        }
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

    private fun showChatRecyclerView(show: Boolean) {
        chatRecyclerView.visibility = if (show) View.VISIBLE else View.INVISIBLE
        startConversationTextView.visibility = if (show) View.INVISIBLE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(firebaseAuthStateListener)
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        LocalBroadcastManager.getInstance(this).registerReceiver(gridCellReceiver,
                IntentFilter(Constants.GRID_CELL_CLICK_ACTION))
    }

    override fun onPause() {
        super.onPause()
        chatDatabaseReference?.removeEventListener(firebaseChildEventListener)
        firebaseAuth.removeAuthStateListener(firebaseAuthStateListener)
        chatListenerAttached = false
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
    }

    override fun onBackPressed() {
        if(bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            onHideChatButtonClick()
            return
        }
        onQuitButtonClick()
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
                try {
                    val currentWordInt = currentWord.toInt()
                    if (currentWordInt >= 1 && currentWordInt <= GRID_SIZE * GRID_SIZE) {
                        onGridCellClick(currentWordInt)
                        foundMatch = true
                    }
                } catch (e: NumberFormatException) {

                }
            }
        }

        if (!foundMatch)
            speechRecognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH))
    }
}