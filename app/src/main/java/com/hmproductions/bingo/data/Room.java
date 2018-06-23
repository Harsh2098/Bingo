package com.hmproductions.bingo.data;

import com.hmproductions.bingo.utils.TimeLimitUtils.TIME_LIMIT;

public class Room {

    private int roomId, count, maxSize;
    private String name;
    private TIME_LIMIT timeLimit;

    public Room(int roomId, int count, int maxSize, String name, TIME_LIMIT timeLimit) {
        this.roomId = roomId;
        this.count = count;
        this.maxSize = maxSize;
        this.name = name;
        this.timeLimit = timeLimit;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getCount() {
        return count;
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
}
