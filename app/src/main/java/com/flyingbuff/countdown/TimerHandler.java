package com.flyingbuff.countdown;

import java.util.ArrayList;

/**
 * Created by Aayush on 8/15/2016.
 */
public class TimerHandler {
    private static final ArrayList<Timer> RUNNING_TIMERS = new ArrayList<>();

    public static void addNewTimer(Timer timer) {
        if (timer.isPaused()) return;

        RUNNING_TIMERS.add(timer);
        validateQueue();
    }

    private static void validateQueue() {

    }

}
