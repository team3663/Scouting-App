package com.team3663.scouting_app.utility.achievements;

public class RuleCoralPickup implements AchievementRule {
    private final int threshold;
    private final String location;

    public RuleCoralPickup(String in_Location, int in_Threshold){
        location = in_Location;
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate(){
        switch (location) {
            case "ground":
                return (Achievements.data_match_CoralPickupGround >= threshold);
            case "station":
                return (Achievements.data_match_CoralPickupStation >= threshold);
        }

        return false;
    }
}