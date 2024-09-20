package com.cpr3663.cpr_scouting_app;

public class Constants {
    public static final String NO_TEAM = "No Team Exists"; // use to check if no team exists for a given team number
    public static final String NO_MATCH = "No Match"; // use to check if no match info exists for a given match number
    public static final String NO_COMPETITION = "No Competition Name Exists"; // use to check if no competition info exists for a given competition id
    public static final int NO_EVENT = 999; // use to check if the eventID you're looking for doesn't exist

    public static final String PHASE_AUTO = "Auto";
    public static final String PHASE_TELEOP = "Teleop";
    public static final String PHASE_NONE = "";

    // Logger Keys - These are also the csv column header names
    public static final String LOGKEY_DID_PLAY = "D_DidPlay";
    public static final String LOGKEY_START_POSITION = "D_StartPos";
    public static final String LOGKEY_TEAM_SCOUTING = "D_TeamScouting";
    public static final String LOGKEY_TEAM_TO_SCOUT = "D_Team";
    public static final String LOGKEY_SCOUTER = "D_Scouter";
    public static final String LOGKEY_DID_LEAVE_START = "D_DidLeaveStart";
    public static final String LOGKEY_CLIMB_POSITION = "D_ClimbPos";
    public static final String LOGKEY_TRAP = "D_Trap";
    public static final String LOGKEY_COMMENTS = "D_Comments";
    public static final String LOGKEY_START_TIME_OFFSET = "D_StartOffset";
    public static final String LOGKEY_DATA_KEY = "D_Key";
    public static final String LOGKEY_EVENT_KEY = "E_Key";
    public static final String LOGKEY_EVENT_SEQ = "E_Seq";
    public static final String LOGKEY_EVENT_ID = "E_ID";
    public static final String LOGKEY_EVENT_TIME = "E_Time";
    public static final String LOGKEY_EVENT_X = "E_X";
    public static final String LOGKEY_EVENT_Y = "E_Y";
    public static final String LOGKEY_EVENT_PREVIOUS_SEQ = "E_PrevSeq";

    public static final int DATA_ID_START_POS_DEFAULT = 0;

    public static final int EVENT_ID_DEFENDED_START = 150;
    public static final int EVENT_ID_DEFENDED_END = 151;
    public static final int EVENT_ID_DEFENSE_START = 152;
    public static final int EVENT_ID_DEFENSE_END = 153;
    public static final int EVENT_ID_AUTO_STARTNOTE = 0;

    // Shared Preferences Keys
    public static final String SP_COMPETITION_ID = "CompetitionId";
    public static final String SP_DEVICE_ID = "DeviceId";
    public static final String SP_SCOUTING_TEAM = "ScoutingTeam";
    public static final String SP_NUM_MATCHES = "NumberOfMatches";
    public static final String SP_COLOR_CONTEXT_MENU = "ColorContextMenu";
}
