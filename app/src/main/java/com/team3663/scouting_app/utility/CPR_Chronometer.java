package com.team3663.scouting_app.utility;

import android.os.SystemClock;
import android.widget.Chronometer;

// =============================================================================================
// Class:       CPR_Chronometer
// Description: Custom class to extend the Chronometer class with extra functions that makes our
//              code easier to implement.
// =============================================================================================
public class CPR_Chronometer {
    private final Chronometer chronometer;
    private long pause_offset;
    private boolean is_running;

    // Constructor: create the new Chronometer object
    public CPR_Chronometer(Chronometer in_chronometer) {
        chronometer = in_chronometer;
        pause_offset = 0;
        is_running = false;
    }

    // Member Function: Start the timer
    public void start() {
        chronometer.start();
        chronometer.setBase(SystemClock.elapsedRealtime());
        pause_offset = 0;
        is_running = true;
    }

    // Member Function: Stop the timer
    public void stop() {
        if (! is_running) return;

        chronometer.stop();
        pause_offset = SystemClock.elapsedRealtime() - chronometer.getBase();
        is_running = false;
    }

    // Member Function: Pause the timer if it was running
    public void pause() {
        if (! is_running) return;

        chronometer.stop();
        pause_offset = SystemClock.elapsedRealtime() - chronometer.getBase();
        is_running = false;
    }

    // Member Function: Resume the timer if it was paused
    public void resume() {
        if (is_running || pause_offset == 0) return;

        chronometer.start();
        chronometer.setBase(SystemClock.elapsedRealtime() - pause_offset);
        pause_offset = 0;
        is_running = true;
    }

    // Member Function: Get the number of seconds that have elapsed since the timer was started
    public int getElapsedSeconds() {
        if (is_running) return (int) ((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000);
        return (int) (pause_offset / 1000);
    }

    // Member Function: Get the number of milliseconds that have elapsed since the timer was started
    public long getElapsedMilliSeconds() {
        if (is_running) return (SystemClock.elapsedRealtime() - chronometer.getBase());
        return pause_offset;
    }

    // Member Function: Set the timer to the specified number of seconds
    public void setTime(int in_seconds) {
        chronometer.setBase(SystemClock.elapsedRealtime() - (in_seconds * 1000L));
    }

    // Member Function: Set the listener for when the timer ticks
    public void setOnChronometerTickListener(Chronometer.OnChronometerTickListener listener) {
        chronometer.setOnChronometerTickListener(listener);
    }

    // Member Function: Return the time that the timer was started.
    public long getStartTime() {
        return chronometer.getBase();
    }
}
