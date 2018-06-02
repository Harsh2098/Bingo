package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.GetSessionIdRequest;
import com.hmproductions.bingo.actions.GetSessionIdResponse;

import static com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo;
import static com.hmproductions.bingo.utils.ConnectionUtils.isReachableByTcp;
import static com.hmproductions.bingo.utils.Constants.SERVER_ADDRESS;
import static com.hmproductions.bingo.utils.Constants.SERVER_PORT;

public class SessionIdLoader extends AsyncTaskLoader<GetSessionIdResponse> {

    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;

    public SessionIdLoader(@NonNull Context context, BingoActionServiceGrpc.BingoActionServiceBlockingStub stub) {
        super(context);
        actionServiceBlockingStub = stub;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Nullable
    @Override
    public GetSessionIdResponse loadInBackground() {
        if (getConnectionInfo(getContext()) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {
            return actionServiceBlockingStub.getSessionId(GetSessionIdRequest.newBuilder().setTime(System.currentTimeMillis()).build());
        }
        return null;
    }
}
