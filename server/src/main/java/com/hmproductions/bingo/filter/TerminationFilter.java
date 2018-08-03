package com.hmproductions.bingo.filter;

import com.hmproductions.bingo.actions.QuitPlayerRequest;
import com.hmproductions.bingo.actions.QuitPlayerResponse;
import com.hmproductions.bingo.actions.RemovePlayerRequest;
import com.hmproductions.bingo.actions.RemovePlayerResponse;
import com.hmproductions.bingo.actions.Unsubscribe;
import com.hmproductions.bingo.models.Player;
import com.hmproductions.bingo.services.BingoActionServiceImpl;

import io.grpc.stub.StreamObserver;

import static com.hmproductions.bingo.services.BingoActionServiceImpl.roomsList;
import static com.hmproductions.bingo.utils.MiscellaneousUtils.getNameFromRoomId;

public class TerminationFilter {

    public static void forceQuitPlayer(int roomId, int playerId) {

        BingoActionServiceImpl bingoActionService = new BingoActionServiceImpl();
        Player player = Player.newBuilder().setId(playerId).setName(getNameFromRoomId(roomsList, roomId, playerId)).build();

        bingoActionService.unsubscribe(Unsubscribe.UnsubscribeRequest.newBuilder().setPlayerId(playerId).build(),
                new StreamObserver<Unsubscribe.UnsubscribeResponse>() {
                    @Override
                    public void onNext(Unsubscribe.UnsubscribeResponse value) {

                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });

        bingoActionService.removePlayer(RemovePlayerRequest.newBuilder().setPlayer(player).build(),
                new StreamObserver<RemovePlayerResponse>() {
                    @Override
                    public void onNext(RemovePlayerResponse value) {

                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });

                /*  This is a very hacky way to check if user has abruptly left the game. Since we call QuitPlayer here we need to tell
                    that the player has abruptly left and don't stream update to this player. This is done by setting winCount to -101  */
        bingoActionService.quitPlayer(QuitPlayerRequest.newBuilder().setRoomId(roomId)
                        .setPlayer(Player.newBuilder().setWinCount(-101).setId(playerId).setReady(true).build()).build(),
                new StreamObserver<QuitPlayerResponse>() {
                    @Override
                    public void onNext(QuitPlayerResponse value) {

                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });

        System.out.println("Transport for player id " + playerId + " terminated");
    }
}
