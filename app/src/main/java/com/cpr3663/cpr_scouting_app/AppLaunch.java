package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cpr3663.cpr_scouting_app.databinding.AppLaunchBinding;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AppLaunch extends AppCompatActivity {
    // =============================================================================================
    // Define constants
    // =============================================================================================
    private static final long SPLASH_SCREEN_DELAY = 300; // delay (ms) in between "loading" messages
    private static final String NO_TEAM = "No Team Exists"; // use to check if no team exists for a given team number
    private static final String NO_MATCH = "No Match"; // use to check if no match info exists for a given match number
    private static final String NO_COMPETITION = "No Competition Name Exists"; // use to check if no competition info exists for a given competition id
    private static final int NO_EVENT = 999; // use to check if the eventID you're looking for doesn't exist

    // =============================================================================================
    // Class:       MatchInfo
    // Description: Defines a structure/class to hold the information for all Matches
    // Methods:     getMatchInfoRow()
    //                  return a MatchInfoRow item for the given match id
    // =============================================================================================
    public static class MatchInfo {
        private ArrayList<MatchInfoRow> match_list;

        // Constructor
        public MatchInfo() {
            match_list = new ArrayList<MatchInfoRow>();
        }

        // Member Function: Add a row of match info into the list giving all of the data individually
        public void addMatchRow(String in_red1, String in_red2, String in_red3, String in_blue1, String in_blue2, String in_blue3) {
            match_list.add(new MatchInfoRow(in_red1, in_red2, in_red3, in_blue1, in_blue2, in_blue3));
        }

        // Member Function: Add a row of match info into the list giving the data in a csv format
        public void addMatchRow(String in_csvRow) {
            match_list.add(new MatchInfoRow(in_csvRow));
        }

        // Member Function: Get back a row of data for a given match
        public MatchInfoRow getMatchInfoRow(int in_match_id) {
            return match_list.get(in_match_id);
        }

        // =============================================================================================
        // Class:       MatchInfoRow (PRIVATE)
        // Description: Defines a structure/class to hold the information for each Match
        // Methods:     getListOfTeams()
        //                  return an array of team numbers (6 of them) that are in this match
        // =============================================================================================
        private static class MatchInfoRow {
            // Class Members
            private int red1 = 0;
            private int red2 = 0;
            private int red3 = 0;
            private int blue1 = 0;
            private int blue2 = 0;
            private int blue3 = 0;

            // Constructor with a csv string
            public MatchInfoRow(String in_csvRow) {
                if (!in_csvRow.equals(NO_MATCH)) {
                    String[] data = in_csvRow.split(",");

                    // Validate we have enough values otherwise this was a bad row and we'll get an out-of-bounds exception
                    if (data.length == 8) {
                        red1 = Integer.parseInt(data[2]);
                        red2 = Integer.parseInt(data[3]);
                        red3 = Integer.parseInt(data[4]);
                        blue1 = Integer.parseInt(data[5]);
                        blue2 = Integer.parseInt(data[6]);
                        blue3 = Integer.parseInt(data[7]);
                    }
                }
            }

            // Constructor with individual data
            public MatchInfoRow(String in_red1, String in_red2, String in_red3, String in_blue1, String in_blue2, String in_blue3) {
                red1 = Integer.parseInt(in_red1);
                red2 = Integer.parseInt(in_red2);
                red3 = Integer.parseInt(in_red3);
                blue1 = Integer.parseInt(in_blue1);
                blue2 = Integer.parseInt(in_blue2);
                blue3 = Integer.parseInt(in_blue3);
            }

            // Member Function: Return a list of team numbers for this match
            public int[] getListOfTeams() {
                return new int[] {red1, red2, red3, blue1, blue2, blue3};
            }
        }
    }

    // =============================================================================================
    // Class:       CompetitionInfo
    // Description: Defines a structure/class to hold the information for all Competitions
    // Methods:     addCompetitionInfoRow()
    //                  add a row of competition info
    //              getCompetitionInfoRow()
    //                  return a CompetitionInfoRow item for the given competition id
    // =============================================================================================
    public static class CompetitionInfo {
        private ArrayList<CompetitionInfoRow> competition_list;

        // Constructor
        public CompetitionInfo() {
            competition_list = new ArrayList<CompetitionInfoRow>();
        }

        // Member Function: Add a row of competition info into the list giving all of the data individually
        public void addCompetitionRow(String in_Id, String in_description) {
            competition_list.add(new CompetitionInfoRow(in_Id, in_description));
        }

        // Member Function: return the size of the list
        public int size() {
            return competition_list.size();
        }

        // Member Function: Get back a row of data for a given competition
        public CompetitionInfoRow getCompetitionInfoRow(int in_index) {
            return competition_list.get(in_index);
        }

        // =============================================================================================
        // Class:       CompetitionInfoRow (PRIVATE)
        // Description: Defines a structure/class to hold the information for each Device.
        // Methods:     getId()
        //                  returns the (int) competition number for this row.
        //              getDescription()
        //                  returns the (String) description for this row.
        // =============================================================================================
        private static class CompetitionInfoRow {
            // Class Members
            private int id = 0;
            private String description = "";

            // Constructor with individual data
            public CompetitionInfoRow(String in_id, String in_description) {
                id = Integer.parseInt(in_id);
                description = in_description;
            }

            // Getter
            public int getId() {
                return id;
            }

            // Getter
            public String getDescription() {
                return description;
            }
        }
    }

    // =============================================================================================
    // Class:       DeviceInfo
    // Description: Defines a structure/class to hold the information for all Devices
    // Methods:     addDeviceInfoRow()
    //                  add a row of device info
    //              getDeviceInfoRow()
    //                  return a MatchInfoRow item for the given match id
    // =============================================================================================
    public static class DeviceInfo {
        private ArrayList<DeviceInfoRow> device_list;

        // Constructor
        public DeviceInfo() {
            device_list = new ArrayList<DeviceInfoRow>();
        }

        // Member Function: Add a row of device info into the list giving the data in a csv format
        public void addDeviceRow(String in_device_number, String in_team_number, String in_description) {
            device_list.add(new DeviceInfoRow(in_device_number, in_team_number, in_description));
        }

        // Member Function: return the size of the list
        public int size() {
            return device_list.size();
        }

        // Member Function: Get back a row of data for a given device
        public DeviceInfoRow getDeviceInfoRow(int in_index) {
            return device_list.get(in_index);
        }

        // =============================================================================================
        // Class:       DeviceInfoRow (PRIVATE)
        // Description: Defines a structure/class to hold the information for each Device.
        // Methods:     getDeviceNumber()
        //                  returns the (int) device number for this row.
        //              getTeamNumber()
        //                  returns the (int) team number for this row.
        //              getDescription()
        //                  returns the (String) description for this row.
        // =============================================================================================
        private static class DeviceInfoRow {
            // Class Members
            private int device_number = 0;
            private int team_number = 0;
            private String description = "";

            // Constructor with individual data
            public DeviceInfoRow(String in_device_number, String in_team_number, String in_description) {
                device_number = Integer.parseInt(in_device_number);
                team_number = Integer.parseInt(in_team_number);
                description = in_description;
            }

            // Getter
            public int getDeviceNumber() {
                return device_number;
            }

            // Getter
            public int getTeamNumber() {
                return team_number;
            }

            // Getter
            public String getDescription() {
                return description;
            }
        }
    }

    // =============================================================================================
    // Class:       DNPInfo
    // Description: Defines a structure/class to hold the information for all DNP reasons
    // Methods:     addDNPInfoRow()
    //                  add a row of device info
    //              getDNPInfoRow()
    //                  return a MatchInfoRow item for the given match id
    // =============================================================================================
    public static class DNPInfo {
        private ArrayList<DNPInfoRow> dnp_list;

        // Constructor
        public DNPInfo() {
            dnp_list = new ArrayList<DNPInfoRow>();
        }

        // Member Function: Add a row of DNP info into the list giving the data individually
        public void addDNPRow(String in_id, String in_description) {
            dnp_list.add(new DNPInfoRow(in_id, in_description));
        }

        // Member Function: return the size of the list
        public int size() {
            return dnp_list.size();
        }

        // Member Function: Get back a row of data for a given DNP entry
        public DNPInfoRow getDNPInfoRow(int in_index) {
            return dnp_list.get(in_index);
        }

        // Member Function: Get back the Id for a given DNP entry (needed for logging)
        public int getDNPId(String in_description) {
            int ret = 0;

            // Loop through the DNP list to find a matching description and return the id
            for (int i = 0; i < dnp_list.size(); i++) {
                if (dnp_list.get(i).getDescription().equals(in_description)) {
                    ret = dnp_list.get(i).id;
                    break;
                }
            }

            return ret;
        }

        // =============================================================================================
        // Class:       DNPInfoRow (PRIVATE)
        // Description: Defines a structure/class to hold the information for each DNP reason
        // Methods:     getId()
        //                  returns the (int) DNP number for this row to use for logging
        //              getDescription()
        //                  returns the (String) description for this row
        // =============================================================================================
        private static class DNPInfoRow {
            // Class Members
            private int id = 0;
            private String description = "";

            // Constructor with individual data
            public DNPInfoRow(String in_id, String in_description) {
                id = Integer.parseInt(in_id);
                description = in_description;
            }

            // Getter
            public int getId() {
                return id;
            }

            // Getter
            public String getDescription() {
                return description;
            }
        }
    }

    // =============================================================================================
    // Class:       EventInfo
    // Description: Defines a structure/class to hold the information for all Event Info
    // Methods:     addEventRow()
    //                  adds a row to the list of events
    //              getEventsForPhase()
    //                  returns a ListArray of possible initial events in the given match phase
    //              getNextEvents(int EventId)
    //                  returns an array of possible Events that can happen after EventId
    //              getEventId(String EventDescription)
    //                  returns the internal ID for an event
    //                  useful for logging events that don't happen on the field of play.
    // =============================================================================================
    public static class EventInfo {
        // Class Members
        private ArrayList<EventInfoRow> event_list;

        // Class constants
        public static final String EVENT_STARTING_NOTE = "Auto_StartingNote";
        public static final String EVENT_DEFENDED_START = "Defended_Start";
        public static final String EVENT_DEFENDED_END = "Defended_End";
        public static final String EVENT_DEFENSE_START = "PlayDefense_Start";
        public static final String EVENT_DEFENSE_END = "PlayDefense_End";

        // Constructor
        public EventInfo() {
            event_list = new ArrayList<EventInfoRow>();
        }

        // Member Function: Add a row of event info into the list giving the data individually
        public void addEventRow(String in_id, String in_description, String in_phase,
                                String in_seq_start, String in_FOP, String in_next_set) {
            event_list.add(new EventInfoRow(Integer.parseInt(in_id), in_description, in_phase,
                    Boolean.parseBoolean(in_seq_start), Boolean.parseBoolean(in_FOP), in_next_set));
        }

        // Member Function: Return a list of Events (description) for a give phase of the match (only ones that start a sequence)
        public ArrayList<String> getEventsForPhase(String in_phase) {
            ArrayList<String> ret = new ArrayList<String>();

            // Error check the input and only do this if they passed in a valid parameter
            if (in_phase.equals(Match.PHASE_AUTO) || in_phase.equals(Match.PHASE_TELEOP)) {
                for (EventInfoRow eventInfoRow : event_list) {
                    // Only build the array if the phase is right AND this is for a FOP (field of play) AND this event starts a sequence
                    if ((eventInfoRow.match_phase.equals(in_phase)) && (eventInfoRow.is_FOP_Event) && (eventInfoRow.is_seq_start)) {
                        ret.add(eventInfoRow.description);
                        }
                }
            }

            return ret;
        }

        // Member Function: Return a list of Events (description) that can follow a given EventId (next Event in the sequence)
        public ArrayList<String> getNextEvents(int in_EventId) {
            ArrayList<String> ret = new ArrayList<String>();
            String next_set = "";
            String[] next_set_ids;

            // Find the event in the list, and get it's list of valid next events
            for (int i = 0; i < event_list.size(); i++) {
                if ((event_list.get(i).id == in_EventId)) {
                    next_set = event_list.get(i).next_event_set;
                    break;
                }
            }

            // Split out the next set of event ids.
            next_set_ids = next_set.split(":");

            // Now find all events match the list of next events we can go to
            for (int i = 0; i < event_list.size(); i++) {
                for (int j = ret.size(); j < next_set_ids.length; j++) {
                    // If the event we're looking at (i) is in the list of valid next event ids (j) add it to the list
                    if (event_list.get(i).id == Integer.parseInt(next_set_ids[j])) {
                        ret.add(event_list.get(i).description);
                    }
                }
            }

            // If we didn't add anything at this point, return null
            // This means there were no valid events that follow the one passed in
            if (ret.size() == 0) {
                return null;
            }

            return ret;
        }

        // Member Function: Return the Id for a given Event (needed for logging)
        public int getEventId(String in_EventDescription) {
            int ret = NO_EVENT;

            // Look through the event rows to find a match
            for (int i = 0; i < event_list.size(); i++) {
                if (event_list.get(i).description.equals(in_EventDescription) &&
                        event_list.get(i).match_phase.equals(Match.matchPhase)) {
                    ret = event_list.get(i).id;
                    break;
                }
            }

            return ret;
        }

        // =============================================================================================
        // Class:       EventInfoRow (PRIVATE)
        // Description: Defines a structure/class to hold the information for each Event
        // Methods:     n/a
        // =============================================================================================
        private class EventInfoRow {
            // Class Members
            private int id = 0;
            private String description = "";
            private String match_phase = "";
            private boolean is_FOP_Event = false;
            private boolean is_seq_start = false;
            private String next_event_set = "";

            // Constructor
            public EventInfoRow(int in_id, String in_description, String in_phase,
                                Boolean in_seq_start, Boolean in_FOP, String in_next_event_set) {
                id = in_id;
                description = in_description;
                match_phase = in_phase;
                is_FOP_Event = in_FOP;
                is_seq_start = in_seq_start;
                next_event_set = in_next_event_set;
           }

           // Getter
           public int getId() {
                return id;
            }

            // Getter
            public String getDescription() {
                return description;
            }
        }
    }

    // =============================================================================================
    // Global variables
    // =============================================================================================
    private AppLaunchBinding applaunchbinding;
    // Variables to store the input data to be used throughout the app
    public static ArrayList<String> TeamList = new ArrayList<String>();
    public static CompetitionInfo CompetitionList = new CompetitionInfo();
    public static MatchInfo MatchList = new MatchInfo();
    public static DeviceInfo DeviceList = new DeviceInfo();
    public static DNPInfo DNPList = new DNPInfo();
    public static EventInfo EventList = new EventInfo();
    // Global to this activity only
    public static int CompetitionId = 4; // THIS NEEDS TO BE READ FROM THE CONFIG FILE
    public static Timer appLaunch_timer = new Timer();

    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Capture screen size. Need to use WindowManager to populate a Point that holds the screen size.
        Display screen = getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        screen.getSize(screen_size);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        applaunchbinding = AppLaunchBinding.inflate(getLayoutInflater());
        View page_root_view = applaunchbinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(applaunchbinding.appLaunch, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        applaunchbinding.textBanner.setText(getResources().getString(R.string.banner_app_name));

        // TODO: Here's how you read in app preferences (settings) and set them.
        // TODO: need an Admin / Settings page with a button Sprocket to go to and return to previous page
        // TODO: Either way, we need to read them in and if empty, force user to admin page to set.  AFTER we load the data
        SharedPreferences sp = this.getSharedPreferences(getResources().getString(R.string.preference_setting_file_key), Context.MODE_PRIVATE);
        int d = sp.getInt("DeviceId", -1);

        SharedPreferences.Editor spe = sp.edit();
        spe.putInt("DeviceId", 4);
        spe.apply();

        // Define a Frame for the Settings Fragment to go in
        FrameLayout frame_Settings = applaunchbinding.frameSettings;
        frame_Settings.setBackgroundColor(Color.TRANSPARENT);
        frame_Settings.setX(300F);
        frame_Settings.setY(200F);
        FrameLayout.LayoutParams frame_Settings_LP = new FrameLayout.LayoutParams(
                screen_size.x - 2 * (int) frame_Settings.getX(), screen_size.y - 2 * (int) frame_Settings.getY());
        frame_Settings.setLayoutParams(frame_Settings_LP);

        // Define a Image Button to open up the Settings Fragment
        ImageButton imgBut_Settings = applaunchbinding.imgButSettings;
        imgBut_Settings.setImageResource(R.drawable.settings_icon);
        imgBut_Settings.setBackgroundColor(Color.TRANSPARENT); // Set background Color
        imgBut_Settings.setVisibility(View.INVISIBLE);
        imgBut_Settings.setClickable(false);

        // Define the Start Scouting Button
        applaunchbinding.butStartScouting.setText(R.string.button_start_scouting);
        applaunchbinding.butStartScouting.setBackgroundColor(Color.WHITE);
        applaunchbinding.butStartScouting.setTextColor(R.color.cpr_bkgnd);
        applaunchbinding.butStartScouting.setVisibility(View.INVISIBLE);
        applaunchbinding.butStartScouting.setClickable(false);
        applaunchbinding.butStartScouting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stop the timer
                appLaunch_timer.cancel();
                appLaunch_timer.purge();

                // Go to the first page
                Intent GoToNextPage = new Intent(AppLaunch.this, PreMatch.class);
                startActivity(GoToNextPage);
            }
        });

        // Set a TimerTask to load the data shortly AFTER this OnCreate finishes
        appLaunch_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Load the data with a BRIEF delay between.  :)
                try {
                    LoadTeamData();
                    Thread.sleep(SPLASH_SCREEN_DELAY);
                    LoadCompetitionData();
                    Thread.sleep(SPLASH_SCREEN_DELAY);
                    LoadDeviceData();
                    Thread.sleep(SPLASH_SCREEN_DELAY);
                    LoadDNPData();
                    Thread.sleep(SPLASH_SCREEN_DELAY);
                    LoadMatchData();
                    Thread.sleep(SPLASH_SCREEN_DELAY);
                    LoadEventData();
                    Thread.sleep(SPLASH_SCREEN_DELAY);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Erase the status text
                applaunchbinding.textStatus.setText("");

                // Enable the start scouting button and settings button
                applaunchbinding.butStartScouting.setClickable(true);
                imgBut_Settings.setClickable(true);

                // Setting the Visibility attribute can't be set from a non-UI thread (like withing a TimerTask
                // that runs on a separate thread.  So we need to make a Runner that will execute on the UI thread
                // to set these.
                AppLaunch.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        applaunchbinding.butStartScouting.setVisibility(View.VISIBLE);
                        imgBut_Settings.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 100);
    }

    // =============================================================================================
    // Function:    LoadTeamData
    // Description: Read the list of teams from the .csv file into the global TeamList structure
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    public void LoadTeamData(){
        String line = "";
        int index = 1;

        applaunchbinding.textStatus.setText(getResources().getString(R.string.loading_teams));

        // Open the asset file holding all of the Teams information (~10,000 records)
        // Read each line and add the team name into the ArrayList and use the team number as
        // the index to the array (faster lookups).  We need to then ensure that if there's a gap
        // in team numbers, we fill the Array with a "NO_TEAM" entry so subsequent teams are
        // matched with their corresponding index into the ArrayList
        try {
            TeamList.add(NO_TEAM);

            InputStream is = getAssets().open(getResources().getString(R.string.file_teams));
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",");
                // Need to make sure there's no gaps so the team number and index align
                for (int i = index; i < Integer.parseInt(info[0]); i++) {
                    TeamList.add(NO_TEAM);
                }
                TeamList.add(info[1]);
                index = Integer.parseInt(info[0]) + 1;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // =============================================================================================
    // Function:    LoadCompetitionData
    // Description: Read the list of competitions from the .csv file into the global
    //              CompetitionList structure.  This is used for ADMIN configuration of the device.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    public void LoadCompetitionData(){
        String line = "";

        // Open the asset file holding all of the Competition information
        // Read each line and add the competition name into the ArrayList and use the competition id
        // as the index to the array.  We need to then ensure that if there's a gap in competition
        // numbers, we fill the Array with a "NO_COMPETITION" entry so subsequent competitions are
        // matched with their corresponding index into the ArrayList.  This should never happen.
        applaunchbinding.textStatus.setText(getResources().getString(R.string.loading_competitions));
        try {
            InputStream is = getAssets().open(getResources().getString(R.string.file_competitions));
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",");
                CompetitionList.addCompetitionRow(info[0], info[1]);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // =============================================================================================
    // Function:    LoadMatchData
    // Description: Read the list of matches from the .csv file into the global
    //              MatchList structure.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    public void LoadMatchData(){
        String line = "";
        int index = 1;

        // Open the asset file holding all of the Match information
        // Read each line and add the match information into the MatchList and use the match number
        // as the index to the array.
        //
        // This list also uses an array of MatchRowInfo since we're storing more than 1 value.
        applaunchbinding.textStatus.setText(getResources().getString(R.string.loading_matches));
        try {
            MatchList.addMatchRow(NO_MATCH);

            InputStream is = getAssets().open(getResources().getString(R.string.file_matches));
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",");
                // Use only the match information that equals the competition we're in.
                if (Integer.parseInt(info[0]) == CompetitionId) {
                    for (int i = index; i < Integer.parseInt(info[1]); i++) {
                        MatchList.addMatchRow(NO_MATCH);
                    }
                    MatchList.addMatchRow(info[2], info[3], info[4], info[5], info[6], info[7]);
                    index = Integer.parseInt(info[1]) + 1;
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // =============================================================================================
    // Function:    LoadDeviceData
    // Description: Read the list of devices from the .csv file into the global
    //              DeviceList structure.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    public void LoadDeviceData(){
        String line = "";

        // Open the asset file holding all of the Device information
        // Read each line and add the device information into the DeviceList.  There is no mapping
        // of the device number and the index into the array (there's no need)
        //
        // This list also uses an array of DeviceRowInfo since we're storing more than 1 value.
        applaunchbinding.textStatus.setText(getResources().getString(R.string.loading_devices));
        try {
            InputStream is = getAssets().open(getResources().getString(R.string.file_devices));
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",");
                DeviceList.addDeviceRow(info[0], info[1], info[2]);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // =============================================================================================
    // Function:    LoadDNPData
    // Description: Read the list of devices from the .csv file into the global
    //              DeviceList structure.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    public void LoadDNPData(){
        String line = "";

        // Open the asset file holding all of the Device information
        // Read each line and add the device information into the DeviceList.  There is no mapping
        // of the device number and the index into the array (there's no need)
        //
        // This list also uses an array of DeviceRowInfo since we're storing more than 1 value
        applaunchbinding.textStatus.setText(getResources().getString(R.string.loading_dnp));
        try {
            InputStream is = getAssets().open(getResources().getString(R.string.file_dnp));
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",");
                // Only load "active" DNP reasons
                if (Boolean.parseBoolean(info[2])) {
                    DNPList.addDNPRow(info[0], info[1]);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // =============================================================================================
    // Function:    LoadEventData
    // Description: Read the list of devices from the .csv file into the global
    //              EventList structure.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    public void LoadEventData(){
        String line = "";

        // Open the asset file holding all of the Event information for AUTO
        // Read each line and add the event information into the EventList.  There is no mapping
        // of the event number and the index into the array (there's no need)
        //
        // This list also uses an array of EventRowInfo since we're storing more than 1 value
        applaunchbinding.textStatus.setText(getResources().getString(R.string.loading_events_auto));

        try {
            InputStream is = getAssets().open(getResources().getString(R.string.file_events_auto));
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",");
                EventList.addEventRow(info[0], info[1], Match.PHASE_AUTO, info[2], info[3], info[4]);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Do the same for Teleop Events.
        applaunchbinding.textStatus.setText(getResources().getString(R.string.loading_events_teleop));

        try {
            InputStream is = getAssets().open(getResources().getString(R.string.file_events_teleop));
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",");
                EventList.addEventRow(info[0], info[1], Match.PHASE_TELEOP, info[2], info[3], info[4]);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
