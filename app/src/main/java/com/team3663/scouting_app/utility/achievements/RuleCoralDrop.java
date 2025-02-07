package com.team3663.scouting_app.utility.achievements;

public class RuleCoralDrop implements AchievementRule {
    private final int threshold;

    public RuleCoralDrop(int in_Threshold){
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate(){
        return (Achievements.data_match_CoralDropped >= threshold);
    }
}