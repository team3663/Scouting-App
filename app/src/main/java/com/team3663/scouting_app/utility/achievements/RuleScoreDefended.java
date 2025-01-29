package com.team3663.scouting_app.utility.achievements;

public class RuleScoreDefended implements AchievementRule {
    private final int threshold;

    public RuleScoreDefended(int in_Threshold){
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate() {
        return (Achievements.data_ScoreWhileDefended >= threshold);
    }
}