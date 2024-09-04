package com.cpr3663.cpr_scouting_app;

public class Constants {
    public static final String NO_TEAM = "No Team Exists"; // use to check if no team exists for a given team number
    public static final String NO_MATCH = "No Match"; // use to check if no match info exists for a given match number
    public static final String NO_COMPETITION = "No Competition Name Exists"; // use to check if no competition info exists for a given competition id
    public static final int NO_EVENT = 999; // use to check if the eventID you're looking for doesn't exist

    public static final String PHASE_AUTO = "Auto";
    public static final String PHASE_TELEOP = "Teleop";
    public static final String PHASE_NONE = "";

    public static final int KEEP_NUMBER_OF_MATCHES = 5;
    public static final String LOGKEY_DID_PLAY = "DidPlay";
    public static final String LOGKEY_START_POSITION = "StartPos";
    public static final String LOGKEY_TEAM_SCOUTING = "TeamScouting";
    public static final String LOGKEY_TEAM_TO_SCOUT = "Team";
    public static final String LOGKEY_SCOUTER = "Scouter";
    public static final String LOGKEY_DID_LEAVE_START = "DidLeaveStart";
    public static final String LOGKEY_CLIMB_POSITION = "ClimbPos";
    public static final String LOGKEY_TRAP = "Trap";
    public static final String LOGKEY_DNPS = "DNP";
    public static final String LOGKEY_COMMENTS = "Comments";

    public static final int EVENT_ID_DEFENDED_START = 150;
    public static final int EVENT_ID_DEFENDED_END = 151;
    public static final int EVENT_ID_DEFENSE_START = 152;
    public static final int EVENT_ID_DEFENSE_END = 153;
}
