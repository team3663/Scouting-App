package com.team3663.scouting_app.config;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {
    public static class Logger {
        // Logger Keys - Data File is written in order of the FILE_HEADER array
        public static final String[] LOGKEY_DATA_FILE_HEADER = new String[]{"Shadow", "Team", "TeamScouting", "Scouter", "DidPlay", "StartGamePiece", "DidLeaveStart", "Comments", "Achievements", "StartOffset", "Start"};
        public static final String LOGKEY_SHADOW_MODE = "Shadow";
        public static final String LOGKEY_ACHIEVEMENT = "Achievements";
        public static final String LOGKEY_MATCH_TYPE = "MatchType";
        public static final String LOGKEY_DID_PLAY = "DidPlay";
        public static final String LOGKEY_TEAM_SCOUTING = "TeamScouting";
        public static final String LOGKEY_TEAM_TO_SCOUT = "Team";
        public static final String LOGKEY_SCOUTER = "Scouter";
        public static final String LOGKEY_DID_LEAVE_START = "DidLeaveStart";
        public static final String LOGKEY_START_WITH_GAME_PIECE = "StartGamePiece";
        public static final String LOGKEY_COMMENTS = "Comments";
        public static final String LOGKEY_START_TIME_OFFSET = "StartOffset";
        public static final String LOGKEY_START_TIME = "Start";
        public static final String FILE_LINE_SEPARATOR = "\r\n";
    }

    public static class Phases {
        public static final String AUTO = "AUTO";
        public static final String TELEOP = "TELEOP";
        public static final String NONE = "NONE";
    }

    public static class PreMatch {
        public static final int NUMBER_OF_MATCH_TYPES = 4;
        public static final String DEFAULT_MATCH_TYPE = "q";
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
        public static int IMAGE_HEIGHT = 0;
        public static int IMAGE_WIDTH = 0;
        public static int STATUS_TEXT_LONG_LENGTH = 22;
        public static int STATUS_TEXT_MED_LENGTH = 14;
        public static int STATUS_TEXT_LONG_SIZE = 14;
        public static int STATUS_TEXT_MED_SIZE = 18;
        public static int STATUS_TEXT_DEFAULT_SIZE = 24;
        public static float START_LINE_X = 43.20f;
    }

    public static class Data {
        public static final String PRIVATE_BASE_DIR = "input_data";
        public static final String PUBLIC_BASE_DIR = "CPR-Scouting";
        public static final String PUBLIC_INPUT_DIR = "Input";
        public static final String PUBLIC_OUTPUT_DIR = "Output";
    }

    public static class Events {
        public static final int ID_DEFENDED_START = 60;
        public static final int ID_DEFENDED_END = 61;
        public static final int ID_DEFENSE_START = 62;
        public static final int ID_DEFENSE_END = 63;
        public static final int ID_NOT_MOVING_START = 64;
        public static final int ID_NOT_MOVING_END = 65;
        public static final int ID_AUTO_START_GAME_PIECE = 0;
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
        public static final String STORAGE_URI = "StorageURI";
        public static final String PREF_ORIENTATION = "PreferredFieldOrientation";
        public static final String QR_SIZE = "PreferredQRSize";
    }

    public static class Settings {
        public static final String[] PREF_TEAM_POS = new String[]{"No Preference", "Blue 1", "Blue 2", "Blue 3", "Red 1", "Red 2", "Red 3"};
        public static final String[] PREF_FIELD_ORIENTATION = new String[]{"Automatic", "Blue On Left", "Red On Left"};
        public static final String RELOAD_DATA_KEY = "ReloadData";
    }

    public static class AppLaunch {
        public static final long SPLASH_SCREEN_DELAY = 10;
    }

    public static class QRCode{
        public static final int QR_LENGTH = 500;
        public static final String EOF = "EOF";
    }

    public static class Achievements {
        public static final int START_DELAY = 500;
        public static final int DISPLAY_TIME = 3_000;
        public static final int IN_BETWEEN_DELAY = 1_000;
        public static final ArrayList<Integer> EVENT_IDS_PICKUP_ALGAE = new ArrayList<>(Arrays.asList(8, 28));
        public static final ArrayList<Integer> EVENT_IDS_SCORE_ALGAE_IN_NET = new ArrayList<>(Arrays.asList(10, 30));
        public static final ArrayList<Integer> EVENT_IDS_SCORE_ALGAE_IN_PROCESSOR = new ArrayList<>(Arrays.asList(12, 32));
        public static final ArrayList<Integer> EVENT_IDS_PICKUP_CORAL_GROUND = new ArrayList<>(Arrays.asList(2, 21));
        public static final ArrayList<Integer> EVENT_IDS_PICKUP_CORAL_STATION = new ArrayList<>(Arrays.asList(1, 20));
        public static final ArrayList<Integer> EVENT_IDS_DROP_CORAL = new ArrayList<>(Arrays.asList(7, 27));
        public static final ArrayList<Integer> EVENT_IDS_PLACE_CORAL = new ArrayList<>(Arrays.asList(3, 4, 5, 6, 23, 24, 25, 26));
        public static final ArrayList<Integer> EVENT_IDS_SCORING = new ArrayList<>(Arrays.asList(3, 4, 5, 6, 10, 12, 23, 24, 25, 26, 30, 32));
        public static final int EVENT_ID_CLIMB_SUCCESS = 41;
        public static final int DEFENDED_EVENT_GROUP = 4;
        public static final String EVENT_TYPE_PRACTICE = "P";
        public static final String EVENT_TYPE_SEMI = "S";
        public static final String EVENT_TYPE_FINAL = "F";
        public static final ArrayList<Integer> COMPETITION_IDS_WORLDS = new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 17));
        public static final ArrayList<Integer> COMPETITION_IDS_DCMP = new ArrayList<>(List.of(9));
        public static final ArrayList<Integer> COMPETITION_IDS_EINSTEIN = new ArrayList<>(List.of(17));
    }
}