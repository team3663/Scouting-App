package com.team3663.scouting_app.utility.achievements;

public class RuleNumEvents implements AchievementRule {
    private final int threshold;

    public RuleNumEvents(int in_Threshold){
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate(){
        return (Achievements.data_NumEvents >= threshold);
    }
}
