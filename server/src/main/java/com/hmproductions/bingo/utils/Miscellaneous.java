package com.hmproductions.bingo.utils;

import com.hmproductions.bingo.models.Player;

import java.util.ArrayList;
import java.util.Random;

import static com.hmproductions.bingo.services.BingoActionServiceImpl.playersList;

public class Miscellaneous {

    private static final String randomStringGenerationString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789=";

    public static boolean playerExists(int playerId) {
        for (com.hmproductions.bingo.data.Player currentPlayer : playersList)
            if (currentPlayer.getId() == playerId) return true;
        return false;
    }

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
}
