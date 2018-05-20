package com.hmproductions.bingo.services;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.AddPlayerRequest;
import com.hmproductions.bingo.actions.AddPlayerResponse;
import com.hmproductions.bingo.actions.BroadcastWinnerRequest;
import com.hmproductions.bingo.actions.BroadcastWinnerResponse;
import com.hmproductions.bingo.actions.ClickGridCell.*;
import com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest;
import com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse;
import com.hmproductions.bingo.actions.RemovePlayerRequest;
import com.hmproductions.bingo.actions.RemovePlayerResponse;
import com.hmproductions.bingo.actions.SetPlayerReadyRequest;
import com.hmproductions.bingo.actions.SetPlayerReadyResponse;
import com.hmproductions.bingo.actions.Unsubscribe.UnsubscribeRequest;
import com.hmproductions.bingo.actions.Unsubscribe.UnsubscribeResponse;
import com.hmproductions.bingo.data.GameEventSubscription;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.data.RoomEventSubscription;
import com.hmproductions.bingo.models.GameSubscription;
import com.hmproductions.bingo.models.Room;
import com.hmproductions.bingo.utils.Constants;

import java.util.ArrayList;

import io.grpc.stub.StreamObserver;

import static com.hmproductions.bingo.services.BingoStreamServiceImpl.currentPlayerPosition;
import static com.hmproductions.bingo.services.BingoStreamServiceImpl.gameEventSubscriptionArrayList;
import static com.hmproductions.bingo.services.BingoStreamServiceImpl.subscriptionArrayList;
import static com.hmproductions.bingo.services.BingoStreamServiceImpl.totalPlayers;
import static com.hmproductions.bingo.utils.Miscellaneous.playerIsNew;

public class BingoActionServiceImpl extends BingoActionServiceGrpc.BingoActionServiceImplBase {

    private static final int ROOM_ID = 17;

    public static ArrayList<Player> playersList = new ArrayList<>();
    private BingoStreamServiceImpl streamService = new BingoStreamServiceImpl();

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
        if (room.getCount() > Constants.MAX_PLAYERS) {
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

                sendRoomEventUpdate();

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

            sendRoomEventUpdate();

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

                sendRoomEventUpdate();

                break;
            }
        }

        if (found) {
            responseObserver.onNext(
                    SetPlayerReadyResponse.newBuilder().setStatusCode(SetPlayerReadyResponse.StatusCode.OK).setIsReady(request.getIsReady())
                            .setStatusMessage("Player set to " + request.getIsReady()).setPlayerId(request.getPlayerId()).build());

        } else {
            responseObserver.onNext(
                    SetPlayerReadyResponse.newBuilder().setStatusCode(SetPlayerReadyResponse.StatusCode.SERVER_ERROR)
                            .setPlayerId(-1).setIsReady(false).setStatusMessage("Player not found").build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void unsubscribe(UnsubscribeRequest request, StreamObserver<UnsubscribeResponse> responseObserver) {

        boolean found = false;
        for (RoomEventSubscription currentSubscription : subscriptionArrayList) {
            if (request.getPlayerId() == currentSubscription.getSubscription().getPlayerId()) {
                subscriptionArrayList.remove(currentSubscription);
                found = true;

                System.out.print("Unsubscribing " + currentSubscription.getSubscription().getPlayerId() + " from this list.");
                break;
            }
        }

        UnsubscribeResponse unsubscribeResponse;
        if (found) {
            unsubscribeResponse = UnsubscribeResponse.newBuilder().setStatusCode(UnsubscribeResponse.StatusCode.OK)
                    .setStatusMessage("Player unsubscribed").build();
        } else {
            unsubscribeResponse = UnsubscribeResponse.newBuilder().setStatusCode(UnsubscribeResponse.StatusCode.NOT_SUBSCRIBED)
                    .setStatusMessage("Player not subscribed").build();
        }

        responseObserver.onNext(unsubscribeResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void clickGridCell(ClickGridCellRequest request, StreamObserver<ClickGridCellResponse> responseObserver) {

        room = room.toBuilder().setStatusCode(Room.StatusCode.IN_GAME).build();

        if (request.getCellClicked() == -1) {
            responseObserver.onNext(ClickGridCellResponse.newBuilder().setStatusMessage("Internal server error")
                    .setStatusCode(ClickGridCellResponse.StatusCode.INTERNAL_SERVER_ERROR).build());
            responseObserver.onCompleted();
            return;
        }

        currentPlayerPosition = (currentPlayerPosition + 1) % totalPlayers;

        for (GameEventSubscription currentSubscription : gameEventSubscriptionArrayList) {

            /*
                HORRIBLE MISTAKE setPlayerId(request.getPlayerId()) Oh Btw, this is to check if player has subscribed to stream
             */

            GameSubscription gameSubscription = GameSubscription.newBuilder().setFirstSubscription(false)
                    .setRoomId(request.getRoomId()).setPlayerId(currentSubscription.getGameSubscription().getPlayerId())
                    .setWinnerId(-1).setCellClicked(request.getCellClicked()).build();

            streamService.getGameEventUpdates(gameSubscription, currentSubscription.getObserver());
        }

        responseObserver.onNext(ClickGridCellResponse.newBuilder().setStatusMessage("Look out for streaming service game update")
                .setStatusCode(ClickGridCellResponse.StatusCode.OK).build());

        responseObserver.onCompleted();
    }

    @Override
    public void broadcastWinner(BroadcastWinnerRequest request, StreamObserver<BroadcastWinnerResponse> responseObserver) {

        com.hmproductions.bingo.models.Player player = request.getPlayer();

        for (GameEventSubscription currentSubscription : gameEventSubscriptionArrayList) {

            /*
                HORRIBLE MISTAKE setPlayerId(request.getPlayerId())
             */

            GameSubscription gameSubscription = GameSubscription.newBuilder().setFirstSubscription(false)
                    .setRoomId(request.getRoomId()).setPlayerId(currentSubscription.getGameSubscription().getPlayerId())
                    .setWinnerId(player.getId()).setCellClicked(-1).build();

            streamService.getGameEventUpdates(gameSubscription, currentSubscription.getObserver());
        }
    }

    private void sendRoomEventUpdate() {
        BingoStreamServiceImpl streamService = new BingoStreamServiceImpl();

        for (RoomEventSubscription currentSubscription : subscriptionArrayList) {
            streamService.getRoomEventUpdates(currentSubscription.getSubscription(), currentSubscription.getObserver());
        }
    }
}
