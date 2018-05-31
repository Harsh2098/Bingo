package com.hmproductions.bingo;

import com.hmproductions.bingo.services.BingoActionServiceImpl;
import com.hmproductions.bingo.services.BingoStreamServiceImpl;
import com.hmproductions.bingo.utils.Constants;

import java.io.IOException;
import java.net.Inet4Address;

import io.grpc.Attributes;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerTransportFilter;

public class BingoServer {

    static public void main(String args[]) {

        Server server = ServerBuilder
                .forPort(Constants.SERVER_PORT)
                .addService(new BingoActionServiceImpl())
                .addService(new BingoStreamServiceImpl())
//                .addTransportFilter(new ServerTransportFilter() {
//                    @Override
//                    public void transportTerminated(Attributes transportAttrs) {
//                        super.transportTerminated(transportAttrs);
//                        transportAttrs.
//                    }
//                })
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