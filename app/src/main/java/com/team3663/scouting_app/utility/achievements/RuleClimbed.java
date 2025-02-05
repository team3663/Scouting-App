package com.team3663.scouting_app.utility.achievements;

public class RuleClimbed implements AchievementRule {
    private final int threshold;

    public RuleClimbed(int in_threshold) {
        threshold = in_threshold;
    }

    @Override
    public boolean evaluate() {
        return Achievements.data_match_ClimbSuccess >= threshold;
    }
}