package com.hmproductions.bingo.services;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.AddPlayerRequest;
import com.hmproductions.bingo.actions.AddPlayerResponse;
import com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest;
import com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse;
import com.hmproductions.bingo.actions.RemovePlayerRequest;
import com.hmproductions.bingo.actions.RemovePlayerResponse;
import com.hmproductions.bingo.actions.SetPlayerReadyRequest;
import com.hmproductions.bingo.actions.SetPlayerReadyResponse;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.models.Room;

import java.util.ArrayList;

import io.grpc.stub.StreamObserver;

import static com.hmproductions.bingo.utils.Miscellaneous.playerIsNew;

public class BingoActionServiceImpl extends BingoActionServiceGrpc.BingoActionServiceImplBase {

    private static final int ROOM_ID = 17;

    public static ArrayList<Player> playersList = new ArrayList<>();

    private Room room;

    public BingoActionServiceImpl() {
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

        } else if (room.getCount() < 4) {

            if (playerIsNew(request.getPlayer().getId())) {

                com.hmproductions.bingo.models.Player currentPlayer = request.getPlayer();

                playersList.add(new Player(currentPlayer.getName(), currentPlayer.getColor(), currentPlayer.getId(),
                        currentPlayer.getReady()));

                room = room.toBuilder().setCount(room.getCount() + 1).build();

                addPlayerResponse = AddPlayerResponse.newBuilder().setStatusCode(AddPlayerResponse.StatusCode.OK)
                        .setStatusMessage("New player added").setRoom(room).build();

                // Server logs
                System.out.println(request.getPlayer().getName() + " added to the game.");

            } else {
                addPlayerResponse = AddPlayerResponse.newBuilder().setStatusCode(AddPlayerResponse.StatusCode.ALREADY_IN_GAME)
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

    @Override
    public void removePlayer(RemovePlayerRequest request, StreamObserver<RemovePlayerResponse> responseObserver) {

        boolean found = false;

        for (Player player : playersList) {
            if (request.getPlayer().getId() == player.getId()) {
                playersList.remove(player);
                found = true;
                break;
            }
        }

        if (found) {
            responseObserver.onNext(RemovePlayerResponse.newBuilder().setStatusMessage("Player removed")
                    .setStatusCode(RemovePlayerResponse.StatusCode.OK).build());

            room = room.toBuilder().setCount(room.getCount() - 1).build();

            //Server logs
            System.out.println(request.getPlayer().getName() + " removed from the game.");

        } else {
            responseObserver.onNext(RemovePlayerResponse.newBuilder().setStatusCode(RemovePlayerResponse.StatusCode.NOT_JOINED)
                    .setStatusMessage("Player not joined").build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void setPlayerReady(SetPlayerReadyRequest request, StreamObserver<SetPlayerReadyResponse> responseObserver) {

        boolean found = false;

        for (Player player : playersList) {
            if (player.getId() == request.getPlayerId()) {
                player.setReady(request.getIsReady());
                found = true;

                // Server logs
                System.out.println(request.getPlayerId() + " id set to " + player.isReady());

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
                            .setStatusMessage("Player not found").build());
        }

        responseObserver.onCompleted();
    }
}
