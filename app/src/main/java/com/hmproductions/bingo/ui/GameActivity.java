package com.hmproductions.bingo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.common.util.NumberUtils;
import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.BingoStreamServiceGrpc;
import com.hmproductions.bingo.R;
import com.hmproductions.bingo.actions.BroadcastWinnerResponse;
import com.hmproductions.bingo.actions.ClickGridCell.ClickGridCellResponse;
import com.hmproductions.bingo.actions.QuitPlayerResponse;
import com.hmproductions.bingo.actions.StartNextRoundResponse;
import com.hmproductions.bingo.adapter.GameGridRecyclerAdapter;
import com.hmproductions.bingo.adapter.LeaderboardRecyclerAdapter;
import com.hmproductions.bingo.dagger.ContextModule;
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent;
import com.hmproductions.bingo.data.ClickCellRequest;
import com.hmproductions.bingo.data.GridCell;
import com.hmproductions.bingo.data.LeaderboardPlayer;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.datastreams.GameEventUpdate;
import com.hmproductions.bingo.loaders.BroadcastWinnerLoader;
import com.hmproductions.bingo.loaders.ClickCellLoader;
import com.hmproductions.bingo.loaders.NextRoundLoader;
import com.hmproductions.bingo.loaders.QuitLoader;
import com.hmproductions.bingo.models.GameEvent;
import com.hmproductions.bingo.models.GameSubscription;
import com.hmproductions.bingo.ui.main.MainActivity;
import com.hmproductions.bingo.utils.ConnectionUtils;
import com.hmproductions.bingo.utils.Constants;
import com.hmproductions.bingo.utils.TimeLimitUtils;
import com.hmproductions.bingo.views.GridRecyclerView;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.grpc.stub.StreamObserver;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

import static com.hmproductions.bingo.models.GameEvent.EventCode.CELL_CLICKED_VALUE;
import static com.hmproductions.bingo.models.GameEvent.EventCode.GAME_STARTED_VALUE;
import static com.hmproductions.bingo.models.GameEvent.EventCode.GAME_WON_VALUE;
import static com.hmproductions.bingo.models.GameEvent.EventCode.NEXT_ROUND_VALUE;
import static com.hmproductions.bingo.models.GameEvent.EventCode.PLAYER_QUIT_VALUE;
import static com.hmproductions.bingo.utils.Constants.FIRST_TIME_PLAYED_KEY;
import static com.hmproductions.bingo.utils.Constants.FIRST_TIME_WON_KEY;
import static com.hmproductions.bingo.utils.Constants.GRID_SIZE;
import static com.hmproductions.bingo.utils.Constants.LEADERBOARD_COL_SPAN;
import static com.hmproductions.bingo.utils.Constants.NEXT_ROUND_LOADER_ID;
import static com.hmproductions.bingo.utils.Constants.TURN_SKIPPED_CODE;
import static com.hmproductions.bingo.utils.Miscellaneous.CreateRandomGameArray;
import static com.hmproductions.bingo.utils.Miscellaneous.getColorFromId;
import static com.hmproductions.bingo.utils.Miscellaneous.getColorFromNextPlayerId;
import static com.hmproductions.bingo.utils.Miscellaneous.getNameFromId;
import static com.hmproductions.bingo.utils.Miscellaneous.valueClicked;
import static com.hmproductions.bingo.utils.TimeLimitUtils.getEnumFromValue;
import static com.hmproductions.bingo.utils.TimeLimitUtils.getExactValueFromEnum;

public class GameActivity extends AppCompatActivity implements
        GameGridRecyclerAdapter.GridCellClickListener,
        LoaderCallbacks<ClickGridCellResponse>,
        ConnectionUtils.OnNetworkDownHandler,
        RecognitionListener {

    public static final String PLAYER_ID = "player-id";
    public static final String ROOM_ID = "room-id";
    public static final String TIME_LIMIT_ID = "time-limit-id";

    public static final String PLAYERS_LIST_ID = "players-list-id";
    private static final String LEADERBOARD_LIST_KEY = "leaderboard-list-key";

    public static final String CELL_CLICKED_ID = "cell-clicked-id";
    public static final String WON_ID = "won-id";
    public static final String CURRENT_PLAYER_ID = "current-player-id";
    public static final String EVENT_CODE_ID = "event-code-id";

    @Inject
    SharedPreferences preferences;

    @Inject
    BingoStreamServiceGrpc.BingoStreamServiceStub streamServiceStub;

    @Inject
    BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    @BindView(R.id.bingo_linearLayout)
    LinearLayout bingoLinearLayout;

    @BindView(R.id.game_recyclerView)
    GridRecyclerView gameRecyclerView;

    @BindView(R.id.leaderBoard_recyclerView)
    RecyclerView leaderBoardRecyclerView;

    @BindView(R.id.B_textView)
    TextView B;

    @BindView(R.id.I_textView)
    TextView I;

    @BindView(R.id.N_textView)
    TextView N;

    @BindView(R.id.G_textView)
    TextView G;

    @BindView(R.id.O_textView)
    TextView O;

    @BindView(R.id.turnOrder_textView)
    TextView turnOrderTextView;

    @BindView(R.id.timeLimit_progressBar)
    ProgressBar timeLimitProgressBar;

    @BindView(R.id.currentTime_textView)
    TextView currentTimeTextView;

    @BindView(R.id.konfettiView)
    KonfettiView konfettiView;

    private SpeechRecognizer speechRecognizer;
    private MediaPlayer celebrationSound, popSound;
    private GameGridRecyclerAdapter gridRecyclerAdapter;
    private Intent speechRecognitionIntent;
    private CountDownTimer gameTimer;

    ArrayList<GridCell> gameGridCellList = new ArrayList<>();

    private int playerId = -1, roomId = -1;
    TimeLimitUtils.TIME_LIMIT currentTimeLimit = TimeLimitUtils.TIME_LIMIT.INFINITE;

    private boolean gameCompleted = false, myTurn = false;
    private ArrayList<Player> playersList = new ArrayList<>();

    /*
     *  cellsClicked[] keeps track of the grid cells clicked by the user
     *  mData holds the list of GridCells
     *  lastClickPosition holds the X,Y position of recently clicked grid cell
     */

    private LoaderCallbacks<BroadcastWinnerResponse> broadcastWinnerLoader = new LoaderCallbacks<BroadcastWinnerResponse>() {

        @NonNull
        @Override
        public Loader<BroadcastWinnerResponse> onCreateLoader(int id, @Nullable Bundle args) {
            return new BroadcastWinnerLoader(GameActivity.this, actionServiceBlockingStub, roomId,
                    com.hmproductions.bingo.models.Player.newBuilder().setName(getNameFromId(playersList, playerId))
                            .setId(playerId).setColor(getColorFromId(playersList, playerId)).setReady(true).build());
        }

        @Override
        public void onLoadFinished(@NonNull Loader<BroadcastWinnerResponse> loader, BroadcastWinnerResponse data) {
            if (data == null) {
                onNetworkDownError();
            } else {
                if (data.getStatusCode() == BroadcastWinnerResponse.StatusCode.INTERNAL_SERVER_ERROR)
                    Toast.makeText(GameActivity.this, data.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<BroadcastWinnerResponse> loader) {
            // Do nothing
        }
    };

    private LoaderCallbacks<QuitPlayerResponse> quitPlayerLoader = new LoaderCallbacks<QuitPlayerResponse>() {
        @NonNull
        @Override
        public Loader<QuitPlayerResponse> onCreateLoader(int id, @Nullable Bundle args) {
            return new QuitLoader(GameActivity.this, actionServiceBlockingStub,
                    com.hmproductions.bingo.models.Player.newBuilder().setColor(getColorFromId(playersList, playerId))
                            .setId(playerId).setReady(true).setName(getNameFromId(playersList, playerId)).setWinCount(0).build(), roomId);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<QuitPlayerResponse> loader, QuitPlayerResponse data) {
            if (data == null) {
                onNetworkDownError();
            } else {
                if (data.getStatusCode() == QuitPlayerResponse.StatusCode.SERVER_ERROR)
                    Toast.makeText(GameActivity.this, data.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<QuitPlayerResponse> loader) {
            // Do nothing
        }
    };

    private LoaderCallbacks<StartNextRoundResponse> startNextRoundLoader = new LoaderCallbacks<StartNextRoundResponse>() {
        @NonNull
        @Override
        public Loader<StartNextRoundResponse> onCreateLoader(int id, @Nullable Bundle args) {
            return new NextRoundLoader(GameActivity.this, actionServiceBlockingStub, playerId, roomId);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<StartNextRoundResponse> loader, StartNextRoundResponse data) {
            if (data == null) {
                onNetworkDownError();
            } else {
                if (data.getStatusCode() == StartNextRoundResponse.StatusCode.INTERNAL_SERVER_ERROR)
                    Toast.makeText(GameActivity.this, data.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<StartNextRoundResponse> loader) {

        }
    };

    private BroadcastReceiver gridCellReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction() != null && intent.getAction().equals(Constants.GRID_CELL_CLICK_ACTION)) {

                int currentPlayerId = intent.getIntExtra(CURRENT_PLAYER_ID, -1);
                int cellClicked = intent.getIntExtra(CELL_CLICKED_ID, -1);
                int winnerId = intent.getIntExtra(WON_ID, -1);

                switch (intent.getIntExtra(EVENT_CODE_ID, -1)) {

                    case GAME_WON_VALUE:

                        /*  1. Celebrations begin if player has won
                            2. Sets up leader board recycler view nad hides BINGO linear layout
                            3. Sets turn order text to winner names
                         */

                        if (winnerId == playerId) {
                            Toast.makeText(GameActivity.this, "You won the game", Toast.LENGTH_SHORT).show();

                            if (preferences.getBoolean(getString(R.string.sound_preference_key), true))
                                celebrationSound.start();

                            konfettiView.build()
                                    .addColors(Color.parseColor("#9162e4"), Color.YELLOW, Color.RED)
                                    .setDirection(0.0, 359.0)
                                    .setSpeed(3f, 6f)
                                    .setFadeOutEnabled(true)
                                    .setTimeToLive(3000)
                                    .addShapes(Shape.RECT, Shape.CIRCLE)
                                    .addSizes(new Size(12, 5))
                                    .setPosition(-50f, konfettiView.getWidth() + 50f, -50f, -50f)
                                    .streamFor(400, 2000);
                        } else {
                            Toast.makeText(GameActivity.this, getNameFromId(playersList, winnerId) + " has won", Toast.LENGTH_SHORT).show();
                        }

                        gameRecyclerView.setEnabled(false);
                        myTurn = false;

                        bingoLinearLayout.setVisibility(View.GONE);
                        leaderBoardRecyclerView.setVisibility(View.VISIBLE);
                        findViewById(R.id.nextRound_button).setVisibility(View.VISIBLE);
                        startNextRoundButtonTapTargetView();

                        leaderBoardRecyclerView.setLayoutManager(new GridLayoutManager(GameActivity.this, LEADERBOARD_COL_SPAN));
                        leaderBoardRecyclerView.setAdapter(new LeaderboardRecyclerAdapter(
                                GameActivity.this, intent.getParcelableArrayListExtra(LEADERBOARD_LIST_KEY), null));
                        leaderBoardRecyclerView.setHasFixedSize(true);

                        if (gameCompleted) {
                            StringBuilder winnerTextBuilder = new StringBuilder(turnOrderTextView.getText().toString());
                            winnerTextBuilder.insert(winnerTextBuilder.lastIndexOf(" "), ", " + getNameFromId(playersList, winnerId));
                            turnOrderTextView.setText(winnerTextBuilder.toString());
                        } else {
                            String winnerText;
                            if (winnerId == playerId)
                                winnerText = "You won";
                            else
                                winnerText = getNameFromId(playersList, winnerId) + " won";
                            turnOrderTextView.setText(winnerText);
                            gameCompleted = true;
                        }

                        gameTimer.cancel();

                        break;

                    case PLAYER_QUIT_VALUE:
                        Intent quitIntent = new Intent(GameActivity.this, MainActivity.class);
                        quitIntent.setAction(Constants.QUIT_GAME_ACTION);

                        quitIntent.putExtra(MainActivity.PLAYER_LEFT_ID, playerId == winnerId);

                        if (currentPlayerId == winnerId) {
                            startActivity(quitIntent);
                            finish();
                            return;
                        }

                        quitIntent.putExtra(PLAYER_ID, playerId);
                        quitIntent.putExtra(ROOM_ID, roomId);

                        gameTimer.cancel();
                        startActivity(quitIntent);
                        finish();

                        break;

                    case CELL_CLICKED_VALUE:

                        if (preferences.getBoolean(getString(R.string.sound_preference_key), true) && cellClicked != TURN_SKIPPED_CODE)
                            popSound.start();

                        for (GridCell gridCell : gameGridCellList) {
                            if (gridCell.getValue() == cellClicked) {
                                gridCell.setIsClicked(true);
                                gridCell.setColor(getColorFromNextPlayerId(playersList, currentPlayerId));
                                gridRecyclerAdapter.swapData(gameGridCellList);
                                break;
                            }
                        }

                        if (numberOfLinesCompleted() == 5 && !gameCompleted)
                            getSupportLoaderManager().restartLoader(Constants.BROADCAST_WINNER_LOADER_ID, null,
                                    broadcastWinnerLoader);

                        myTurn = currentPlayerId == playerId;

                        if (myTurn) {
                            startGameTimer();

                            if (preferences.getBoolean(getString(R.string.tts_preference_key), false))
                                startListening();
                            gameRecyclerView.setEnabled(true);
                        } else {
                            gameTimer.cancel();
                            gameRecyclerView.setEnabled(false);
                        }

                        setTurnOrderText(currentPlayerId);
                        break;

                    case GAME_STARTED_VALUE:

                        myTurn = currentPlayerId == playerId;

                        if (myTurn) {
                            Toast.makeText(GameActivity.this, "Start the game", Toast.LENGTH_SHORT).show();
                            startGameTimer();

                            if (preferences.getBoolean(getString(R.string.tts_preference_key), false))
                                startListening();
                            else
                                gameRecyclerView.setEnabled(true);
                        } else {
                            gameTimer.cancel();
                            gameRecyclerView.setEnabled(false);
                        }
                        findViewById(R.id.nextRound_button).setVisibility(View.GONE);

                        startMicTapTargetView();

                        setTurnOrderText(currentPlayerId);
                        break;

                    case NEXT_ROUND_VALUE:
                        recreate();
                        break;

                    default:
                        Toast.makeText(GameActivity.this, "Internal server error", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);

        DaggerBingoApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);

        roomId = getIntent().getIntExtra(ROOM_ID, -1);
        playerId = getIntent().getIntExtra(PLAYER_ID, -1);
        currentTimeLimit = getEnumFromValue(getIntent().getIntExtra(TIME_LIMIT_ID, 2));
        playersList = getIntent().getParcelableArrayListExtra(PLAYERS_LIST_ID);

        new Handler().post(() -> subscribeToGameEventUpdates(playerId, roomId));

        // Creates an ArrayList made up of random values
        CreateGameGridArrayList();
        CreateGameTimer();

        gridRecyclerAdapter = new GameGridRecyclerAdapter(this, GRID_SIZE, gameGridCellList, this);

        // Setting up animations for grid recycler view
        GridLayoutAnimationController animationController = (GridLayoutAnimationController) AnimationUtils.loadLayoutAnimation(this, R.anim.game_grid_animation);

        gameRecyclerView.setLayoutManager(new GridLayoutManager(this, GRID_SIZE));
        gameRecyclerView.setLayoutAnimation(animationController);
        gameRecyclerView.setAdapter(gridRecyclerAdapter);
        gameRecyclerView.setHasFixedSize(true);

        celebrationSound = MediaPlayer.create(this, R.raw.tada_celebration);
        popSound = MediaPlayer.create(this, R.raw.pop);

        speechRecognitionIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
    }

    // Creates an ArrayList of GridCell using int[][] made by CreateRandomGameArray()
    private void CreateGameGridArrayList() {

        // Returns an array with numbers 1 to GRID_SIZE * GRID_SIZE randomly placed in it
        int[] randomArray = CreateRandomGameArray(GRID_SIZE);

        for (int i = 0; i < GRID_SIZE * GRID_SIZE; ++i) {
            gameGridCellList.add(new GridCell(randomArray[i], false));
        }
    }

    private void CreateGameTimer() {
        int totalTime = getExactValueFromEnum(currentTimeLimit);

        gameTimer = new CountDownTimer((totalTime + 1) * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                timeLimitProgressBar.setProgress((int) (totalTime - millisUntilFinished / 1000));
                currentTimeTextView.setText(String.valueOf((int) (millisUntilFinished / 1000)));
            }

            @Override
            public void onFinish() {
                timeLimitProgressBar.setProgress(timeLimitProgressBar.getMax());
                currentTimeTextView.setText(String.valueOf(getExactValueFromEnum(currentTimeLimit)));

                Bundle bundle = new Bundle();
                bundle.putInt(CELL_CLICKED_ID, TURN_SKIPPED_CODE);
                getSupportLoaderManager().restartLoader(Constants.CLICK_CELL_LOADER_ID, bundle, GameActivity.this);
            }
        };
    }

    // Returns the number of lines completed
    public int numberOfLinesCompleted() {

        int counter = 0;

        // Checking for columns
        for (int i = 0; i < GRID_SIZE; ++i) {
            boolean columnFormed = true;
            for (int j = 0; j < GRID_SIZE; ++j)
                if (!gameGridCellList.get(j * GRID_SIZE + i).getIsClicked()) {
                    columnFormed = false;
                    break;
                }
            if (columnFormed) counter++;
        }

        // Checking for rows
        for (int i = 0; i < GRID_SIZE; ++i) {
            boolean rowFormed = true;
            for (int j = 0; j < GRID_SIZE; ++j) {
                if (!gameGridCellList.get(i * GRID_SIZE + j).getIsClicked()) {
                    rowFormed = false;
                    break;
                }
            }
            if (rowFormed) counter++;
        }

        // Checking for diagonals
        boolean mainDiagonalFormed = true;
        for (int i = 0; i < GRID_SIZE; ++i) {
            if (!gameGridCellList.get(i * GRID_SIZE + i).getIsClicked()) {
                mainDiagonalFormed = false;
                break;
            }
        }
        if (mainDiagonalFormed) counter++;

        boolean secondaryDiagonalFormed = true;
        for (int i = 0; i < GRID_SIZE; ++i) {
            if (!gameGridCellList.get(i * GRID_SIZE + (GRID_SIZE - i - 1)).getIsClicked()) {
                secondaryDiagonalFormed = false;
                break;
            }
        }
        if (secondaryDiagonalFormed) counter++;

        if (counter > GRID_SIZE) counter = GRID_SIZE;

        switch (counter) {
            // severe fall through
            case 5:
                O.setTextColor(Color.parseColor("#FF0000"));
            case 4:
                G.setTextColor(Color.parseColor("#FF0000"));
            case 3:
                N.setTextColor(Color.parseColor("#FF0000"));
            case 2:
                I.setTextColor(Color.parseColor("#FF0000"));
            case 1:
                B.setTextColor(Color.parseColor("#FF0000"));
        }

        return counter;
    }

    @OnClick(R.id.quitButton)
    void onQuitButtonClick() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Confirm Quit")
                .setMessage("Game will be cancelled. Do you want to forfeit ?")
                .setPositiveButton(R.string.quit, (dialogInterface, i) ->
                        getSupportLoaderManager().restartLoader(Constants.QUIT_PLAYER_LOADER_ID, null, quitPlayerLoader))
                .setNegativeButton(R.string.no, (dI, i) -> dI.dismiss())
                .show();
    }

    @OnClick(R.id.nextRound_button)
    void onNextRoundButtonClick() {
        Toast.makeText(this, "Waiting for other players", Toast.LENGTH_SHORT).show();
        getSupportLoaderManager().restartLoader(NEXT_ROUND_LOADER_ID, null, startNextRoundLoader);
    }

    @OnClick(R.id.talkToSpeak_imageButton)
    void onImageButtonClick() {
        if (myTurn && preferences.getBoolean(getString(R.string.tts_preference_key), false))
            speechRecognizer.startListening(speechRecognitionIntent);
        else if (myTurn && !preferences.getBoolean(getString(R.string.tts_preference_key), false))
            Toast.makeText(this, "Mic is disabled", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Not your turn", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGridCellClick(int value) {

        if (preferences.getBoolean(getString(R.string.tts_preference_key), false)) {
            speechRecognizer.stopListening();
        }

        if (myTurn && !valueClicked(gameGridCellList, value)) {
            Bundle bundle = new Bundle();
            bundle.putInt(CELL_CLICKED_ID, value);
            getSupportLoaderManager().restartLoader(Constants.CLICK_CELL_LOADER_ID, bundle, this);
        }
    }

    private void startGameTimer() {
        timeLimitProgressBar.setIndeterminate(false);

        if (currentTimeLimit == TimeLimitUtils.TIME_LIMIT.INFINITE) {
            timeLimitProgressBar.setMax(1);
            timeLimitProgressBar.setProgress(timeLimitProgressBar.getMax());
            currentTimeTextView.setText(DecimalFormatSymbols.getInstance().getInfinity());
        } else {
            int totalTime = getExactValueFromEnum(currentTimeLimit);
            timeLimitProgressBar.setProgress(0);
            timeLimitProgressBar.setMax(totalTime);
            gameTimer.start();
        }
    }

    private void subscribeToGameEventUpdates(int playerId, int roomId) {

        GameSubscription gameSubscription = GameSubscription.newBuilder().setFirstSubscription(true).setWinnerId(-1)
                .setCellClicked(-1).setRoomId(roomId).setPlayerId(playerId).build();

        streamServiceStub.getGameEventUpdates(gameSubscription, new StreamObserver<GameEventUpdate>() {
            @Override
            public void onNext(GameEventUpdate value) {

                GameEvent gameEvent = value.getGameEvent();
                ArrayList<LeaderboardPlayer> leaderboardPlayerArrayList = new ArrayList<>();

                for (com.hmproductions.bingo.models.Player currentPlayer : gameEvent.getLeaderboardList()) {
                    leaderboardPlayerArrayList.add(new LeaderboardPlayer(currentPlayer.getName(), currentPlayer.getColor(),
                            currentPlayer.getWinCount()));
                }

                Intent intent = new Intent(Constants.GRID_CELL_CLICK_ACTION);
                intent.putExtra(EVENT_CODE_ID, gameEvent.getEventCodeValue());
                intent.putExtra(CELL_CLICKED_ID, gameEvent.getCellClicked());
                intent.putExtra(CURRENT_PLAYER_ID, gameEvent.getCurrentPlayerId());
                intent.putExtra(WON_ID, gameEvent.getWinner());
                intent.putParcelableArrayListExtra(LEADERBOARD_LIST_KEY, leaderboardPlayerArrayList);
                LocalBroadcastManager.getInstance(GameActivity.this).sendBroadcast(intent);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    @NonNull
    @Override
    public Loader<ClickGridCellResponse> onCreateLoader(int id, @Nullable Bundle args) {

        if (args != null) {
            return new ClickCellLoader(this, actionServiceBlockingStub,
                    new ClickCellRequest(roomId, playerId, args.getInt(CELL_CLICKED_ID)));
        } else {
            return new ClickCellLoader(this, actionServiceBlockingStub,
                    new ClickCellRequest(roomId, playerId, -1));
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ClickGridCellResponse> loader, ClickGridCellResponse data) {
        if (data.getStatusCode() == ClickGridCellResponse.StatusCode.INTERNAL_SERVER_ERROR ||
                data.getStatusCode() == ClickGridCellResponse.StatusCode.NOT_PLAYER_TURN)
            Toast.makeText(this, data.getStatusMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ClickGridCellResponse> loader) {
        // Do nothing
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(gridCellReceiver,
                new IntentFilter(Constants.GRID_CELL_CLICK_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(gridCellReceiver);
    }

    private void setTurnOrderText(int currentPlayerId) {
        String turnOrder;
        if (currentPlayerId == playerId)
            turnOrder = "Your turn";
        else
            turnOrder = getNameFromId(playersList, currentPlayerId) + "\'s turn";

        turnOrderTextView.setText(turnOrder);
    }

    private void startMicTapTargetView() {
        if (preferences.getBoolean(FIRST_TIME_PLAYED_KEY, true)) {
            TapTargetView.showFor(this,
                    TapTarget
                            .forView(findViewById(R.id.talkToSpeak_imageButton), "How to use Mic", "Tap this once to call out number if mic does not recognise your number in the first time")
                            .targetRadius(50)
                            .icon(getDrawable(R.drawable.mic_icon_white))
                            .cancelable(true));

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(FIRST_TIME_PLAYED_KEY, false);
            editor.putBoolean(getString(R.string.tutorial_preference_key), false);
            editor.apply();
        }
    }

    private void startNextRoundButtonTapTargetView() {
        if (preferences.getBoolean(FIRST_TIME_WON_KEY, true)) {
            TapTargetView.showFor(this,
                    TapTarget
                            .forView(findViewById(R.id.nextRound_button), "Next Round", "Tap to mark yourself ready and start next round")
                            .targetRadius(40)
                            .icon(getDrawable(R.drawable.next_icon))
                            .cancelable(true),
                    new TapTargetView.Listener() {
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);
                            onNextRoundButtonClick();
                        }
                    });

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(FIRST_TIME_WON_KEY, false);
            editor.apply();
        }
    }

    private void startListening() {
        speechRecognizer.startListening(speechRecognitionIntent);
    }

    @Override
    public void onNetworkDownError() {
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    // ================================== Speech Recognition Methods Implementations ==================================
    @Override
    public void onReadyForSpeech(Bundle bundle) {
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onResults(Bundle bundle) {

        boolean foundMatch = false;

        ArrayList<String> speechResult = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (speechResult != null) {
            for (String currentWord : speechResult) {
                if (NumberUtils.isNumeric(currentWord) && Integer.parseInt(currentWord) >= 1 && Integer.parseInt(currentWord) <= GRID_SIZE * GRID_SIZE) {
                    onGridCellClick(Integer.parseInt(currentWord));
                    foundMatch = true;
                }
            }
        }

        if (!foundMatch)
            speechRecognizer.startListening(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        boolean foundMatch = false;

        ArrayList<String> speechResult = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (speechResult != null) {
            for (String currentWord : speechResult) {
                if (NumberUtils.isNumeric(currentWord) && Integer.parseInt(currentWord) >= 1 && Integer.parseInt(currentWord) <= GRID_SIZE * GRID_SIZE) {
                    onGridCellClick(Integer.parseInt(currentWord));
                    foundMatch = true;
                }
            }
        }

        if (!foundMatch)
            speechRecognizer.startListening(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));
    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }
}