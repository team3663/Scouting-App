package com.team3663.scouting_app.utility.achievements;

import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;

import java.util.ArrayList;
import java.util.HashMap;

// =================================================================================================
// Class:       Achievements
// Description: Everything related to scouter achievements
// =================================================================================================
public class Achievements {
    private static final ArrayList<Achievement> achievement_list = new ArrayList<>();

    // Scouter data
    public static int data_NumMatches = 0;
    public static String data_TeamToScout = "";
    public static long data_StartTime = 0;
    public static int data_NumEvents = 0;
    public static int data_IdleTime = 0;
    public static int data_PracticeType = 0;
    public static int data_SemiFinalType = 0;
    public static int data_FinalType = 0;
    public static int data_Toggle_Defense = 0;
    public static int data_Toggle_Defended = 0;
    public static int data_Toggle_NotMoving = 0;
    public static int data_ScoreWhileDefended = 0;
    public static int data_FieldReset = 0;
    public static HashMap<Integer, Integer> data_NumMatchesByCompetition = new HashMap<>();

    // Scouter data per match
    public static int data_match_FuelShoot = 0;
    public static int data_match_FuelShootWithAccuracy;
    public static int data_match_FuelPassTotal = 0;
    public static int data_match_Toggle_NotMoving = 0;
    public static int data_match_ClimbSuccessAuto = 0;
    public static int data_match_ClimbSuccessTele = 0;
    public static int data_match_FuelPassAlliance = 0;
    public static int data_match_FuelPassNeutral = 0;
    public static int data_match_FuelPassOpponent = 0;
    public static int data_match_FuelPickUpOutpost = 0;
    public static int data_match_FuelPickUpDepot = 0;
    public static int data_match_ClimbSuccessTeleL3 = 0;

    // Constructor: Define all of the achievements and the rule(s) they are based on
    public Achievements() {
        Achievement ach1 = new Achievement(1, "Getting the hang of it", "Scouted 2 matches in a row", 10);
        ach1.addRule(new RuleNumMatches(2));
        achievement_list.add(ach1);

        Achievement ach2 = new Achievement(2, "Know thyself", "You just scouted team " + Globals.CurrentScoutingTeam, 15);
        ach2.addRule(new RuleTeamScouted(Globals.CurrentScoutingTeam));
        achievement_list.add(ach2);

        Achievement ach3 = new Achievement(3, "Sore in the saddle", "Scouted for 30 minutes non-stop", 30);
        ach3.addRule(new RuleTimeScouting(1_800_000)); // 30 minutes
        achievement_list.add(ach3);

        Achievement ach4 = new Achievement(4, "Master scouter", "Scouted 15 matches in a row", 100);
        ach4.addRule(new RuleNumMatches(15));
        achievement_list.add(ach4);

        Achievement ach5 = new Achievement(5, "Practice makes perfect", "You scouted a practice match", 5);
        ach5.addRule(new RuleMatchType("p", 1));
        achievement_list.add(ach5);

        Achievement ach6 = new Achievement(6, "Semi-good scouting", "You scouted a semi-final match", 5);
        ach6.addRule(new RuleMatchType("s", 1));
        achievement_list.add(ach6);

        Achievement ach7 = new Achievement(7, "Finalist", "You scouted a finals match", 5);
        ach7.addRule(new RuleMatchType("f", 1));
        achievement_list.add(ach7);

        Achievement ach8 = new Achievement(8, "Buddies with the enemy", "Played defense 5 times", 10);
        ach8.addRule(new RuleToggles("defense", false, 5));
        achievement_list.add(ach8);

        Achievement ach9 = new Achievement(9, "Not on my watch", "Scored while being defended", 50);
        ach9.addRule(new RuleScoreDefended(1));
        achievement_list.add(ach9);

        Achievement ach10 = new Achievement(10, "Mater! I need a lift", "Broken down more than once", 5);
        ach10.addRule(new RuleToggles("not moving", false, 2));
        achievement_list.add(ach10);

        Achievement ach11 = new Achievement(11, "It's not my fault", "Experienced a field reset", 5);
        ach11.addRule(new RuleFieldReset(1));
        achievement_list.add(ach11);

        Achievement ach12 = new Achievement(12, "Maybe it IS my fault", "Experienced 2 field resets", 10);
        ach12.addRule(new RuleFieldReset(2));
        achievement_list.add(ach12);

        Achievement ach13 = new Achievement(13, "This seat is mine now", "Scouted for 60 minutes non-stop", 30);
        ach13.addRule(new RuleTimeScouting(3_600_000)); // 60 minutes
        achievement_list.add(ach13);

        Achievement ach14 = new Achievement(14, "Swiss Army Bot", "This bot did everything", 75);
        ach14.addRule(new RulePassFuel("neutral", 1));
        ach14.addRule(new RulePassFuel("opponent", 1));
        ach14.addRule(new RuleShootFuel("none", 1));
        ach14.addRule(new RulePickUpFuel("outpost", 1));
        ach14.addRule(new RulePickUpFuel("depot", 1));
        ach14.addRule(new RuleClimbed("auto", 1));
        ach14.addRule(new RuleClimbed("tele", 1));
        ach14.addRule(new RuleDefense(1));
        achievement_list.add(ach14);

        Achievement ach15 = new Achievement(15, "World Renowned Scouter", "Scouted at Worlds", 10);
        ach15.addRule(new RuleCompetition(Constants.Achievements.COMPETITION_IDS_WORLDS, 1));
        achievement_list.add(ach15);

        Achievement ach16 = new Achievement(16, "Did you get my good side?", "Video scouted a match", 10);
        ach16.addRule(new RuleAttendedCompetition(false, 1));
        achievement_list.add(ach16);

        Achievement ach17 = new Achievement(17, "Nobel Peace Prize winner", "Scouted an Einstein match", 200);
        ach17.addRule(new RuleCompetition(Constants.Achievements.COMPETITION_IDS_EINSTEIN, 1));
        achievement_list.add(ach17);

        Achievement ach18 = new Achievement(18, "Driving Cars Makes Patience", "Scouted at DCMP", 10);
        ach18.addRule(new RuleCompetition(Constants.Achievements.COMPETITION_IDS_DCMP, 1));
        achievement_list.add(ach18);

        // #19 - Count Dracula -> gSheet Only
        // #20 - Count-a-holic -> gSheet Only
        // #21 - Count Master -> gSheet Only
        // #22 - In only counts once -> gSheet Only

        Achievement ach23 = new Achievement(23, "All Around Passer", "Passed 250 fuel in a match", 10);
        ach23.addRule(new RulePassFuel("everywhere", 250));
        ach23.addRule(new RulePassFuel("neutral", 1));
        ach23.addRule(new RulePassFuel("opponent", 1));
        achievement_list.add(ach23);

        Achievement ach24 = new Achievement(24, "The Floor is Lava", "Climbed in both auto and tele", 5);
        ach24.addRule(new RuleClimbed("auto", 1));
        ach24.addRule(new RuleClimbed("tele", 1));
        achievement_list.add(ach24);

        Achievement ach25 = new Achievement(25, "Up, Up, and Away!", "Maxed out climbs in a match", 10);
        ach25.addRule(new RuleClimbed("auto", 1));
        ach25.addRule(new RuleClimbed("teleL3", 1));
        achievement_list.add(ach25);

        Achievement ach26 = new Achievement(26, "Fuel Efficiency", "Accurately shot 300 fuel", 10);
        ach26.addRule(new RuleShootFuel("accuracy", 300));
        achievement_list.add(ach26);
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
    public void clearAllData() {
        data_NumMatches = 0;
        data_TeamToScout = "";
        data_StartTime = 0;
        data_NumEvents = 0;
        data_IdleTime = 0;
        data_PracticeType = 0;
        data_SemiFinalType = 0;
        data_FinalType = 0;
        data_Toggle_Defense = 0;
        data_Toggle_Defended = 0;
        data_Toggle_NotMoving = 0;
        data_ScoreWhileDefended = 0;
        data_FieldReset = 0;
        data_NumMatchesByCompetition.put(Globals.CurrentCompetitionId, 0);

        clearMatchData();
    }

    // Member Function: Clear all data on achievements (ie: new scouter)
    public void clearMatchData() {
        data_match_Toggle_NotMoving = 0;
        data_match_FuelShoot = 0;
        data_match_FuelShootWithAccuracy = 0;
        data_match_FuelPassTotal = 0;
        data_match_FuelPassAlliance = 0;
        data_match_FuelPassNeutral = 0;
        data_match_FuelPassOpponent = 0;
        data_match_ClimbSuccessAuto = 0;
        data_match_ClimbSuccessTele = 0;
        data_match_ClimbSuccessTeleL3 = 0;
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
