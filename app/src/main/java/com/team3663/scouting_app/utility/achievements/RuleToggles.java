package com.team3663.scouting_app.utility.achievements;

public class RuleToggles implements AchievementRule {
    private final int threshold;
    private final String toggle;
    private final boolean perMatch;

    public RuleToggles(String in_Toggle, boolean in_perMatch, int in_Threshold){
        toggle = in_Toggle;
        perMatch = in_perMatch;
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate(){
        switch (toggle) {
            case "defense":
                return (Achievements.data_Toggle_Defense >= threshold);
            case "defended":
                return (Achievements.data_Toggle_Defended >= threshold);
            case "not moving":
                if (perMatch) return (Achievements.data_match_Toggle_NotMoving >= threshold);
                else return (Achievements.data_Toggle_NotMoving >= threshold);
        }

        // All toggles
        return ((Achievements.data_Toggle_Defense + Achievements.data_Toggle_Defended + Achievements.data_Toggle_NotMoving) >= threshold);
    }
}
