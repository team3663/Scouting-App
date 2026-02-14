package com.team3663.scouting_app.utility.achievements;

public class RulePickUpFuel implements AchievementRule {
    private final int threshold;
    private final String location;

    public RulePickUpFuel(String in_Location, int in_Threshold){
        location = in_Location;
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate(){
        switch (location) {
            case "outpost":
                return (Achievements.data_match_FuelPickUpOutpost >= threshold);
            case "depot":
                return (Achievements.data_match_FuelPickUpDepot >= threshold);
        }

        return false;
    }
}