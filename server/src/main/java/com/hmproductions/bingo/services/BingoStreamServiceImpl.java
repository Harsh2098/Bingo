package com.hmproductions.bingo.services;

import com.hmproductions.bingo.BingoStreamServiceGrpc;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.data.RoomEventSubscription;
import com.hmproductions.bingo.datastreams.RoomEventUpdate;
import com.hmproductions.bingo.models.RoomEvent;
import com.hmproductions.bingo.models.Subscription;

import java.util.ArrayList;

import io.grpc.stub.StreamObserver;

import static com.hmproductions.bingo.services.BingoActionServiceImpl.playersList;
import static com.hmproductions.bingo.utils.Miscellaneous.getArrayListFromPlayersList;

public class BingoStreamServiceImpl extends BingoStreamServiceGrpc.BingoStreamServiceImplBase {

    static ArrayList<RoomEventSubscription> subscriptionArrayList = new ArrayList<>();

    @Override
    public void getRoomEventUpdates(Subscription request, StreamObserver<RoomEventUpdate> responseObserver) {

        boolean found = false;

        for (RoomEventSubscription currentSubscription : subscriptionArrayList) {
            if (currentSubscription.getSubscription().getPlayerId() == request.getPlayerId()) {
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Adding " + request.getPlayerId() + " to the room update list.");
            subscriptionArrayList.add(new RoomEventSubscription(responseObserver, request));
        }

        RoomEvent roomEvent = RoomEvent.newBuilder()
                .addAllPlayers(getArrayListFromPlayersList(playersList)).setEventCode(RoomEvent.EventCode.PLAYER_STATE_CHANGED).build();

        System.out.println("Sending a room event update to " + request.getPlayerId());

        for (Player player : playersList) {
            System.out.print("Name:" + player.getName() + " Id:" + player.getId() + " Ready:" + player.isReady() + "\n");
        }

        responseObserver.onNext(RoomEventUpdate.newBuilder().setRoomEvent(roomEvent).build());
    }
}
