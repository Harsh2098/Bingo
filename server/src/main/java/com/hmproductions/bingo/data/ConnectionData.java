package com.hmproductions.bingo.data;

public class ConnectionData {

    private String sessionId, remoteAddress;
    private int playerId, roomId;

    public ConnectionData(String sessionId, String remoteAddress, int playerId, int roomId) {
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
        this.playerId = playerId;
        this.roomId = roomId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
}
