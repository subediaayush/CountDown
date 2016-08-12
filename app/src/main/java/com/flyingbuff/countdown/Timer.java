package com.flyingbuff.countdown;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.Locale;

/**
 * Created by Aayush on 8/7/2016.
 */
public class Timer extends TimerBase implements Comparable {

    public static final int MILLI = 0,
            SECOND = 1,
            MINUTE = 2,
            HOUR = 3,
            DAY = 4,
            MONTH = 5,
            YEAR = 6;

    public static final String[] TIME_UNIT = new String[]{"milli", "sec", "min", "hour", "day", "month", "year"};

    int id;

    String name;

    final long start;
    final long end;

    long paused_at;
    long remaining;
    long elapsed;
    long duration;
    long time_out;

    boolean single_use;
    boolean paused;
    boolean notify;
    boolean silent;

    int tone;

    public Timer(
            int id,
            String name,
            long start,
            long end,
            long paused_at,
            long duration,
            long remaining,
            long elapsed,
            long time_out,
            boolean single_use,
            boolean paused,
            boolean notify,
            boolean silent,
            int tone
    ) {
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.paused_at = paused_at;
        this.duration = Countdown.normalize(duration);
        this.remaining = Countdown.normalize(remaining);
        this.elapsed = Countdown.normalize(elapsed);
        this.time_out = Countdown.normalize(time_out);
        this.single_use = single_use;
        this.paused = paused;
        this.notify = notify;
        this.silent = silent;
        this.tone = tone;
    }

    public Timer(long end) {
        this("", end);
    }

    public Timer(String name, long end) {
        this(name, end, false);
    }

    public Timer(long end, boolean single_use) {
        this("", end, single_use);
    }

    public Timer(String name, long end, boolean single_use) {
        this(name, end, single_use, true, false);
    }

    public Timer(String name, long end, boolean single_use, boolean notify, boolean silent) {
        this(name, DateTime.now().getMillis(), end, single_use, notify, silent);
    }

    public Timer(String name, long start, long end, boolean single_use, boolean notify, boolean silent) {
        start = Countdown.normalize(start);
        end = Countdown.normalize(end);

        this.name = name;
        this.start = start;
        this.end = end;
        this.single_use = single_use;
        this.notify = notify;
        this.silent = silent;

        this.id = -1;
        this.paused_at = end;
        this.remaining = end - start;
        this.elapsed = 0;
        this.duration = end - start;
        this.time_out = end;

        this.paused = false;
    }

    public static int getOrder(long duration) {
        Period period = new Period(duration).normalizedStandard();

        if (period.getYears() > 0) return YEAR;
        if (period.getMonths() > 0) return MONTH;
        if (period.getDays() > 0 || period.getWeeks() > 0) return DAY;
        if (period.getHours() > 0) return HOUR;
        if (period.getMinutes() > 0) return MINUTE;
        if (period.getSeconds() > 0) return SECOND;
        else return MILLI;
    }

    public static int getField(long duration, int order) {
        Period period = new Period(duration).normalizedStandard();

        switch (order) {
            case YEAR:
                return period.getYears();
            case MONTH:
                return period.getMonths();
            case DAY:
                return period.getWeeks() * 7 + period.getDays();
            case HOUR:
                return period.getHours();
            case MINUTE:
                return period.getMinutes();
            case SECOND:
                return period.getSeconds();
            case MILLI:
                return period.getMillis();
            default:
                return -1;
        }
    }

    public String[] formatDuration() {
        return formatDuration(getRemainingTime());
    }

    public static String[] formatDuration(long duration) {
        if (duration < 1) return new String[]{"-1"};

        int order = getOrder(duration);

        String output = String.valueOf(order);
        Locale l = Locale.getDefault();

        for (int i = order; i >= Math.max(order - 2, SECOND); i--) {
            output += String.format(l, " %02d ", getField(duration, i));
            output += TIME_UNIT[i];
        }
        return output.split(" ");
    }

    public long getRemainingTime() {
        return time_out - DateTime.now().getMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getPausedAt() {
        return paused_at;
    }

    public void setPausedAt(long stopped_at) {
        this.paused_at = stopped_at;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getTimeOut() {
        return time_out;
    }

    public void setTimeOut(long time_out) {
        this.time_out = time_out;
    }

    public long getRemaining() {
        return remaining;
    }

    public void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    public boolean isSingleUse() {
        return single_use;
    }

    public void setSingleUse(boolean singleUse) {
        this.single_use = single_use;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public int getTone() {
        return tone;
    }

    public void setTone(int tone) {
        this.tone = tone;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        if (another instanceof Timer)
            return (int) (time_out - ((Timer) another).time_out);
        return 0;
    }

    public long getElapsed() {
        return elapsed;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    @Override
    protected void pauseTimer() {
        super.pauseTimer();

        long now = DateTime.now().getMillis();
        remaining = paused_at - now;
        elapsed += remaining;
        paused_at = now;
        paused = true;

        ContentValues args = new ContentValues();
        args.put(Countdown.COLUMN_REMAINING, remaining);
        args.put(Countdown.COLUMN_ELAPSED, elapsed);
        args.put(Countdown.COLUMN_PAUSED_AT, paused_at);
        args.put(Countdown.COLUMN_PAUSED, paused);

        getDatabaseHelper().editTimer(id, args);
    }

    @Override
    protected void resumeTimer() {
        super.resumeTimer();

        time_out = paused_at + remaining;
        paused = false;

        ContentValues args = new ContentValues();
        args.put(Countdown.COLUMN_TIMEOUT, time_out);
        args.put(Countdown.COLUMN_PAUSED, paused);

        getDatabaseHelper().editTimer(id, args);
    }

    @Override
    protected void resetTimer() {
        super.resetTimer();

        long now = DateTime.now().getMillis();
        remaining = duration;
        paused_at = now;

        ContentValues args = new ContentValues();
        args.put(Countdown.COLUMN_REMAINING, remaining);
        args.put(Countdown.COLUMN_PAUSED_AT, paused_at);

        getDatabaseHelper().editTimer(id, args);

        resumeTimer();
    }

    @Override
    protected void saveTimer() {
        super.saveTimer();

        getDatabaseHelper().saveTimer(this);
    }

    @Override
    protected void timeOut() {
        super.timeOut();
    }
}
