package com.hmproductions.bingo.data;

public class ClickCellRequest {

    private int roomId, playerId, cellClicked;
    private boolean won;

    public ClickCellRequest(int roomId, int playerId, int cellClicked, boolean won) {
        this.roomId = roomId;
        this.playerId = playerId;
        this.cellClicked = cellClicked;
        this.won = won;
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

    public boolean isWon() {
        return won;
    }
}
