package com.flyingbuff.countdown;

import org.joda.time.DateTime;

/**
 * Created by Aayush on 8/7/2016.
 */
public class Timer {
    int id;

    String name;

    long init;
    long end;
    long resumed_at;
    long stopped_at;
    long goal;

    boolean single_use;

    public Timer(int id, String name, long init, long end, long resumed_at, long stopped_at, long goal, boolean single_use) {
        this.id = id;
        this.name = name;
        this.init = init;
        this.end = end;
        this.resumed_at = resumed_at;
        this.stopped_at = stopped_at;
        this.goal = goal;
        this.single_use = single_use;
    }

    public Timer(long end){
        this("", end);
    }

    public Timer(String name, long end) {
        this(name, end, false);
    }

    public Timer (long end, boolean single_use){
        this("", end, single_use);
    }

    public Timer (String name, long end, boolean single_use){
        this(name, DateTime.now().getMillis(), end, single_use);
    }

    public Timer(String name, long init, long end, boolean single_use) {
        this.name = name;
        this.init = init;
        this.end = end;
        this.single_use = single_use;

        this.id = -1;
        this.resumed_at = init;
        this.stopped_at = end;
        this. goal = end - init;
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

    public long getInit() {
        return init;
    }

    public void setInit(long init) {
        this.init = init;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getResumedAt() {
        return resumed_at;
    }

    public void setResumed_at(long resumed_at) {
        this.resumed_at = resumed_at;
    }

    public long getStoppedAt() {
        return stopped_at;
    }

    public void setStopped_at(long stopped_at) {
        this.stopped_at = stopped_at;
    }

    public long getGoal() {
        return goal;
    }

    public void setGoal(long goal) {
        this.goal = goal;
    }

    public boolean isSingleUse() {
        return single_use;
    }

    public void setSingle_use(boolean single_use) {
        this.single_use = single_use;
    }
}
