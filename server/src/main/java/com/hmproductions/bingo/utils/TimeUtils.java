package com.hmproductions.bingo.utils;

import com.hmproductions.bingo.data.Room;
import com.hmproductions.bingo.filter.TerminationFilter;

import java.util.TimerTask;

public class TimeUtils {

    public static int getValueFromEnum(Room.TIME_LIMIT timeLimit) {
        switch (timeLimit) {
            case SECONDS_3: return 0;
            case SECONDS_10: return 1;
            default: return 2;
        }
    }

    public static Room.TIME_LIMIT getEnumFromValue(int value) {
        switch (value) {
            case 0 : return Room.TIME_LIMIT.SECONDS_3;
            case 1 : return Room.TIME_LIMIT.SECONDS_10;
            default: return Room.TIME_LIMIT.MINUTE_1;
        }
    }

    public static int getExactValueFromEnum(Room.TIME_LIMIT timeLimit) {
        switch (timeLimit) {
            case SECONDS_3:
                return 3;
            case SECONDS_10:
                return 10;
            case MINUTE_1:
                return 60;
            default:
                return -1;
        }
    }
}
