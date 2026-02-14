package com.team3663.scouting_app.utility.achievements;

public class RuleClimbed implements AchievementRule {
    private final int threshold;
    private final String location;

    public RuleClimbed(String in_Location, int in_Threshold) {
        location = in_Location;
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate() {
        switch (location) {
            case "auto":
                return (Achievements.data_match_ClimbSuccessAuto >= threshold);
            case "tele":
                return (Achievements.data_match_ClimbSuccessTele >= threshold);
            case "teleL3":
                return (Achievements.data_match_ClimbSuccessTeleL3 >= threshold);
        }

        return false;
    }
}