package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.QuitPlayerRequest;
import com.hmproductions.bingo.actions.QuitPlayerResponse;
import com.hmproductions.bingo.models.Player;

import static com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo;
import static com.hmproductions.bingo.utils.ConnectionUtils.isReachableByTcp;
import static com.hmproductions.bingo.utils.Constants.SERVER_ADDRESS;
import static com.hmproductions.bingo.utils.Constants.SERVER_PORT;

public class QuitLoader extends AsyncTaskLoader<QuitPlayerResponse> {

    private int roomId;
    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;
    private Player player;

    public QuitLoader(@NonNull Context context, BingoActionServiceGrpc.BingoActionServiceBlockingStub stub, Player player, int roomId) {
        super(context);
        this.player = player;
        this.actionServiceBlockingStub = stub;
        this.roomId = roomId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public QuitPlayerResponse loadInBackground() {
        if (getConnectionInfo(getContext()) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {

            return actionServiceBlockingStub.quitPlayer(QuitPlayerRequest.newBuilder().setRoomId(roomId).setPlayer(player).build());
        } else {
            return null;
        }
    }
}
