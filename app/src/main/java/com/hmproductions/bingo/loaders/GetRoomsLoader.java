package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.GetRoomsRequest;
import com.hmproductions.bingo.actions.GetRoomsResponse;

import static com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo;
import static com.hmproductions.bingo.utils.ConnectionUtils.isReachableByTcp;
import static com.hmproductions.bingo.utils.Constants.SERVER_ADDRESS;
import static com.hmproductions.bingo.utils.Constants.SERVER_PORT;

public class GetRoomsLoader extends AsyncTaskLoader<GetRoomsResponse> {

    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    public GetRoomsLoader(@NonNull Context context, BingoActionServiceGrpc.BingoActionServiceBlockingStub stub) {
        super(context);
        this.actionServiceBlockingStub = stub;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public GetRoomsResponse loadInBackground() {
        if (getConnectionInfo(getContext()) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {
            return actionServiceBlockingStub.getRooms(GetRoomsRequest.newBuilder().build());
        }

        return null;
    }
}
