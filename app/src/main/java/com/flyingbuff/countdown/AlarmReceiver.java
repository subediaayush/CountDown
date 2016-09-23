package com.flyingbuff.countdown;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

/**
 * Created by Aayush on 8/15/2016.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_NOISY_ALARM = "com.flyingbuff.countdown.ACTION_NOISY_ALARM";
    public static final String ACTION_SILENT_ALARM = "com.flyingbuff.countdown.ACTION_SILENT_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i("AlarmReceiver", "received action " + action);

        if (action.equals(ACTION_NOISY_ALARM)) handleNoisyAlarm(context, intent);

        if (action.equals(ACTION_SILENT_ALARM)) handleSilentAlarm(context, intent);

    }

    private void handleSilentAlarm(Context context, Intent intent) {
        DatabaseHelper db = new DatabaseHelper(context);
        Log.i("AlarmReceiver", "Timeout silent alarm received");

        int timerId = intent.getIntExtra(Countdown.KEY_TIMER_TAG, -1);
        if (timerId == -1) return;

        Timer timer = db.loadTimer(timerId);

        if (timer == null) {
            AlarmHandler.validate();
            return;
        }

        timer.stopTimer();
        if (timer.isRepeat()) timer.startTimer();

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        AlarmHandler.validate(timer);
    }

    private void handleNoisyAlarm(Context context, Intent intent) {
        DatabaseHelper db = new DatabaseHelper(context);
        Log.i("AlarmReceiver", "Timeout noisy alarm received");

        int timerId = intent.getIntExtra(Countdown.KEY_TIMER_TAG, -1);
        if (timerId == -1) return;

        Timer timer = db.loadTimer(timerId);
        if (timer == null) {
            AlarmHandler.validate();
            return;
        }

        Uri tone = db.retrieveTone(timerId);

        timer.stopTimer();
        if (timer.isRepeat()) timer.startTimer();

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        AlarmHandler.validate(timer);

        Bundle args = new Bundle();
        args.putParcelable(Countdown.KEY_TIMER_TONE, tone);

        Intent notificationIntent = new Intent(context, AlarmNotificationHandler.class);
        notificationIntent.putExtras(args);

        PendingIntent p = PendingIntent.getBroadcast(
                context,
                (int) System.currentTimeMillis(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        Notification n = new NotificationCompat.Builder(context)
                .setContentTitle("You have new message")
                .setContentText("Timed out")
                .setSmallIcon(R.drawable.ic_notify)
                .setContentIntent(p)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(tone)
                .build();

        n.defaults |= Notification.DEFAULT_VIBRATE;
        n.flags |= Notification.FLAG_INSISTENT;
        n.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(timerId, n);
    }
}