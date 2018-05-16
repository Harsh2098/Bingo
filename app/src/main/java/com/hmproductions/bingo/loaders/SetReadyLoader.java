package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.SetPlayerReadyRequest;
import com.hmproductions.bingo.actions.SetPlayerReadyResponse;

public class SetReadyLoader extends AsyncTaskLoader<SetPlayerReadyResponse> {

    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;
    private int playerId;
    private boolean ready;

    public SetReadyLoader(Context context, BingoActionServiceGrpc.BingoActionServiceBlockingStub stub, int playerId, boolean ready) {
        super(context);
        this.actionServiceBlockingStub = stub;
        this.playerId = playerId;
        this.ready = ready;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public SetPlayerReadyResponse loadInBackground() {
        return actionServiceBlockingStub.setPlayerReady(SetPlayerReadyRequest.newBuilder()
                .setPlayerId(playerId).setIsReady(ready).build());
    }
}