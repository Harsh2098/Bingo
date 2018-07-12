package com.hmproductions.bingo.filter;

import com.hmproductions.bingo.utils.Constants;

import io.grpc.Metadata;
import io.grpc.ServerStreamTracer;
import io.grpc.Status;

public class StreamTerminationTracer extends ServerStreamTracer.Factory {

    @Override
    public ServerStreamTracer newServerStreamTracer(String fullMethodName, Metadata headers) {

        System.out.println("Stream tracer called with method name " + fullMethodName);

        switch (fullMethodName) {
            case Constants.ROOM_STREAMING_METHOD:

                return new ServerStreamTracer() {
                    @Override
                    public void streamClosed(Status status) {
                        super.streamClosed(status);
                        System.out.println("Room streaming closed");
                    }
                };

            case Constants.GAME_STREAMING_METHOD:
                return new ServerStreamTracer() {
                    @Override
                    public void streamClosed(Status status) {
                        super.streamClosed(status);
                        System.out.println("Game streaming closed");
                    }
                };

            default:
                return new ServerStreamTracer() {};
        }
    }
}
