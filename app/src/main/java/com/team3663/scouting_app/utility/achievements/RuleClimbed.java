package com.team3663.scouting_app.utility.achievements;

import java.util.List;

public class RuleClimbed implements AchievementRule {
    private final List<String> acceptedClimbs;

    public RuleClimbed(List<String> acceptedClimbs) {
        this.acceptedClimbs = acceptedClimbs;
    }

    @Override
    public boolean evaluate() {
        return acceptedClimbs.contains(Achievements.data_match_ClimbType);
    }
}