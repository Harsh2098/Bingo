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

import static com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo;
import static com.hmproductions.bingo.utils.ConnectionUtils.isReachableByTcp;
import static com.hmproductions.bingo.utils.Constants.SERVER_ADDRESS;
import static com.hmproductions.bingo.utils.Constants.SERVER_PORT;

public class HostRoomLoader extends AsyncTaskLoader<HostRoomResponse> {

    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;
    private Room room;
    private Player player;

    public HostRoomLoader(@NonNull Context context, BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub, Room room, Player player) {
        super(context);
        this.actionServiceBlockingStub = actionServiceBlockingStub;
        this.room = room;
        this.player = player;
    }

    @Nullable
    @Override
    public HostRoomResponse loadInBackground() {
        if (getConnectionInfo(getContext()) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {
            return actionServiceBlockingStub.hostRoom(HostRoomRequest.newBuilder()
                    .setRoomName(room.getName()).setMaxSize(room.getMaxSize()).setPlayerId(player.getId())
                    .setPlayerColor(player.getColor()).setPlayerName(player.getName()).build());
        } else {
            return null;
        }
    }
}
