package com.team3663.scouting_app.utility.achievements;

public class RuleOrphanEvents implements AchievementRule {
    private final long threshold;
    private final boolean perMatch;

    public RuleOrphanEvents(long in_Threshold, boolean in_perMatch){
        threshold = in_Threshold;
        perMatch = in_perMatch;
    }

    @Override
    public boolean evaluate(){
        if (perMatch) return (Achievements.data_match_OrphanEvents >= threshold);
        else return (Achievements.data_OrphanEvents >= threshold);
    }
}
