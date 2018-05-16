package com.hmproductions.bingo;

import com.hmproductions.bingo.datastreams.RoomEventUpdate;
import com.hmproductions.bingo.models.RoomEvent;
import com.hmproductions.bingo.models.Subscription;

import io.grpc.stub.StreamObserver;

import static com.hmproductions.bingo.BingoActionServiceImpl.playersList;

public class BingoStreamServiceImpl extends BingoStreamServiceGrpc.BingoStreamServiceImplBase {

    @Override
    public void getRoomEventUpdates(Subscription request, StreamObserver<RoomEventUpdate> responseObserver) {

        RoomEvent roomEvent = RoomEvent.newBuilder()
                .addAllPlayers(playersList).setEventCode(RoomEvent.EventCode.ADD_PLAYER).build();

        responseObserver.onNext(RoomEventUpdate.newBuilder().setRoomEvent(roomEvent).build());
    }
}
