package com.hmproductions.bingo.filter;

import com.hmproductions.bingo.data.ConnectionData;
import com.hmproductions.bingo.data.Room;
import com.hmproductions.bingo.data.RoomEventSubscription;
import com.hmproductions.bingo.utils.RoomUtils;

import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

import static com.hmproductions.bingo.BingoServer.connectionDataList;
import static com.hmproductions.bingo.utils.Constants.ADD_PLAYER_METHOD;
import static com.hmproductions.bingo.utils.Constants.GET_ROOMS_METHOD;
import static com.hmproductions.bingo.utils.Constants.GET_SESSION_ID_METHOD;
import static com.hmproductions.bingo.utils.Constants.HOST_ROOM_METHOD;
import static com.hmproductions.bingo.utils.Constants.PLAYER_ID_KEY;
import static com.hmproductions.bingo.utils.Constants.QUIT_PLAYER_METHOD;
import static com.hmproductions.bingo.utils.Constants.REMOVE_PLAYER_METHOD;
import static com.hmproductions.bingo.utils.Constants.ROOM_ID_KEY;
import static com.hmproductions.bingo.utils.Constants.ROOM_STREAMING_METHOD;
import static com.hmproductions.bingo.utils.Constants.SESSION_ID_KEY;
import static com.hmproductions.bingo.utils.RoomUtils.getRoomFromId;

public class ServerHeaderInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();

        Metadata.Key<String> metadataSessionIdKey = Metadata.Key.of(SESSION_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
        String sessionId = headers.get(metadataSessionIdKey);

        System.out.println("Call with session id : " + sessionId);

        if (methodName.equals(GET_SESSION_ID_METHOD)) {
            return next.startCall(call, headers);
        }

        if (sessionId != null && methodName.equals(GET_ROOMS_METHOD)) {
            return next.startCall(call, headers);
        }

        if (sessionId != null && (methodName.equals(ADD_PLAYER_METHOD) || methodName.equals(HOST_ROOM_METHOD))) {
            Metadata.Key<String> metadataPlayerIdKey = Metadata.Key.of(PLAYER_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
            String playerIdString = headers.get(metadataPlayerIdKey);

            Metadata.Key<String> metadataRoomIdKey = Metadata.Key.of(ROOM_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
            String roomIdString = headers.get(metadataRoomIdKey);

            if (playerIdString != null && roomIdString != null && call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR) != null) {
                int playerId = Integer.parseInt(playerIdString);
                int roomId = Integer.parseInt(roomIdString);
                System.out.println("Adding " + playerId + " to connection data list. Room ID = " + roomId);
                connectionDataList.add(new ConnectionData(sessionId, call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR).toString(), playerId, roomId));
            }

            return next.startCall(call, headers);
        }

        if (sessionId == null) {
            call.close(Status.UNAUTHENTICATED.withDescription("Session Id is empty"), headers);
            return new ServerCall.Listener<ReqT>() {
            };
        }

        if (!sessionIdExists(sessionId)) {
            call.close(Status.NOT_FOUND.withDescription("Session Id is invalid"), headers);
            return new ServerCall.Listener<ReqT>() {
            };
        }

        if (methodName.equals(ROOM_STREAMING_METHOD) && call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR) != null) {
            System.out.println("remote address = " + call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR));

            /* 1. Update remote address in connectionDataList
             * 2. Remove existing room streaming data       */
            Metadata.Key<String> metadataPlayerIdKey = Metadata.Key.of(PLAYER_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
            String playerIdString = headers.get(metadataPlayerIdKey);

            if (playerIdString != null)
                removeExistingRoomStreamingConnectionData(call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR).toString(), Integer.valueOf(playerIdString));
        }

        if (methodName.equals(REMOVE_PLAYER_METHOD) || methodName.equals(QUIT_PLAYER_METHOD)) {
            removeSessionIdFromList(sessionId);
            new Thread(new RoomUtils.RoomDestroyRunnable()).start();
        }

        return next.startCall(call, headers);
    }

    private boolean sessionIdExists(String sessionId) {
        for (ConnectionData currentData : connectionDataList) {
            if (currentData.getSessionId().equals(sessionId))
                return true;
        }
        return false;
    }

    private void removeSessionIdFromList(String sessionId) {

        boolean found = false;
        ConnectionData removalData = null;

        for (ConnectionData currentData : connectionDataList) {
            if (sessionId.equals(currentData.getSessionId())) {
                found = true;
                removalData = currentData;
                break;
            }
        }

        if (found)
            connectionDataList.remove(removalData);
    }

    private void removeExistingRoomStreamingConnectionData(String remoteIpAddress, int playerId) {
        for (ConnectionData connectionData : connectionDataList) {
            if (connectionData.getPlayerId() == playerId) {
                connectionData.setRemoteAddress(remoteIpAddress);
                Room currentRoom = getRoomFromId(connectionData.getRoomId());
                if (currentRoom != null) {
                    RoomEventSubscription removalSubscription = null;
                    for (RoomEventSubscription subscription : currentRoom.getRoomEventSubscriptionArrayList()) {
                        if (subscription.getSubscription().getPlayerId() == playerId) {
                            removalSubscription = subscription;
                            System.out.println("Removing " + connectionData.getRemoteAddress() + " with ID " + connectionData.getPlayerId() + " temporarily");
                            break;
                        }
                    }
                    if (removalSubscription != null)
                        currentRoom.getRoomEventSubscriptionArrayList().remove(removalSubscription);
                }
            }
        }
    }
}
