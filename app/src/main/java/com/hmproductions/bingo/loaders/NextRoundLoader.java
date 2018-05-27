package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.StartNextRoundRequest;
import com.hmproductions.bingo.actions.StartNextRoundResponse;

public class NextRoundLoader extends AsyncTaskLoader<StartNextRoundResponse> {

    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;
    private int playerId, roomId;

    public NextRoundLoader(@NonNull Context context, BingoActionServiceGrpc.BingoActionServiceBlockingStub stub, int playerId, int roomId) {
        super(context);
        this.playerId = playerId;
        this.roomId = roomId;
        this.actionServiceBlockingStub = stub;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public StartNextRoundResponse loadInBackground() {
        return actionServiceBlockingStub.startNextRound(StartNextRoundRequest.newBuilder().setPlayerId(playerId).setRoomId(roomId).build());
    }
}
