package com.hmproductions.bingo.filter;

import com.hmproductions.bingo.data.ConnectionData;
import com.hmproductions.bingo.data.Room;
import com.hmproductions.bingo.data.RoomEventSubscription;
import com.hmproductions.bingo.services.BingoStreamServiceImpl;
import com.hmproductions.bingo.utils.Constants;

import io.grpc.Metadata;
import io.grpc.ServerStreamTracer;
import io.grpc.Status;

import static com.hmproductions.bingo.BingoServer.connectionDataList;
import static com.hmproductions.bingo.utils.Constants.SESSION_ID_KEY;
import static com.hmproductions.bingo.utils.RoomUtils.getRoomFromId;

public class StreamTerminationTracer extends ServerStreamTracer.Factory {

    @Override
    public ServerStreamTracer newServerStreamTracer(String fullMethodName, Metadata headers) {

        /* System.out.println("Stream tracer called with method name " + fullMethodName); */

        switch (fullMethodName) {
            case Constants.ROOM_STREAMING_METHOD:

                return new ServerStreamTracer() {
                    @Override
                    public void streamClosed(Status status) {
                        super.streamClosed(status);

                        Metadata.Key<String> metadataSessionIdKey = Metadata.Key.of(SESSION_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
                        String sessionId = headers.get(metadataSessionIdKey);

                        ConnectionData data = ConnectionData.getConnectionDataFromSessionId(connectionDataList, sessionId);
                        if (data != null) {
                            TerminationFilter.forceQuitPlayer(data.getRoomId(), data.getPlayerId());

                            BingoStreamServiceImpl streamService = new BingoStreamServiceImpl();

                            Room currentRoom = getRoomFromId(data.getRoomId());
                            if (currentRoom != null) {
                                for (RoomEventSubscription currentSubscription : currentRoom.getRoomEventSubscriptionArrayList()) {
                                    streamService.getRoomEventUpdates(currentSubscription.getSubscription(), currentSubscription.getObserver());
                                }
                            }
                        }

                        System.out.println("Room streaming closed " + headers.toString());
                    }
                };

            case Constants.GAME_STREAMING_METHOD:
                return new ServerStreamTracer() {
                    @Override
                    public void streamClosed(Status status) {
                        super.streamClosed(status);
                        System.out.println("Game streaming closed" + status.toString());
                    }
                };

            default:
                return new ServerStreamTracer() {};
        }
    }
}
