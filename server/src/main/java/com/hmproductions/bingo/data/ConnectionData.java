package com.hmproductions.bingo.data;

import java.util.ArrayList;

public class ConnectionData {

    private String sessionId, remoteAddress;
    private int playerId, roomId;

    public ConnectionData(String sessionId, String remoteAddress, int playerId, int roomId) {
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress.substring(0, remoteAddress.length() - 6);
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

    public static ConnectionData getConnectionDataFromSessionId(ArrayList<ConnectionData> list, String sessionId) {
        for (ConnectionData data : list) {
            if (data.getSessionId().equals(sessionId))
                return data;
        }
        return null;
    }
}
