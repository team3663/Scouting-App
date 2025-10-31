package com.team3663.scouting_app.utility.achievements;

import java.util.ArrayList;

public class RuleCompetition implements AchievementRule {
    private final int threshold;
    private final ArrayList<Integer> competitionsIds;

    public RuleCompetition(ArrayList<Integer> in_competitionsIds, int in_threshold) {
        competitionsIds = in_competitionsIds;
        threshold = in_threshold;
    }

    @Override
    public boolean evaluate() {
        int sum = 0;
        for (int compId : competitionsIds)
            sum += Achievements.data_NumMatchesByCompetition.getOrDefault(compId, 0);
        return sum >= threshold;
    }
}