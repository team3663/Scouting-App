package com.team3663.scouting_app.utility.achievements;

public class RuleAlgaePickup implements AchievementRule {
    private final int threshold;

    public RuleAlgaePickup(int in_Threshold) {
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate() {
        return (Achievements.data_match_AlgaePickup >= threshold);
    }
}