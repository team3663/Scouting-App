package com.team3663.scouting_app.utility.achievements;

public class RuleMatchType implements AchievementRule {
    private final int threshold;
    private final String matchType;

    public RuleMatchType(String in_matchType, int in_Threshold){
        matchType = in_matchType;
        threshold = in_Threshold;
    }

    @Override
    public boolean evaluate(){
        switch (matchType) {
            case "p":
                return (Achievements.data_PracticeType >= threshold);
            case "s":
                return (Achievements.data_SemiFinalType >= threshold);
            case "f":
                return (Achievements.data_FinalType >= threshold);
        }

        return false;
    }
}