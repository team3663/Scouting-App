package com.team3663.scouting_app.utility.achievements;

public class RuleNumMatches implements AchievementRule {
    private final int threshold;

    public RuleNumMatches(int in_Threshold){
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate(){
        return (Achievements.data_NumMatches >= threshold);
    }
}
