package com.team3663.scouting_app.utility.achievements;

import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;

import java.util.ArrayList;
import java.util.Arrays;

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
    public static int data_PracticeType = 0;
    public static int data_SemiFinalType = 0;
    public static int data_FinalType = 0;
    public static int data_Toggle_Defense = 0;
    public static int data_Toggle_Defended = 0;
    public static int data_Toggle_NotMoving = 0;
    public static int data_ScoreWhileDefended = 0;
    public static int data_FieldReset = 0;
    public static int[] data_NumMatchesByCompetition = new int[Globals.CompetitionList.size() + 1];

    // Scouter data per match
    public static int data_match_OrphanEvents = 0;
    public static int data_match_AlgaeInNet = 0;
    public static int data_match_AlgaeInProcessor = 0;
    public static int data_match_AlgaePickup = 0;
    public static int[] data_match_CoralLevel = {0, 0, 0, 0, 0};
    public static int data_match_CoralPickupGround = 0;
    public static int data_match_CoralPickupStation = 0;
    public static int data_match_CoralDropped = 0;
    public static int data_match_Toggle_NotMoving = 0;
    public static int data_match_ClimbSuccess = 0;

    // Constructor: Define all of the achievements and the rule(s) they are based on
    public Achievements() {
        Globals.DebugLogger.In("Achievements:constructor");

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

        Achievement ach5 = new Achievement(5, "Calloused fingers", "Entered 300 events", 75);
        ach5.addRule(new RuleNumEvents(300));
        achievement_list.add(ach5);

        Achievement ach6 = new Achievement(6, "Orphan Annie", "You left an event incomplete (orphan)", 5);
        ach6.addRule(new RuleOrphanEvents(1, true));
        achievement_list.add(ach6);

        Achievement ach7 = new Achievement(7, "Orphanage", "You left 10 events incomplete (orphan)", 10);
        ach7.addRule(new RuleOrphanEvents(10, false));
        achievement_list.add(ach7);

        Achievement ach8 = new Achievement(8, "Practice makes perfect", "You scouted a practice match", 5);
        ach8.addRule(new RuleMatchType("p", 1));
        achievement_list.add(ach8);

        Achievement ach9 = new Achievement(9, "Semi-good scouting", "You scouted a semi-final match", 5);
        ach9.addRule(new RuleMatchType("s", 1));
        achievement_list.add(ach9);

        Achievement ach10 = new Achievement(10, "Finalist", "You scouted a finals match", 5);
        ach10.addRule(new RuleMatchType("f", 1));
        achievement_list.add(ach10);

        Achievement ach11 = new Achievement(11, "Algae Hat Trick", "3 Algae scored in the net", 15);
        ach11.addRule(new RuleScoreAlgae("net", 3));
        achievement_list.add(ach11);

        Achievement ach12 = new Achievement(12, "Hand in hand", "Processed 2 Algae", 10);
        ach12.addRule(new RuleScoreAlgae("processor", 2));
        achievement_list.add(ach12);

        Achievement ach13 = new Achievement(13, "Hit all the elevator buttons", "Scored Coral on all Reef levels", 40);
        ach13.addRule(new RuleScoreCoral(1, 1));
        ach13.addRule(new RuleScoreCoral(2, 1));
        ach13.addRule(new RuleScoreCoral(3, 1));
        ach13.addRule(new RuleScoreCoral(4, 1));
        achievement_list.add(ach13);

        Achievement ach14 = new Achievement(14, "Janitorial duties", "Cleaned 5 Coral from the ground", 5);
        ach14.addRule(new RuleCoralPickup("ground", 5));
        achievement_list.add(ach14);

        Achievement ach15 = new Achievement(15, "Crossed the line", "Went on the defensive", 5);
        ach15.addRule(new RuleToggles("defense", true, 1));
        achievement_list.add(ach15);

        Achievement ach16 = new Achievement(16, "Buddies with the enemy", "Played defense 5 times", 10);
        ach16.addRule(new RuleToggles("defense", false, 5));
        achievement_list.add(ach16);

        Achievement ach17 = new Achievement(17, "Not on my watch", "Scored while being defended", 50);
        ach17.addRule(new RuleScoreDefended(1));
        achievement_list.add(ach17);

        Achievement ach18 = new Achievement(18, "Mater! I need a lift", "Broken down more than once", 5);
        ach18.addRule(new RuleToggles("not moving", false, 2));
        achievement_list.add(ach18);

        Achievement ach19 = new Achievement(19, "It's not my fault", "Experienced a field reset", 5);
        ach19.addRule(new RuleFieldReset(1));
        achievement_list.add(ach19);

        Achievement ach20 = new Achievement(20, "Maybe it IS my fault", "Experienced 2 field resets", 10);
        ach20.addRule(new RuleFieldReset(2));
        achievement_list.add(ach20);

        Achievement ach21 = new Achievement(21, "Top of the class", "Scored 6 Coral on top 2 levels each", 60);
        ach21.addRule(new RuleScoreCoral(3, 6));
        ach21.addRule(new RuleScoreCoral(4, 6));
        achievement_list.add(ach21);


        Achievement ach22 = new Achievement(22, "This seat is mine now", "Scouted for 60 minutes non-stop", 30);
        ach22.addRule(new RuleTimeScouting(3_600_000)); // 30 minutes
        achievement_list.add(ach22);

        Achievement ach23 = new Achievement(23, "Swiss Army Bot", "This bot did everything", 75);
        ach23.addRule(new RuleCoralPickup("ground", 1));
        ach23.addRule(new RuleCoralPickup("station", 1));
        ach23.addRule(new RuleAlgaePickup(1));
        ach23.addRule(new RuleScoreCoral(1, 1));
        ach23.addRule(new RuleScoreCoral(2, 1));
        ach23.addRule(new RuleScoreCoral(3, 1));
        ach23.addRule(new RuleScoreCoral(4, 1));
        ach23.addRule(new RuleScoreAlgae("processor", 1));
        ach23.addRule(new RuleScoreAlgae("net", 1));
        ach23.addRule(new RuleClimbed(1));
        achievement_list.add(ach23);

        Achievement ach24 = new Achievement(24, "World Renowned Scouter", "Scouted at Worlds", 10);
        ach24.addRule(new RuleCompetition(Constants.Achievements.COMPETITION_IDS_WORLDS, 1));
        achievement_list.add(ach24);

        Achievement ach25 = new Achievement(25, "Did you get my good side?", "Video scouted a match", 10);
        ach25.addRule(new RuleAttendedCompetition(false, 1));
        achievement_list.add(ach25);

        Achievement ach26 = new Achievement(26, "Nobel Peace Prize winner", "Scouted an Einstein match", 200);
        ach26.addRule(new RuleCompetition(Constants.Achievements.COMPETITION_IDS_EINSTEIN, 1));
        achievement_list.add(ach26);

        Achievement ach27 = new Achievement(27, "Butter Fingers", "Dropped 5 Coral in a match", 10);
        ach27.addRule(new RuleCoralDrop(5));
        achievement_list.add(ach27);

        // #28 - Achievement Hunter -> Tableau Only
        // #29 - In for a penny - in for a pound -> Tableau Only
        // #30 - In only counts once -> Tableau Only
        // #31 - Globe Trotters -> Tableau Only
        // #32 - Making this a career -> Tableau Only

        Achievement ach33 = new Achievement(33, "Driving Cars Makes Patience", "Scouted at DCMP", 10);
        ach33.addRule(new RuleCompetition(Constants.Achievements.COMPETITION_IDS_DCMP, 1));
        achievement_list.add(ach33);

        Globals.DebugLogger.Out();
    }

    // Member Function: pop (to the screen) any achievements "met" but not already "popped"
    public static ArrayList<PoppedAchievement> popAchievements() {
        Globals.DebugLogger.In("Achievements:popAchievements");

        ArrayList<PoppedAchievement> ret = new ArrayList<>();
        for (Achievement a : achievement_list) {
            a.evaluate();
            if (a.met && !a.popped) {
                a.popped = true;

                ret.add(new PoppedAchievement(a.id, a.reward + " - " + a.title, a.description));
            }
        }

        Globals.DebugLogger.Out();
        return ret;
    }

    // Member Function: Clear all data on achievements (ie: new scouter)
    public void clearAllData() {
        Globals.DebugLogger.In("Achievements:clearAllData");

        data_NumMatches = 0;
        data_TeamToScout = 0;
        data_StartTime = 0;
        data_NumEvents = 0;
        data_IdleTime = 0;
        data_OrphanEvents = 0;
        data_PracticeType = 0;
        data_SemiFinalType = 0;
        data_FinalType = 0;
        data_Toggle_Defense = 0;
        data_Toggle_Defended = 0;
        data_Toggle_NotMoving = 0;
        data_ScoreWhileDefended = 0;
        data_FieldReset = 0;
        data_NumMatchesByCompetition = new int[Globals.CompetitionList.size()];

        clearMatchData();

        Globals.DebugLogger.Out();
    }

    // Member Function: Clear all data on achievements (ie: new scouter)
    public void clearMatchData() {
        Globals.DebugLogger.In("Achievements:clearMatchData");

        data_match_Toggle_NotMoving = 0;
        data_match_OrphanEvents = 0;
        data_match_AlgaeInNet = 0;
        data_match_AlgaeInProcessor = 0;
        data_match_AlgaePickup = 0;
        Arrays.fill(data_match_CoralLevel, 0);
        data_match_CoralPickupGround = 0;
        data_match_CoralPickupStation = 0;
        data_match_CoralDropped = 0;
        data_match_ClimbSuccess = 0;

        Globals.DebugLogger.Out();
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
            Globals.DebugLogger.Params.add("in_id=" + in_id);
            Globals.DebugLogger.Params.add("in_title=" + in_title);
            Globals.DebugLogger.Params.add("in_description=" + in_description);
            Globals.DebugLogger.Params.add("in_reward=" + in_reward);
            Globals.DebugLogger.In("Achievements:Achievement:constructor");

            id = in_id;
            title = in_title;
            description = in_description;
            met = false;
            popped = false;
            reward = in_reward;

            Globals.DebugLogger.Out();
        }

        private void addRule(AchievementRule in_rule) {
            rules.add(in_rule);
        }

        private void evaluate() {
            Globals.DebugLogger.In("Achievments:Achievement:evaluate");

            boolean rc = true;
            for (AchievementRule ar : rules) {
                if (!ar.evaluate()) rc = false;
            }

            met = rc;

            Globals.DebugLogger.Out();
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
            Globals.DebugLogger.Params.add("in_id=" + in_id);
            Globals.DebugLogger.Params.add("in_title=" + in_title);
            Globals.DebugLogger.Params.add("in_description=" + in_description);
            Globals.DebugLogger.In("Achievments:PoppedAchievement:constructor");

            id = in_id;
            title = in_title;
            description = in_description;

            Globals.DebugLogger.Out();
        }
    }
}
