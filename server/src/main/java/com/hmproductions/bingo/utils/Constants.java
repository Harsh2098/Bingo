package com.hmproductions.bingo.utils;

public class Constants {

    public static final int SERVER_PORT = 8080;

    static final String SESSION_ID_KEY = "sessionid";
    static final String PLAYER_ID_KEY = "playerid";
    static final String ROOM_ID_KEY = "roomid";

    public static final int SESSION_ID_LENGTH = 23;
    public static final int ROOM_ID_LENGTH = 17;

    static final String ADD_PLAYER_METHOD_NAME = "com.hmproductions.bingo.BingoActionService/AddPlayer";
    static final String HOST_ROOM_METHOD_NAME = "com.hmproductions.bingo.BingoActionService/HostRoom";
    static final String GET_ROOMS_METHOD_NAME = "com.hmproductions.bingo.BingoActionService/GetRooms";
}
