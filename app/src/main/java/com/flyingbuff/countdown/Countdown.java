package com.flyingbuff.countdown;

import android.app.Application;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by Aayush on 8/7/2016.
 */
public class Countdown extends Application {
    public static final String PACKAGE_NAME = "com.flyingbuff.countdown";

    public static final String TABLE_TIMER = "timer";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_INIT = "init";
    public static final String COLUMN_END = "end";
    public static final String COLUMN_RESUMED_AT = "resumed_at";
    public static final String COLUMN_STOPPED_AT = "stoppped at";
    public static final String COLUMN_GOAL = "goal";
    public static final String COLUMN_SINGLE_USE = "one_time";

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);
    }
}
