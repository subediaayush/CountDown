package com.flyingbuff.countdown;

import android.content.Context;
import android.util.Log;

/**
 * Created by Aayush on 8/8/2016.
 */
public class TimerBase {
    static Context context;
    static DatabaseHelper db;

    public static void init(Context context) {
        TimerBase.context = context;
        TimerBase.db = new DatabaseHelper(context);
    }

    protected void saveTimer() {
    }

    protected void startTimer() {
        AlarmHandler.validate(this);
        Log.i("TimerBase", "Timer Started");
    }

    protected void pauseTimer() {
        AlarmHandler.validate(this);
        Log.i("TimerBase", "Timer Paused");
    }

    protected void resumeTimer() {
        AlarmHandler.validate(this);
        Log.i("TimerBase", "Timer Resumed");
    }

    protected void resetTimer() {
        AlarmHandler.validate(this);
        Log.i("TimerBase", "Timer Reset");
    }

    protected void stopTimer() {
        AlarmHandler.validate(this);
        Log.i("TimerBase", "Timer Stopped");
    }

    protected static DatabaseHelper getDatabaseHelper() {
        return db;
    }


}
