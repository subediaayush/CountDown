package com.flyingbuff.countdown;

import android.app.Application;
import android.content.Context;
import android.util.TypedValue;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

/**
 * Created by Aayush on 8/7/2016.
 */
public class Countdown extends Application {
    public static final String PACKAGE_NAME = "com.flyingbuff.countdown";

    public static final String TABLE_TIMER = "timer";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_INIT = "start";
    public static final String COLUMN_END = "end";
    public static final String COLUMN_PAUSED_AT = "paused_at";
    public static final String COLUMN_REMAINING = "remaining";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_TIMEOUT = "timeout";
    public static final String COLUMN_SINGLE_USE = "one_time";
    public static final String COLUMN_PAUSED = "paused";
    public static final String COLUMN_NOTIFY = "notify";
    public static final String COLUMN_SILENT = "silent";
    public static final String COLUMN_TONE = "tone";
    public static final String COLUMN_ELAPSED = "elapsed";

    public static final String KEY_TIMER_END = "timer_end_time";
    public static final String KEY_TIMER_NAME = "timer_name";

    public static final long MILLIS_IN_DAY = 86400000;
    public static final long MILLIS_IN_HOUR = 3600000;
    public static final String KEY_TIMER_NOTIFY = "timer_notify";
    public static final String KEY_TIMER_AUTO_DEL = "timer_auto_delete";
    public static final int ACTIVITY_ACTION_RINGTONE = 1000;
    public static final String KEY_TIMER_TONE = "timer_tone";

    public static final long SECONDS_IN_HOUR = 3600;
    public static final long SECONDS_IN_MINUTE = 60;
    public static final String TABLE_ALERT = "alert";
    public static final String COLUMN_TIMER_ID = "timer";
    public static final String COLUMN_URI = "uri";
    public static final String KEY_TIMER_ADD = "timer_add";

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);
        TimerBase.init(this);
    }


    public static float dpToPixel(Context context, int dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics()
        );
    }

    public static DateTime normalize(DateTime datetime) {
        return datetime.withMillisOfSecond(0);
    }

    public static LocalTime normalize(LocalTime localTime) {
        return localTime.withMillisOfSecond(0);
    }

    public static long normalize(long datetime) {
        return (datetime / 1000) * 1000;
    }
}
