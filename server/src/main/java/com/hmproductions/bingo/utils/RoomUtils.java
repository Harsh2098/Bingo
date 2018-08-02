package com.hmproductions.bingo.utils;

import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.data.Room;
import com.hmproductions.bingo.data.Room.TIME_LIMIT;

import java.util.ArrayList;

import javax.annotation.Nullable;

import static com.hmproductions.bingo.services.BingoActionServiceImpl.roomsList;

public class RoomUtils {

    @Nullable
    public static Room getRoomFromId(int roomId) {
        for (Room currentRoom : roomsList) {
            if (currentRoom.getRoomId() == roomId)
                return currentRoom;
        }
        return null;
    }

    public static class RoomDestroyRunnable implements Runnable {

        @Override
        public void run() {
            ArrayList<Room> deletionList = new ArrayList<>();

            for (Room currentRoom : roomsList) if (currentRoom.getCount() == 0) deletionList.add(currentRoom);
            for (Room deletionRoom : deletionList) roomsList.remove(deletionRoom);
        }
    }

    public static boolean colorAlreadyTaken(int roomId, String color) {
        for (Room room : roomsList)
            if (room.getRoomId() == roomId)
                for (Player player : room.getPlayersList())
                    if (player.getColor().equals(color))
                        return true;

        return false;
    }

    public static boolean roomNameAlreadyTaken(String name) {
        for (Room room : roomsList)
            if (room.getName().equals(name))
                return true;

        return false;
    }

    public static int getValueFromEnum(TIME_LIMIT timeLimit) {
        switch (timeLimit) {
            case SECONDS_3: return 0;
            case SECONDS_10: return 1;
            default: return 2;
        }
    }

    public static TIME_LIMIT getEnumFromValue(int value) {
        switch (value) {
            case 0 : return TIME_LIMIT.SECONDS_3;
            case 1 : return TIME_LIMIT.SECONDS_10;
            default: return TIME_LIMIT.MINUTE_1;
        }
    }
}
