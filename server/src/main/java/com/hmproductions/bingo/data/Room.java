package com.hmproductions.bingo.data;

import java.util.ArrayList;

public class Room {

    private int roomId, count;

    private ArrayList<Player> playersList;
    private ArrayList<RoomEventSubscription> subscriptionArrayList = new ArrayList<>();
    private ArrayList<GameEventSubscription> gameEventSubscriptionArrayList = new ArrayList<>();

    public Room(int roomId, ArrayList<Player> playersList, int count) {
        this.roomId = roomId;
        this.playersList = playersList;
        this.count = count;
    }

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

    public ArrayList<RoomEventSubscription> getSubscriptionArrayList() {
        return subscriptionArrayList;
    }

    public void setSubscriptionArrayList(ArrayList<RoomEventSubscription> subscriptionArrayList) {
        this.subscriptionArrayList = subscriptionArrayList;
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
}
