package com.team3663.scouting_app.utility.achievements;

import com.team3663.scouting_app.config.Globals;

public class RuleAttendedCompetition implements AchievementRule {
    private final int threshold;
    private final boolean attended;

    public RuleAttendedCompetition(boolean in_attended, int in_threshold) {
        attended = in_attended;
        threshold = in_threshold;
    }

    @Override
    public boolean evaluate() {
        int sum = 0;

        for (int compId = 0 ; compId < Globals.CompetitionList.size(); ++compId)
            if (Globals.CompetitionList.isAttended(compId) == attended)
                sum += Achievements.data_NumMatchesByCompetition[compId];

        return sum >= threshold;
    }
}