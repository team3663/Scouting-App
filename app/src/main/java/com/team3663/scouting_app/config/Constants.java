package com.team3663.scouting_app.config;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {
    public static class Logger {
        // Logger Keys - Data File is written in order of the FILE_HEADER array
        public static final String[] LOGKEY_DATA_FILE_HEADER = new String[]{"Shadow", "Team", "TeamScouting", "Scouter", "DidPlay", "StartGamePiece", "DidLeaveStart", "Accuracy", "ClimbLevel", "ClimbPosition", "Comments", "Achievements", "StartOffset", "Start"};
        public static final String LOGKEY_SHADOW_MODE = "Shadow";
        public static final String LOGKEY_ACHIEVEMENT = "Achievements";
        public static final String LOGKEY_MATCH_TYPE = "MatchType";
        public static final String LOGKEY_DID_PLAY = "DidPlay";
        public static final String LOGKEY_TEAM_SCOUTING = "TeamScouting";
        public static final String LOGKEY_TEAM_TO_SCOUT = "Team";
        public static final String LOGKEY_SCOUTER = "Scouter";
        public static final String LOGKEY_DID_LEAVE_START = "DidLeaveStart";
        public static final String LOGKEY_START_WITH_GAME_PIECE = "StartGamePiece";
        public static final String LOGKEY_ACCURACY = "Accuracy";
        public static final String LOGKEY_CLIMB_LEVEL = "ClimbLevel";
        public static final String LOGKEY_CLIMB_POSITION = "ClimbPosition";
        public static final String LOGKEY_STEALFUEL = "StealFuel";
        public static final String LOGKEY_AFFECTED_BY_DEFENSE = "AffectedByDefense";
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
        public static final String DEFAULT_MATCH_TYPE = "q";
        public static final int STARTING_GAME_PIECES = 8;
    }

    public static class Match {
        public static final int TIMER_AUTO_LENGTH = 15; // in seconds
        public static final int TIMER_TELEOP_LENGTH = 135; // in seconds
        public static final int TIMER_AUTO_TELEOP_DELAY = 3; // in seconds
        public static final int BUTTON_FLASH_INTERVAL = 2_000; // in milliseconds
        public static final int BUTTON_FLASH_BLINK_INTERVAL = 250; // in milliseconds
        public static final int BUTTON_FLASH_FADE_IN_TIME = 250; // in milliseconds
        public static final int BUTTON_COLOR_FLASH = Color.RED;
        public static final int BUTTON_COLOR_NORMAL = Color.TRANSPARENT;
        public static final int BUTTON_TEXT_COLOR_DISABLED = Color.LTGRAY;
        public static final String ORIENTATION_LANDSCAPE = "l"; // used in old Match code - needs to be converted to MatchTelly approach
        public static final String ORIENTATION_LANDSCAPE_REVERSE = "lr"; // used in old Match code - needs to be converted to MatchTelly approach
        public static final String  ORIENTATION_BLUE_ON_LEFT = "B";
        public static final String ORIENTATION_RED_ON_LEFT = "R";
        public static int IMAGE_HEIGHT = 0;
        public static int IMAGE_WIDTH = 0;
        public static int STATUS_TEXT_LONG_LENGTH = 22;
        public static int STATUS_TEXT_MED_LENGTH = 14;
        public static int STATUS_TEXT_LONG_SIZE = 14;
        public static int STATUS_TEXT_MED_SIZE = 18;
        public static int STATUS_TEXT_DEFAULT_SIZE = 24;
        public static float START_LINE_X = 24.17f;
        public static final int TRANSITION_EVENT_DNE = -1;
        public static final int SEEKBAR_MAX = 60;
    }

    public static class PostMatch {
        public static final String ACCURACY = "-1";
        public static final String CLIMB_LEVEL = "-1";
        public static final String CLIMB_POSITION = "-1";
        public static final String STEAL_FUEL = "-1";
        public static final String AFFECTED_BY_DEFENSE = "-1";
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
        public static final int QR_PREFERRED_SIZE_PERCENTAGE = 75;
        public static final int QR_SIZE_DEFAULT = 750;
        public static final int QR_LENGTH = 500;
        public static final String EOF = "EOF";
    }

    public static class Achievements {
        public static final int START_DELAY = 500;
        public static final int DISPLAY_TIME = 3_000;
        public static final int IN_BETWEEN_DELAY = 1_000;
        public static final String EVENT_TYPE_PRACTICE = "P";
        public static final String EVENT_TYPE_SEMI = "S";
        public static final String EVENT_TYPE_FINAL = "F";
        public static final ArrayList<Integer> COMPETITION_IDS_WORLDS = new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 17));
        public static final ArrayList<Integer> COMPETITION_IDS_DCMP = new ArrayList<>(List.of(9));
        public static final ArrayList<Integer> COMPETITION_IDS_EINSTEIN = new ArrayList<>(List.of(17));
    }
}