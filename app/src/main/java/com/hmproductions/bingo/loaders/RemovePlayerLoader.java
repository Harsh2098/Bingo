package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.RemovePlayerRequest;
import com.hmproductions.bingo.actions.RemovePlayerResponse;
import com.hmproductions.bingo.data.Player;

import static com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo;
import static com.hmproductions.bingo.utils.ConnectionUtils.isReachableByTcp;
import static com.hmproductions.bingo.utils.Constants.SERVER_ADDRESS;
import static com.hmproductions.bingo.utils.Constants.SERVER_PORT;

public class RemovePlayerLoader extends AsyncTaskLoader<RemovePlayerResponse> {

    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;
    private Player player;

    public RemovePlayerLoader(Context context, BingoActionServiceGrpc.BingoActionServiceBlockingStub stub, Player player) {
        super(context);
        this.actionServiceBlockingStub = stub;
        this.player = player;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public RemovePlayerResponse loadInBackground() {
        if (getConnectionInfo(getContext()) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {

            com.hmproductions.bingo.models.Player player = com.hmproductions.bingo.models.Player.newBuilder().setId(this.player.getId())
                .setName(this.player.getName()).setReady(this.player.isReady()).setColor(this.player.getColor()).build();

        return actionServiceBlockingStub.removePlayer(RemovePlayerRequest.newBuilder().setPlayer(player).build());
        } else {
            return null;
        }
    }
}
