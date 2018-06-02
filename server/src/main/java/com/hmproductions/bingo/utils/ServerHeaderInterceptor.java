package com.hmproductions.bingo.utils;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

import static com.hmproductions.bingo.BingoServer.sessionIdsList;
import static com.hmproductions.bingo.utils.Constants.SESSION_ID_KEY;

public class ServerHeaderInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        Metadata.Key<String> metadataKey = Metadata.Key.of(SESSION_ID_KEY, Metadata.ASCII_STRING_MARSHALLER);
        String sessionId = headers.get(metadataKey);

        if (call.getMethodDescriptor().getFullMethodName().equals("com.hmproductions.bingo.BingoActionService/GetSessionId")) {
            return next.startCall(call, headers);
        }

        if (sessionId == null || !sessionIdExists(sessionId)) {
            call.close(Status.UNAUTHENTICATED.withDescription("Session id is empty or invalid"), headers);
            return new ServerCall.Listener<ReqT>() {
            };
        }

        if (call.getMethodDescriptor().getFullMethodName().equals("com.hmproductions.bingo.BingoActionService/RemovePlayer")) {
            removeSessionIdFromList(sessionId);
        }

        return next.startCall(call, headers);
    }

    private boolean sessionIdExists(String sessionId) {
        for (String currentSessionId : sessionIdsList) {
            if (currentSessionId.equals(sessionId))
                return true;
        }
        return false;
    }

    private void removeSessionIdFromList(String sessionId) {

        boolean found = false;
        for (String currentSessionId : sessionIdsList) {
            if (sessionId.equals(currentSessionId)) {
                found = true;
                break;
            }
        }

        if (found)
            sessionIdsList.remove(sessionId);
    }
}
