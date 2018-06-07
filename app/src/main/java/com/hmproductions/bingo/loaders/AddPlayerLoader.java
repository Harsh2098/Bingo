package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.AddPlayerRequest;
import com.hmproductions.bingo.actions.AddPlayerResponse;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.utils.Constants;

import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import static com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo;
import static com.hmproductions.bingo.utils.ConnectionUtils.isReachableByTcp;
import static com.hmproductions.bingo.utils.Constants.CLASSIC_TAG;
import static com.hmproductions.bingo.utils.Constants.PLAYER_ID_KEY;
import static com.hmproductions.bingo.utils.Constants.SERVER_ADDRESS;
import static com.hmproductions.bingo.utils.Constants.SERVER_PORT;
import static com.hmproductions.bingo.utils.Constants.SESSION_ID_KEY;

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

        // TODO (3) : Check why this loader is called twice when returning from Settings

        if (getConnectionInfo(getContext()) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {

            com.hmproductions.bingo.models.Player player = com.hmproductions.bingo.models.Player.newBuilder().setId(this.player.getId())
                    .setName(this.player.getName()).setReady(this.player.isReady()).setColor(this.player.getColor()).build();

            Metadata metadata = new Metadata();

            Metadata.Key<String> metadataKey = Metadata.Key.of(PLAYER_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
            metadata.put(metadataKey, String.valueOf(player.getId()));

            metadataKey = Metadata.Key.of(SESSION_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
            metadata.put(metadataKey, Constants.SESSION_ID);

            actionServiceBlockingStub = MetadataUtils.attachHeaders(actionServiceBlockingStub, metadata);

            return actionServiceBlockingStub.addPlayer(AddPlayerRequest.newBuilder().setPlayer(player).build());
        } else {
            return null;
        }
    }
}
