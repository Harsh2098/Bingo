package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.BroadcastWinnerRequest;
import com.hmproductions.bingo.actions.BroadcastWinnerResponse;
import com.hmproductions.bingo.models.Player;

import static com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo;
import static com.hmproductions.bingo.utils.ConnectionUtils.isReachableByTcp;
import static com.hmproductions.bingo.utils.Constants.SERVER_ADDRESS;
import static com.hmproductions.bingo.utils.Constants.SERVER_PORT;

public class BroadcastWinnerLoader extends AsyncTaskLoader<BroadcastWinnerResponse> {

    private int roomId;
    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;
    private Player player;

    public BroadcastWinnerLoader(@NonNull Context context, BingoActionServiceGrpc.BingoActionServiceBlockingStub stub, int roomId, Player player) {
        super(context);
        this.actionServiceBlockingStub = stub;
        this.roomId = roomId;
        this.player = player;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public BroadcastWinnerResponse loadInBackground() {

        if (getConnectionInfo(getContext()) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {

            return actionServiceBlockingStub.broadcastWinner(BroadcastWinnerRequest.newBuilder().setPlayer(player)
                .setRoomId(roomId).build());
        } else {
            return null;
        }
    }
}
