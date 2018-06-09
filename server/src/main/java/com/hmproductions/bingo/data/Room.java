package com.hmproductions.bingo.data;

import java.util.ArrayList;

public class Room {

    public enum Status {
        WAITING, INGAME
    }

    private int roomId, count, currentPlayerPosition;
    private Status status;

    private ArrayList<Player> playersList;
    private ArrayList<RoomEventSubscription> roomEventSubscriptionArrayList = new ArrayList<>();
    private ArrayList<GameEventSubscription> gameEventSubscriptionArrayList = new ArrayList<>();

    /* Constructor */
    public Room(int roomId, ArrayList<Player> playersList, int count, int currentPlayerPosition, Status status) {
        this.roomId = roomId;
        this.playersList = playersList;
        this.count = count;
        this.currentPlayerPosition = currentPlayerPosition;
        this.status = status;
    }

    public int getCurrentPlayerId() {
        return currentPlayerPosition != -1 ? playersList.get(currentPlayerPosition).getId() : -1;
    }

    public void changeCurrentPlayer() {
        currentPlayerPosition = (currentPlayerPosition + 1) % count;
    }

    public void changeRoomStatus(Status status) {
        this.status = status;
    }

    /* =========================== Getters and Setters =========================== */

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public ArrayList<Player> getPlayersList() {
        return playersList;
    }

    public void setPlayersList(ArrayList<Player> playersList) {
        this.playersList = playersList;
    }

    public ArrayList<RoomEventSubscription> getRoomEventSubscriptionArrayList() {
        return roomEventSubscriptionArrayList;
    }

    public void setRoomEventSubscriptionArrayList(ArrayList<RoomEventSubscription> roomEventSubscriptionArrayList) {
        this.roomEventSubscriptionArrayList = roomEventSubscriptionArrayList;
    }

    public ArrayList<GameEventSubscription> getGameEventSubscriptionArrayList() {
        return gameEventSubscriptionArrayList;
    }

    public void setGameEventSubscriptionArrayList(ArrayList<GameEventSubscription> gameEventSubscriptionArrayList) {
        this.gameEventSubscriptionArrayList = gameEventSubscriptionArrayList;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getCurrentPlayerPosition() {
        return currentPlayerPosition;
    }

    public void setCurrentPlayerPosition(int currentPlayerPosition) {
        this.currentPlayerPosition = currentPlayerPosition;
    }
}
