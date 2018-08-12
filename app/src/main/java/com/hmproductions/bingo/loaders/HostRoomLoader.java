package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.HostRoomRequest;
import com.hmproductions.bingo.actions.HostRoomResponse;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.data.Room;
import com.hmproductions.bingo.utils.Constants;

import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import static com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo;
import static com.hmproductions.bingo.utils.ConnectionUtils.isReachableByTcp;
import static com.hmproductions.bingo.utils.Constants.PLAYER_ID_KEY;
import static com.hmproductions.bingo.utils.Constants.ROOM_ID_KEY;
import static com.hmproductions.bingo.utils.Constants.SERVER_ADDRESS;
import static com.hmproductions.bingo.utils.Constants.SERVER_PORT;
import static com.hmproductions.bingo.utils.Constants.SESSION_ID_KEY;
import static com.hmproductions.bingo.utils.TimeLimitUtils.getValueFromEnum;

public class HostRoomLoader extends AsyncTaskLoader<HostRoomResponse> {

    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;
    private Room room;
    private Player player;
    private String password;

    public HostRoomLoader(@NonNull Context context, BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub, Room room, Player player, String password) {
        super(context);
        this.actionServiceBlockingStub = actionServiceBlockingStub;
        this.room = room;
        this.player = player;
        this.password = password;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public HostRoomResponse loadInBackground() {
        if (getConnectionInfo(getContext()) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {

            Metadata metadata = new Metadata();

            Metadata.Key<String> metadataKey = Metadata.Key.of(PLAYER_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
            metadata.put(metadataKey, String.valueOf(player.getId()));

            metadataKey = Metadata.Key.of(SESSION_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
            metadata.put(metadataKey, Constants.SESSION_ID);

            metadataKey = Metadata.Key.of(ROOM_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
            metadata.put(metadataKey, String.valueOf(room.getRoomId()));

            actionServiceBlockingStub = MetadataUtils.attachHeaders(actionServiceBlockingStub, metadata);

            return actionServiceBlockingStub.hostRoom(HostRoomRequest.newBuilder()
                    .setRoomName(room.getName()).setMaxSize(room.getMaxSize()).setPlayerId(player.getId()).setPassword(password)
                    .setPlayerColor(player.getColor()).setTimeLimitValue(getValueFromEnum(room.getTimeLimit())).setPlayerName(player.getName()).build());
        } else {
            return null;
        }
    }
}
