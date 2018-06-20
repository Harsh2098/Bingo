package com.hmproductions.bingo.filter;

import com.hmproductions.bingo.actions.QuitPlayerRequest;
import com.hmproductions.bingo.actions.QuitPlayerResponse;
import com.hmproductions.bingo.actions.RemovePlayerRequest;
import com.hmproductions.bingo.actions.RemovePlayerResponse;
import com.hmproductions.bingo.actions.Unsubscribe;
import com.hmproductions.bingo.models.Player;
import com.hmproductions.bingo.services.BingoActionServiceImpl;

import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.ServerTransportFilter;
import io.grpc.stub.StreamObserver;

import static com.hmproductions.bingo.BingoServer.connectionDataList;
import static com.hmproductions.bingo.services.BingoActionServiceImpl.roomsList;
import static com.hmproductions.bingo.utils.MiscellaneousUtils.getNameFromRoomId;
import static com.hmproductions.bingo.utils.MiscellaneousUtils.getPlayerIdFromRemoteAddress;
import static com.hmproductions.bingo.utils.MiscellaneousUtils.getRoomIdFromRemoteAddress;

public class TerminationFilter extends ServerTransportFilter {

    @Override
    public void transportTerminated(Attributes transportAttrs) {
        super.transportTerminated(transportAttrs);

        // We can call unsubscribe and remove player on new instance since both of these service functions work on static data
        if (transportAttrs != null && transportAttrs.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR) != null) {

            BingoActionServiceImpl bingoActionService = new BingoActionServiceImpl();

            String remoteAddress = transportAttrs.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR).toString();
            int playerId = getPlayerIdFromRemoteAddress(connectionDataList, remoteAddress);
            int roomId = getRoomIdFromRemoteAddress(connectionDataList, remoteAddress);

            System.out.println("Transport termination detected. Player ID = " + playerId + " Room ID = " + roomId);

            if (playerId != -1 && roomId != -1) {
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

                /* This is a very hacky way to check if user has abruply left the game. Since we call QuitPlayer from transport
                terminated method we need to check if this method is called from filter. This is done by settings winCount to -101*/
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
    }
}
