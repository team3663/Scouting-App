package com.team3663.scouting_app.utility.achievements;

public class RuleFieldReset implements AchievementRule {
    private final int threshold;

    public RuleFieldReset(int in_Threshold){
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate(){
        return (Achievements.data_FieldReset >= threshold);
    }
}