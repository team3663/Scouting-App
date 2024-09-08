package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment; // TODO - test code - can be removed
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cpr3663.cpr_scouting_app.databinding.AppLaunchBinding;

import java.io.BufferedReader;
import java.io.File; // TODO - test code - can be removed
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream; // TODO - test code - can be removed
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Timer;
import java.util.TimerTask;

public class AppLaunch extends AppCompatActivity {
    // =============================================================================================
    // Define constants
    // =============================================================================================
    private static final long SPLASH_SCREEN_DELAY = 200; // delay (ms) in between "loading" messages

    // =============================================================================================
    // Global variables
    // =============================================================================================
    private AppLaunchBinding appLaunchBinding;
    public static int CompetitionId = 4; // THIS NEEDS TO BE READ FROM THE CONFIG FILE
    public static Timer appLaunch_timer = new Timer();

    // Doesn't appear to be needed on Tablet but helps on Virtual Devices.
    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility", "ResourceAsColor"})
    @Override
    protected void onResume() {
        super.onResume();

        // Hide the status and action bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.hide();
    }

    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Capture screen size. Need to use WindowManager to populate a Point that holds the screen size.
        Display screen = getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        screen.getSize(screen_size);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        appLaunchBinding = AppLaunchBinding.inflate(getLayoutInflater());
        View page_root_view = appLaunchBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(appLaunchBinding.appLaunch, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Define a Image Button to open up the Settings
        ImageButton imgBut_Settings = appLaunchBinding.imgButSettings;
        imgBut_Settings.setImageResource(R.drawable.settings_icon);
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
        Button but_StartScouting = appLaunchBinding.butStartScouting;
        but_StartScouting.setVisibility(View.INVISIBLE);
        but_StartScouting.setClickable(false);
        but_StartScouting.setOnClickListener(new View.OnClickListener() {
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
      
        // Make sure that we aren't coming back to the page and it is the first time running this
        if (Globals.TeamList.size() == 0) {
            // Set a TimerTask to load the data shortly AFTER this OnCreate finishes
            appLaunch_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Make sure that we aren't coming back to the page and it is the first time running this
                    if (Globals.TeamList.size() == 0) {
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
                            LoadCommentData();
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                            LoadTrapResultsData();
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                            LoadClimbPositionsData();
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                            LoadStartPositionsData();
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    // Erase the status text
                    appLaunchBinding.textStatus.setText("");

                    // Enable the start scouting button and settings button
                    but_StartScouting.setClickable(true);
                    imgBut_Settings.setClickable(true);

                    // Setting the Visibility attribute can't be set from a non-UI thread (like withing a TimerTask
                    // that runs on a separate thread.  So we need to make a Runner that will execute on the UI thread
                    // to set these.
                    AppLaunch.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            but_StartScouting.setVisibility(View.VISIBLE);
                            imgBut_Settings.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }, 100);
        } else {
            // Enable the start scouting button and settings button because it isn't
            but_StartScouting.setClickable(true);
            imgBut_Settings.setClickable(true);
            but_StartScouting.setVisibility(View.VISIBLE);
            imgBut_Settings.setVisibility(View.VISIBLE);
        }
    }

    // =============================================================================================
    // Function:    CopyPrivateToPublicFile
    // Description: If the public file doesn't exist, read in the private one and copy it to the
    //              public one.
    // Output:      void
    // Parameters:  in_PrivateFileName
    //                  filename for the "private" accessible file
    //              in_PublicFileName
    //                  filename for the "public" accessible file
    // =============================================================================================
    private void CopyPrivateToPublicFile(String in_PrivateFileName, String in_PublicFileName) throws IOException {
        InputStream in = getAssets().open(in_PrivateFileName);
        File out_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), in_PublicFileName);

        // Ensure the directory structure exists first
        out_file.getParentFile().mkdirs();

        // If the output file doesn't exist, output a stream to it and copy contents over
        if (!out_file.exists()) {
            out_file.createNewFile();
            OutputStream out = new FileOutputStream(out_file);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

    }

    // =============================================================================================
    // Function:    LoadTeamData
    // Description: Read the list of teams from the .csv file into the global TeamList structure
    //              Read from the shared location.  If no file, then read from the private location
    //              created when installing the app AND then make a copy to the shared location.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    private void LoadTeamData(){
        String line = "";
        int index = 1;

        // Ensure the public file exists, and if not, copy the private one there.
        try {
            CopyPrivateToPublicFile(getResources().getString(R.string.private_file_teams), getResources().getString(R.string.public_file_teams));
        } catch (IOException e) {
            AppLaunch.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppLaunch.this, R.string.file_error_teams, Toast.LENGTH_SHORT).show();
                }
            });
        }

        appLaunchBinding.textStatus.setText(getResources().getString(R.string.loading_teams));

        // Open the asset file holding all of the Teams information (~10,000 records)
        // Read each line and add the team name into the ArrayList and use the team number as
        // the index to the array (faster lookups).  We need to then ensure that if there's a gap
        // in team numbers, we fill the Array with a "NO_TEAM" entry so subsequent teams are
        // matched with their corresponding index into the ArrayList
        try {
            Globals.TeamList.add(Constants.NO_TEAM);

            File in_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getResources().getString(R.string.public_file_teams));
            InputStream is = new FileInputStream(in_file);
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
    //              Read from the shared location.  If no file, then read from the private location
    //              created when installing the app AND then make a copy to the shared location.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    private void LoadCompetitionData(){
        String line = "";

        // Ensure the public file exists, and if not, copy the private one there.
        try {
            CopyPrivateToPublicFile(getResources().getString(R.string.private_file_competitions), getResources().getString(R.string.public_file_competitions));
        } catch (IOException e) {
            AppLaunch.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppLaunch.this, R.string.file_error_competitions, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Open the asset file holding all of the Competition information
        // Read each line and add the competition name into the ArrayList and use the competition id
        // as the index to the array.  We need to then ensure that if there's a gap in competition
        // numbers, we fill the Array with a "NO_COMPETITION" entry so subsequent competitions are
        // matched with their corresponding index into the ArrayList.  This should never happen.
        appLaunchBinding.textStatus.setText(getResources().getString(R.string.loading_competitions));

        try {
            File in_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getResources().getString(R.string.public_file_competitions));
            InputStream is = new FileInputStream(in_file);
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
    //              Read from the shared location.  If no file, then read from the private location
    //              created when installing the app AND then make a copy to the shared location.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    private void LoadMatchData(){
        String line = "";
        int index = 1;

        // Ensure the public file exists, and if not, copy the private one there.
        try {
            CopyPrivateToPublicFile(getResources().getString(R.string.private_file_matches), getResources().getString(R.string.public_file_matches));
        } catch (IOException e) {
            AppLaunch.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppLaunch.this, R.string.file_error_matches, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Open the asset file holding all of the Match information
        // Read each line and add the match information into the MatchList and use the match number
        // as the index to the array.
        //
        // This list also uses an array of MatchRowInfo since we're storing more than 1 value.
        appLaunchBinding.textStatus.setText(getResources().getString(R.string.loading_matches));
        try {
            Globals.MatchList.addMatchRow(Constants.NO_MATCH);

            File in_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getResources().getString(R.string.public_file_matches));
            InputStream is = new FileInputStream(in_file);
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
    //              Read from the shared location.  If no file, then read from the private location
    //              created when installing the app AND then make a copy to the shared location.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    private void LoadDeviceData(){
        String line = "";

        // Ensure the public file exists, and if not, copy the private one there.
        try {
            CopyPrivateToPublicFile(getResources().getString(R.string.private_file_devices), getResources().getString(R.string.public_file_devices));
        } catch (IOException e) {
            AppLaunch.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppLaunch.this, R.string.file_error_devices, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Open the asset file holding all of the Device information
        // Read each line and add the device information into the DeviceList.  There is no mapping
        // of the device number and the index into the array (there's no need)
        //
        // This list also uses an array of DeviceRowInfo since we're storing more than 1 value.
        appLaunchBinding.textStatus.setText(getResources().getString(R.string.loading_devices));
        try {
            File in_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getResources().getString(R.string.public_file_devices));
            InputStream is = new FileInputStream(in_file);
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
    // Description: Read the list of DNP reasons from the .csv file into the global
    //              DNPList structure.
    //              Read from the shared location.  If no file, then read from the private location
    //              created when installing the app AND then make a copy to the shared location.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    private void LoadDNPData(){
        String line = "";

        // Ensure the public file exists, and if not, copy the private one there.
        try {
            CopyPrivateToPublicFile(getResources().getString(R.string.private_file_dnp), getResources().getString(R.string.public_file_dnp));
        } catch (IOException e) {
            AppLaunch.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppLaunch.this, R.string.file_error_dnp, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Open the asset file holding all of the Device information
        // Read each line and add the device information into the DeviceList.  There is no mapping
        // of the device number and the index into the array (there's no need)
        //
        // This list also uses an array of DeviceRowInfo since we're storing more than 1 value
        appLaunchBinding.textStatus.setText(getResources().getString(R.string.loading_dnp));
        try {
            File in_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getResources().getString(R.string.public_file_dnp));
            InputStream is = new FileInputStream(in_file);
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
    // Description: Read the list of events from the .csv file into the global
    //              EventList structure.
    //              Read from the shared location.  If no file, then read from the private location
    //              created when installing the app AND then make a copy to the shared location.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    private void LoadEventData(){
        String line = "";

        // Ensure the public file exists, and if not, copy the private one there.
        try {
            CopyPrivateToPublicFile(getResources().getString(R.string.private_file_events_auto), getResources().getString(R.string.public_file_events_auto));
        } catch (IOException e) {
            AppLaunch.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppLaunch.this, R.string.file_error_events_auto, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Open the asset file holding all of the Event information
        // Read each line and add the event information into the Eventist.  There is no mapping
        // of the event number and the index into the array (there's no need)
        //
        // This list also uses an array of EventRowInfo since we're storing more than 1 value
        appLaunchBinding.textStatus.setText(getResources().getString(R.string.loading_events_auto));

        try {
            File in_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getResources().getString(R.string.public_file_events_auto));
            InputStream is = new FileInputStream(in_file);
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

        // Ensure the public file exists, and if not, copy the private one there.
        try {
            CopyPrivateToPublicFile(getResources().getString(R.string.private_file_events_teleop), getResources().getString(R.string.public_file_events_teleop));
        } catch (IOException e) {
            AppLaunch.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppLaunch.this, R.string.file_error_events_teleop, Toast.LENGTH_SHORT).show();
                }
            });
        }

        appLaunchBinding.textStatus.setText(getResources().getString(R.string.loading_events_teleop));

        try {
            File in_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getResources().getString(R.string.public_file_events_teleop));
            InputStream is = new FileInputStream(in_file);
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

        // Now that the data is all loaded, pre-build the list of next event descriptions for all
        // events so we can use this quickly in Match.java
        Globals.EventList.buildNextEvents();
    }

    // =============================================================================================
    // Function:    LoadCommentData
    // Description: Read the list of comments from the .csv file into the global
    //              CommentList structure.
    //              Read from the shared location.  If no file, then read from the private location
    //              created when installing the app AND then make a copy to the shared location.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    public void LoadCommentData() {
        String line = "";

        // Ensure the public file exists, and if not, copy the private one there.
        try {
            CopyPrivateToPublicFile(getResources().getString(R.string.private_file_comments), getResources().getString(R.string.public_file_comments));
        } catch (IOException e) {
            AppLaunch.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppLaunch.this, R.string.file_error_comments, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Open the asset file holding all of the Device information
        // Read each line and add the device information into the DeviceList.  There is no mapping
        // of the device number and the index into the array (there's no need)
        //
        // This list also uses an array of DeviceRowInfo since we're storing more than 1 value
        appLaunchBinding.textStatus.setText(getResources().getString(R.string.loading_comments));

        try {
            File in_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getResources().getString(R.string.public_file_comments));
            InputStream is = new FileInputStream(in_file);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",");
                // Only load "active" Comments reasons
                if (Boolean.parseBoolean(info[1])) {
                    Globals.CommentList.addCommentRow(info[0], info[2], info[3]);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // =============================================================================================
    // Function:    LoadTrapResultsData
    // Description: Read the list of trap options from the .csv file into the global
    //              TrapList structure.
    //              Read from the shared location.  If no file, then read from the private location
    //              created when installing the app AND then make a copy to the shared location.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    public void LoadTrapResultsData() {
        String line = "";

        // Ensure the public file exists, and if not, copy the private one there.
        try {
            CopyPrivateToPublicFile(getResources().getString(R.string.private_file_trap_results), getResources().getString(R.string.public_file_trap_results));
        } catch (IOException e) {
            AppLaunch.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppLaunch.this, R.string.file_error_trap_results, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Open the asset file holding all of the Trap information
        // Read each line and add the device information into the TrapList.  There is no mapping
        // of the trap number and the index into the array (there's no need)
        //
        // This list also uses an array of TrapRowInfo since we're storing more than 1 value
        appLaunchBinding.textStatus.setText(getResources().getString(R.string.loading_trap_results));

        try {
            File in_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getResources().getString(R.string.public_file_trap_results));
            InputStream is = new FileInputStream(in_file);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",");
                // Only load "active" rows
                if (Boolean.parseBoolean(info[1])) {
                    Globals.TrapResultsList.addTrapResultRow(info[0], info[2]);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // =============================================================================================
    // Function:    LoadClimbPositionsData
    // Description: Read the list of climbing positions from the .csv file into the global
    //              ClimbPositionList structure.
    //              Read from the shared location.  If no file, then read from the private location
    //              created when installing the app AND then make a copy to the shared location.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    public void LoadClimbPositionsData() {
        String line = "";

        // Ensure the public file exists, and if not, copy the private one there.
        try {
            CopyPrivateToPublicFile(getResources().getString(R.string.private_file_climb_positions), getResources().getString(R.string.public_file_climb_positions));
        } catch (IOException e) {
            AppLaunch.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppLaunch.this, R.string.file_error_climb_positions, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Open the asset file holding all of the Climbing Position information
        // Read each line and add the information into the ClimbPositionList.  There is no mapping
        // of the climb position number and the index into the array (there's no need)
        //
        // This list also uses an array of ClimbPositionRow since we're storing more than 1 value
        appLaunchBinding.textStatus.setText(getResources().getString(R.string.loading_climb_positions));

        try {
            File in_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getResources().getString(R.string.public_file_climb_positions));
            InputStream is = new FileInputStream(in_file);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",");
                // Only load "active" rows
                if (Boolean.parseBoolean(info[1])) {
                    Globals.ClimbPositionList.addClimbPositionRow(info[0], info[2]);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // =============================================================================================
    // Function:    LoadStartPositionsData
    // Description: Read the list of starting positions from the .csv file into the global
    //              StartPositionList structure.
    //              Read from the shared location.  If no file, then read from the private location
    //              created when installing the app AND then make a copy to the shared location.
    // Output:      void
    // Parameters:  n/a
    // =============================================================================================
    public void LoadStartPositionsData() {
        String line = "";

        // Ensure the public file exists, and if not, copy the private one there.
        try {
            CopyPrivateToPublicFile(getResources().getString(R.string.private_file_start_positions), getResources().getString(R.string.public_file_start_positions));
        } catch (IOException e) {
            AppLaunch.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppLaunch.this, R.string.file_error_start_positions, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Open the asset file holding all of the Start Positions information
        // Read each line and add the information into the StartPositionList.  There is no mapping
        // of the start position number and the index into the array (there's no need)
        //
        // This list also uses an array of StartPositionRow since we're storing more than 1 value
        appLaunchBinding.textStatus.setText(getResources().getString(R.string.loading_start_positions));

        try {
            File in_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getResources().getString(R.string.public_file_start_positions));
            InputStream is = new FileInputStream(in_file);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",");
                // Only load "active" rows
                if (Boolean.parseBoolean(info[1])) {
                    Globals.StartPositionList.addStartPositionRow(info[0], info[2]);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}