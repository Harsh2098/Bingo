package com.hmproductions.bingo.data;

public class Room {

    private int roomId, count, maxSize;
    private String name;

    public Room(int roomId, int count, int maxSize, String name) {
        this.roomId = roomId;
        this.count = count;
        this.maxSize = maxSize;
        this.name = name;
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

}
