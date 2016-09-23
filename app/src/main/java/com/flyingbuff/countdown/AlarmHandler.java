package com.flyingbuff.countdown;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Aayush on 8/15/2016.
 */
public class AlarmHandler {
    private static final int TIMER_VALIDATION_STARTED = 100232;
    private static Handler silentAlarmMangaer;
    private static AlarmManager noisyAlarmManager;
    private static Context context;

    public static void init(Context context) {
        silentAlarmMangaer = new Handler();
        noisyAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        AlarmHandler.context = context;
    }

    public static void validate(TimerBase timer) {
        validate((Timer) timer);
    }

    public static void validate(Timer timer) {
        validate(timer.isNotify());
    }

    public static void validate(Boolean isNoisy) {
        if (isNoisy) validateNoisyTimers();
        else validateSilentTimers();
    }

    private static void validateSilentTimers() {
        DatabaseHelper db = new DatabaseHelper(context);

        final ArrayList<Timer> runningTimers = db.loadSilentTimer();

        if (runningTimers.isEmpty()) {
            silentAlarmMangaer.removeCallbacksAndMessages(null);
            Log.i("TimerBaseValidator", "0 running silent timers, cancelling alarm");
            return;
        }

        final Timer minTimer = Collections.min(runningTimers, Timer.REMAINING_TIME_COMPARATOR);

        long smallestTimer = minTimer.getRemainingTime();

        Log.i("TimerBaseValidator", "Removing last silent alarm");
        silentAlarmMangaer.removeCallbacksAndMessages(null);

        silentAlarmMangaer.sendEmptyMessage(TIMER_VALIDATION_STARTED);
        silentAlarmMangaer.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent silentTimerIntent = new Intent(context, AlarmReceiver.class);
                silentTimerIntent.setAction(AlarmReceiver.ACTION_SILENT_ALARM);
                silentTimerIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);

                silentTimerIntent.putExtra(Countdown.KEY_TIMER_TAG, minTimer.getId());
                Log.i("TimerBaseValidator", "Silent alarm triggered");

                context.sendBroadcast(silentTimerIntent);
            }
        }, smallestTimer);

        String logMessage = "Message set for " + DateTime.now().plus(smallestTimer).toString("hh:mm:ss a");
        Log.i("TimerBaseValidator", logMessage);
//        if (BuildConfig.DEBUG) Toast.makeText(context, logMessage, Toast.LENGTH_SHORT).show();
    }

    public static void validateNoisyTimers() {
        DatabaseHelper db = new DatabaseHelper(context);

        ArrayList<Timer> runningTimers = db.loadNoisyTimers();

        Intent alarmTimerIntent = new Intent(context, AlarmReceiver.class);
        alarmTimerIntent.setAction(AlarmReceiver.ACTION_NOISY_ALARM);
        alarmTimerIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);

        if (runningTimers.isEmpty()) {
            PendingIntent alarmIntent = PendingIntent.getBroadcast(
                    context,
                    Countdown.ALARM_REQUEST_CODE,
                    alarmTimerIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            noisyAlarmManager.cancel(alarmIntent);
            alarmIntent.cancel();
            Log.i("TimerBaseValidator", "0 running noisy timers, cancelling alarm");
            return;
        }

        Timer minTimer = Collections.min(runningTimers, Timer.REMAINING_TIME_COMPARATOR);

        long smallestTimer = minTimer.getRemainingTime();

        alarmTimerIntent.putExtra(Countdown.KEY_TIMER_TAG, minTimer.getId());

        long alarmTime = System.currentTimeMillis() + smallestTimer;

        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                context,
                Countdown.ALARM_REQUEST_CODE,
                alarmTimerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        noisyAlarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, alarmIntent);
        String logMessage = "Alarm set for " + new DateTime(alarmTime).toString("hh:mm:ss a");
        Log.i("TimerBaseValidator", logMessage);
//        if (BuildConfig.DEBUG) Toast.makeText(context, logMessage, Toast.LENGTH_SHORT).show();
    }

    public static void validate() {
        validateNoisyTimers();
        validateSilentTimers();
    }

    public static void cancelAllSilentAlarms() {
        silentAlarmMangaer.removeCallbacksAndMessages(null);
    }
}
