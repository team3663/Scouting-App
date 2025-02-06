package com.team3663.scouting_app.utility.achievements;

public class RuleCompetition implements AchievementRule {
    private final int threshold;
    private final String TbaCompetitionName;

    public RuleCompetition(String in_TbaCompetitionName, int in_threshold) {
        TbaCompetitionName = in_TbaCompetitionName;
        threshold = in_threshold;
    }

    @Override
    public boolean evaluate() {
        switch (TbaCompetitionName) {
            case "worlds":
                return Achievements.data_WorldMatches >= threshold;
            case "2025new":
                return Achievements.data_WorldNewtonMatches >= threshold;
            default:
                return false;
        }
    }
}