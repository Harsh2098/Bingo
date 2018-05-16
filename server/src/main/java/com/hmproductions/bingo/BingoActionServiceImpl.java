package com.hmproductions.bingo;

import com.hmproductions.bingo.actions.AddPlayerRequest;
import com.hmproductions.bingo.actions.AddPlayerResponse;
import com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest;
import com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse;
import com.hmproductions.bingo.actions.RemovePlayerRequest;
import com.hmproductions.bingo.actions.RemovePlayerResponse;
import com.hmproductions.bingo.actions.SetPlayerReadyRequest;
import com.hmproductions.bingo.actions.SetPlayerReadyResponse;
import com.hmproductions.bingo.models.Player;
import com.hmproductions.bingo.models.Room;

import java.util.ArrayList;

import io.grpc.stub.StreamObserver;

public class BingoActionServiceImpl extends BingoActionServiceGrpc.BingoActionServiceImplBase {

    private static final int ROOM_ID = 17;

    static ArrayList<Player> playersList = new ArrayList<>();
    private Room room;

    BingoActionServiceImpl() {
        room = Room.newBuilder().setId(ROOM_ID).setCount(0).setStatusCode(Room.StatusCode.WAITING).build();
    }

    @Override
    public void getGridSize(GetGridSizeRequest request, StreamObserver<GetGridSizeResponse> responseObserver) {

        GetGridSizeResponse response;

        if (request.getPlayerId() > 0) {
            response = GetGridSizeResponse.newBuilder().setSize(5).build();
        } else {
            response = GetGridSizeResponse.newBuilder().setSize(-1).build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void addPlayer(AddPlayerRequest request, StreamObserver<AddPlayerResponse> responseObserver) {

        AddPlayerResponse addPlayerResponse;

        // Room is not full
        if (room.getCount() > 4) {
            addPlayerResponse =
                    AddPlayerResponse.newBuilder().setStatusCode(AddPlayerResponse.StatusCode.ROOM_FULL)
                    .setStatusMessage("Room is full").setRoom(Room.newBuilder().setId(-1).build()).build();

        } else if (room.getCount() < 4){

            if (playerIsNew(request.getPlayer())) {
                Player currentPlayer = request.getPlayer();
                playersList.add(Player.newBuilder()
                        .setId(currentPlayer.getId())
                        .setName(currentPlayer.getName())
                        .setColor(currentPlayer.getColor())
                        .setReady(currentPlayer.getReady())
                        .build());

                room = room.toBuilder().setCount(room.getCount() + 1).build();

                addPlayerResponse = AddPlayerResponse.newBuilder().setStatusCode(AddPlayerResponse.StatusCode.OK)
                        .setStatusMessage("New player added").setRoom(room).build();
            } else {
                addPlayerResponse = AddPlayerResponse.newBuilder().setStatusCode(AddPlayerResponse.StatusCode.SERVER_ERROR)
                        .setStatusMessage("Player already in game").setRoom(room).build();
            }


        } else {
            addPlayerResponse =
                    AddPlayerResponse.newBuilder().setStatusCode(AddPlayerResponse.StatusCode.SERVER_ERROR)
                    .setStatusMessage("Internal server error").setRoom(Room.newBuilder().setId(-1).build()).build();
        }

        responseObserver.onNext(addPlayerResponse);
        responseObserver.onCompleted();
    }

    private boolean playerIsNew(Player player) {

        boolean exists = false;

        for (Player currentPlayer : playersList) {
            if (currentPlayer.getId() == player.getId()) {
                exists = true;
                break;
            }
        }

        return !exists;
    }

    @Override
    public void removePlayer(RemovePlayerRequest request, StreamObserver<RemovePlayerResponse> responseObserver) {

        RemovePlayerResponse removePlayerResponse;

        if (room.getCount() == 0) {
            removePlayerResponse = RemovePlayerResponse.newBuilder().setStatusMessage("Player not joined")
                    .setStatusCode(RemovePlayerResponse.StatusCode.NOT_JOINED).build();
        } else {
            for (Player player : playersList) {
                if (request.getPlayer().getId() == player.getId()) {
                    playersList.remove(player);
                    break;
                }
            }

            removePlayerResponse = RemovePlayerResponse.newBuilder().setStatusMessage("Player removed")
                    .setStatusCode(RemovePlayerResponse.StatusCode.OK).build();

            room = room.toBuilder().setCount(room.getCount()-1).build();
        }

        responseObserver.onNext(removePlayerResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void setPlayerReady(SetPlayerReadyRequest request, StreamObserver<SetPlayerReadyResponse> responseObserver) {

        boolean found = false;

        for (Player player : playersList) {
            if (player.getId() == request.getPlayerId()) {
                player.toBuilder().setReady(request.getIsReady());
                found = true;
                break;
            }
        }

        if (found) {
            responseObserver.onNext(
                    SetPlayerReadyResponse.newBuilder().setStatusCode(SetPlayerReadyResponse.StatusCode.OK)
                            .setStatusMessage("Player set to " + request.getIsReady()).build());
        } else {
            responseObserver.onNext(
                    SetPlayerReadyResponse.newBuilder().setStatusCode(SetPlayerReadyResponse.StatusCode.SERVER_ERROR)
                            .setStatusMessage("Player set to " + request.getIsReady()).build());
        }

        responseObserver.onCompleted();
    }
}
