package com.flyingbuff.countdown;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import java.io.IOException;

/**
 * Created by Aayush on 8/17/2016.
 */
public class AlarmPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    public static final String ACTION_START_PLAYING = "com.flyingbuff.countdown.ACTION_START_PLAYING";
    public static final String ACTION_STOP_PLAYING = "com.flyingbuff.countdown.ACTION_STOP_PLAYING";

    private static final String ACTION_PLAY = "PLAY";
    private static String mUrl;
    private static AlarmPlayerService mInstance = null;

    private MediaPlayer mMediaPlayer = null;    // The Media Player
    private int mBufferPosition;
    private static String mSongTitle;
    private static String mSongPicUrl;

    NotificationManager mNotificationManager;
    Notification mNotification = null;
    final int NOTIFICATION_ID = 1;


    // indicates the state our service:
    enum State {
        Retrieving, // the MediaRetriever is retrieving music
        Stopped, // media player is stopped and not prepared to play
        Preparing, // media player is preparing...
        Playing, // playback active (media player ready!). (but the media player may actually be
        // paused in this state if we don't have audio focus. But we stay in this state
        // so that we know we have to resume playback once we get focus back)
        Paused
        // playback paused (media player ready!)
    }

    @Override
    public void onCreate() {
        mInstance = this;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent) return -1;

        String action = intent.getAction();

        if (ACTION_START_PLAYING.equals(action)) {
            Bundle data = intent.getExtras();

            Uri uri = data.getParcelable(Countdown.KEY_TIMER_TONE);
            if (uri == null) uri = Settings.System.DEFAULT_ALARM_ALERT_URI;

            mMediaPlayer = getMediaPlayer(getApplicationContext(), uri);
            mMediaPlayer.prepareAsync();

        } else if (ACTION_STOP_PLAYING.equals(action)) {
            stopPlaying();
        }

        return START_STICKY;
    }

    private void initMediaPlayer(Uri uri) {
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), uri);
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            // ...
        }

        try {
            mMediaPlayer.prepareAsync(); // prepare async to not block main thread
        } catch (IllegalStateException e) {
            // ...
        }
    }

    /**
     * Called when MediaPlayer is ready
     */
    @Override
    public void onPrepared(MediaPlayer player) {
        // Begin playing music
        startPlaying();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            stopPlaying();

            mMediaPlayer.release();
        }
    }

    private MediaPlayer getMediaPlayer(Context context, Uri tone) {
        if (mMediaPlayer != null) stopPlaying();

        mMediaPlayer = new MediaPlayer();


        if (tone == null) tone = Settings.System.DEFAULT_ALARM_ALERT_URI;
        try {
            mMediaPlayer.setDataSource(context, tone);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);

            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mMediaPlayer;
    }

    public void startPlaying() {
        Context context = getApplicationContext();

        final AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);

        int mediaVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        context.getSharedPreferences(Countdown.PACKAGE_NAME, MODE_PRIVATE).edit()
                .putInt(Countdown.KEY_MUSIC_VOLUME, mediaVol)
                .apply();

        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                Math.min(mediaVol, maxVol / 4),
                AudioManager.ADJUST_LOWER
        );

        mMediaPlayer.start();
    }

    public void stopPlaying() {
        if (!isPlaying()) return;

        Context context = getApplicationContext();

        final AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);

        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int mediaVol = context.getSharedPreferences(Countdown.PACKAGE_NAME, MODE_PRIVATE)
                .getInt(Countdown.KEY_MUSIC_VOLUME, maxVol);

        audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                Math.max(mediaVol, maxVol / 4),
                AudioManager.ADJUST_RAISE
        );

        mMediaPlayer.stop();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }


    public static AlarmPlayerService getInstance() {
        return mInstance;
    }

//    /**
//     * Configures service as a foreground service. A foreground service is a service that's doing something the user is
//     * actively aware of (such as playing music), and must appear to the user as a notification. That's why we create
//     * the notification here.
//     */
//    void setUpAsForeground(String text) {
//        PendingIntent pi =
//                PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MusicActivity.class),
//                        PendingIntent.FLAG_UPDATE_CURRENT);
//        mNotification = new Notification();
//        mNotification.tickerText = text;
//        mNotification.icon = R.drawable.ic_mshuffle_icon;
//        mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
//        mNotification.setLatestEventInfo(getApplicationContext(), getResources().getString(R.string.app_name), text, pi);
//        startForeground(NOTIFICATION_ID, mNotification);
//    }
}
