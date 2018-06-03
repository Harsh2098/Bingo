package com.hmproductions.bingo.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.RemovePlayerRequest;
import com.hmproductions.bingo.actions.Unsubscribe;
import com.hmproductions.bingo.dagger.ContextModule;
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent;

import javax.inject.Inject;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import static com.hmproductions.bingo.utils.Constants.CLASSIC_TAG;

public class RemoveService extends IntentService {

    public static final String SESSION_ID_KEY = "session-id-key";
    public static final String PLAYER_NAME_KEY = "player-name-key";
    public static final String PLAYER_COLOR_KEY = "player-color-key";
    public static final String PLAYER_ID_KEY = "player-id-key";
    public static final String ROOM_ID_KEY = "room-id-key";

    @Inject
    ManagedChannel channel;

    public RemoveService() {
        super("RemoveService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.v(CLASSIC_TAG, "Starting remove service.");

        DaggerBingoApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);
        BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub = BingoActionServiceGrpc.newBlockingStub(channel);

        if (intent != null) {

            String color = intent.getStringExtra(PLAYER_COLOR_KEY);
            String name = intent.getStringExtra(PLAYER_NAME_KEY);
            int playerId = intent.getIntExtra(PLAYER_ID_KEY, -1);
            int roomId = intent.getIntExtra(ROOM_ID_KEY, -1);

            actionServiceBlockingStub = MetadataUtils.
                    attachHeaders(actionServiceBlockingStub, getStubMetadata(intent.getStringExtra(SESSION_ID_KEY)));

            com.hmproductions.bingo.models.Player player = com.hmproductions.bingo.models.Player.newBuilder().setId(playerId)
                    .setName(name).setReady(true).setColor(color).build();

            actionServiceBlockingStub.unsubscribe(Unsubscribe.UnsubscribeRequest.newBuilder()
                    .setPlayerId(playerId).setRoomId(roomId).build());

            actionServiceBlockingStub.removePlayer(RemovePlayerRequest.newBuilder().setPlayer(player).build());
        }
    }

    private Metadata getStubMetadata(String sessionId) {
        Metadata metadata = new Metadata();
        Metadata.Key<String> metadataKey = Metadata.Key.of("sessionid", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(metadataKey, sessionId);
        return metadata;
    }
}
