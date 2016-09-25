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

//    public static final Long

    public static final String TABLE_TIMER = "timer";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_INIT = "start";
    public static final String COLUMN_END = "end";
    public static final String COLUMN_PAUSED_AT = "paused_at";
    public static final String COLUMN_REMAINING = "remaining";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_TIMEOUT = "timeout";
    public static final String COLUMN_REPEAT = "repeat";
    public static final String COLUMN_PAUSED = "paused";
    public static final String COLUMN_NOTIFY = "notify";
    public static final String COLUMN_SILENT = "silent";
    public static final String COLUMN_TONE = "tone";
    public static final String COLUMN_ELAPSED = "elapsed";

    public static final String KEY_TIMER_END = "timer_end_time";
    public static final String KEY_TIMER_NAME = "timer_name";

    public static final long MILLIS_IN_SECOND = 1000;
    public static final long MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;
    public static final long MILLIS_IN_HOUR = MILLIS_IN_MINUTE * 60;
    public static final long MILLIS_IN_DAY = MILLIS_IN_HOUR * 24;

    public static final String KEY_TIMER_NOTIFY = "timer_notify";
    public static final String KEY_TIMER_REPEAT = "timer_auto_delete";
    public static final String KEY_TIMER_TONE = "timer_tone";

    public static final int ACTIVITY_ACTION_RINGTONE = 1000;
    public static final int ACTIVITY_TAGS = 1001;
    public static final int ACTIVITY_TIMER_DETAIL = 1002;

    public static final long SECONDS_IN_HOUR = 3600;
    public static final long SECONDS_IN_MINUTE = 60;

    public static final String TABLE_ALERT = "alert";
    public static final String COLUMN_TIMER_ID = "timer";
    public static final String COLUMN_URI = "uri";
    public static final String KEY_TIMER_ADD = "timer_add";
    public static final String TABLE_TAGS = "tags";
    public static final String COLUMN_TAG = "tag";
    public static final String TABLE_TAG_REFERENCE = "tag_reference";
    public static final String KEY_TAG = "tag";
    public static final String KEY_SINGLE_TAG = "single_tag";
    public static final String KEY_TIMER_TAG = "timer_tag";
    public static final String COLUMN_RESUMED_AT = "resumed_at";
    public static final int ALARM_REQUEST_CODE = 101;
    public static final String COLUMN_STOPPED = "stopped";
    public static final String COLUMN_MISSED = "missed";
    public static final String KEY_MEDIA_PLAYER = "media_player";
    public static final String KEY_MUSIC_VOLUME = "media_impl";
    public static final String KEY_TIMER_ID = "id";
    public static final String KEY_TIMER = "timer";
    public static final String KEY_TIMER_START = "start";
    public static final String KEY_TIMER_EDITED = "timer_edited";
    public static final String KEY_INITIAL_PROGRESS = "initial_progress";
    public static final int ACTIVITY_EDIT_TIMER = 1003;
    public static final String KEY_TIMER_DELETED = "timer_deleted";

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

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);
        TimerBase.init(this);
        AlarmHandler.init(this);
    }
}
