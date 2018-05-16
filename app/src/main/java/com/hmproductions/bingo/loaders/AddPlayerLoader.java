package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.AddPlayerRequest;
import com.hmproductions.bingo.actions.AddPlayerResponse;
import com.hmproductions.bingo.data.Player;

public class AddPlayerLoader extends AsyncTaskLoader<AddPlayerResponse> {

    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;
    private Player player;

    public AddPlayerLoader(Context context, BingoActionServiceGrpc.BingoActionServiceBlockingStub stub, Player player) {
        super(context);
        this.actionServiceBlockingStub = stub;
        this.player = player;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public AddPlayerResponse loadInBackground() {
        com.hmproductions.bingo.models.Player player = com.hmproductions.bingo.models.Player.newBuilder().setId(this.player.getId())
                .setName(this.player.getName()).setReady(this.player.isReady()).setColor(this.player.getColor()).build();

        return actionServiceBlockingStub.addPlayer(AddPlayerRequest.newBuilder().setPlayer(player).build());
    }
}
