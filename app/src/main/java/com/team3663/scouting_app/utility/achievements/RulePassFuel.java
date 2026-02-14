package com.team3663.scouting_app.utility.achievements;

public class RulePassFuel implements AchievementRule {
    private final int threshold;
    private final String location;

    public RulePassFuel(String in_Location, int in_Threshold){
        location = in_Location;
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate(){
        switch (location) {
            case "neutral":
                return (Achievements.data_match_FuelPassNeutral >= threshold);
            case "opponent":
                return (Achievements.data_match_FuelPassOpponent >= threshold);
            case "everywhere":
                return (Achievements.data_match_FuelPassTotal >= threshold);
        }

        return false;
    }
}