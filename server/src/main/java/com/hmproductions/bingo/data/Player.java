package com.hmproductions.bingo.data;

public class Player {

    private String name, color;
    private int id, winCount;
    private boolean isReady;
    private boolean skipped;

    public Player(String name, String color, int id, boolean isReady, boolean skipped) {
        this.name = name;
        this.color = color;
        this.id = id;
        this.isReady = isReady;
        this.skipped = skipped;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getId() {
        return id;
    }

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public void setSkipped(boolean skipped) {
        this.skipped = skipped;
    }
}
