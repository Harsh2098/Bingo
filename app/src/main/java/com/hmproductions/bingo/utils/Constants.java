package com.hmproductions.bingo.utils;

public class Constants {

    public static final String CLASSIC_TAG = ":::";

    public static final int GRID_SIZE = 5;
    public static final double SCALING_FACTOR = 0.95;
    public static final int LEADERBOARD_COL_SPAN = 2;

    public static final int SERVER_PORT = 8080;
    public static final String SERVER_ADDRESS = "192.168.0.160";

    public static final int ADD_PLAYER_LOADER_ID = 101;
    public static final int REMOVE_PLAYER_LOADER_ID = 201;
    public static final int READY_PLAYER_LOADER_ID = 301;
    public static final int UNSUBSCRIBE_LOADER_ID = 401;
    public static final int CLICK_CELL_LOADER_ID = 501;
    public static final int BROADCAST_WINNER_LOADER_ID = 601;
    public static final int QUIT_PLAYER_LOADER_ID = 701;
    public static final int NEXT_ROUND_LOADER_ID = 801;
    public static final int INTERNET_CONNECTION_LOADER_ID = 901;

    public static final String GRID_CELL_CLICK_ACTION = "grid-cell-click-action";
    public static final String QUIT_GAME_ACTION = "quit-game-action";

    public static final String SAMPLE_SESSION_ID = "bingo4lyfxD1020";

    public static final String SERVER_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIFBTCCA+2gAwIBAgISA+OThMLh+yVPNP5jen9UpZLLMA0GCSqGSIb3DQEBCwUA\n" +
            "MEoxCzAJBgNVBAYTAlVTMRYwFAYDVQQKEw1MZXQncyBFbmNyeXB0MSMwIQYDVQQD\n" +
            "ExpMZXQncyBFbmNyeXB0IEF1dGhvcml0eSBYMzAeFw0xODAyMTgxNDQ3MDRaFw0x\n" +
            "ODA1MTkxNDQ3MDRaMBwxGjAYBgNVBAMTEWRhbGFsLnByYWd5YW4ub3JnMIIBIjAN\n" +
            "BgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvGT5r53rUVLDgTRitxcf76Y+3p0O\n" +
            "AVVDR+EZdSwlGNFyaZeBc3cwPUodevPImvhfCDvL5LoJAwIwpIQ/Ufqdcb6I2Duh\n" +
            "27QcxDXAId3PQPSrV6ubAAxeLUNRoqvGoxJ5hebUSuVmwfIvx4seO/f1VqD8gqiR\n" +
            "cZFkOag+sRRYGqwxWlaTcTQ6r1RZmW5FxSfvwUHOLYWzcc1pdP1XBWZ4n0KEdgCa\n" +
            "N8ht7XDp3xRlZp+Oo+Avu6TnyJsOGob5YuxbzZC3S1sHDvtGY6czmaxOCCT1IRC9\n" +
            "8q16RhC9aYL7iouEH2XOK/QRYXS5oKXQoQmaAlKIkYpUE3Cor5GrJwN9UwIDAQAB\n" +
            "o4ICETCCAg0wDgYDVR0PAQH/BAQDAgWgMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggr\n" +
            "BgEFBQcDAjAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBTLe29ihoYDX3nePTMkeKR+\n" +
            "8Y7axjAfBgNVHSMEGDAWgBSoSmpjBH3duubRObemRWXv86jsoTBvBggrBgEFBQcB\n" +
            "AQRjMGEwLgYIKwYBBQUHMAGGImh0dHA6Ly9vY3NwLmludC14My5sZXRzZW5jcnlw\n" +
            "dC5vcmcwLwYIKwYBBQUHMAKGI2h0dHA6Ly9jZXJ0LmludC14My5sZXRzZW5jcnlw\n" +
            "dC5vcmcvMBwGA1UdEQQVMBOCEWRhbGFsLnByYWd5YW4ub3JnMIH+BgNVHSAEgfYw\n" +
            "gfMwCAYGZ4EMAQIBMIHmBgsrBgEEAYLfEwEBATCB1jAmBggrBgEFBQcCARYaaHR0\n" +
            "cDovL2Nwcy5sZXRzZW5jcnlwdC5vcmcwgasGCCsGAQUFBwICMIGeDIGbVGhpcyBD\n" +
            "ZXJ0aWZpY2F0ZSBtYXkgb25seSBiZSByZWxpZWQgdXBvbiBieSBSZWx5aW5nIFBh\n" +
            "cnRpZXMgYW5kIG9ubHkgaW4gYWNjb3JkYW5jZSB3aXRoIHRoZSBDZXJ0aWZpY2F0\n" +
            "ZSBQb2xpY3kgZm91bmQgYXQgaHR0cHM6Ly9sZXRzZW5jcnlwdC5vcmcvcmVwb3Np\n" +
            "dG9yeS8wDQYJKoZIhvcNAQELBQADggEBACepTdQrvrR+BKRZHhkhExrtbYELPgeF\n" +
            "X5TUz3yKeUCmf1CJfgyCuRBAPrrDU1LKlImRbUGnAxt+IHk3GgmdbbImyL/8x22j\n" +
            "DYHdsy/3x4wkAMMDeOiDI84KWKLNGXYCUx9XMT394djNpAtynhuINFgTKpi1U1aw\n" +
            "JxHjr5qGna4LLoL2OyUtLZnLPfrhBdbe1EfQTQ5TZgr5g6rgtGIX2o/g3ORLYUGM\n" +
            "EYrt2uzl6wJHiNDrIyEmr1YepVDyNiYaSt0S6T72qd68eBeDZ5q6rI1bQcsrp1/N\n" +
            "4yxybWqAkiH/0e4JsG3sXIABMClz0rcoS4hq6wdin8K45mxnFBZaK1Q=\n" +
            "-----END CERTIFICATE-----";
}
