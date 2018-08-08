package com.hmproductions.bingo.data;

import com.hmproductions.bingo.models.GameSubscription;
import com.hmproductions.bingo.services.BingoStreamServiceImpl;

import java.util.ArrayList;
import java.util.Timer;

import static com.hmproductions.bingo.utils.Constants.CLEAR_GAME_SUBSCRIPTION;
import static com.hmproductions.bingo.utils.TimeUtils.getValueFromEnum;

public class Room {

    public enum Status { WAITING, INGAME }

    public enum TIME_LIMIT { SECONDS_3, SECONDS_10, MINUTE_1 }

    private int roomId, count, currentPlayerPosition, maxSize;
    private Status status;
    private String name;
    private TIME_LIMIT timeLimit;
    private Timer timer;
    private boolean timerStarted;

    private ArrayList<Player> playersList;
    private ArrayList<RoomEventSubscription> roomEventSubscriptionArrayList = new ArrayList<>();
    private ArrayList<GameEventSubscription> gameEventSubscriptionArrayList = new ArrayList<>();

    /* Constructor */
    public Room(int roomId, ArrayList<Player> playersList, int count, int currentPlayerPosition, int maxSize, Status status, String name, TIME_LIMIT timeLimit) {
        this.roomId = roomId;
        this.playersList = playersList;
        this.count = count;
        this.currentPlayerPosition = currentPlayerPosition;
        this.maxSize = maxSize;
        this.status = status;
        this.name = name;
        this.timeLimit = timeLimit;

        timer = new Timer();
        timerStarted = false;
    }

    public int getCurrentPlayerId() {
        if (playersList.size() == 0) return -1;
        return currentPlayerPosition != -1 ? playersList.get(currentPlayerPosition).getId() : -1;
    }

    public void changeCurrentPlayer() {
        currentPlayerPosition = (currentPlayerPosition + 1) % count;
    }

    public void changeRoomStatus(Status status) {
        this.status = status;

        if (status == Status.WAITING) {
            BingoStreamServiceImpl streamService = new BingoStreamServiceImpl();
            for(GameEventSubscription subscription : gameEventSubscriptionArrayList) {
                streamService.getGameEventUpdates(GameSubscription.newBuilder().setCellClicked(CLEAR_GAME_SUBSCRIPTION).build(),
                        subscription.getObserver());
            }
            gameEventSubscriptionArrayList.clear();
        }
    }

    public static String getRoomNameFromId(ArrayList<Room> roomArrayList, int roomId) {
        for (Room room : roomArrayList) {
            if (room.getRoomId() == roomId)
                return room.getName();
        }

        return null;
    }

    public static int getTimeLimitFromRoomId(ArrayList<Room> roomArrayList, int roomId) {
        for (Room room : roomArrayList) {
            if (room.getRoomId() == roomId)
                return getValueFromEnum(room.getTimeLimit());
        }

        return -1;
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

    public int getMaxSize() {
        return maxSize;
    }

    public String getName() {
        return name;
    }

    public TIME_LIMIT getTimeLimit() {
        return timeLimit;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public boolean isTimerStarted() {
        return timerStarted;
    }

    public void setTimerStarted(boolean timerStarted) {
        this.timerStarted = timerStarted;
    }
}
