package com.flyingbuff.countdown;

import android.content.Context;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Aayush on 8/8/2016.
 */
public class TimerBase {
    private static Context context;

    private static ArrayList<Timer> runningTimers = new ArrayList<>();

    private static DatabaseHelper db;

    protected static TimerListener timerListener = new TimerListener() {
        @Override
        public void OnTimeOut(Timer timer) {
            runningTimers.remove(timer);
            if (timer.simpleTimerListener != null) timer.simpleTimerListener.OnTimeOut();
        }

        @Override
        public void OnReset(Timer timer) {
            runningTimers.add(timer);
            if (timer.simpleTimerListener != null) timer.simpleTimerListener.OnReset();
        }

        @Override
        public void OnPaused(Timer timer) {
            runningTimers.remove(timer);
            if (timer.simpleTimerListener != null) timer.simpleTimerListener.OnPaused();
        }

        @Override
        public void OnResumed(Timer timer) {
            runningTimers.add(timer);
            if (timer.simpleTimerListener != null) timer.simpleTimerListener.OnResumed();
        }
    };

    protected SimpleTimerListener simpleTimerListener;

    public static void init(Context context) {
        TimerBase.context = context;
        TimerBase.db = new DatabaseHelper(context);
    }

    public TimerBase() {
        runningTimers.add((Timer) this);
        validateList();
        Log.i("Timerbase", "New timer created");
    }

    public SimpleTimerListener getSimpleTimerListener() {
        return simpleTimerListener;
    }

    public void setSimpleTimerListener(SimpleTimerListener simpleTimerListener) {
        this.simpleTimerListener = simpleTimerListener;
    }

    private static void validateList() {
        Timer maxTimer = Collections.max(runningTimers);
        Timer minTimer = Collections.min(runningTimers);

        long smallestTimer = minTimer.getRemainingTime();
        long largestTimer = maxTimer.getRemainingTime();
        long commonSteps = gcd(runningTimers);
    }

    private void monitorRunningTimers(ArrayList<Timer> timers) {
        ArrayList<Timer> newList = new ArrayList<>();
        long currentTime = DateTime.now().getMillis();
        for (Timer timer : timers) {
            if (timer.getRemaining() <= 0) timer.timeOut();
        }
    }

    protected void saveTimer() {
    }

    protected void pauseTimer() {
        timerListener.OnPaused((Timer) this);
        Log.i("TimerBase", "Timer Paused");
    }

    protected void resumeTimer() {
        timerListener.OnResumed((Timer) this);
        Log.i("TimerBase", "Timer Paused");

    }

    protected void resetTimer() {
        timerListener.OnReset((Timer) this);
        Log.i("TimerBase", "Timer Reset");
    }

    protected void timeOut() {
        timerListener.OnTimeOut((Timer) this);
        Log.i("TimerBase", "Timer Paused");
    }

    private static long gcd(long a, long b) {
        while (b > 0) {
            long temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }

    private static long gcd(ArrayList<Timer> timer) {
        long result = timer.get(0).getRemainingTime();
        for (int i = 1; i < timer.size(); i++)
            result = gcd(result, timer.get(i).getRemainingTime());
        return result;
    }

    public void notifyTimer() {
        for (TimerBase timer : runningTimers) timer.notify();
    }

    protected interface TimerListener {
        void OnTimeOut(Timer timer);

        void OnReset(Timer timer);

        void OnPaused(Timer timer);

        void OnResumed(Timer timer);
    }

    protected class SimpleTimerListener {
        public void OnTimeOut() {
            Log.i("Timerbase", "Timer timed out");
        }

        public void OnReset() {
        }

        public void OnPaused() {
        }

        public void OnResumed() {
        }
    }

    protected DatabaseHelper getDatabaseHelper() {
        return db;
    }
}
