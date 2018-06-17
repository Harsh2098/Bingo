package com.hmproductions.bingo;

import com.hmproductions.bingo.actions.QuitPlayerRequest;
import com.hmproductions.bingo.actions.QuitPlayerResponse;
import com.hmproductions.bingo.actions.RemovePlayerRequest;
import com.hmproductions.bingo.actions.RemovePlayerResponse;
import com.hmproductions.bingo.actions.Unsubscribe;
import com.hmproductions.bingo.data.ConnectionData;
import com.hmproductions.bingo.models.Player;
import com.hmproductions.bingo.services.BingoActionServiceImpl;
import com.hmproductions.bingo.services.BingoStreamServiceImpl;
import com.hmproductions.bingo.utils.Constants;
import com.hmproductions.bingo.utils.ServerHeaderInterceptor;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;

import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerTransportFilter;
import io.grpc.stub.StreamObserver;

import static com.hmproductions.bingo.services.BingoActionServiceImpl.roomsList;
import static com.hmproductions.bingo.utils.MiscellaneousUtils.getNameFromRoomId;
import static com.hmproductions.bingo.utils.MiscellaneousUtils.getPlayerIdFromRemoteAddress;
import static com.hmproductions.bingo.utils.MiscellaneousUtils.getRoomIdFromRemoteAddress;

public class BingoServer {

    public static ArrayList<ConnectionData> connectionDataList = new ArrayList<>();

    static public void main(String args[]) {

        File serverCertificateFile = new File("server/server.crt");
        File serverKeyFile = new File("server/server.key");

        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        ServerTransportFilter connectionTransportFilter = new ServerTransportFilter() {
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

                        bingoActionService.quitPlayer(QuitPlayerRequest.newBuilder().setRoomId(roomId)
                                .setPlayer(Player.newBuilder().setId(playerId).setReady(true).build()).build(),
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
        };

        Server server = ServerBuilder
                .forPort(Constants.SERVER_PORT)
                .addService(new BingoActionServiceImpl())
                .addService(new BingoStreamServiceImpl())
                .intercept(new ServerHeaderInterceptor())
                .addTransportFilter(connectionTransportFilter)
                .useTransportSecurity(serverCertificateFile, serverKeyFile)
                .build();

        try {

            System.out.print("Server started !\n");
            server.start();

            System.out.println(Inet4Address.getLocalHost().getHostAddress());

            server.awaitTermination();

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}