package com.hmproductions.bingo.utils;

import com.hmproductions.bingo.data.ConnectionData;
import com.hmproductions.bingo.data.Room;
import com.hmproductions.bingo.models.Player;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

public class MiscellaneousUtils {

    private static final String randomStringGenerationString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789=";

    public static ArrayList<Player> getArrayListFromPlayersList(ArrayList<com.hmproductions.bingo.data.Player> playerArrayList) {

        ArrayList<Player> convertedList = new ArrayList<>();

        for (com.hmproductions.bingo.data.Player player : playerArrayList) {
            convertedList.add(Player.newBuilder().setName(player.getName()).setColor(player.getColor())
                    .setReady(player.isReady()).setId(player.getId()).build());
        }

        return convertedList;
    }

    public static boolean allPlayersReady(ArrayList<com.hmproductions.bingo.data.Player> playerArrayList) {
        for (com.hmproductions.bingo.data.Player player : playerArrayList)
            if (!player.isReady())
                return false;
        return true;
    }

    public static String generateSessionId(int length) {
        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i < length; i++) {
            stringBuilder.append(randomStringGenerationString.charAt(new Random().nextInt(randomStringGenerationString.length())));
        }

        return stringBuilder.toString();
    }

    public static int generateRoomId(String name) {
        return ((int)name.charAt(0) * (int)name.charAt(name.length()/2) * (int)name.charAt(name.length() - 1) *
                (int)(System.currentTimeMillis() % 100) * 97) % 101;
    }

    public static void removeConnectionData(ArrayList<ConnectionData> data, int playerId) {

        ConnectionData removalData = null;

        for (ConnectionData currentData : data) {
            if (currentData.getPlayerId() == playerId) {
                removalData = currentData;
            }
        }

        if (removalData != null) {
            data.remove(removalData);
        }
    }

    public static int getPlayerIdFromRemoteAddress(ArrayList<ConnectionData> data, String remoteAddress) {

        for (ConnectionData currentData : data) {
            if (currentData.getRemoteAddress().equals(remoteAddress))
                return currentData.getPlayerId();
        }
        return -1;
    }

    public static int getRoomIdFromRemoteAddress(ArrayList<ConnectionData> data, String remoteAddress) {

        for (ConnectionData currentData : data) {
            if (currentData.getRemoteAddress().equals(remoteAddress))
                return currentData.getRoomId();
        }
        return -1;
    }

    @Nullable
    public static String getNameFromRoomId(ArrayList<Room> roomsList, int roomId, int playerId) {
        for (Room room : roomsList) {
            if (room.getRoomId() == roomId) {
                for (com.hmproductions.bingo.data.Player player : room.getPlayersList()) {
                    if (player.getId() == playerId) {
                        return player.getName();
                    }
                }
            }
        }

        return null;
    }
}
