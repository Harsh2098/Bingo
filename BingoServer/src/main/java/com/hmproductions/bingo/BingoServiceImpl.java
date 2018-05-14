package com.hmproductions.bingo;

import com.hmproductions.bingo.actions.GetGridSize.GetGridSizeRequest;
import com.hmproductions.bingo.actions.GetGridSize.GetGridSizeResponse;

import io.grpc.stub.StreamObserver;

public class BingoServiceImpl extends BingoActionServiceGrpc.BingoActionServiceImplBase {

    @Override
    public void getGridSize(GetGridSizeRequest request, StreamObserver<GetGridSizeResponse> responseObserver) {

        GetGridSizeResponse response;

        if (request.getPlayerId() == 101) {
            response = GetGridSizeResponse.newBuilder().setSize(5).build();
        } else {
            response = GetGridSizeResponse.newBuilder().setSize(-1).build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
