package com.hmproductions.bingo.data;

public class ConnectionData {

    private String sessionId, remoteAddress;
    private int playerId;

    public ConnectionData(String sessionId, String remoteAddress, int playerId) {
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
        this.playerId = playerId;
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
}
