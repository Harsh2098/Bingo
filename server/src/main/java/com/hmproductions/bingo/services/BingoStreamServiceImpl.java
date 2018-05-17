package com.hmproductions.bingo.services;

import com.hmproductions.bingo.BingoStreamServiceGrpc;
import com.hmproductions.bingo.datastreams.RoomEventUpdate;
import com.hmproductions.bingo.models.Player;
import com.hmproductions.bingo.models.RoomEvent;
import com.hmproductions.bingo.models.Subscription;

import java.util.ArrayList;

import io.grpc.stub.StreamObserver;

import static com.hmproductions.bingo.services.BingoActionServiceImpl.playersList;
import static com.hmproductions.bingo.utils.Miscellaneous.getArrayListFromPlayersList;

public class BingoStreamServiceImpl extends BingoStreamServiceGrpc.BingoStreamServiceImplBase {

    @Override
    public void getRoomEventUpdates(Subscription request, StreamObserver<RoomEventUpdate> responseObserver) {

        RoomEvent roomEvent = RoomEvent.newBuilder()
                .addAllPlayers(getArrayListFromPlayersList(playersList)).setEventCode(RoomEvent.EventCode.ADD_PLAYER).build();

        System.out.println("Sending a room event update");

        responseObserver.onNext(RoomEventUpdate.newBuilder().setRoomEvent(roomEvent).build());
    }
}
