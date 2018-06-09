package com.hmproductions.bingo.utils;

import com.hmproductions.bingo.data.Room;

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
}
