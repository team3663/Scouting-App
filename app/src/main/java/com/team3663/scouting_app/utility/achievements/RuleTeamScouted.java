package com.team3663.scouting_app.utility.achievements;

public class RuleTeamScouted implements AchievementRule {
    private final String team;

    public RuleTeamScouted(String in_Team){
        team = in_Team;
    }

    @Override
    public boolean evaluate(){
        return ((!Achievements.data_TeamToScout.isEmpty()) && (Achievements.data_TeamToScout.equals(team)));
    }
}
