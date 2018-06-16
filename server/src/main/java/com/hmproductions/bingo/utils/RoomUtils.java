package com.hmproductions.bingo.utils;

import com.hmproductions.bingo.data.Player;
import com.hmproductions.bingo.data.Room;

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

    static class RoomDestroyRunnable implements Runnable {

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
}
