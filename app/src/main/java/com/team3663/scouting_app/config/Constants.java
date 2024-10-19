package com.team3663.scouting_app.config;

import android.graphics.Color;

public class Constants {

    public static class Logger {
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
        public static final String LOGKEY_START_TIME = "D_Start";
        public static final String LOGKEY_EVENT_KEY = "E_Key";
        public static final String LOGKEY_EVENT_SEQ = "E_Seq";
        public static final String LOGKEY_EVENT_ID = "E_ID";
        public static final String LOGKEY_EVENT_TIME = "E_Time";
        public static final String LOGKEY_EVENT_X = "E_X";
        public static final String LOGKEY_EVENT_Y = "E_Y";
        public static final String LOGKEY_EVENT_PREVIOUS_SEQ = "E_PrevSeq";
    }

    public static class Teams {
        public static final String NO_TEAM = "No Team Exists"; // use to check if no team exists for a given team number
    }

    public static class Matches {
        public static final String NO_MATCH = "No Match"; // use to check if no match info exists for a given match number
    }

    public static class Phases {
        public static final String AUTO = "Auto";
        public static final String TELEOP = "Teleop";
        public static final String NONE = "";
    }

    public static class Match {
        public static final int TIMER_AUTO_LENGTH = 15; // in seconds
        public static final int TIMER_TELEOP_LENGTH = 135; // in seconds
        public static final int TIMER_UPDATE_RATE = 1_000; // in milliseconds
        public static final int TIMER_AUTO_TELEOP_DELAY = 3; // in seconds
        public static final int BUTTON_FLASH_INTERVAL = 2_000; // in milliseconds
        public static final int BUTTON_FLASH_BLINK_INTERVAL = 250; // in milliseconds
        public static final int BUTTON_COLOR_FLASH = Color.RED;
        public static final int BUTTON_COLOR_NORMAL = Color.TRANSPARENT;
        public static final int BUTTON_TEXT_COLOR_DISABLED = Color.LTGRAY;
        public static final String ORIENTATION_LANDSCAPE = "l";
        public static final String ORIENTATION_LANDSCAPE_REVERSE = "lr";
        public static int IMAGE_HEIGHT;
        public static int IMAGE_WIDTH;
    }

    public static class Data {
        public static final int ID_START_POS_DEFAULT = 0;
    }

    public static class Events {
        public static final int ID_DEFENDED_START = 150;
        public static final int ID_DEFENDED_END = 151;
        public static final int ID_DEFENSE_START = 152;
        public static final int ID_DEFENSE_END = 153;
        public static final int ID_NOT_MOVING_START = 154;
        public static final int ID_NOT_MOVING_END = 155;
        public static final int ID_AUTO_STARTNOTE = 0;
        public static final int ID_NO_EVENT = 999;   // use to check if the eventID you're looking for doesn't exist
    }

    public static class Prefs {
        // Shared Preferences Keys
        public static final String COMPETITION_ID = "CompetitionId";
        public static final String DEVICE_ID = "DeviceId";
        public static final String SCOUTING_TEAM = "ScoutingTeam";
        public static final String NUM_MATCHES = "NumberOfMatches";
        public static final String COLOR_CONTEXT_MENU = "ColorContextMenu";
        public static final String PREF_TEAM_POS = "PreferredTeamPosition";
    }

    public static class Settings {
        public static final String[] PREF_TEAM_POS = new String[]{"No Preference", "Blue 1", "Blue 2", "Blue 3", "Red 1", "Red 2", "Red 3"};
    }

    public static class AppLaunch {
        public static final long SPLASH_SCREEN_DELAY = 100;
    }
}