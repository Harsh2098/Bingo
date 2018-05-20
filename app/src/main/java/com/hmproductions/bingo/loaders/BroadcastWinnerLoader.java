package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.BroadcastWinnerRequest;
import com.hmproductions.bingo.actions.BroadcastWinnerResponse;
import com.hmproductions.bingo.models.Player;

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
        return actionServiceBlockingStub.broadcastWinner(BroadcastWinnerRequest.newBuilder().setPlayer(player)
                .setRoomId(roomId).build());
    }
}
