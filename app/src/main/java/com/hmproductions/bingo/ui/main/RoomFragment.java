package com.hmproductions.bingo.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.BingoStreamServiceGrpc;
import com.hmproductions.bingo.R;
import com.hmproductions.bingo.actions.RemovePlayerResponse;
import com.hmproductions.bingo.actions.SetPlayerReadyResponse;
import com.hmproductions.bingo.actions.Unsubscribe;
import com.hmproductions.bingo.adapter.PlayersRecyclerAdapter;
import com.hmproductions.bingo.dagger.ContextModule;
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.datastreams.RoomEventUpdate;
import com.hmproductions.bingo.loaders.RemovePlayerLoader;
import com.hmproductions.bingo.loaders.SetReadyLoader;
import com.hmproductions.bingo.loaders.UnsubscribeLoader;
import com.hmproductions.bingo.models.RoomEvent;
import com.hmproductions.bingo.models.RoomSubscription;
import com.hmproductions.bingo.ui.GameActivity;
import com.hmproductions.bingo.utils.ConnectionUtils;
import com.hmproductions.bingo.utils.Constants;
import com.hmproductions.bingo.utils.Miscellaneous;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.grpc.stub.StreamObserver;

import static com.hmproductions.bingo.ui.main.MainActivity.currentPlayerId;
import static com.hmproductions.bingo.ui.main.MainActivity.currentRoomId;
import static com.hmproductions.bingo.ui.main.MainActivity.playersList;

public class RoomFragment extends Fragment implements PlayersRecyclerAdapter.OnPlayerClickListener {

    @Inject
    BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    @Inject
    BingoStreamServiceGrpc.BingoStreamServiceStub streamServiceStub;

    RecyclerView playersRecyclerView;

    AlertDialog loadingDialog;
    ConnectionUtils.OnNetworkDownHandler networkDownHandler;
    Miscellaneous.OnFragmentChangeRequest fragmentChangeRequest;

    PlayersRecyclerAdapter playersRecyclerAdapter;

    private Player fakePlayer = new Player("", "", -1, false);

    public RoomFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            networkDownHandler = (ConnectionUtils.OnNetworkDownHandler) context;
            fragmentChangeRequest = (Miscellaneous.OnFragmentChangeRequest) context;
        } catch (ClassCastException classCastException) {
            throw new ClassCastException(context.toString() + " must implement network down handler.");
        }
    }

    private LoaderManager.LoaderCallbacks<RemovePlayerResponse> removePlayerLoader = new LoaderManager.LoaderCallbacks<RemovePlayerResponse>() {

        @NonNull
        @Override
        public Loader<RemovePlayerResponse> onCreateLoader(int id, @Nullable Bundle args) {

            loadingDialog.show();

            for (Player player : playersList) {
                if (player.getId() == currentPlayerId) {
                    return new RemovePlayerLoader(getContext(), actionServiceBlockingStub, player, currentRoomId);
                }
            }

            // If not found
            return new RemovePlayerLoader(getContext(), actionServiceBlockingStub, fakePlayer, currentRoomId);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<RemovePlayerResponse> loader, RemovePlayerResponse data) {

            loadingDialog.dismiss();

            if (data == null) {
                networkDownHandler.onNetworkDownError();
            } else {
                Toast.makeText(getContext(), data.getStatusMessage(), Toast.LENGTH_SHORT).show();

                if (data.getStatusCode() == RemovePlayerResponse.StatusCode.OK) {
                    currentPlayerId = currentRoomId = -1;
                    playersList.clear();
                    playersRecyclerAdapter.swapData(null);
                    fragmentChangeRequest.changeFragment();
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<RemovePlayerResponse> loader) {
            // Do nothing
        }
    };

    private LoaderManager.LoaderCallbacks<SetPlayerReadyResponse> setPlayerReadyLoader = new LoaderManager.LoaderCallbacks<SetPlayerReadyResponse>() {
        @NonNull
        @Override
        public Loader<SetPlayerReadyResponse> onCreateLoader(int id, @Nullable Bundle args) {

            loadingDialog.show();

            return new SetReadyLoader(getContext(), actionServiceBlockingStub, currentPlayerId, !getPlayerReady(currentPlayerId), currentRoomId);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<SetPlayerReadyResponse> loader, SetPlayerReadyResponse data) {

            loadingDialog.dismiss();

            if (data == null) {
                networkDownHandler.onNetworkDownError();
            } else {
                if (data.getStatusCode() == SetPlayerReadyResponse.StatusCode.SERVER_ERROR) {
                    Toast.makeText(getContext(), data.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<SetPlayerReadyResponse> loader) {
            // Do nothing
        }
    };

    private LoaderManager.LoaderCallbacks<Unsubscribe.UnsubscribeResponse> unsubscribeLoader = new LoaderManager.LoaderCallbacks<Unsubscribe.UnsubscribeResponse>() {
        @NonNull
        @Override
        public Loader<Unsubscribe.UnsubscribeResponse> onCreateLoader(int id, @Nullable Bundle args) {

            return new UnsubscribeLoader(getContext(), actionServiceBlockingStub, currentPlayerId, currentRoomId);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Unsubscribe.UnsubscribeResponse> loader, Unsubscribe.UnsubscribeResponse data) {

            if (data == null) {
                networkDownHandler.onNetworkDownError();
            } else {
                if (data.getStatusCode() == Unsubscribe.UnsubscribeResponse.StatusCode.INTERNAL_SERVER_ERROR)
                    Toast.makeText(getContext(), data.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Unsubscribe.UnsubscribeResponse> loader) {
            // Do nothing
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View customView = inflater.inflate(R.layout.fragment_room, container, false);

        DaggerBingoApplicationComponent.builder().contextModule(new ContextModule(getContext())).build().inject(this);
        ButterKnife.bind(this, customView);

        playersRecyclerView = customView.findViewById(R.id.players_recyclerView);

        if (getContext() != null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.loading_dialog, null);
            ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.processing_request);
            loadingDialog = new AlertDialog.Builder(getContext()).setView(dialogView).setCancelable(false).create();
        }

        playersRecyclerAdapter = new PlayersRecyclerAdapter(playersList, getContext(), this);

        playersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        playersRecyclerView.setAdapter(playersRecyclerAdapter);
        playersRecyclerView.setHasFixedSize(false);

        subscribeToRoomEventsUpdate(currentRoomId);

        return customView;
    }

    boolean getPlayerReady(int playerId) {
        for (Player currentPlayer : playersList) {
            if (currentPlayer.getId() == playerId)
                return currentPlayer.isReady();
        }
        return true;
    }

    @OnClick(R.id.leave_button)
    void onLeaveButtonClick() {

        if (currentPlayerId != -1) {
            getLoaderManager().restartLoader(Constants.UNSUBSCRIBE_LOADER_ID, null, unsubscribeLoader);
            getLoaderManager().restartLoader(Constants.REMOVE_PLAYER_LOADER_ID, null, removePlayerLoader);
        } else
            Toast.makeText(getContext(), "You have not joined", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlayerClick(int position) {
        if (playersList.get(position).getId() == currentPlayerId) {
            this.getLoaderManager().restartLoader(Constants.READY_PLAYER_LOADER_ID, null, setPlayerReadyLoader);
        }
    }

    private void subscribeToRoomEventsUpdate(int roomId) {

        streamServiceStub.getRoomEventUpdates(RoomSubscription.newBuilder().setRoomId(roomId).setPlayerId(currentPlayerId).build(), new StreamObserver<RoomEventUpdate>() {
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

                    if (getActivity() != null)
                        getActivity().runOnUiThread(() -> playersRecyclerAdapter.swapData(playersList));

                } else if (value.getRoomEvent().getEventCode() == RoomEvent.EventCode.GAME_START) {

                    Intent gameIntent = new Intent(getContext(), GameActivity.class);
                    gameIntent.putExtra(GameActivity.PLAYER_ID, currentPlayerId);
                    gameIntent.putExtra(GameActivity.ROOM_ID, currentRoomId);
                    gameIntent.putParcelableArrayListExtra(GameActivity.PLAYERS_LIST_ID, playersList);
                    startActivity(gameIntent);

                    fragmentChangeRequest.finishCurrentActivity();
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
}
