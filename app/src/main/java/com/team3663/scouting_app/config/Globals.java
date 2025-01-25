package com.team3663.scouting_app.config;

import android.content.SharedPreferences;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.team3663.scouting_app.data.MatchTypes;
import com.team3663.scouting_app.utility.Logger;
import com.team3663.scouting_app.data.ClimbPositions;
import com.team3663.scouting_app.data.Colors;
import com.team3663.scouting_app.data.Comments;
import com.team3663.scouting_app.data.Competitions;
import com.team3663.scouting_app.data.Devices;
import com.team3663.scouting_app.data.Events;
import com.team3663.scouting_app.data.Matches;
import com.team3663.scouting_app.data.StartPositions;
import com.team3663.scouting_app.utility.achievements.Achievements;

import java.util.ArrayList;

public class Globals {
    public static ArrayList<String> TeamList = new ArrayList<String>();
    public static Competitions CompetitionList = new Competitions();
    public static MatchTypes MatchTypeList = new MatchTypes();
    public static Matches MatchList = new Matches();
    public static Devices DeviceList = new Devices();
    public static Events EventList = new Events();
    public static Comments CommentList = new Comments();
    public static ClimbPositions ClimbPositionList = new ClimbPositions();
    public static StartPositions StartPositionList = new StartPositions();
    public static Colors ColorList = new Colors();

    public static int CurrentCompetitionId;
    public static int CurrentMatchNumber;
    public static int CurrentScoutingTeam;
    public static int CurrentDeviceId;
    public static int CurrentStartPosition;
    public static int CurrentColorId;
    public static int CurrentTeamOverrideNum;
    public static int CurrentPrefTeamPos;
    public static int CurrentTeamToScout;
    public static String CurrentMatchPhase;
    public static int CurrentMatchType = Constants.PreMatch.DEFAULT_MATCH_TYPE;

    public static String CheckBoxTextPadding = "       ";

    public static Logger EventLogger = null;
    public static Achievements myAchievements;

    public static int NumberMatchFilesKept;
    public static int MaxEventGroups = 0;

    public static SharedPreferences sp;
    public static SharedPreferences.Editor spe;
    public static Uri baseStorageURI = null;

    public static boolean isPractice = false;
    public static boolean isStartingGamePiece = true;
    public static boolean isShadowMode = false;

    public static long startTime = 0;

    public static DocumentFile base_df = null;
    public static DocumentFile input_df = null;
    public static DocumentFile output_df = null;

    public static int transmitMatchNum;
    public static int transmitMatchType;
    public static String deviceAddress;
}
