package com.team3663.scouting_app.utility.achievements;

public class RuleClimbed implements AchievementRule {
    private final String matchphase;
    private final String level;

    public RuleClimbed(String in_MatchPhase, String in_Level) {
        matchphase = in_MatchPhase;
        level = in_Level;
    }

    @Override
    public boolean evaluate() {
        switch (matchphase.toUpperCase()) {
            case "AUTO":
                return (Achievements.data_match_ClimbSuccessAuto.equals(level));
            case "TELE":
                return (!Achievements.data_match_ClimbSuccessTele.isEmpty());
            case "TELE_L3":
                return (Achievements.data_match_ClimbSuccessTele.equals("L3"));
        }

        return false;
    }
}