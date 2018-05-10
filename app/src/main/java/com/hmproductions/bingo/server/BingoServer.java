package com.hmproductions.bingo.server;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import static com.hmproductions.bingo.utils.Constants.SERVER_PORT;

public class BingoServer {

    private Server server;

    public BingoServer() {
        server = ServerBuilder.forPort(SERVER_PORT).addService(new BingoServiceImpl()).build();
    }

    public void startServer() {
        try {
            server.start();
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
