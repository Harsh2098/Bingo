package com.hmproductions.bingo.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.BingoStreamServiceGrpc;
import com.hmproductions.bingo.R;
import com.hmproductions.bingo.actions.AddPlayerResponse;
import com.hmproductions.bingo.actions.RemovePlayerResponse;
import com.hmproductions.bingo.actions.SetPlayerReadyResponse;
import com.hmproductions.bingo.actions.Unsubscribe;
import com.hmproductions.bingo.adapter.PlayersRecyclerAdapter;
import com.hmproductions.bingo.dagger.ContextModule;
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.datastreams.RoomEventUpdate;
import com.hmproductions.bingo.loaders.AddPlayerLoader;
import com.hmproductions.bingo.loaders.RemovePlayerLoader;
import com.hmproductions.bingo.loaders.SetReadyLoader;
import com.hmproductions.bingo.loaders.UnsubscribeLoader;
import com.hmproductions.bingo.models.RoomEvent;
import com.hmproductions.bingo.models.Subscription;
import com.hmproductions.bingo.utils.ConnectionUtils;
import com.hmproductions.bingo.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.grpc.stub.StreamObserver;

import static com.hmproductions.bingo.utils.Miscellaneous.getNameFromId;
import static com.hmproductions.bingo.utils.Miscellaneous.nameToIdHash;

public class MainActivity extends AppCompatActivity implements
        PlayersRecyclerAdapter.OnPlayerClickListener,
        ConnectionUtils.OnNetworkDownHandler {

    public static final String PLAYER_LEFT_ID = "player-left-id";

    private static final String PLAYER_NAME_KEY = "player-name-key";
    private static final String PLAYER_COLOR_KEY = "player-color-key";

    @Inject
    BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    BingoStreamServiceGrpc.BingoStreamServiceStub streamServiceStub;

    @Inject
    SharedPreferences preferences;

    @BindView(R.id.hostIP_editText)
    EditText hostIP_editText;

    @BindView(R.id.playerName_editText)
    EditText playerNameEditText;

    @BindView(R.id.color_spinner)
    Spinner colorSpinner;

    @BindView(R.id.playersList_recyclerView)
    RecyclerView playersRecyclerView;

    private int currentPlayerId = -1, currentRoomId = -1;
    private ArrayList<Player> playersList = new ArrayList<>();

    private Player fakePlayer = new Player("", "", -1, false);
    private PlayersRecyclerAdapter playersRecyclerAdapter;

    AlertDialog loadingDialog;

    // Support loaders callbacks definition
    private LoaderCallbacks<AddPlayerResponse> addPlayerLoader = new LoaderCallbacks<AddPlayerResponse>() {
        @NonNull
        @Override
        public Loader<AddPlayerResponse> onCreateLoader(int id, @Nullable Bundle args) {

            loadingDialog.dismiss();

            String name = playerNameEditText.getText().toString();
            loadingDialog.show();

            currentPlayerId = nameToIdHash(name);

            return new AddPlayerLoader(MainActivity.this, actionServiceBlockingStub,
                    new Player(name, colorSpinner.getSelectedItem().toString(), currentPlayerId, false));
        }

        @Override
        public void onLoadFinished(@NonNull Loader<AddPlayerResponse> loader, AddPlayerResponse data) {

            loadingDialog.dismiss();

            if (data == null) {
                onNetworkDownError();
            } else {

                switch (data.getStatusCodeValue()) {

                    case AddPlayerResponse.StatusCode.OK_VALUE:
                        currentRoomId = data.getRoom().getId();
                        subscribeToRoomEventsUpdate(currentRoomId);
                        break;

                    case AddPlayerResponse.StatusCode.ROOM_FULL_VALUE:
                    default:
                        currentPlayerId = -1;
                        Toast.makeText(MainActivity.this, data.getStatusMessage(), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<AddPlayerResponse> loader) {
            // Do nothing
        }
    };

    private LoaderCallbacks<RemovePlayerResponse> removePlayerLoader = new LoaderCallbacks<RemovePlayerResponse>() {

        @NonNull
        @Override
        public Loader<RemovePlayerResponse> onCreateLoader(int id, @Nullable Bundle args) {

            loadingDialog.show();

            for (Player player : playersList) {
                if (player.getId() == currentPlayerId) {
                    return new RemovePlayerLoader(MainActivity.this, actionServiceBlockingStub, player);
                }
            }

            // If not found
            return new RemovePlayerLoader(MainActivity.this, actionServiceBlockingStub, fakePlayer);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<RemovePlayerResponse> loader, RemovePlayerResponse data) {

            loadingDialog.dismiss();

            if (data == null) {
                onNetworkDownError();
            } else {
                Toast.makeText(MainActivity.this, data.getStatusMessage(), Toast.LENGTH_SHORT).show();

                if (data.getStatusCode() == RemovePlayerResponse.StatusCode.OK) {
                    currentPlayerId = currentRoomId = -1;
                    playersList.clear();
                    playersRecyclerAdapter.swapData(null);
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<RemovePlayerResponse> loader) {
            // Do nothing
        }
    };

    private LoaderCallbacks<SetPlayerReadyResponse> setPlayerReadyLoader = new LoaderCallbacks<SetPlayerReadyResponse>() {
        @NonNull
        @Override
        public Loader<SetPlayerReadyResponse> onCreateLoader(int id, @Nullable Bundle args) {

            loadingDialog.show();

            return new SetReadyLoader(MainActivity.this, actionServiceBlockingStub,
                    currentPlayerId, !getPlayerReady(currentPlayerId));
        }

        @Override
        public void onLoadFinished(@NonNull Loader<SetPlayerReadyResponse> loader, SetPlayerReadyResponse data) {

            loadingDialog.dismiss();

            if (data == null) {
                onNetworkDownError();
            } else {
                if (data.getStatusCode() == SetPlayerReadyResponse.StatusCode.SERVER_ERROR) {
                    Toast.makeText(MainActivity.this, data.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<SetPlayerReadyResponse> loader) {
            // Do nothing
        }
    };

    private LoaderCallbacks<Unsubscribe.UnsubscribeResponse> unsubscribeLoader = new LoaderCallbacks<Unsubscribe.UnsubscribeResponse>() {
        @NonNull
        @Override
        public Loader<Unsubscribe.UnsubscribeResponse> onCreateLoader(int id, @Nullable Bundle args) {

            return new UnsubscribeLoader(MainActivity.this, actionServiceBlockingStub, currentPlayerId, currentRoomId);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Unsubscribe.UnsubscribeResponse> loader, Unsubscribe.UnsubscribeResponse data) {

            if (data == null) {
                onNetworkDownError();
            } else {
                if (data.getStatusCode() == Unsubscribe.UnsubscribeResponse.StatusCode.INTERNAL_SERVER_ERROR)
                    Toast.makeText(MainActivity.this, data.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Unsubscribe.UnsubscribeResponse> loader) {
            // Do nothing
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        DaggerBingoApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);

        setupSpinner();
        setupJoinButton();

        playersRecyclerAdapter = new PlayersRecyclerAdapter(null, this, this);

        playersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        playersRecyclerView.setAdapter(playersRecyclerAdapter);
        playersRecyclerView.setHasFixedSize(false);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.loading_dialog, null);
        ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.processing_request);
        loadingDialog = new AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create();

        if (getIntent().getAction() != null && getIntent().getAction().equals(Constants.QUIT_GAME_ACTION)) {

            if (getIntent().getBooleanExtra(PLAYER_LEFT_ID, true)) {
                playersRecyclerAdapter.swapData(null);
            } else {
                playersList = getIntent().getParcelableArrayListExtra(GameActivity.PLAYERS_LIST_ID);
                playersRecyclerAdapter.swapData(playersList);

                currentPlayerId = getIntent().getIntExtra(GameActivity.PLAYER_ID, -1);
                currentRoomId = getIntent().getIntExtra(GameActivity.ROOM_ID, -1);
                playerNameEditText.setText(getNameFromId(playersList, currentPlayerId));
            }
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @OnClick(R.id.leave_button)
    void onLeaveButtonClick() {

        if (currentPlayerId != -1) {
            getSupportLoaderManager().restartLoader(Constants.UNSUBSCRIBE_LOADER_ID, null, unsubscribeLoader);
            getSupportLoaderManager().restartLoader(Constants.REMOVE_PLAYER_LOADER_ID, null, removePlayerLoader);
        } else
            Toast.makeText(this, "You have not joined", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlayerClick(int position) {
        if (playersList.get(position).getId() == currentPlayerId) {
            getSupportLoaderManager().restartLoader(Constants.READY_PLAYER_LOADER_ID, null, setPlayerReadyLoader);
        }
    }

    private void subscribeToRoomEventsUpdate(int roomId) {

        streamServiceStub.getRoomEventUpdates(Subscription.newBuilder().setRoomId(roomId).setPlayerId(currentPlayerId).build(), new StreamObserver<RoomEventUpdate>() {
            @Override
            public void onNext(RoomEventUpdate value) {

                if (value.getRoomEvent().getEventCode() == RoomEvent.EventCode.ADD_PLAYER ||
                        value.getRoomEvent().getEventCode() == RoomEvent.EventCode.PLAYER_READY_CHANGED ||
                        value.getRoomEvent().getEventCode() == RoomEvent.EventCode.PLAYER_STATE_CHANGED ||
                        value.getRoomEvent().getEventCode() == RoomEvent.EventCode.REMOVE_PLAYER) {
                    List<com.hmproductions.bingo.models.Player> responseList = value.getRoomEvent().getPlayersList();

                    playersList.clear();
                    for (com.hmproductions.bingo.models.Player currentPlayer : responseList) {
                        playersList.add(new Player(currentPlayer.getName(), currentPlayer.getColor(), currentPlayer.getId(),
                                currentPlayer.getReady()));
                    }

                    runOnUiThread(() -> playersRecyclerAdapter.swapData(playersList));

                } else if (value.getRoomEvent().getEventCode() == RoomEvent.EventCode.GAME_START) {

                    Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                    gameIntent.putExtra(GameActivity.PLAYER_ID, currentPlayerId);
                    gameIntent.putExtra(GameActivity.ROOM_ID, currentRoomId);
                    gameIntent.putParcelableArrayListExtra(GameActivity.PLAYERS_LIST_ID, playersList);
                    startActivity(gameIntent);

                    finish();
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    private void setupSpinner() {
        ArrayAdapter colorSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.colorsName, android.R.layout.simple_list_item_1);
        colorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        colorSpinner.setAdapter(colorSpinnerAdapter);
    }

    private void setupJoinButton() {
        findViewById(R.id.join_button).setOnClickListener(view -> {
            if (playerNameEditText.getText().toString().isEmpty()) {
                playerNameEditText.setError("A-Z, 0-9");
                Toast.makeText(this, "Please enter the name", Toast.LENGTH_SHORT).show();
                return;
            } else if (currentRoomId != -1) {
                Toast.makeText(this, "Player already joined", Toast.LENGTH_SHORT).show();
                return;
            }

            getSupportLoaderManager().restartLoader(Constants.ADD_PLAYER_LOADER_ID, null, addPlayerLoader);
        });
    }

    boolean getPlayerReady(int playerId) {
        for (Player currentPlayer : playersList) {
            if (currentPlayer.getId() == playerId)
                return currentPlayer.isReady();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings_action:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            default:
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        playerNameEditText.setText(preferences.getString(PLAYER_NAME_KEY, ""));
        colorSpinner.setSelection(preferences.getInt(PLAYER_COLOR_KEY, 0));

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PLAYER_NAME_KEY, playerNameEditText.getText().toString());
        editor.putInt(PLAYER_COLOR_KEY, colorSpinner.getSelectedItemPosition());
        editor.apply();
    }

    @Override
    public void onNetworkDownError() {
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }
}
