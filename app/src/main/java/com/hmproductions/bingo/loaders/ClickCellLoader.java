package com.hmproductions.bingo.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.hmproductions.bingo.BingoActionServiceGrpc;
import com.hmproductions.bingo.actions.ClickGridCell.*;
import com.hmproductions.bingo.data.ClickCellRequest;

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

        return actionServiceBlockingStub.clickGridCell(ClickGridCellRequest.newBuilder().setRoomId(clickCellRequest.getRoomId())
                .setPlayerId(clickCellRequest.getPlayerId()).setCellClicked(clickCellRequest.getCellClicked()).build());
    }
}
