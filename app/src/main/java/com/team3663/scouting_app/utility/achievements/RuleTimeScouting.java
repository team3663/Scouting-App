package com.team3663.scouting_app.utility.achievements;

import android.os.SystemClock;

import com.team3663.scouting_app.config.Globals;

public class RuleTimeScouting implements AchievementRule {
    private final long threshold;

    public RuleTimeScouting(long in_Threshold){
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate(){
        return ((Achievements.data_StartTime > 0) && (SystemClock.elapsedRealtime() - Achievements.data_StartTime >= threshold));
    }
}
