package com.hmproductions.bingo.utils;

public class Constants {

    public static final int SERVER_PORT = 8080;

    public static final String SESSION_ID_KEY = "sessionid";
    public static final String PLAYER_ID_KEY = "playerid";
    public static final String ROOM_ID_KEY = "roomid";

    public static final int SESSION_ID_LENGTH = 23;

    public static final String GET_SESSION_ID_METHOD = "com.hmproductions.bingo.BingoActionService/GetSessionId";
    public static final String RECONNECT_METHOD = "com.hmproductions.bingo.BingoActionService/Reconnect";
    public static final String ADD_PLAYER_METHOD = "com.hmproductions.bingo.BingoActionService/AddPlayer";
    public static final String HOST_ROOM_METHOD = "com.hmproductions.bingo.BingoActionService/HostRoom";
    public static final String GET_ROOMS_METHOD = "com.hmproductions.bingo.BingoActionService/GetRooms";
    public static final String REMOVE_PLAYER_METHOD = "com.hmproductions.bingo.BingoActionService/RemovePlayer";
    public static final String QUIT_PLAYER_METHOD = "com.hmproductions.bingo.BingoActionService/QuitPlayer";

    public static final String ROOM_STREAMING_METHOD = "com.hmproductions.bingo.BingoStreamService/GetRoomEventUpdates";
    public static final String GAME_STREAMING_METHOD = "com.hmproductions.bingo.BingoStreamService/GetGameEventUpdates";

    // TODO: Do negative values actually matter
    public static final int NO_WINNER_ID_CODE = -1;
    public static final int PLAYER_QUIT_CODE = -2;
    public static final int NEXT_ROUND_CODE = -3;
    public static final int SKIPPED_TURN_CODE = 123;
    public static final int CLEAR_GAME_SUBSCRIPTION = 456;
    public static final int CLEAR_ROOM_SUBSCRIPTION = 476;

}
