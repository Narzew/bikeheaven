package org.narzew.bikeheaven;

import android.content.Context;

public class Config {

    protected Context context;
    public static String LOG_KEY = "BikeHeaven";
    public static String PREFS_NAME = "BikeHeaven";
    public static String SERVER_PATH = "http://narzew.org/bikeheaven/api";
    public static final int PASSWORD_MIN_CHARS = 5;
    public static final int PASSWORD_MAX_CHARS = 30;
    public static final int LOGIN_MIN_CHARS = 5;
    public static final int LOGIN_MAX_CHARS = 30;
    public static final int EMAIL_MIN_CHARS = 4;
    public static final int EMAIL_MAX_CHARS = 60;
    public static final int PHONE_NUMBRE_MIN = 9;
    public static final int PHONE_NUMBRE_MAX = 12;
    public static final int FNAME_MIN = 2;
    public static final int FNAME_MAX = 40;
    public static final int SURNAME_MIN = 3;
    public static final int SURNAME_MAX = 40;
    public static final int CITY_MIN = 2;
    public static final int CITY_MAX = 40;
    public static final int DESCRIPTION_MIN_CHARS = 0;
    public static final int DESCRIPTION_MAX_CHARS = 50;
    public static final String DB_NAME = "bikeheaven.db";
    public static final int DB_VERSION = 1;


    public Config(Context context) {
        this.context = context;
    }
}
