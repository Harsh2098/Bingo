package com.hmproductions.bingo.sync;

import com.hmproductions.bingo.data.Room;

import javax.annotation.Nullable;

import static com.hmproductions.bingo.sync.BingoActionServiceImpl.roomsList;

public class RoomUtils {

    @Nullable
    static Room getRoomFromId(int roomId) {
        for (Room currentRoom : roomsList) {
            if (currentRoom.getRoomId() == roomId)
                return currentRoom;
        }
        return null;
    }
}
