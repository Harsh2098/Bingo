package com.hmproductions.bingo.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Player implements Parcelable {

    private String name, color;
    private int id;
    private boolean isReady;

    public Player(String name, String color, int id, boolean isReady) {
        this.name = name;
        this.color = color;
        this.id = id;
        this.isReady = isReady;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getId() {
        return id;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Player(Parcel in) {
        this.name = in.readString();
        this.color = in.readString();
        this.id = in.readInt();
        this.isReady = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(color);
        parcel.writeInt(id);
        parcel.writeByte((byte) (isReady? 1 : 0));
    }

    public static final Creator<Player> CREATOR = new Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel in) {
            return new Player(in);
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };
}
