package com.hmproductions.bingo.data;

/**
 * Created by Harsh Mahajan on 4/7/2017.
 *
 * GridCell holds Value, PositionX, PositionY of each cell in the recycler view.
 */

public class GridCell {

    private int value, positionX, positionY;
    private boolean isClicked;

    public GridCell(int value, int positionX, int positionY, boolean isClicked) {
        this.value = value;
        this.positionX = positionX;
        this.positionY = positionY;
        this.isClicked = isClicked;
    }

    public int getValue() {
        return value;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public boolean getIsClicked() {
        return isClicked;
    }

    public void setIsClicked(boolean clicked) {
        isClicked = clicked;
    }
}
