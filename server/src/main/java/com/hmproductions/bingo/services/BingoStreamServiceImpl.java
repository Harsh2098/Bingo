package com.hmproductions.bingo.services;

import com.hmproductions.bingo.BingoStreamServiceGrpc;
import com.hmproductions.bingo.data.GameEventSubscription;
import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.data.RoomEventSubscription;
import com.hmproductions.bingo.datastreams.GameEventUpdate;
import com.hmproductions.bingo.datastreams.RoomEventUpdate;
import com.hmproductions.bingo.models.GameEvent;
import com.hmproductions.bingo.models.GameSubscription;
import com.hmproductions.bingo.models.RoomEvent;
import com.hmproductions.bingo.models.Subscription;

import java.util.ArrayList;
import java.util.Random;

import io.grpc.stub.StreamObserver;

import static com.hmproductions.bingo.services.BingoActionServiceImpl.playersList;
import static com.hmproductions.bingo.utils.Miscellaneous.allPlayersReady;
import static com.hmproductions.bingo.utils.Miscellaneous.getArrayListFromPlayersList;

public class BingoStreamServiceImpl extends BingoStreamServiceGrpc.BingoStreamServiceImplBase {

    static int totalPlayers = 0, currentPlayerPosition = -1;
    private static boolean gameStarted = false;

    static ArrayList<RoomEventSubscription> subscriptionArrayList = new ArrayList<>();
    static ArrayList<GameEventSubscription> gameEventSubscriptionArrayList = new ArrayList<>();

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
            subscriptionArrayList.add(new RoomEventSubscription(responseObserver, request));
        }

        RoomEvent roomEvent = RoomEvent.newBuilder()
                .addAllPlayers(getArrayListFromPlayersList(playersList)).setEventCode(RoomEvent.EventCode.PLAYER_STATE_CHANGED).build();

        responseObserver.onNext(RoomEventUpdate.newBuilder().setRoomEvent(roomEvent).build());


        if (allPlayersReady(playersList) && playersList.size() > 1) {

            setupCurrentPlayerAndStartGame();

            roomEvent = RoomEvent.newBuilder().setEventCode(RoomEvent.EventCode.GAME_START)
                    .addAllPlayers(getArrayListFromPlayersList(playersList)).build();

            responseObserver.onNext(RoomEventUpdate.newBuilder().setRoomEvent(roomEvent).build());

            for (Player player : playersList) {
                player.setWinCount(0);
            }
            // TODO : Call onCompleted()
        }
    }

    @Override
    public void getGameEventUpdates(GameSubscription request, StreamObserver<GameEventUpdate> responseObserver) {

        if (!gameSubscriptionExists(request.getPlayerId())) {
            gameEventSubscriptionArrayList.add(new GameEventSubscription(responseObserver, request));
        }

        GameEvent gameEvent;

        if (request.getFirstSubscription()) {

            gameEvent = GameEvent.newBuilder().setEventCode(GameEvent.EventCode.GAME_STARTED)
                    .setWinner(-1).setCellClicked(-1).setCurrentPlayerId(playersList.get(currentPlayerPosition).getId()).build();

        } else if (request.getCellClicked() == -2) {

            gameEvent = GameEvent.newBuilder().setCurrentPlayerId(playersList.get(currentPlayerPosition).getId())
                    .setEventCode(GameEvent.EventCode.PLAYER_QUIT)
                    .setCellClicked(request.getCellClicked()).setWinner(request.getWinnerId()).build();

        } else if (request.getCellClicked() == -3) {

            gameEvent = GameEvent.newBuilder().setCurrentPlayerId(playersList.get(currentPlayerPosition).getId())
                    .setEventCode(GameEvent.EventCode.NEXT_ROUND)
                    .setCellClicked(request.getCellClicked()).setWinner(request.getWinnerId()).build();
        } else {

            if (request.getWinnerId() == -1) {

                gameEvent = GameEvent.newBuilder().setCurrentPlayerId(playersList.get(currentPlayerPosition).getId())
                        .setEventCode(GameEvent.EventCode.CELL_CLICKED).setCellClicked(request.getCellClicked())
                        .setWinner(request.getWinnerId()).build();
            } else {

                ArrayList<com.hmproductions.bingo.models.Player> modelsPlayerList = new ArrayList<>();

                for (Player player : playersList) {
                    if (player.getId() == request.getWinnerId())
                        player.setWinCount(player.getWinCount());

                    modelsPlayerList.add(com.hmproductions.bingo.models.Player.newBuilder().setName(player.getName())
                            .setColor(player.getColor()).setId(player.getId()).setReady(player.isReady())
                            .setWinCount(player.getWinCount()).build());
                }

                gameEvent = GameEvent.newBuilder().setCurrentPlayerId(playersList.get(currentPlayerPosition).getId())
                        .setEventCode(GameEvent.EventCode.GAME_WON).setCellClicked(request.getCellClicked())
                        .addAllLeaderboard(modelsPlayerList).setWinner(request.getWinnerId()).build();
            }

        }

        responseObserver.onNext(GameEventUpdate.newBuilder().setGameEvent(gameEvent).build());
    }

    private void setupCurrentPlayerAndStartGame() {

        if (!gameStarted) {
            totalPlayers = playersList.size();
            currentPlayerPosition = new Random().nextInt(totalPlayers);
        }

        gameStarted = true;
    }

    private boolean gameSubscriptionExists(int playerId) {

        for (GameEventSubscription currentSubscription : gameEventSubscriptionArrayList) {
            if (currentSubscription.getGameSubscription().getPlayerId() == playerId)
                return true;
        }

        return false;
    }
}
