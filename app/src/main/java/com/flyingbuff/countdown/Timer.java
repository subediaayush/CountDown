package com.flyingbuff.countdown;

import android.content.ContentValues;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.util.Comparator;

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
    private boolean stopped;

    private boolean missed;

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
            boolean paused,
            boolean stopped,
            boolean missed
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
        this.stopped = stopped;
        this.missed = missed;
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
        this.stopped = true;

        this.missed = false;
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

    public int[] formatDuration() {
        return formatDuration(getRemainingTime());
    }

    public static int[] formatDuration(long duration) {
        boolean negativeDuration = false;

        if (duration < 0) {
            negativeDuration = true;
            duration = -duration;
        }

        int order = negativeDuration ? -getOrder(duration) : getOrder(duration);

        int absOrder = Math.abs(order);

        int output[] = new int[absOrder + 2];

        for (int i = output.length - 1; i > MILLI; i--) {
            output[i] = getField(duration, i - 1);
        }

        output[0] = order;

        return output;
    }

    public String humanize() {
        return humanize(getRemainingTime());
    }

    public static String humanize(long duration) {

        duration += duration % 1000 == 0 ? 0 : 1000;

        int[] durationArray = Timer.formatDuration(duration);

        int order = durationArray[0];

        StringBuilder builder = new StringBuilder();
        for (int i = order + 1; i >= Math.max(order - 2, 2); i--) {
            if (durationArray[i] != 0) {
                builder.append(durationArray[i]).append(" ");
                builder.append(TIME_UNIT[i - 1]).append(" ");
            }
        }
        return builder.toString();
    }

    public static String humanizeDateTime(long dateTime) {
        DateTime endDateTime = new DateTime(dateTime);

        LocalDate endDate = endDateTime.toLocalDate();
        LocalDate today = LocalDate.now();

        StringBuilder summary = new StringBuilder();

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
        if (stopped) return duration;

        if (paused_at <= resumed_at)
            return Math.min(duration, elapsed + DateTime.now().getMillis() - resumed_at);
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
    protected void startTimer() {
        long now = DateTime.now().getMillis();

        paused_at = now;
        resumed_at = now;
        paused = false;
        elapsed = 0;
        stopped = false;

        ContentValues args = new ContentValues();
        args.put(Countdown.COLUMN_STOPPED, stopped);
        args.put(Countdown.COLUMN_ELAPSED, elapsed);
        args.put(Countdown.COLUMN_PAUSED_AT, paused_at);
        args.put(Countdown.COLUMN_PAUSED, paused);
        args.put(Countdown.COLUMN_RESUMED_AT, resumed_at);

        getDatabaseHelper().editTimer(id, args);

        super.startTimer();
    }

    @Override
    protected void pauseTimer() {
        long now = DateTime.now().getMillis();

        elapsed = getElapsedTime();
        paused_at = now;
        paused = true;

        ContentValues args = new ContentValues();
        args.put(Countdown.COLUMN_ELAPSED, elapsed);
        args.put(Countdown.COLUMN_PAUSED_AT, paused_at);
        args.put(Countdown.COLUMN_PAUSED, paused);

        getDatabaseHelper().editTimer(id, args);

        super.pauseTimer();
    }

    @Override
    protected void resumeTimer() {

        long now = DateTime.now().getMillis();

        paused = false;
        resumed_at = now;

        ContentValues args = new ContentValues();
        args.put(Countdown.COLUMN_RESUMED_AT, resumed_at);
        args.put(Countdown.COLUMN_PAUSED, paused);

        getDatabaseHelper().editTimer(id, args);

        super.resumeTimer();
    }

    @Override
    protected void resetTimer() {

        long now = DateTime.now().getMillis();

        elapsed = 0;
        paused_at = now;
        resumed_at = now;
        paused = false;

        ContentValues args = new ContentValues();
        args.put(Countdown.COLUMN_ELAPSED, elapsed);
        args.put(Countdown.COLUMN_RESUMED_AT, resumed_at);
        args.put(Countdown.COLUMN_PAUSED_AT, paused_at);
        args.put(Countdown.COLUMN_PAUSED, paused);

        getDatabaseHelper().editTimer(id, args);

        super.resetTimer();
    }

    @Override
    protected void saveTimer() {

        getDatabaseHelper().saveTimer(this);
        super.saveTimer();
    }

    @Override
    protected void stopTimer() {
        elapsed = getDuration();
        stopped = true;
        missed = true;

        ContentValues args = new ContentValues();
        args.put(Countdown.COLUMN_ELAPSED, elapsed);
        args.put(Countdown.COLUMN_STOPPED, stopped);
        args.put(Countdown.COLUMN_MISSED, missed);

        getDatabaseHelper().editTimer(id, args);

        super.stopTimer();
    }

    public static final Comparator<Timer> REMAINING_TIME_COMPARATOR = new Comparator<Timer>() {
        @Override
        public int compare(Timer lhs, Timer rhs) {
            long firstTimerValue;
            long secondTimerValue;

            if (lhs.isMissed()) firstTimerValue = lhs.getDuration() + Long.MIN_VALUE;
            else firstTimerValue = lhs.getRemainingTime();

            if (rhs.isMissed()) secondTimerValue = rhs.getDuration() + Long.MIN_VALUE;
            else secondTimerValue = rhs.getRemainingTime();

            if (firstTimerValue == secondTimerValue) {
                firstTimerValue = lhs.getStart();
                secondTimerValue = rhs.getStart();
            }

            return (int) Math.signum((firstTimerValue) - (secondTimerValue));
        }
    };
    public static final Comparator<Timer> ALPHABETICAL_COMPARATOR = new Comparator<Timer>() {
        @Override
        public int compare(Timer lhs, Timer rhs) {
            int output = (int) Math.signum(lhs.getName().compareTo(rhs.getName()));
            if (output == 0) output = REMAINING_TIME_COMPARATOR.compare(lhs, rhs);
            return output;
        }
    };

    public static final Comparator<Timer> CREATION_DATE_COMPARATOR = new Comparator<Timer>() {
        @Override
        public int compare(Timer lhs, Timer rhs) {
            int output = (int) (lhs.getStart() - rhs.getStart());
            if (output == 0) output = REMAINING_TIME_COMPARATOR.compare(lhs, rhs);
            return output;
        }
    };

    public void setId(int id) {
        this.id = id;
    }

    public int getProgress() {
        float normalizedElapsedTime = Countdown.normalize(getElapsedTime());
        float normalizedDuration = Countdown.normalize(getDuration());

        return (int) Math.ceil(normalizedElapsedTime / normalizedDuration * 100);
    }

    public boolean isStopped() {
        return stopped;
    }

    public boolean isMissed() {
        return missed;
    }
}
