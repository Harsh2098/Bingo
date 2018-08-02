package com.hmproductions.bingo.utils;

public class TimeLimitUtils {

    public enum TIME_LIMIT {SECONDS_3, SECONDS_10, MINUTE_1}

    public static int getValueFromEnum(TIME_LIMIT timeLimit) {
        switch (timeLimit) {
            case SECONDS_3:
                return 0;
            case SECONDS_10:
                return 1;
            default:
                return 2;
        }
    }

    public static int getExactValueFromEnum(TIME_LIMIT timeLimit) {
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

    public static TIME_LIMIT getEnumFromValue(int value) {
        switch (value) {
            case 0:
                return TIME_LIMIT.SECONDS_3;
            case 1:
                return TIME_LIMIT.SECONDS_10;
            default:
                return TIME_LIMIT.MINUTE_1;
        }
    }
}
