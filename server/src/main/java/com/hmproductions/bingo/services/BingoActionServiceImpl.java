package com.hmproductions.bingo.services;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.AddPlayerRequest;
import com.hmproductions.bingo.actions.AddPlayerResponse;
import com.hmproductions.bingo.actions.BroadcastWinnerRequest;
import com.hmproductions.bingo.actions.BroadcastWinnerResponse;
import com.hmproductions.bingo.actions.ClickGridCell.ClickGridCellRequest;
import com.hmproductions.bingo.actions.ClickGridCell.ClickGridCellResponse;
import com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest;
import com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse;
import com.hmproductions.bingo.actions.GetRoomsRequest;
import com.hmproductions.bingo.actions.GetRoomsResponse;
import com.hmproductions.bingo.actions.GetSessionIdRequest;
import com.hmproductions.bingo.actions.GetSessionIdResponse;
import com.hmproductions.bingo.actions.HostRoomRequest;
import com.hmproductions.bingo.actions.HostRoomResponse;
import com.hmproductions.bingo.actions.QuitPlayerRequest;
import com.hmproductions.bingo.actions.QuitPlayerResponse;
import com.hmproductions.bingo.actions.RemovePlayerRequest;
import com.hmproductions.bingo.actions.RemovePlayerResponse;
import com.hmproductions.bingo.actions.SetPlayerReadyRequest;
import com.hmproductions.bingo.actions.SetPlayerReadyResponse;
import com.hmproductions.bingo.actions.StartNextRoundRequest;
import com.hmproductions.bingo.actions.StartNextRoundResponse;
import com.hmproductions.bingo.actions.Unsubscribe.UnsubscribeRequest;
import com.hmproductions.bingo.actions.Unsubscribe.UnsubscribeResponse;
import com.hmproductions.bingo.data.ConnectionData;
import com.hmproductions.bingo.data.GameEventSubscription;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.data.Room;
import com.hmproductions.bingo.data.RoomEventSubscription;
import com.hmproductions.bingo.models.GameSubscription;

import java.util.ArrayList;

import io.grpc.stub.StreamObserver;

import static com.hmproductions.bingo.BingoServer.connectionDataList;
import static com.hmproductions.bingo.utils.Constants.SESSION_ID_LENGTH;
import static com.hmproductions.bingo.utils.MiscellaneousUtils.allPlayersReady;
import static com.hmproductions.bingo.utils.MiscellaneousUtils.generateRoomId;
import static com.hmproductions.bingo.utils.MiscellaneousUtils.generateSessionId;
import static com.hmproductions.bingo.utils.MiscellaneousUtils.removeConnectionData;
import static com.hmproductions.bingo.utils.RoomUtils.colorAlreadyTaken;
import static com.hmproductions.bingo.utils.RoomUtils.getRoomFromId;
import static com.hmproductions.bingo.utils.RoomUtils.roomNameAlreadyTaken;

public class BingoActionServiceImpl extends BingoActionServiceGrpc.BingoActionServiceImplBase {

    private BingoStreamServiceImpl streamService = new BingoStreamServiceImpl();
    public static ArrayList<Room> roomsList = new ArrayList<>();

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
    public void getSessionId(GetSessionIdRequest request, StreamObserver<GetSessionIdResponse> responseObserver) {

        String sessionId = generateSessionId(SESSION_ID_LENGTH);
        System.out.println("New session ID: " + sessionId);

        responseObserver.onNext(GetSessionIdResponse.newBuilder().setStatusCode(GetSessionIdResponse.StatusCode.OK)
                .setStatusMessage("New session ID attached").setSessionId(sessionId).build());

        responseObserver.onCompleted();
    }

    @Override
    public void hostRoom(HostRoomRequest request, StreamObserver<HostRoomResponse> responseObserver) {

        ArrayList<Player> playersArrayList = new ArrayList<>();
        playersArrayList.add(new Player(request.getPlayerName(), request.getPlayerColor(), request.getPlayerId(), false));

        if (!roomNameAlreadyTaken(request.getRoomName())) {
            int newRoomId = generateRoomId(request.getRoomName());
            Room newRoom = new Room(newRoomId, playersArrayList, 1, -1,
                    request.getMaxSize(), Room.Status.WAITING, request.getRoomName());

            boolean newRoomCreatedSuccessfully = roomsList.add(newRoom);

            if (newRoomCreatedSuccessfully) {
                updateRoomId(request.getPlayerId(), newRoomId);
                responseObserver.onNext(HostRoomResponse.newBuilder().setRoomId(newRoomId).setStatusCode(HostRoomResponse.StatusCode.OK)
                        .setStatusMessage("New room created").build());
            }
            else {
                responseObserver.onNext(HostRoomResponse.newBuilder().setRoomId(-1).setStatusMessage("Could not create new room")
                        .setStatusCode(HostRoomResponse.StatusCode.INTERNAL_SERVER_ERROR).build());

                removeConnectionData(connectionDataList, request.getPlayerId());
            }
        } else {
            responseObserver.onNext(HostRoomResponse.newBuilder().setRoomId(-1).setStatusMessage("Room name already taken")
                    .setStatusCode(HostRoomResponse.StatusCode.NAME_TAKEN).build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void getRooms(GetRoomsRequest request, StreamObserver<GetRoomsResponse> responseObserver) {

        ArrayList<com.hmproductions.bingo.models.Room> protoRoomsList = new ArrayList<>();
        for (Room currentRoom : roomsList) {
            protoRoomsList.add(com.hmproductions.bingo.models.Room.newBuilder().setRoomName(currentRoom.getName())
                    .setRoomId(currentRoom.getRoomId()).setCount(currentRoom.getCount()).setMaxSize(currentRoom.getMaxSize()).build());
        }

        if (protoRoomsList.size() == 0) {
            responseObserver.onNext(GetRoomsResponse.newBuilder().setStatusCode(GetRoomsResponse.StatusCode.NO_ROOMS)
                    .setStatusMessage("No rooms available").addAllRooms(protoRoomsList).build());
        } else {
            responseObserver.onNext(GetRoomsResponse.newBuilder().setStatusCode(GetRoomsResponse.StatusCode.OK)
                    .setStatusMessage("Available rooms provided").addAllRooms(protoRoomsList).build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void addPlayer(AddPlayerRequest request, StreamObserver<AddPlayerResponse> responseObserver) {

        AddPlayerResponse addPlayerResponse;
        Room currentRoom = getRoomFromId(request.getRoomId());

        if (currentRoom != null) {
            // Room is not full
            if (currentRoom.getCount() >= currentRoom.getMaxSize()) {
                addPlayerResponse =
                        AddPlayerResponse.newBuilder().setStatusCode(AddPlayerResponse.StatusCode.ROOM_FULL)
                                .setStatusMessage("Room is full").setRoomId(request.getRoomId()).build();

            } else if (currentRoom.getCount() < currentRoom.getMaxSize()) {

                // player id -1 denotes player already in game (currently -1 is not sent from app)
                if (request.getPlayer().getId() != -1) {

                    com.hmproductions.bingo.models.Player currentPlayer = request.getPlayer();

                    // Checks if the color is already taken
                    if (!colorAlreadyTaken(currentRoom.getRoomId(), currentPlayer.getColor())) {

                        currentRoom.getPlayersList().add(new Player(currentPlayer.getName(), currentPlayer.getColor(), currentPlayer.getId(),
                                currentPlayer.getReady()));

                        currentRoom.setCount(currentRoom.getCount() + 1);

                        addPlayerResponse = AddPlayerResponse.newBuilder().setStatusCode(AddPlayerResponse.StatusCode.OK).setRoomId(request.getRoomId())
                                .setStatusMessage("New room joined").build();

                        sendRoomEventUpdate(request.getRoomId());

                        // Server logs
                        System.out.println(request.getPlayer().getName() + " added to the game.");
                    } else {
                        addPlayerResponse = AddPlayerResponse.newBuilder().setStatusCode(AddPlayerResponse.StatusCode.COLOR_TAKEN)
                                .setStatusMessage("Color already taken").setRoomId(request.getRoomId()).build();
                    }

                } else {
                    addPlayerResponse = AddPlayerResponse.newBuilder().setStatusCode(AddPlayerResponse.StatusCode.ALREADY_IN_GAME)
                            .setStatusMessage("Player already in game").setRoomId(request.getRoomId()).build();
                }


            } else {
                addPlayerResponse =
                        AddPlayerResponse.newBuilder().setStatusCode(AddPlayerResponse.StatusCode.SERVER_ERROR)
                                .setStatusMessage("Internal server error").setRoomId(-1).build();
            }

            /*  If player was not added successfully, remove it from connection data list. This is the only way as of now. Connection data is added when filter detects
                AddPlayer method was called from action service                                                                                                      */

            if (addPlayerResponse.getStatusCode() != AddPlayerResponse.StatusCode.OK) {
                removeConnectionData(connectionDataList, request.getPlayer().getId());
            }
        } else {
            addPlayerResponse =
                    AddPlayerResponse.newBuilder().setStatusCode(AddPlayerResponse.StatusCode.ROOM_NOT_EXIST)
                            .setStatusMessage("Room does not exist.").setRoomId(-1).build();
        }

        responseObserver.onNext(addPlayerResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void removePlayer(RemovePlayerRequest request, StreamObserver<RemovePlayerResponse> responseObserver) {

        Room currentRoom = getRoomFromId(request.getRoomId());

        // Check if room exists
        if (currentRoom != null) {

            boolean found = false;
            Player removePlayer = null;

            // Check if player exists in the room
            for (Player player : currentRoom.getPlayersList()) {
                if (request.getPlayer().getId() == player.getId()) {
                    removePlayer = player;
                    found = true;
                    break;
                }
            }

            // If found: 1. Remove player from player list   2. Reduce room count   3. If room count is 0 destroy room
            if (found) {
                currentRoom.getPlayersList().remove(removePlayer);

                responseObserver.onNext(RemovePlayerResponse.newBuilder().setStatusMessage("Player removed")
                        .setStatusCode(RemovePlayerResponse.StatusCode.OK).build());

                currentRoom.setCount(currentRoom.getCount() - 1);

                if (currentRoom.getCount() == 0) {
                    System.out.print("Room with id " + currentRoom.getRoomId() + " destroyed.");
                    roomsList.remove(currentRoom);
                } else
                    sendRoomEventUpdate(request.getRoomId());

                //Server logs
                System.out.println(request.getPlayer().getName() + " removed from the game.");

            } else {
                responseObserver.onNext(RemovePlayerResponse.newBuilder().setStatusCode(RemovePlayerResponse.StatusCode.NOT_JOINED)
                        .setStatusMessage("Player not joined").build());
            }
        } else {
            responseObserver.onNext(RemovePlayerResponse.newBuilder().setStatusCode(RemovePlayerResponse.StatusCode.ROOM_NOT_EXIST)
                    .setStatusMessage("Room does not exist.").build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void setPlayerReady(SetPlayerReadyRequest request, StreamObserver<SetPlayerReadyResponse> responseObserver) {

        Room currentRoom = getRoomFromId(request.getRoomId());

        // Check if room exists
        if (currentRoom != null) {

            boolean found = false;
            for (Player player : currentRoom.getPlayersList()) {
                if (player.getId() == request.getPlayerId()) {
                    player.setReady(request.getIsReady());
                    found = true;
                    break;
                }
            }

            // If found send room update
            if (found) {
                // Server logs
                System.out.println(request.getPlayerId() + " id set to " + request.getIsReady());

                sendRoomEventUpdate(request.getRoomId());

                responseObserver.onNext(
                        SetPlayerReadyResponse.newBuilder().setStatusCode(SetPlayerReadyResponse.StatusCode.OK).setIsReady(request.getIsReady())
                                .setStatusMessage("Player set to " + request.getIsReady()).setPlayerId(request.getPlayerId()).build());

            } else {
                responseObserver.onNext(
                        SetPlayerReadyResponse.newBuilder().setStatusCode(SetPlayerReadyResponse.StatusCode.SERVER_ERROR)
                                .setPlayerId(-1).setIsReady(false).setStatusMessage("Player not found").build());
            }
        } else {
            responseObserver.onNext(
                    SetPlayerReadyResponse.newBuilder().setStatusCode(SetPlayerReadyResponse.StatusCode.ROOM_NOT_EXIST)
                            .setPlayerId(-1).setIsReady(false).setStatusMessage("Room does not exist.").build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void unsubscribe(UnsubscribeRequest request, StreamObserver<UnsubscribeResponse> responseObserver) {

        Room currentRoom = getRoomFromId(request.getRoomId());
        UnsubscribeResponse unsubscribeResponse;

        if (currentRoom != null) {
            boolean found = false;
            RoomEventSubscription removalSubscription = null;
            for (RoomEventSubscription currentSubscription : currentRoom.getRoomEventSubscriptionArrayList()) {
                if (request.getPlayerId() == currentSubscription.getSubscription().getPlayerId()) {
                    removalSubscription = currentSubscription;
                    found = true;

                    System.out.println("Unsubscribing " + currentSubscription.getSubscription().getPlayerId() + " from this list.");
                    break;
                }
            }

            // If found, remove subscription with player ID equal to player id sent in the request
            if (found) {
                currentRoom.getRoomEventSubscriptionArrayList().remove(removalSubscription);
                unsubscribeResponse = UnsubscribeResponse.newBuilder().setStatusCode(UnsubscribeResponse.StatusCode.OK)
                        .setStatusMessage("Player unsubscribed").build();
            } else {
                unsubscribeResponse = UnsubscribeResponse.newBuilder().setStatusCode(UnsubscribeResponse.StatusCode.NOT_SUBSCRIBED)
                        .setStatusMessage("Player not subscribed").build();
            }
        } else {
            unsubscribeResponse = UnsubscribeResponse.newBuilder().setStatusCode(UnsubscribeResponse.StatusCode.ROOM_NOT_EXIST)
                    .setStatusMessage("Room does not exist.").build();
        }

        responseObserver.onNext(unsubscribeResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void clickGridCell(ClickGridCellRequest request, StreamObserver<ClickGridCellResponse> responseObserver) {

        Room currentRoom = getRoomFromId(request.getRoomId());

        // Checks if the room exists
        if (currentRoom != null) {

            if (request.getCellClicked() == -1) {
                responseObserver.onNext(ClickGridCellResponse.newBuilder().setStatusMessage("Internal server error")
                        .setStatusCode(ClickGridCellResponse.StatusCode.INTERNAL_SERVER_ERROR).build());
                responseObserver.onCompleted();
                return;
            } else if (request.getPlayerId() != currentRoom.getCurrentPlayerId()) {
                responseObserver.onNext(ClickGridCellResponse.newBuilder().setStatusMessage("Not your turn")
                        .setStatusCode(ClickGridCellResponse.StatusCode.NOT_PLAYER_TURN).build());
                responseObserver.onCompleted();
                return;
            }

            System.out.print("Cell clicked = " + request.getCellClicked() + "\n");

            // Change current player
            currentRoom.changeCurrentPlayer();  // currentPlayer = (currentPlayer + 1) % count

            // Sets current player ID, winner ID to -1 and cell number clicked
            for (GameEventSubscription currentSubscription : currentRoom.getGameEventSubscriptionArrayList()) {

                GameSubscription gameSubscription = GameSubscription.newBuilder().setFirstSubscription(false)
                        .setRoomId(request.getRoomId()).setPlayerId(currentSubscription.getGameSubscription().getPlayerId())
                        .setWinnerId(-1).setCellClicked(request.getCellClicked()).build();

                streamService.getGameEventUpdates(gameSubscription, currentSubscription.getObserver());
            }

            responseObserver.onNext(ClickGridCellResponse.newBuilder().setStatusMessage("Look out for streaming service game update")
                    .setStatusCode(ClickGridCellResponse.StatusCode.OK).build());
        } else {
            responseObserver.onNext(ClickGridCellResponse.newBuilder().setStatusMessage("Room does not exist.")
                    .setStatusCode(ClickGridCellResponse.StatusCode.ROOM_NOT_EXIST).build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void broadcastWinner(BroadcastWinnerRequest request, StreamObserver<BroadcastWinnerResponse> responseObserver) {

        Room currentRoom = getRoomFromId(request.getRoomId());

        // Checks if the room exists
        if (currentRoom != null) {
            com.hmproductions.bingo.models.Player player = request.getPlayer();

            for (Player currentPlayer : currentRoom.getPlayersList()) {
                if (currentPlayer.getId() == player.getId()) {
                    currentPlayer.setWinCount(currentPlayer.getWinCount() + 1);
                }
            }

            for (Player currentPlayer : currentRoom.getPlayersList()) {
                currentPlayer.setReady(false);
            }

            // Streams winner ID to all room players
            for (GameEventSubscription currentSubscription : currentRoom.getGameEventSubscriptionArrayList()) {

                GameSubscription gameSubscription = GameSubscription.newBuilder().setFirstSubscription(false)
                        .setRoomId(request.getRoomId()).setPlayerId(currentSubscription.getGameSubscription().getPlayerId())
                        .setWinnerId(player.getId()).setCellClicked(-1).build();

                streamService.getGameEventUpdates(gameSubscription, currentSubscription.getObserver());
            }

            responseObserver.onNext(BroadcastWinnerResponse.newBuilder().setStatusCode(BroadcastWinnerResponse.StatusCode.OK)
                    .setStatusMessage("Player declared as winner").build());
        } else {
            responseObserver.onNext(BroadcastWinnerResponse.newBuilder().setStatusMessage("Room does not exist")
                    .setStatusCode(BroadcastWinnerResponse.StatusCode.ROOM_NOT_EXIST).build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void quitPlayer(QuitPlayerRequest request, StreamObserver<QuitPlayerResponse> responseObserver) {

        Room currentRoom = getRoomFromId(request.getRoomId());

        if (currentRoom != null) {
            com.hmproductions.bingo.models.Player player = request.getPlayer();

            for (GameEventSubscription currentSubscription : currentRoom.getGameEventSubscriptionArrayList()) {

                GameSubscription gameSubscription = GameSubscription.newBuilder().setFirstSubscription(false)
                        .setRoomId(request.getRoomId()).setPlayerId(currentSubscription.getGameSubscription().getPlayerId())
                        .setWinnerId(player.getId()).setCellClicked(-2).build();

                /* This is a very hacky way to check if user has abruply left the game. Since we call QuitPlayer from transport
                terminated method we need to check if this method is called from filter. This is done by settings winCount to -101*/

                if (!(player.getId() == currentSubscription.getGameSubscription().getPlayerId() && player.getWinCount() == -101))
                    streamService.getGameEventUpdates(gameSubscription, currentSubscription.getObserver());
            }

            currentRoom.getGameEventSubscriptionArrayList().clear();

            boolean found = false;
            Player removePlayer = null;

            for (Player currentPlayer : currentRoom.getPlayersList()) {
                if (currentPlayer.getId() == request.getPlayer().getId()) {
                    removePlayer = currentPlayer;
                    found = true;
                } else {
                    currentPlayer.setReady(false);
                }
            }

            // Server logs
            if (removePlayer != null)
                System.out.println(removePlayer.getName() + " removed from list.");

            if (found) {
                currentRoom.getPlayersList().remove(removePlayer);
                currentRoom.setCount(currentRoom.getCount() - 1);
            }

            found = false;
            RoomEventSubscription removeSubscription = null;

            for (RoomEventSubscription subscription : currentRoom.getRoomEventSubscriptionArrayList()) {
                if (subscription.getSubscription().getPlayerId() == request.getPlayer().getId()) {
                    found = true;
                    removeSubscription = subscription;
                }
            }

            if (found)
                currentRoom.getRoomEventSubscriptionArrayList().remove(removeSubscription);

            if (currentRoom.getCount() == 0) {
                System.out.print("Room with id " + currentRoom.getRoomId() + " destroyed.");
                roomsList.remove(currentRoom);
            }

            responseObserver.onNext(QuitPlayerResponse.newBuilder().setStatusCode(QuitPlayerResponse.StatusCode.OK)
                    .setStatusMessage("Player quit the game").build());
        } else {
            responseObserver.onNext(QuitPlayerResponse.newBuilder().setStatusCode(QuitPlayerResponse.StatusCode.ROOM_NOT_EXIST)
                    .setStatusMessage("Room does not exist").build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void startNextRound(StartNextRoundRequest request, StreamObserver<StartNextRoundResponse> responseObserver) {

        Room currentRoom = getRoomFromId(request.getRoomId());
        if (currentRoom != null) {
            for (Player player : currentRoom.getPlayersList()) {
                if (player.getId() == request.getPlayerId())
                    player.setReady(true);
            }

            if (allPlayersReady(currentRoom.getPlayersList())) {
                for (GameEventSubscription currentSubscription : currentRoom.getGameEventSubscriptionArrayList()) {

                    GameSubscription gameSubscription = GameSubscription.newBuilder().setFirstSubscription(false)
                            .setRoomId(request.getRoomId()).setPlayerId(currentSubscription.getGameSubscription().getPlayerId())
                            .setWinnerId(-1).setCellClicked(-3).build();

                    streamService.getGameEventUpdates(gameSubscription, currentSubscription.getObserver());
                }
            }

            responseObserver.onNext(StartNextRoundResponse.newBuilder().setStatusCode(StartNextRoundResponse.StatusCode.OK)
                    .setStatusMessage("Player next round request accepted").build());
        } else {
            responseObserver.onNext(StartNextRoundResponse.newBuilder().setStatusCode(StartNextRoundResponse.StatusCode.ROOM_NOT_EXIST)
                    .setStatusMessage("Room does not exist").build());
        }

        responseObserver.onCompleted();
    }

    private void sendRoomEventUpdate(int roomId) {
        BingoStreamServiceImpl streamService = new BingoStreamServiceImpl();

        Room currentRoom = getRoomFromId(roomId);
        if (currentRoom != null) {
            for (RoomEventSubscription currentSubscription : currentRoom.getRoomEventSubscriptionArrayList()) {
                streamService.getRoomEventUpdates(currentSubscription.getSubscription(), currentSubscription.getObserver());
            }
        }
    }

    private void updateRoomId(int playerId, int roomId) {
        for (ConnectionData data : connectionDataList) {
            if (data.getPlayerId() == playerId)
                data.setRoomId(roomId);
        }
    }
}
