package com.hmproductions.bingo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.hmproductions.bingo.data.GridCell;
import com.hmproductions.bingo.data.Player;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Random;

public class Miscellaneous {

    // Returns an int[size][size] containing numbers 1 to 25 randomly placed
    public static int[] CreateRandomGameArray(int size) {

        int[] randomArray = new int[size * size];
        int randomNumber1, randomNumber2, temp;

        for (int i = 0; i < size * size; ++i)
            randomArray[i] = i + 1;

        // Swapping two random elements of array 100 times
        for (int i = 0; i < 10; ++i)
            for (int j = 0; j < 10; ++j) {

                randomNumber1 = (new Random().nextInt(size * size));
                randomNumber2 = (new Random().nextInt(size * size));

                temp = randomArray[randomNumber1];
                randomArray[randomNumber1] = randomArray[randomNumber2];
                randomArray[randomNumber2] = temp;
            }

        return randomArray;
    }

    public static int nameToIdHash(String name) {
        return ((int)name.charAt(0) * (int)name.charAt(name.length()/2) * (int)name.charAt(name.length() - 1) *
                (int)(System.currentTimeMillis() % 100)) % 101;
    }

    public static String getNameFromId(ArrayList<Player> playerArrayList, int id) {
        for (Player player : playerArrayList) {
            if (player.getId() == id)
                return player.getName();
        }

        return null;
    }

    public static String getColorFromId(ArrayList<Player> playerArrayList, int id) {
        for (Player player : playerArrayList) {
            if (player.getId() == id)
                return player.getColor();
        }

        return null;
    }

    public static String getColorFromNextPlayerId(ArrayList<Player> playerArrayList, int nextPlayerId) {

        int nextPlayerIndex = 0;

        for (Player player : playerArrayList) {
            if (player.getId() == nextPlayerId) {
                nextPlayerIndex = playerArrayList.indexOf(player);
                break;
            }
        }

        if (nextPlayerIndex == 0)
            nextPlayerIndex = playerArrayList.size() - 1;
        else
            nextPlayerIndex--;

        return playerArrayList.get(nextPlayerIndex).getColor();
    }

    public static boolean valueClicked(ArrayList<GridCell> gridCellArrayList, int value) {
        for (GridCell gridCell : gridCellArrayList) {
            if (gridCell.getValue() == value && gridCell.getIsClicked())
                return true;
        }
        return false;
    }
}
