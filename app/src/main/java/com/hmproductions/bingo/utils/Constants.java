package com.hmproductions.bingo.utils;

public class Constants {

    public static final String CLASSIC_TAG = ":::";

    public static final int GRID_SIZE = 5;
    public static final float GRID_SCALING_FACTOR = 0.95f;
    public static final float CELL_SCALING_FACTOR = 0.92f;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 500;

    public static final int LEADERBOARD_COL_SPAN = 2;
    public static final int LEADERBOARD_TAB_COL_SPAN = 4;
    public static final int TURN_SKIPPED_CODE = 123;

    // First time keys
    public static final String FIRST_TIME_OPENED_KEY = "first-time-key";
    public static final String FIRST_TIME_PLAYED_KEY = "first-time-played-key";
    public static final String FIRST_TIME_JOINED_KEY = "first-time-joined";
    public static final String FIRST_TIME_WON_KEY = "first-time-won-key";

    // TODO (Release) : Change min players
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 4;
    static final int MAX_SERVER_CAPACITY = 10001;

    public static final int SERVER_PORT = 8080;
    public static final String SERVER_ADDRESS = "35.187.246.194"; // Compute Engine - 35.187.246.194 Last known home public IP - 116.88.119.133

    public static final int ADD_PLAYER_LOADER_ID = 101;
    public static final int REMOVE_PLAYER_LOADER_ID = 201;
    public static final int READY_PLAYER_LOADER_ID = 301;
    public static final int UNSUBSCRIBE_LOADER_ID = 401;
    public static final int GET_ROOMS_LOADER_ID = 701;
    public static final int HOST_ROOM_LOADER_ID = 801;

    public static final String GRID_CELL_CLICK_ACTION = "grid-cell-click-action";
    public static final String QUIT_GAME_ACTION = "quit-game-action";
    public static final String RECONNECT_ACTION = "reconnect-action";

    public static String SESSION_ID = null;
    public static int READ_COUNT = 0;
    public static final String SESSION_ID_KEY = "sessionid";
    public static final String PLAYER_ID_KEY = "playerid";
    public static final String ROOM_ID_KEY = "roomid";

    public static final String SERVER_CERT = "" +
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIEDzCCAvegAwIBAgIJALz3nK5gc6keMA0GCSqGSIb3DQEBCwUAMIGdMQswCQYD\n" +
            "VQQGEwJTRzESMBAGA1UECAwJU2luZ2Fwb3JlMRIwEAYDVQQHDAlTaW5nYXBvcmUx\n" +
            "FjAUBgNVBAoMDWhhcnNoIG1haGFqYW4xFDASBgNVBAsMC2RldmVsb3BtZW50MQ4w\n" +
            "DAYDVQQDDAVoYXJzaDEoMCYGCSqGSIb3DQEJARYZaGFyc2htYWhhamFuOTI3QHlh\n" +
            "aG9vLmNvbTAeFw0xODA2MDIwMzMyMjZaFw0yODA1MzAwMzMyMjZaMIGdMQswCQYD\n" +
            "VQQGEwJTRzESMBAGA1UECAwJU2luZ2Fwb3JlMRIwEAYDVQQHDAlTaW5nYXBvcmUx\n" +
            "FjAUBgNVBAoMDWhhcnNoIG1haGFqYW4xFDASBgNVBAsMC2RldmVsb3BtZW50MQ4w\n" +
            "DAYDVQQDDAVoYXJzaDEoMCYGCSqGSIb3DQEJARYZaGFyc2htYWhhamFuOTI3QHlh\n" +
            "aG9vLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANZT8fGK0KaM\n" +
            "c4tbGU+RkpxsCoBUqQVvAGWMy9iSya6hhGzUkeiFN+KNpx+ygUJyEDdq8TVEJqhl\n" +
            "7TfZ+xfYmBNomwXI4NayzjMZuQdv0nY9RAHtXvDPWiQO3nwwbq58dikN8JsYpRrI\n" +
            "d+r4tyRDnMgFR4NatbkLKpqDCbe6CUzQHkcZ2Gmdj0x0146zW37A8rbYLI1YG4+7\n" +
            "d9HmnNDq9K5MZ8aM6ylW9J//UOCouBb3LeP9wIRqOxXGX1Fl/wWK4tWE2y9yke5X\n" +
            "jYO7Kq70A2YyjBqTO9g/21Ksjq6SYjFKe5765L2Zyj1fHlt+cKQGYh5ZhL8Aywel\n" +
            "UZ1YGqYaSCECAwEAAaNQME4wHQYDVR0OBBYEFKSUH4H96EmjJPkJi4kzjcVbDI8m\n" +
            "MB8GA1UdIwQYMBaAFKSUH4H96EmjJPkJi4kzjcVbDI8mMAwGA1UdEwQFMAMBAf8w\n" +
            "DQYJKoZIhvcNAQELBQADggEBAAFLHy8NJab+5LZ/+uvaLwxxiVVYph8Nsg9DGgex\n" +
            "das5tM8LPIgJjMTVJ2xRE368VWrTePncYXZA+hej/Rgci7kGL0o8ux3wVe2I+HM2\n" +
            "GKHQJAYrI6XiQJcVeY77BxZzrS25vwbqL3Q8+JzalMKI0Nz1fEcMcW5CqtGYEyVx\n" +
            "GmbkDaO3qgyUkhP7aAWsS2V3t80p9DCdt9iXBDIUzCCi3w08Dxlfikd5SqLD8lD/\n" +
            "SKBxr7VvdmLnsWpD4875UHxX8L7WzE/BjDM6G4BqPn8g4Hxw4JVrnPS0QjfAW4cb\n" +
            "YtenYpwKn55X3UVP/uylizBWYGv9lEaY0ARFp5ECRGObDxI=\n" +
            "-----END CERTIFICATE-----\n";
}
