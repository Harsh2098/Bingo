package com.hmproductions.bingo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.BingoStreamServiceGrpc;
import com.hmproductions.bingo.R;
import com.hmproductions.bingo.actions.AddPlayerResponse;
import com.hmproductions.bingo.actions.RemovePlayerResponse;
import com.hmproductions.bingo.actions.SetPlayerReadyResponse;
import com.hmproductions.bingo.adapter.PlayersRecyclerAdapter;
import com.hmproductions.bingo.dagger.ContextModule;
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.datastreams.RoomEventUpdate;
import com.hmproductions.bingo.loaders.AddPlayerLoader;
import com.hmproductions.bingo.loaders.RemovePlayerLoader;
import com.hmproductions.bingo.loaders.SetReadyLoader;
import com.hmproductions.bingo.models.RoomEvent;
import com.hmproductions.bingo.models.Subscription;
import com.hmproductions.bingo.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.grpc.stub.StreamObserver;

public class MainActivity extends AppCompatActivity implements PlayersRecyclerAdapter.OnPlayerClickListener{

    @Inject
    BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    BingoStreamServiceGrpc.BingoStreamServiceStub streamServiceStub;

    @BindView(R.id.hostIP_editText)
    EditText hostIP_editText;

    @BindView(R.id.playerName_editText)
    EditText playerNameEditText;

    @BindView(R.id.color_spinner)
    Spinner colorSpinner;

    @BindView(R.id.game_recyclerView)
    RecyclerView playersRecyclerView;

    private int currentPlayerId = -1, currentRoomId = -1;
    private List<Player> playersList = new ArrayList<>();

    private PlayersRecyclerAdapter playersRecyclerAdapter;

    private LoaderCallbacks<AddPlayerResponse> addPlayerLoader = new LoaderCallbacks<AddPlayerResponse>() {
        @NonNull
        @Override
        public Loader<AddPlayerResponse> onCreateLoader(int id, @Nullable Bundle args) {

            String name = playerNameEditText.getText().toString();

            return new AddPlayerLoader(MainActivity.this, actionServiceBlockingStub,
                    new Player(name, colorSpinner.getSelectedItem().toString(), name.length(), false));
        }

        @Override
        public void onLoadFinished(@NonNull Loader<AddPlayerResponse> loader, AddPlayerResponse data) {
            switch (data.getStatusCodeValue()) {

                case AddPlayerResponse.StatusCode.OK_VALUE:
                    currentPlayerId = playerNameEditText.getText().toString().length();
                    currentRoomId = data.getRoom().getId();
                    subscribeToRoomEventsUpdate(currentRoomId);
                    break;

                case AddPlayerResponse.StatusCode.ROOM_FULL_VALUE:
                default:
                    Toast.makeText(MainActivity.this, data.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    break;
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

            String name = playerNameEditText.getText().toString();

            return new RemovePlayerLoader(MainActivity.this, actionServiceBlockingStub,
                    new Player(name, colorSpinner.getSelectedItem().toString(), name.length(), false));
        }

        @Override
        public void onLoadFinished(@NonNull Loader<RemovePlayerResponse> loader, RemovePlayerResponse data) {

            Toast.makeText(MainActivity.this, data.getStatusMessage(), Toast.LENGTH_SHORT).show();

            if (data.getStatusCodeValue() == RemovePlayerResponse.StatusCode.OK_VALUE)
                currentPlayerId = currentRoomId = -1;
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
            return new SetReadyLoader(MainActivity.this, actionServiceBlockingStub,
                    currentPlayerId, !getPlayerReady(currentPlayerId));
        }

        @Override
        public void onLoadFinished(@NonNull Loader<SetPlayerReadyResponse> loader, SetPlayerReadyResponse data) {
            if (data.getStatusCode() == SetPlayerReadyResponse.StatusCode.SERVER_ERROR) {
                Toast.makeText(MainActivity.this, data.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<SetPlayerReadyResponse> loader) {
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

        playersRecyclerAdapter = new PlayersRecyclerAdapter(null, this, this);

        playersRecyclerView.setAdapter(playersRecyclerAdapter);
        playersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        playersRecyclerView.setHasFixedSize(false);
    }

    private void setupSpinner() {
        ArrayAdapter colorSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.colors, android.R.layout.simple_list_item_1);
        colorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        colorSpinner.setAdapter(colorSpinnerAdapter);
    }

    @OnClick(R.id.join_button)
    void onJoinButtonClick() {

        if (playerNameEditText.getText().toString().isEmpty()) {
            playerNameEditText.setError("Please enter your name");
            return;
        }

        getSupportLoaderManager().restartLoader(Constants.ADD_PLAYER_LOADER_ID, null, addPlayerLoader);
    }

    @Override
    public void onPlayerClick(int position) {
        if(playersList.get(position).getId() == currentPlayerId) {
            getSupportLoaderManager().restartLoader(Constants.READY_PLAYER_LOADER_ID, null, setPlayerReadyLoader);
        }
    }

    private void subscribeToRoomEventsUpdate(int roomId) {
        streamServiceStub.getRoomEventUpdates(Subscription.newBuilder().setRoomId(roomId).build(), new StreamObserver<RoomEventUpdate>() {
            @Override
            public void onNext(RoomEventUpdate value) {

                if (value.getRoomEvent().getEventCode() == RoomEvent.EventCode.ADD_PLAYER || value.getRoomEvent().getEventCode() == RoomEvent.EventCode.PLAYER_READY_CHANGED
                        || value.getRoomEvent().getEventCode() == RoomEvent.EventCode.REMOVE_PLAYER) {
                    List<com.hmproductions.bingo.models.Player> responseList = value.getRoomEvent().getPlayersList();
                    playersList.clear();

                    for (com.hmproductions.bingo.models.Player currentPlayer : responseList) {
                        playersList.add(new Player(currentPlayer.getName(), currentPlayer.getColor(), currentPlayer.getId(),
                                currentPlayer.getReady()));
                    }

                    playersRecyclerAdapter.swapData(playersList);

                } else if (value.getRoomEvent().getEventCode() == RoomEvent.EventCode.GAME_START) {
                    startActivity(new Intent(MainActivity.this, GameActivity.class));
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

    boolean getPlayerReady(int playerId) {
        for (Player currentPlayer : playersList) {
            if (currentPlayer.getId() == playerId)
                return currentPlayer.isReady();
        }

        return true;
    }
}
