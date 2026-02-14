package com.team3663.scouting_app.utility.achievements;

public class RuleShootFuel implements AchievementRule {
    private final int threshold;
    private final String accuracy;

    public RuleShootFuel(String in_Accuracy, int in_Threshold) {
        accuracy = in_Accuracy;
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate() {
        switch (accuracy) {
            case "none":
                return (Achievements.data_match_FuelShoot >= threshold);
            case "accuracy":
                return (Achievements.data_match_FuelShootWithAccuracy >= threshold);
        }

        return false;
    }
}