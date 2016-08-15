package com.flyingbuff.countdown;

import android.content.ContentValues;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.util.Comparator;
import java.util.Locale;

/**
 * Created by Aayush on 8/7/2016.
 */
public class Timer extends TimerBase {

    public static final int MILLI = 0,
            SECOND = 1,
            MINUTE = 2,
            HOUR = 3,
            DAY = 4,
            MONTH = 5,
            YEAR = 6;

    public static final String[] TIME_UNIT = new String[]{"milli", "sec", "min", "hour", "day", "month", "year"};

    private int id;

    private String name;

    private final long start;
    private final long end;

    private long paused_at;
    private long resumed_at;

    private long duration;
    private long elapsed;

    private boolean repeat;
    private boolean notify;
    private boolean silent;

    private boolean paused;

    public Timer(
            int id,
            String name,
            long start,
            long end,
            long paused_at,
            long resumed_at,
            long duration,
            long elapsed,
            boolean repeat,
            boolean notify,
            boolean silent,
            boolean paused
    ) {
        this.id = id;
        this.name = name;
        this.start = start;
        this.end = end;
        this.paused_at = paused_at;
        this.resumed_at = resumed_at;
        this.duration = duration;
        this.elapsed = elapsed;
        this.repeat = repeat;
        this.notify = notify;
        this.silent = silent;
        this.paused = paused;
    }

    public Timer(long end) {
        this("", end);
    }

    public Timer(String name, long end) {
        this(name, end, false);
    }

    public Timer(long end, boolean repeat) {
        this("", end, repeat);
    }

    public Timer(String name, long end, boolean repeat) {
        this(name, end, repeat, true, false);
    }

    public Timer(String name, long end, boolean repeat, boolean notify, boolean silent) {
        this(name, DateTime.now().getMillis(), end, repeat, notify, silent);
    }

    public Timer(String name, long start, long end, boolean repeat, boolean notify, boolean silent) {
        start = Countdown.normalize(start);
        end = Countdown.normalize(end);

        this.name = name;

        this.start = start;
        this.end = end;
        this.duration = end - start;

        this.elapsed = 0;

        this.paused_at = start;
        this.resumed_at = start;

        this.id = -1;
        this.repeat = repeat;
        this.notify = notify;
        this.silent = silent;

        this.paused = false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public long getResumedAt() {
        return resumed_at;
    }

    public long getDuration() {
        return duration;
    }

    public long getElapsed() {
        return elapsed;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public boolean isNotify() {
        return notify;
    }

    public boolean isSilent() {
        return silent;
    }

    public boolean isPaused() {
        return paused;
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

    public String humanize() {
        return humanize(getRemainingTime());
    }

    public static String humanize(long duration) {
        String[] durationString = Timer.formatDuration(duration);

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < durationString.length; i += 2) {
            if (!"00".equals(durationString[i])) {
                builder.append(durationString[i]).append(" ");
                builder.append(durationString[i + 1]).append(" ");
            }
        }
        return builder.toString();
    }

    public String humanizeEndDateTime(String prefix) {
        DateTime endDateTime = DateTime.now().plus(getRemainingTime());

        LocalDate endDate = endDateTime.toLocalDate();
        LocalDate today = LocalDate.now();

        StringBuilder summary = new StringBuilder();

        summary.append(prefix);
        summary.append(" ");
        summary.append(endDateTime.toString("hh:mm a"));
        if (endDate.isAfter(today.plusDays(1))) {
            summary.append(", ");
            if (endDate.isAfter(today.plusDays(5)))
                summary.append(endDate.toString("dd MMM"));
            else
                summary.append(endDate.toString("EEEE"));
            if (endDate.getYear() != today.getYear())
                summary.append(", ")
                        .append(endDate.toString("yyyy"));
        } else if (endDate.isAfter(today))
            summary.append(" tommorow");

        return summary.toString();
    }

    public String humanizeEndDateTime() {
        return humanizeEndDateTime("at");
    }

    public long getElapsedTime() {
        if (paused_at <= resumed_at)
            return elapsed + DateTime.now().getMillis() - resumed_at;
        else
            return elapsed;
    }

    public long getRemainingTime() {
        return duration - getElapsedTime();
    }

    public long getTimeOut() {
        return DateTime.now().getMillis() + getRemainingTime();
    }

    @Override
    protected void pauseTimer() {
        super.pauseTimer();

        long now = Countdown.normalize(DateTime.now().getMillis());

        elapsed = getElapsedTime();
        paused_at = now;
        paused = true;

        ContentValues args = new ContentValues();
        args.put(Countdown.COLUMN_ELAPSED, elapsed);
        args.put(Countdown.COLUMN_PAUSED_AT, paused_at);
        args.put(Countdown.COLUMN_PAUSED, paused);

        getDatabaseHelper().editTimer(id, args);
    }

    @Override
    protected void resumeTimer() {
        super.resumeTimer();

        long now = Countdown.normalize(DateTime.now().getMillis());

        paused = false;
        resumed_at = now;

        ContentValues args = new ContentValues();
        args.put(Countdown.COLUMN_RESUMED_AT, resumed_at);
        args.put(Countdown.COLUMN_PAUSED, paused);

        getDatabaseHelper().editTimer(id, args);
    }

    @Override
    protected void resetTimer() {
        super.resetTimer();

        long now = Countdown.normalize(DateTime.now().getMillis());

        elapsed = 0;
        paused_at = now;
        resumed_at = now;

        ContentValues args = new ContentValues();
        args.put(Countdown.COLUMN_ELAPSED, elapsed);
        args.put(Countdown.COLUMN_RESUMED_AT, resumed_at);
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

    public static final Comparator<Timer> REMAINING_TIME_COMPARATOR = new Comparator<Timer>() {
        @Override
        public int compare(Timer lhs, Timer rhs) {
            return (int) (lhs.getTimeOut() - rhs.getTimeOut());
        }
    };
    public static final Comparator<Timer> ALPHABETICAL_COMPARATOR = new Comparator<Timer>() {
        @Override
        public int compare(Timer lhs, Timer rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    };

    public static final Comparator<Timer> CREATION_DATE_COMPARATOR = new Comparator<Timer>() {
        @Override
        public int compare(Timer lhs, Timer rhs) {
            return (int) (lhs.getStart() - rhs.getStart());
        }
    };

    public void setId(int id) {
        this.id = id;
    }

    public float getProgress() {
        return getElapsedTime() / (float) getDuration();
    }
}
