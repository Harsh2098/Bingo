package com.hmproductions.bingo.data;

public class ClickCellRequest {

    private int roomId, playerId, cellClicked;

    public ClickCellRequest(int roomId, int playerId, int cellClicked) {
        this.roomId = roomId;
        this.playerId = playerId;
        this.cellClicked = cellClicked;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getCellClicked() {
        return cellClicked;
    }

}
