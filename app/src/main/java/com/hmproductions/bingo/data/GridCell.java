package com.hmproductions.bingo.data;

/**
 * Created by Harsh Mahajan on 4/7/2017.
 *
 * GridCell holds Value, PositionX, PositionY of each cell in the recycler view.
 */

public class GridCell {

    private int mValue, mPositionX, mPositionY;

    public GridCell(int value, int positionX, int positionY) {

        mValue = value;
        mPositionX = positionX;
        mPositionY = positionY;
    }

    public int getValue() {
        return mValue;
    }

    public int getPositionX() {
        return mPositionX;
    }

    public int getPositionY() {
        return mPositionY;
    }
}
