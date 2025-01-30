package com.team3663.scouting_app.utility.achievements;

public class RuleScoreCoral implements AchievementRule {
    private final int threshold;
    private final int level;

    public RuleScoreCoral(int in_Level, int in_Threshold){
        level = in_Level;
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate(){
        return (Achievements.data_match_CoralLevel[level] >= threshold);
    }
}