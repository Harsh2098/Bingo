package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.Unsubscribe;

import static com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo;
import static com.hmproductions.bingo.utils.ConnectionUtils.isReachableByTcp;
import static com.hmproductions.bingo.utils.Constants.SERVER_ADDRESS;
import static com.hmproductions.bingo.utils.Constants.SERVER_PORT;

public class UnsubscribeLoader extends AsyncTaskLoader<Unsubscribe.UnsubscribeResponse> {

    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;
    private int playerId, roomId;

    public UnsubscribeLoader(Context context, BingoActionServiceGrpc.BingoActionServiceBlockingStub stub, int playerId, int roomId) {
        super(context);
        this.actionServiceBlockingStub = stub;
        this.playerId = playerId;
        this.roomId = roomId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Unsubscribe.UnsubscribeResponse loadInBackground() {
        if (getConnectionInfo(getContext()) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {
            return actionServiceBlockingStub.unsubscribe(Unsubscribe.UnsubscribeRequest.newBuilder()
                .setPlayerId(this.playerId).setRoomId(this.roomId).build());
        } else {
            return null;
        }
    }
}
