package com.cpr3663.cpr_scouting_app;

import android.content.SharedPreferences;

import com.cpr3663.cpr_scouting_app.data.ClimbPositions;
import com.cpr3663.cpr_scouting_app.data.Colors;
import com.cpr3663.cpr_scouting_app.data.Comments;
import com.cpr3663.cpr_scouting_app.data.Competitions;
import com.cpr3663.cpr_scouting_app.data.Devices;
import com.cpr3663.cpr_scouting_app.data.Events;
import com.cpr3663.cpr_scouting_app.data.Matches;
import com.cpr3663.cpr_scouting_app.data.StartPositions;
import com.cpr3663.cpr_scouting_app.data.TrapResults;

import java.util.ArrayList;

public class Globals {
    public static ArrayList<String> TeamList = new ArrayList<String>();
    public static Competitions CompetitionList = new Competitions();
    public static Matches MatchList = new Matches();
    public static Devices DeviceList = new Devices();
    public static Events EventList = new Events();
    public static Comments CommentList = new Comments();
    public static ClimbPositions ClimbPositionList = new ClimbPositions();
    public static TrapResults TrapResultsList = new TrapResults();
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

    public static String CheckBoxTextPadding = "       ";

    public static Logger EventLogger = null;

    public static int NumberMatchFilesKept;

    public static SharedPreferences sp;
    public static SharedPreferences.Editor spe;

    public static boolean NeedToLoadData = true;
    public static boolean isPractice = false;
}
