package com.hmproductions.bingo.utils;

import com.hmproductions.bingo.data.ConnectionData;

import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

import static com.hmproductions.bingo.BingoServer.connectionDataList;
import static com.hmproductions.bingo.utils.Constants.ADD_PLAYER_METHOD_NAME;
import static com.hmproductions.bingo.utils.Constants.GET_ROOMS_METHOD_NAME;
import static com.hmproductions.bingo.utils.Constants.HOST_ROOM_METHOD_NAME;
import static com.hmproductions.bingo.utils.Constants.PLAYER_ID_KEY;
import static com.hmproductions.bingo.utils.Constants.ROOM_ID_KEY;
import static com.hmproductions.bingo.utils.Constants.SESSION_ID_KEY;

public class ServerHeaderInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();

        Metadata.Key<String> metadataSessionIdKey = Metadata.Key.of(SESSION_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
        String sessionId = headers.get(metadataSessionIdKey);

        System.out.println("Call with session id : " + sessionId + " and method name is " + methodName);

        if (methodName.equals("com.hmproductions.bingo.BingoActionService/GetSessionId")) {
            return next.startCall(call, headers);
        }

        if (sessionId != null && methodName.equals(GET_ROOMS_METHOD_NAME)) {
            return next.startCall(call, headers);
        }

        if (sessionId != null && (methodName.equals(ADD_PLAYER_METHOD_NAME) || methodName.equals(HOST_ROOM_METHOD_NAME))) {
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

        if (sessionId == null || !sessionIdExists(sessionId)) {
            call.close(Status.UNAUTHENTICATED.withDescription("Session id is empty or invalid"), headers);
            return new ServerCall.Listener<ReqT>() {
            };
        }

        if (methodName.equals("com.hmproductions.bingo.BingoActionService/RemovePlayer") ||
                methodName.equals("com.hmproductions.bingo.BingoActionService/QuitPlayer")) {
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
}
