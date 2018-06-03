package com.hmproductions.bingo.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.QuitPlayerRequest;
import com.hmproductions.bingo.dagger.ContextModule;
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent;
import com.hmproductions.bingo.data.Player;

import java.util.ArrayList;

import javax.inject.Inject;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import static com.hmproductions.bingo.utils.Miscellaneous.getColorFromId;
import static com.hmproductions.bingo.utils.Miscellaneous.getNameFromId;

public class QuitService extends IntentService {

    public static final String PLAYER_ID_KEY = "player-id-key";
    public static final String PLAYER_LIST_KEY = "player-list-key";
    public static final String ROOM_ID_KEY = "room-id-key";
    public static final String SESSION_ID_KEY = "session-id-key";

    @Inject
    ManagedChannel channel;

    public QuitService() {
        super("QuitService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        DaggerBingoApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);

        BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub =
                BingoActionServiceGrpc.newBlockingStub(channel);

        if (intent != null) {
            actionServiceBlockingStub = MetadataUtils.
                    attachHeaders(actionServiceBlockingStub, getStubMetadata(intent.getStringExtra(SESSION_ID_KEY)));

            int playerId = intent.getIntExtra(PLAYER_ID_KEY, -1);
            int roomId = intent.getIntExtra(ROOM_ID_KEY, -1);
            ArrayList<Player> playersList = intent.getParcelableArrayListExtra(PLAYER_LIST_KEY);

            com.hmproductions.bingo.models.Player player = com.hmproductions.bingo.models.Player.newBuilder().setColor(getColorFromId(playersList, playerId))
                    .setId(playerId).setReady(true).setName(getNameFromId(playersList, playerId)).build();

            actionServiceBlockingStub.quitPlayer(QuitPlayerRequest.newBuilder().setRoomId(roomId).setPlayer(player).build());
        }
    }

    private Metadata getStubMetadata(String sessionId) {
        Metadata metadata = new Metadata();
        Metadata.Key<String> metadataKey = Metadata.Key.of("sessionid", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(metadataKey, sessionId);
        return metadata;
    }
}
