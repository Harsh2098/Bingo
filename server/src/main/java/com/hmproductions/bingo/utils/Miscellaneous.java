package com.hmproductions.bingo.utils;

import com.hmproductions.bingo.models.Player;

import java.util.ArrayList;

import static com.hmproductions.bingo.services.BingoActionServiceImpl.playersList;

public class Miscellaneous {

    public static boolean playerIsNew(int playerId) {

        boolean exists = false;

        for (com.hmproductions.bingo.data.Player currentPlayer : playersList) {
            if (currentPlayer.getId() == playerId) {
                exists = true;
                break;
            }
        }

        return !exists;
    }

    public static ArrayList<Player> getArrayListFromPlayersList(ArrayList<com.hmproductions.bingo.data.Player> playerArrayList) {

        ArrayList<Player> convertedList = new ArrayList<>();

        for (com.hmproductions.bingo.data.Player player : playerArrayList) {
            convertedList.add(Player.newBuilder().setName(player.getName()).setColor(player.getColor())
                    .setReady(player.isReady()).setId(player.getId()).build());
        }

        return convertedList;
    }
}
