package com.hmproductions.bingo.data;

public class GridCell {

    private int value;
    private boolean isClicked;

    public GridCell(int value, boolean isClicked) {
        this.value = value;
        this.isClicked = isClicked;
    }

    public int getValue() {
        return value;
    }

    public boolean getIsClicked() {
        return isClicked;
    }

    public void setIsClicked(boolean clicked) {
        isClicked = clicked;
    }
}
