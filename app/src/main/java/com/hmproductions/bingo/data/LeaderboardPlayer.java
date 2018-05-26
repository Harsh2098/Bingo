package com.hmproductions.bingo.data;

import android.os.Parcel;
import android.os.Parcelable;

public class LeaderboardPlayer implements Parcelable {

    private String name, color;
    private int winCount;

    public LeaderboardPlayer(String name, String color, int winCount) {
        this.name = name;
        this.color = color;
        this.winCount = winCount;
    }

    private LeaderboardPlayer(Parcel in) {
        this.name = in.readString();
        this.color = in.readString();
        this.winCount = in.readInt();
    }

    public String getName() {
        return name;
    }

    public int getWinCount() {
        return winCount;
    }

    public String getColor() {
        return color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(color);
        parcel.writeInt(winCount);
    }

    public static final Creator<LeaderboardPlayer> CREATOR = new Creator<LeaderboardPlayer>() {
        @Override
        public LeaderboardPlayer createFromParcel(Parcel in) {
            return new LeaderboardPlayer(in);
        }

        @Override
        public LeaderboardPlayer[] newArray(int size) {
            return new LeaderboardPlayer[size];
        }
    };
}
