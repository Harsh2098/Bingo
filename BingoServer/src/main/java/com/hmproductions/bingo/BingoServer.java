package com.hmproductions.bingo;

import java.io.IOException;
import java.net.Inet4Address;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class BingoServer {

    static public void main(String args[]) {

        java.security.Security.addProvider(
                new org.bouncycastle.jce.provider.BouncyCastleProvider()
        );

        Server server = ServerBuilder
                .forPort(Constants.SERVER_PORT)
                .addService(new BingoServiceImpl())
                //.useTransportSecurity(new File("BingoServer/server.crt"), new File("BingoServer/server.key"))
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

