package com.team3663.scouting_app.utility.achievements;

public class RuleScoreAlgae implements AchievementRule {
    private final int threshold;
    private final String location;

    public RuleScoreAlgae(String in_Location, int in_Threshold){
        location = in_Location;
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate(){
        switch (location) {
            case "net":
                return (Achievements.data_match_AlgaeInNet >= threshold);
            case "processor":
                return (Achievements.data_match_AlgaeInProcessor >= threshold);
        }

        return false;
    }
}