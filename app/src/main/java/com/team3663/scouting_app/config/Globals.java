package com.team3663.scouting_app.config;

import android.content.SharedPreferences;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.team3663.scouting_app.data.Accuracy;
import com.team3663.scouting_app.data.ClimbLevel;
import com.team3663.scouting_app.data.ClimbPosition;
import com.team3663.scouting_app.data.MatchTypes;
import com.team3663.scouting_app.utility.Logger;
import com.team3663.scouting_app.data.Colors;
import com.team3663.scouting_app.data.Comments;
import com.team3663.scouting_app.data.Competitions;
import com.team3663.scouting_app.data.Devices;
import com.team3663.scouting_app.data.Events;
import com.team3663.scouting_app.data.Matches;
import com.team3663.scouting_app.utility.achievements.Achievements;

import java.util.HashMap;

public class Globals {
    public static HashMap<String, String> TeamList = new HashMap<>();
    public static Competitions CompetitionList = new Competitions();
    public static MatchTypes MatchTypeList = new MatchTypes();
    public static Matches MatchList = new Matches();
    public static Accuracy AccuracyTypeList = new Accuracy();
    public static String CurrentAccuracy = Constants.PostMatch.ACCURACY;
    public static ClimbLevel ClimbLevelList = new ClimbLevel();
    public static String CurrentClimbLevel = Constants.PostMatch.CLIMB_LEVEL;
    public static ClimbPosition ClimbPositionList = new ClimbPosition();
    public static String CurrentClimbPosition = Constants.PostMatch.CLIMB_POSITION;
    public static Devices DeviceList = new Devices();
    public static Events EventList = new Events();
    public static Comments CommentList = new Comments();
    public static Colors ColorList = new Colors();

    public static int CurrentCompetitionId;
    public static int CurrentMatchNumber;
    public static int CurrentDeviceId;
    public static int CurrentColorId;
    public static int CurrentPrefTeamPos;
    public static int CurrentQRSize;
    public static int CurrentFieldOrientationPos;
    public static String CurrentMatchPhase = Constants.Phases.NONE;
    public static String CurrentScoutingTeam;
    public static String CurrentTeamOverrideNum;
    public static String CurrentTeamToScout;
    public static String CurrentMatchType = Constants.PreMatch.DEFAULT_MATCH_TYPE;

    public static String CheckBoxTextPadding = "       ";

    public static Logger EventLogger = null;
    public static Achievements myAchievements;
    public static boolean isDefended = false;

    public static int NumberMatchFilesKept;
    public static int MaxEventGroups = 0;

    public static SharedPreferences sp;
    public static SharedPreferences.Editor spe;
    public static Uri baseStorageURI = null;

    public static boolean isPractice = false;
    public static int numStartingGamePiece = Constants.PreMatch.STARTING_GAME_PIECES;
    public static String stealFuelValue = Constants.PostMatch.STEAL_FUEL;
    public static String affectedByDefenseValue = Constants.PostMatch.AFFECTED_BY_DEFENSE;
    public static boolean isShadowMode = false;

    public static DocumentFile base_df = null;
    public static DocumentFile input_df = null;
    public static DocumentFile output_df = null;
    public static HashMap<String, Long> FileList = new HashMap<>();

    public static int TransmitMatchNum;
    public static String TransmitMatchType;
}
