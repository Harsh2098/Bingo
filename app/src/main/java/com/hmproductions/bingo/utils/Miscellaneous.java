package com.hmproductions.bingo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.hmproductions.bingo.data.GridCell;
import com.hmproductions.bingo.data.Player;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import static com.hmproductions.bingo.utils.Constants.MAX_SERVER_CAPACITY;

public class Miscellaneous {

    public interface OnFragmentChangeRequest {
        void changeFragment(@Nullable String roomName, int timeLimit, @Nullable View view);
        void finishCurrentActivity();
    }

    public interface OnSnackBarRequest {
        void showSnackBar(String message, int duration);
    }

    // For java classes, otherwise kotlin has view extensions for hiding keyboard
    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);

        if (imm != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }

        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

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
                (int)(System.currentTimeMillis() % 100) * 7) % MAX_SERVER_CAPACITY;
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
            if (gridCell.getValue() == value && gridCell.isClicked())
                return true;
        }
        return false;
    }

    public static float convertDpToPixel(Context context, float dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static String generateColor() {
        String randomStringGenerationString = "0123456789abcdef";
        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i < 6; i++) {
            stringBuilder.append(randomStringGenerationString.charAt(new Random().nextInt(randomStringGenerationString.length())));
        }
        return stringBuilder.toString();
    }
    
    public static String getTimeLimitString(TimeLimitUtils.TIME_LIMIT timeLimit) {
        switch (timeLimit) {
            case SECONDS_3:
                return "3 sec";

            case SECONDS_10:
                return "10 sec";

            case MINUTE_1:
                return "1 min";

            default:
                return "";
        }
    }

    public static String shortenName(String name, int limit) {

        if(name.length() > limit) {
            StringBuilder builder = new StringBuilder(name);
            builder.delete(limit-2, name.length());
            builder.append("...");
            name = builder.toString();
        }

        return name;
    }

    public static boolean isTablet(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float yInches= metrics.heightPixels/metrics.ydpi;
        float xInches= metrics.widthPixels/metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches*xInches + yInches*yInches);
        return diagonalInches >= 6.5;
    }
}
