package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Toast;
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
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class AppLaunch extends AppCompatActivity {
    // =============================================================================================
    // Define constants
    // =============================================================================================
    private static final long SPLASH_SCREEN_DELAY = 300; // delay (ms) in between "loading" messages

    // =============================================================================================
    // Global variables
    // =============================================================================================
    private AppLaunchBinding applaunchbinding;
    public static int CompetitionId = 4; // THIS NEEDS TO BE READ FROM THE CONFIG FILE
    public static Timer appLaunch_timer = new Timer();

    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility", "ResourceAsColor"})
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

        // Define the Start Scouting Button
        applaunchbinding.butStartScouting.setText(R.string.button_start_scouting);
//        applaunchbinding.startScoutingbutton.setBackgroundColor(Color.WHITE);
//        applaunchbinding.startScoutingbutton.setTextColor(R.color.cpr_bkgnd);
        applaunchbinding.butStartScouting.setVisibility(View.INVISIBLE);
        applaunchbinding.butStartScouting.setClickable(false);
        applaunchbinding.butStartScouting.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent GoToSettings = new Intent(AppLaunch.this, Settings.class);
                 startActivity(GoToSettings);
            }
        });

        // Define a Image Button to open up the Settings
        ImageButton imgBut_Settings = applaunchbinding.settingsButton;
        imgBut_Settings.setImageResource(R.drawable.settings_icon);
        imgBut_Settings.setBackgroundColor(Color.TRANSPARENT); // Set background Color
        imgBut_Settings.setVisibility(View.INVISIBLE);
        imgBut_Settings.setClickable(false);
        imgBut_Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GoToSettings = new Intent(AppLaunch.this, Settings.class);
                startActivity(GoToSettings);
            }
        });

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
                Intent GoToPreMatch = new Intent(AppLaunch.this, PreMatch.class);
                startActivity(GoToPreMatch);
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
            Globals.TeamList.add(Constants.NO_TEAM);

            InputStream is = getAssets().open(getResources().getString(R.string.file_teams));
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",");
                // Need to make sure there's no gaps so the team number and index align
                for (int i = index; i < Integer.parseInt(info[0]); i++) {
                    Globals.TeamList.add(Constants.NO_TEAM);
                }
                Globals.TeamList.add(info[1]);
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
                Globals.CompetitionList.addCompetitionRow(info[0], info[1]);
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
            Globals.MatchList.addMatchRow(Constants.NO_MATCH);

            InputStream is = getAssets().open(getResources().getString(R.string.file_matches));
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",");
                // Use only the match information that equals the competition we're in.
                if (Integer.parseInt(info[0]) == CompetitionId) {
                    for (int i = index; i < Integer.parseInt(info[1]); i++) {
                        Globals.MatchList.addMatchRow(Constants.NO_MATCH);
                    }
                    Globals.MatchList.addMatchRow(info[2], info[3], info[4], info[5], info[6], info[7]);
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
                Globals.DeviceList.addDeviceRow(info[0], info[1], info[2]);
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
                    Globals.DNPList.addDNPRow(info[0], info[1]);
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
                Globals.EventList.addEventRow(info[0], info[1], Constants.PHASE_AUTO, info[2], info[3], info[4]);
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
                Globals.EventList.addEventRow(info[0], info[1], Constants.PHASE_TELEOP, info[2], info[3], info[4]);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}