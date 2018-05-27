package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.ClickGridCell.*;
import com.hmproductions.bingo.data.ClickCellRequest;

import static com.hmproductions.bingo.utils.Constants.SERVER_ADDRESS;
import static com.hmproductions.bingo.utils.Constants.SERVER_PORT;
import static com.hmproductions.bingo.utils.ConnectionUtils.getConnectionInfo;
import static com.hmproductions.bingo.utils.ConnectionUtils.isReachableByTcp;

public class ClickCellLoader extends AsyncTaskLoader<ClickGridCellResponse> {

    private BingoActionServiceGrpc.BingoActionServiceBlockingStub actionServiceBlockingStub;
    private ClickCellRequest clickCellRequest;

    public ClickCellLoader(Context context, BingoActionServiceGrpc.BingoActionServiceBlockingStub stub, ClickCellRequest cellRequest) {
        super(context);
        this.clickCellRequest = cellRequest;
        this.actionServiceBlockingStub = stub;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public ClickGridCellResponse loadInBackground() {
        if (getConnectionInfo(getContext()) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {

            return actionServiceBlockingStub.clickGridCell(ClickGridCellRequest.newBuilder().setRoomId(clickCellRequest.getRoomId())
                .setPlayerId(clickCellRequest.getPlayerId()).setCellClicked(clickCellRequest.getCellClicked()).build());
        } else {
            return null;
        }
    }
}
