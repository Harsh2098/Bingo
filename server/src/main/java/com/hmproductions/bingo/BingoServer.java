package com.hmproductions.bingo;

import com.hmproductions.bingo.services.BingoActionServiceImpl;
import com.hmproductions.bingo.services.BingoStreamServiceImpl;
import com.hmproductions.bingo.utils.Constants;
import com.hmproductions.bingo.utils.ServerHeaderInterceptor;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class BingoServer {

    public static List<String> sessionIdsList = new ArrayList<>();

    static public void main(String args[]) {

        File serverCertificateFile = new File("server/server.crt");
        File serverKeyFile = new File("server/server.key");

        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        Server server = ServerBuilder
                .forPort(Constants.SERVER_PORT)
                .addService(new BingoActionServiceImpl())
                .addService(new BingoStreamServiceImpl())
                .intercept(new ServerHeaderInterceptor())
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