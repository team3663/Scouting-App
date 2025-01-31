package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.AppLaunchBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AppLaunch extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private AppLaunchBinding appLaunchBinding;
    public static Timer appLaunch_timer = new Timer();

    @Override
    protected void onActivityResult(int in_requestCode, int in_resultCode, Intent in_data) {
        super.onActivityResult(in_requestCode, in_resultCode, in_data);

        // check that it is the SecondActivity with an OK result
        if (in_resultCode == RESULT_OK) { // Activity.RESULT_OK
            switch (in_requestCode) {
                case Constants.AppLaunch.ACTIVITY_CODE_SETTINGS:
                    // get String data from Intent
                    if (in_data.getIntExtra(Constants.Settings.RELOAD_DATA_KEY, 0) == 1) {
                        try {
                            Globals.MatchList.clear();
                            Globals.MatchList.addMatchRow(Constants.Matches.NO_MATCH);
                            LoadDataFile(getString(R.string.file_matches), getString(R.string.applaunch_loading_matches), getString(R.string.applaunch_file_error_matches));
                            Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);
                            appLaunchBinding.textStatus.setText("");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    break;
                case Constants.AppLaunch.ACTIVITY_CODE_STORAGE:
                    Uri treeUri = in_data.getData();
                    if (treeUri != null) {
                        getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        Globals.spe.putString(Constants.Prefs.STORAGE_URI, treeUri.toString());
                        Globals.spe.apply();
                        Globals.baseStorageURI = treeUri;
                    }

                    loadData();
                    break;
            }
        }
    }

    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle in_savedInstanceState) {
        super.onCreate(in_savedInstanceState);

        EdgeToEdge.enable(this);
        appLaunchBinding = AppLaunchBinding.inflate(getLayoutInflater());
        View page_root_view = appLaunchBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(appLaunchBinding.appLaunch, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Display app version
        PackageInfo pInfo;
        try {
            pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        appLaunchBinding.textVersion.setText(getString(R.string.app_version) + " " + pInfo.versionName);

        // Get the Shared Preferences where we save off app settings to use next time
        if (Globals.sp == null)
            Globals.sp = this.getSharedPreferences(getString(R.string.preference_setting_file_key), Context.MODE_PRIVATE);
        if (Globals.spe == null) Globals.spe = Globals.sp.edit();

        // Initialize activity components
        initSettings();
        initScouting();

        // Find out if we have permissions.  Since our app is only requesting one location, if it's not empty, we're good
        List<UriPermission> perm_list = getContentResolver().getPersistedUriPermissions();
        boolean havePerms = !perm_list.isEmpty();

        // If we have storage permissions go ahead and load the data.
        // Otherwise, initiate getting permissions (which will then load the data afterwards)
        if (havePerms) {
            Globals.baseStorageURI = Uri.parse(Globals.sp.getString(Constants.Prefs.STORAGE_URI, null));
            loadData();
        } else
            initStoragePermissions();

        // While loading Matches, we messed with Globals.CurrentMatchType, so reset it
        Globals.CurrentMatchType = Constants.PreMatch.DEFAULT_MATCH_TYPE;
    }

    // =============================================================================================
    // Function:    loadData
    // Description: load all of the configurable data for the app
    // Output:      void
    // Parameters:  void
    // =============================================================================================
    private void loadData() {
        // Make sure we have our directory structure created
        DocumentFile storage_df = DocumentFile.fromTreeUri(this, Globals.baseStorageURI);

        // Check that the BASE directory exists and create it if not.
        assert storage_df != null;
        Globals.base_df = storage_df.findFile(Constants.Data.PUBLIC_BASE_DIR);

        // If the base dir isn't there, the others won't be either, so create them
        if (Globals.base_df == null) {
            Globals.base_df = storage_df.createDirectory(Constants.Data.PUBLIC_BASE_DIR);
            if (Globals.base_df != null) {
                Globals.input_df = Globals.base_df.createDirectory(Constants.Data.PUBLIC_INPUT_DIR);
                Globals.output_df = Globals.base_df.createDirectory(Constants.Data.PUBLIC_OUTPUT_DIR);
            }
        } else {
            // The base dir is there but we should check the two sub directories are there.
            Globals.input_df = Globals.base_df.findFile(Constants.Data.PUBLIC_INPUT_DIR);
            if (Globals.input_df == null)
                Globals.input_df = Globals.base_df.createDirectory(Constants.Data.PUBLIC_INPUT_DIR);
            Globals.output_df = Globals.base_df.findFile(Constants.Data.PUBLIC_OUTPUT_DIR);
            if (Globals.output_df == null)
                Globals.output_df = Globals.base_df.createDirectory(Constants.Data.PUBLIC_OUTPUT_DIR);
        }

        // Set a TimerTask to load the data shortly AFTER this OnCreate finishes
        appLaunch_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Force all lists to be empty (just to be sure)
                Globals.ClimbPositionList.clear();
                Globals.ColorList.clear();
                Globals.CommentList.clear();
                Globals.CompetitionList.clear();
                Globals.DeviceList.clear();
                Globals.EventList.clear();
                Globals.MatchList.clear();
                Globals.StartPositionList.clear();
                Globals.TeamList.clear();

                // Load the data with a BRIEF delay between.  :)
                try {
                    // First index (zero) needs to be a "NO TEAM" entry so the rest line up when they are loaded
                    Globals.TeamList.add(Constants.Teams.NO_TEAM);

                    LoadDataFile(getString(R.string.file_climb_positions), getString(R.string.applaunch_loading_climb_positions), getString(R.string.applaunch_file_error_climb_positions));
                    Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);
                    LoadDataFile(getString(R.string.file_colors), getString(R.string.applaunch_loading_colors), getString(R.string.applaunch_file_error_colors));
                    Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);
                    LoadDataFile(getString(R.string.file_comments), getString(R.string.applaunch_loading_comments), getString(R.string.applaunch_file_error_comments));
                    Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);
                    LoadDataFile(getString(R.string.file_competitions), getString(R.string.applaunch_loading_competitions), getString(R.string.applaunch_file_error_competitions));
                    Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);
                    LoadDataFile(getString(R.string.file_devices), getString(R.string.applaunch_loading_devices), getString(R.string.applaunch_file_error_devices));
                    Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);
                    LoadDataFile(getString(R.string.file_event_groups), getString(R.string.applaunch_loading_event_groups), getString(R.string.applaunch_file_error_event_groups));
                    Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);
                    LoadDataFile(getString(R.string.file_events), getString(R.string.applaunch_loading_events), getString(R.string.applaunch_file_error_events));
                    Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);
                    LoadDataFile(getString(R.string.file_match_types), getString(R.string.applaunch_loading_match_types), getString(R.string.applaunch_file_error_match_types));
                    Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);
                    LoadDataFile(getString(R.string.file_matches), getString(R.string.applaunch_loading_matches), getString(R.string.applaunch_file_error_matches));
                    Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);
                    LoadDataFile(getString(R.string.file_start_positions), getString(R.string.applaunch_loading_start_positions), getString(R.string.applaunch_file_error_start_positions));
                    Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);
                    LoadDataFile(getString(R.string.file_teams), getString(R.string.applaunch_loading_teams), getString(R.string.applaunch_file_error_teams));
                    Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);

                    // We need to build the "Next Events" possible but needs to be done now, after all data is loaded.
                    Globals.EventList.buildNextEvents();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Erase the status text
                appLaunchBinding.textStatus.setText("");

                // Enable the start scouting button and settings button
                appLaunchBinding.butStartScouting.setClickable(true);
                appLaunchBinding.imgButSettings.setClickable(true);

                // Setting the Visibility attribute can't be set from a non-UI thread (like withing a TimerTask
                // that runs on a separate thread.  So we need to make a Runner that will execute on the UI thread
                // to set these.
                AppLaunch.this.runOnUiThread(() -> {
                    appLaunchBinding.butStartScouting.setVisibility(View.VISIBLE);
                    appLaunchBinding.imgButSettings.setVisibility(View.VISIBLE);
                    appLaunchBinding.butStartScouting.setClickable(true);
                    appLaunchBinding.imgButSettings.setClickable(true);
                    appLaunchBinding.butStartScouting.setVisibility(View.VISIBLE);
                    appLaunchBinding.imgButSettings.setVisibility(View.VISIBLE);
                });
            }
        }, 10);
    }

    // =============================================================================================
    // Function:    CopyPrivateToPublicFile
    // Description: If the public file doesn't exist, read in the private one and copy it to the
    //              public one.
    // Output:      Whether to use the public file or not
    // Parameters:  in_PrivateFileName
    //                  filename for the "private" accessible file
    //              in_PublicFileName
    //                  filename for the "public" accessible file
    // =============================================================================================
    private boolean CopyPrivateToPublicFile(String in_FileName, String in_msgError) {
        boolean ret = true;

        try {
            InputStream in = getAssets().open(Constants.Data.PRIVATE_BASE_DIR + "/" + in_FileName);
            DocumentFile out_file = Globals.input_df.findFile(in_FileName);

            // If the output file doesn't exist, output a stream to it and copy contents over
            if (out_file == null) {
                out_file = Globals.input_df.createFile("text/csv", in_FileName);
                if (out_file != null) {
                    OutputStream out = getContentResolver().openOutputStream(out_file.getUri());

                    if (out != null) {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }

                        out.close();
                    }
                }

            }
        } catch (IOException e) {
            // If anything goes wrong, just use the Private file
            ret = false;

            AppLaunch.this.runOnUiThread(() -> Toast.makeText(AppLaunch.this, in_msgError, Toast.LENGTH_SHORT).show());
        }

        return ret;
    }

    // =============================================================================================
    // Function:    LoadDataFile
    // Description: Read from the .csv data file and populates the data into the in_List.
    //              StartPositionList structure.
    //              If the in_PublicFileName doesn't exist, try to create if from the private one.
    //              If we can't read from the Public file, read from the Private one.
    // Parameters:  in_msgLoading
    //                  String to display to the UI that we're loading the file
    //              in_msgError
    //                  String to display to user if there's an error loading the file
    // Output:      void
    // =============================================================================================
    public void LoadDataFile(String in_fileName, String in_msgLoading, String in_msgError) {
        boolean usePublic;
        String line;
        int index = 1;

        // Ensure the public file exists, and if not, copy the private one there.
        // Return back if we should use the private or public file.
        usePublic = CopyPrivateToPublicFile(in_fileName, in_msgError);

        // Update the loading status
        appLaunchBinding.textStatus.setText(in_msgLoading);

        try {
            // Open up the correct input stream
            InputStream is;

            // If we can use the Public file, open the file, then the stream.  for the Private file, we can open the stream directly.
            if (usePublic) {
                DocumentFile df = Globals.input_df.findFile(in_fileName);
                assert df != null;
                is = getContentResolver().openInputStream(df.getUri());
            } else {
                is = getAssets().open(Constants.Data.PRIVATE_BASE_DIR + "/" + in_fileName);
            }

            // Read in the data
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            br.readLine();
            while ((line = br.readLine()) != null) {
                // Split out the csv line.
                String[] info = line.split(",", -1);

                // A bit messy but we need to know which Global to add the data to, and which fields to pass in.
                // Switch needs a constant in the "case" expression, and complains about using getResources().
                if (in_fileName.equals(getString(R.string.file_climb_positions))) {
                    if (Boolean.parseBoolean(info[1]))
                        Globals.ClimbPositionList.addClimbPositionRow(info[0], info[2]);
                } else if (in_fileName.equals(getString(R.string.file_colors))) {
                    // We don't know how many color codes there will be so re-split the line and pass in the csv of color choices
                    if (Boolean.parseBoolean(info[1])) {
                        String[] info_colors = line.split(",", 4);
                        Globals.ColorList.addColorRow(info[0], info[2], info_colors[3]);
                    }
                } else if (in_fileName.equals(getString(R.string.file_comments))) {
                    if (Boolean.parseBoolean(info[1]))
                        Globals.CommentList.addCommentRow(info[0], info[2]);
                } else if (in_fileName.equals(getString(R.string.file_competitions))) {
                    Globals.CompetitionList.addCompetitionRow(info[0], info[4]);
                } else if (in_fileName.equals(getString(R.string.file_devices))) {
                    Globals.DeviceList.addDeviceRow(info[0], info[1], info[5]);
                } else if (in_fileName.equals(getString(R.string.file_event_groups))) {
                    Globals.EventList.addEventGroup(info[0], info[1]);
                } else if (in_fileName.equals(getString(R.string.file_events))) {
                    Globals.EventList.addEventRow(info[0], info[1], info[3], info[2].toUpperCase(), info[4], info[5], info[6], info[7]);
                } else if (in_fileName.equals(getString(R.string.file_match_types))) {
                    Globals.MatchTypeList.addMatchTypeRow(info[0], info[1], info[2]);
                } else if (in_fileName.equals(getString(R.string.file_matches))) {
                    // Use only the match information that equals the competition we're in.
                    if (Integer.parseInt(info[0]) == Globals.sp.getInt(Constants.Prefs.COMPETITION_ID, -1)) {
                        Globals.CurrentMatchType = Globals.MatchTypeList.getMatchTypeId(info[1]);
                        for (int i = Globals.MatchList.size(); i < Integer.parseInt(info[2]); i++) {
                            Globals.MatchList.addMatchRow(Constants.Matches.NO_MATCH);
                        }
                        Globals.MatchList.addMatchRow(info[1], info[3], info[4], info[5], info[6], info[7], info[8]);
                    }
                } else if (in_fileName.equals(getString(R.string.file_start_positions))) {
                    if (Boolean.parseBoolean(info[1]))
                        Globals.StartPositionList.addStartPositionRow(info[0], info[2]);
                } else if (in_fileName.equals(getString(R.string.file_teams))) {
                    // Need to make sure there's no gaps so the team number and index align
                    for (int i = index; i < Integer.parseInt(info[0]); i++) {
                        Globals.TeamList.add(Constants.Teams.NO_TEAM);
                    }
                    Globals.TeamList.add(info[1]);
                    index = Integer.parseInt(info[0]) + 1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // You can't TOAST on a non-UI thread (like in a Timer)
            AppLaunch.this.runOnUiThread(() -> Toast.makeText(AppLaunch.this, R.string.applaunch_malformed_file, Toast.LENGTH_LONG).show());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // =============================================================================================
    // Function:    initSettings
    // Description: Initialize the Settings button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initSettings() {
        // Define a Image Button to open up the Settings
        appLaunchBinding.imgButSettings.setImageResource(R.drawable.settings_icon);
        appLaunchBinding.imgButSettings.setVisibility(View.INVISIBLE);
        appLaunchBinding.imgButSettings.setClickable(false);
        appLaunchBinding.imgButSettings.setOnClickListener(view -> {
            Intent GoToSettings = new Intent(AppLaunch.this, Settings.class);
            startActivityForResult(GoToSettings, Constants.AppLaunch.ACTIVITY_CODE_SETTINGS);
        });
    }

    // =============================================================================================
    // Function:    initScouting
    // Description: Initialize the Start Scouting button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initScouting() {
        // Define the Start Scouting Button
        appLaunchBinding.butStartScouting.setVisibility(View.INVISIBLE);
        appLaunchBinding.butStartScouting.setClickable(false);
        appLaunchBinding.butStartScouting.setOnClickListener(view -> {
            // Stop the timer
            if (appLaunch_timer != null) {
                appLaunch_timer.cancel();
                appLaunch_timer.purge();
            }

            // Default Globals
            Globals.CurrentTeamOverrideNum = 0;

            if ((Globals.sp == null) ||
                    (Globals.sp.getInt(Constants.Prefs.COMPETITION_ID, -1) == -1) ||
                    (Globals.sp.getInt(Constants.Prefs.DEVICE_ID, -1) == -1)) {
                Toast.makeText(AppLaunch.this, R.string.applaunch_not_configured, Toast.LENGTH_SHORT).show();
            } else {
                // Go to the first page
                Intent GoToPreMatch = new Intent(AppLaunch.this, PreMatch.class);
                startActivity(GoToPreMatch);

                finish();
            }
        });
    }

    // =============================================================================================
    // Function:    initStoragePermissions
    // Description: Initialize the Storage Permissions for the CPR-Scouting public directory
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initStoragePermissions() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        Uri initialUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toURI().toString());
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);

        startActivityForResult(intent, Constants.AppLaunch.ACTIVITY_CODE_STORAGE);
    }
}