package com.team3663.scouting_app.utility.achievements;

import com.team3663.scouting_app.config.Globals;

import java.util.ArrayList;

// =================================================================================================
// Class:       Achievements
// Description: Everything related to scouter achievements
// =================================================================================================
public class Achievements {
    private static final ArrayList<Achievement> achievement_list = new ArrayList<>();
    // Scouter data
    public static int data_NumMatches = 0;
    public static int data_TeamToScout = 0;
    public static long data_StartTime = 0;
    public static int data_NumEvents = 0;
    public static int data_IdleTime = 0;
    public static int data_OrphanEvents = 0;
    // Scouter data per match
    public static int data_match_Toggles = 0;
    public static int data_match_OrphanEvents = 0;

    // Constructor: Define all of the achievements and the rule(s) they are based on
    public Achievements() {
        Achievement ach1 = new Achievement(1, "Getting the hang of it", "Scouted 2 matches in a row", 40);
        ach1.addRule(new RuleNumMatches(2));
        achievement_list.add(ach1);

        Achievement ach2 = new Achievement(2, "Know thyself", "You just scouted team " + Globals.CurrentScoutingTeam, 20);
        ach2.addRule(new RuleTeamScouted(Globals.CurrentScoutingTeam));
        achievement_list.add(ach2);

        Achievement ach3 = new Achievement(3, "Sore in the saddle", "Scouted for 30 minutes non-stop", 30);
        ach3.addRule(new RuleTimeScouting(1_800_000)); // 30 minutes
        achievement_list.add(ach3);

        Achievement ach4 = new Achievement(4, "Master scouter", "Scouted 15 matches in a row", 100);
        ach4.addRule(new RuleNumMatches(15));
        achievement_list.add(ach4);

        Achievement ach5 = new Achievement(5, "Calloused fingers", "Entered 300 events", 75);
        ach5.addRule(new RuleNumEvents(300));
        achievement_list.add(ach5);

        Achievement ach6 = new Achievement(6, "Orphan Annie", "You left an event incomplete (orphan)", 5);
        ach6.addRule(new RuleOrphanEvents(1, true));
        achievement_list.add(ach6);

        Achievement ach7 = new Achievement(7, "Orphanage", "You left 10 events incomplete (orphan)", 15);
        ach7.addRule(new RuleOrphanEvents(10, false));
        achievement_list.add(ach7);
    }

    // Member Function: pop (to the screen) any achievements "met" but not already "popped"
    public static ArrayList<PoppedAchievement> popAchievements() {
        ArrayList<PoppedAchievement> ret = new ArrayList<>();
        for (Achievement a : achievement_list) {
            a.evaluate();
            if (a.met && !a.popped) {
                a.popped = true;

                ret.add(new PoppedAchievement(a.id, a.reward + " - " + a.title, a.description));
            }
        }

        return ret;
    }

    // Member Function: Clear all data on achievements (ie: new scouter)
    public void clearAllData(){
        data_NumMatches = 0;
        data_TeamToScout = 0;
        data_StartTime = 0;
        data_NumEvents = 0;
        data_IdleTime = 0;
        data_OrphanEvents = 0;

        clearMatchData();
    }

    // Member Function: Clear all data on achievements (ie: new scouter)
    public void clearMatchData(){
        data_match_Toggles = 0;
        data_match_OrphanEvents = 0;
    }

    // =============================================================================================
    // Class:       Achievement
    // Description: Everything about a single achievement
    // =============================================================================================
     public static class Achievement {
        private final ArrayList<AchievementRule> rules = new ArrayList<>();
        private final int id;
        private final String title;
        private final String description;
        private boolean met;
        private boolean popped;
        private final int reward;

        private Achievement(int in_id, String in_title, String in_description, int in_reward) {
            id = in_id;
            title = in_title;
            description = in_description;
            met = false;
            popped = false;
            reward = in_reward;
        }

        private void addRule(AchievementRule in_rule) {
            rules.add(in_rule);
        }

        private void evaluate() {
            boolean rc = true;
            for (AchievementRule ar : rules) {
                if (!ar.evaluate()) rc = false;
            }

            met = rc;
        }
    }

    // =============================================================================================
    // Class:       PoppedAchievement
    // Description: A simple object for a popped achievement
    // =============================================================================================
    public static class PoppedAchievement {
        public int id;
        public String title;
        public String description;

        public PoppedAchievement(int in_id, String in_title, String in_description) {
            id = in_id;
            title = in_title;
            description = in_description;
        }
    }
}
