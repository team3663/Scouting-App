package com.team3663.scouting_app.utility.achievements;

public class RuleTeamScouted implements AchievementRule {
    private final int team;

    public RuleTeamScouted(int in_Team){
        team = in_Team;
    }

    @Override
    public boolean evaluate(){
        return ((Achievements.data_TeamToScout > 0) && (Achievements.data_TeamToScout == team));
    }
}
