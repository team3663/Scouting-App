package com.team3663.scouting_app.utility.achievements;

public class RuleDefense implements AchievementRule {
    private final int threshold;

    public RuleDefense(int in_Threshold) {
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate() {
        return (Achievements.data_Toggle_Defense >= threshold);
    }
}