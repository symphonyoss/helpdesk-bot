package org.symphonyoss.webdesk.models.calls;

/**
 * Created by nicktarsillo on 7/21/16.
 */
public class Timer {
    private double time;

    public Timer(){}

    public void start(){
        TimerThread.timerSet.add(this);
    }

    public void stop(){
        TimerThread.timerSet.remove(this);
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }
}