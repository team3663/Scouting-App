package com.team3663.scouting_app.config;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.team3663.scouting_app.data.Accuracy;
import com.team3663.scouting_app.data.ClimbLevel;
import com.team3663.scouting_app.data.ClimbPosition;
import com.team3663.scouting_app.utility.Logger;
import com.team3663.scouting_app.utility.achievements.Achievements;
import com.team3663.scouting_app.utility.dataFile.*;

import java.util.HashMap;

public class Globals {
    @SuppressLint("StaticFieldLeak") public static TeamsFile TeamList;
    @SuppressLint("StaticFieldLeak") public static CompetitionsFile CompetitionList;
    @SuppressLint("StaticFieldLeak") public static ColorsFile ColorList;
    @SuppressLint("StaticFieldLeak") public static CommentsFile CommentList;
    @SuppressLint("StaticFieldLeak") public static DevicesFile DeviceList;
    @SuppressLint("StaticFieldLeak") public static MatchTypesFile MatchTypeList;
    @SuppressLint("StaticFieldLeak") public static MatchesFile MatchList;
    @SuppressLint("StaticFieldLeak") public static EventGroupsFile EventGroupList;
    @SuppressLint("StaticFieldLeak") public static EventsFile EventList;

    public static Accuracy AccuracyTypeList = new Accuracy();
    public static int CurrentAccuracy = Constants.PostMatch.ACCURACY_NOT_SELECTED;
    public static ClimbLevel ClimbLevelList = new ClimbLevel();
    public static String CurrentClimbLevel = Constants.PostMatch.CLIMB_LEVEL_NOT_SELECTED;
    public static ClimbPosition ClimbPositionList = new ClimbPosition();
    public static String CurrentClimbPosition = Constants.PostMatch.CLIMB_POSITION_NOT_SELECTED;

    public static int CurrentCompetitionId;
    public static int CurrentMatchNumber = 0;
    public static int CurrentDeviceId;
    public static int CurrentColorId;
    public static int CurrentPrefTeamPos;
    public static int CurrentQRSize;
    public static int CurrentFieldOrientationPos;
    public static String CurrentMatchPhase = Constants.Phases.NONE;
    public static String CurrentScoutingTeam;
    public static String CurrentOverrideTeamNum;
    public static String CurrentOverrideAlliance;
    public static String CurrentTeamToScout;
    public static String CurrentMatchType = Constants.PreMatch.DEFAULT_MATCH_TYPE;

    public static String CheckBoxTextPadding = "  ";

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
    public static String stealFuelValue = Constants.PostMatch.STEAL_FUEL_NOT_SELECTED;
    public static String affectedByDefenseValue = Constants.PostMatch.AFFECTED_BY_DEFENSE_NOT_SELECTED;
    public static boolean isShadowMode = false;

    public static DocumentFile base_df = null;
    public static DocumentFile input_df = null;
    public static DocumentFile output_df = null;
    public static HashMap<String, Long> FileList = new HashMap<>();

    public static int TransmitMatchNum;
    public static String TransmitMatchType;
}
