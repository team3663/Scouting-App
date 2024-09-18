package com.cpr3663.cpr_scouting_app;

//import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
//import android.content.ContentResolver;
//import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.content.UriPermission;
import android.graphics.Point;
//import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
//import android.provider.DocumentsContract;
//import android.provider.MediaStore;
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
import com.cpr3663.cpr_scouting_app.Settings;

import java.io.BufferedReader;
import java.io.File;
//import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
//import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
//import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AppLaunch extends AppCompatActivity {
    // =============================================================================================
    // Define constants
    // =============================================================================================
    private static final long SPLASH_SCREEN_DELAY = 100; // delay (ms) in between "loading" messages

    // =============================================================================================
    // Global variables
    // =============================================================================================
    private AppLaunchBinding appLaunchBinding;
    public static Timer appLaunch_timer = new Timer();

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 3663 && resultCode == RESULT_OK) {
//            Uri treeUri = data.getData();
//            if (treeUri != null) {
//                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                spe.putString(Settings.SP_PUBLIC_DOCUMENTS_URI, treeUri.toString());
//                spe.apply();
//            }
//        }
//    }

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

        // Get the Shared Preferences where we save off app settings to use next time
        if (Globals.sp == null) Globals.sp = this.getSharedPreferences(getString(R.string.preference_setting_file_key), Context.MODE_PRIVATE);
        if (Globals.spe == null) Globals.spe = Globals.sp.edit();

        // Ask the user to allow permissions to the public/shared files.
//        String uri = sp.getString(Settings.SP_PUBLIC_DOCUMENTS_URI, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toURI().toString());
//        String uri = sp.getString(Settings.SP_PUBLIC_DOCUMENTS_URI, Environment.getExternalStorageDirectory().toURI().toString());
//        String uri = Environment.getExternalStorageDirectory().toURI().toString();
//        Uri uri2 = Uri.parse(uri);
//        MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
//
//        ContentResolver contentresolver = this.getContentResolver();
//        ContentValues contentvalues = new ContentValues();
//        contentvalues.put(MediaStore.MediaColumns.DISPLAY_NAME, "CPR Scouting/Input/test.csv");
//        contentvalues.put(MediaStore.MediaColumns.MIME_TYPE, "plain/text");
////        contentvalues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);
//        OutputStream out;
//        try {
//            Uri testuri = contentresolver.insert(MediaStore.Files.getContentUri("external"), contentvalues);
//            out = contentresolver.openOutputStream((testuri));
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            out.write("this is a test".getBytes(StandardCharsets.UTF_8));
//            out.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        // Check if we have permissions already.  since we're only asking for 1 permission, if the list
        // has anything in it (size > 0) then we're going to be okay.
//        List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
//        boolean perm_found = false;
//        for (UriPermission permission : permissions) {
//            if (permission.getUri().equals(uri2) && permission.isReadPermission() && permission.isWritePermission()) {
//                perm_found = true;
//                break;
//            }
//        }

        // If we don't have permissions, prompt the user to grant it.
//        if (!perm_found) {
//            // Choose a directory using the system's file picker.
//            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
//            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
//            startActivityForResult(intent, 3663);
//        }

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
        if (Globals.TeamList.isEmpty()) {
            // Set a TimerTask to load the data shortly AFTER this OnCreate finishes
            appLaunch_timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Make sure that we aren't coming back to the page and it is the first time running this
                    if (Globals.TeamList.isEmpty()) {
                        // First first index (zero) needs to be a "NO TEAM" entry so the rest line up when they are loaded
                        Globals.TeamList.add(Constants.NO_TEAM);
                        Globals.MatchList.addMatchRow(Constants.NO_MATCH);

                        // Load the data with a BRIEF delay between.  :)
                        try {
                            LoadDataFile(getString(R.string.file_climb_positions), getString(R.string.loading_climb_positions), getString(R.string.file_error_climb_positions));
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                            LoadDataFile(getString(R.string.file_colors), getString(R.string.loading_colors), getString(R.string.file_error_colors));
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                            LoadDataFile(getString(R.string.file_comments), getString(R.string.loading_comments), getString(R.string.file_error_comments));
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                            LoadDataFile(getString(R.string.file_competitions), getString(R.string.loading_competitions), getString(R.string.file_error_competitions));
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                            LoadDataFile(getString(R.string.file_devices), getString(R.string.loading_devices), getString(R.string.file_error_devices));
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                            LoadDataFile(getString(R.string.file_events_auto), getString(R.string.loading_events_auto), getString(R.string.file_error_events_auto));
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                            LoadDataFile(getString(R.string.file_events_teleop), getString(R.string.loading_events_teleop), getString(R.string.file_error_events_teleop));
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                            LoadDataFile(getString(R.string.file_matches), getString(R.string.loading_matches), getString(R.string.file_error_matches));
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                            LoadDataFile(getString(R.string.file_start_positions), getString(R.string.loading_start_positions), getString(R.string.file_error_start_positions));
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                            LoadDataFile(getString(R.string.file_teams), getString(R.string.loading_teams), getString(R.string.file_error_teams));
                            Thread.sleep(SPLASH_SCREEN_DELAY);
                            LoadDataFile(getString(R.string.file_trap_results), getString(R.string.loading_trap_results), getString(R.string.file_error_trap_results));
                            Thread.sleep(SPLASH_SCREEN_DELAY);

                            // We need to build the "Next Events" possible but needs to be done now, after all data is loaded.
                            Globals.EventList.buildNextEvents();
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
    // Output:      Whether to use the public file or not
    // Parameters:  in_PrivateFileName
    //                  filename for the "private" accessible file
    //              in_PublicFileName
    //                  filename for the "public" accessible file
    // =============================================================================================
    private boolean CopyPrivateToPublicFile(String in_PrivateFileName, String in_PublicFileName, String in_msgError) {
        boolean ret = true;

        try {
            InputStream in = getAssets().open(in_PrivateFileName);
            File out_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), in_PublicFileName);

            // Ensure the directory structure exists first
            out_file.getParentFile().mkdirs();

            // If the output file doesn't exist, output a stream to it and copy contents over
            if (!out_file.exists()) {
                if (out_file.createNewFile()) {
                    OutputStream out = Files.newOutputStream(out_file.toPath());

                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
            }
        } catch (IOException e) {
            // If anything goes wrong, just use the Private file
            ret = false;

            AppLaunch.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppLaunch.this, (CharSequence) in_msgError, Toast.LENGTH_SHORT).show();
                }
            });
        }

        return ret;
    }

    // =============================================================================================
    // Function:    LoadDataFile
    // Description: Read from the .csv data file and populates the data into the in_List.
    //              StartPositionList structure.
    //              If the in_PublicFileName doesn't exist, try to create if from the private one.
    //              If we can't read fromthe Public file, read from the Private one.
    // Parameters:  in_msgLoading
    //                  String to display to the UI that we're loading the file
    //              in_msgError
    //                  String to display to user if there's an error loading the file
    // Output:      void
    // =============================================================================================
    public void LoadDataFile(String in_fileName, String in_msgLoading, String in_msgError) {
        boolean usePublic;
        String line = "";
        int index = 1;

        // Ensure the public file exists, and if not, copy the private one there.
        // Return back if we should use the private or public file.
       usePublic = CopyPrivateToPublicFile(getString(R.string.private_path) + "/" + in_fileName, getString(R.string.public_path) + "/" + in_fileName, in_msgError);

        // Update the loading status
        appLaunchBinding.textStatus.setText(in_msgLoading);

        try {
            // Open up the correct input stream
            InputStream is;

            // If we can use the Public file, open the file, then the stream.  for the Private file, we can open the stream directly.
            // We assume this will work (no try/catch) and if THIS fails, it's likely good that we're going to crash the app.  :(
            if (usePublic) {
                File in_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), getString(R.string.public_path) + "/" + in_fileName);
                is = Files.newInputStream(in_file.toPath());
            } else {
                is = getAssets().open(getString(R.string.private_path) + "/" + in_fileName);
            }

            // Read in the data
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                // Split out the csv line.
                String[] info = line.split(",");

                // A bit messy but we need to know which Global to add the data to, and which fields to pass in.
                // Switch needs a constant in the "case" expression, and complains about using getResources().
                if (in_fileName.equals(getString(R.string.file_climb_positions))) {
                    if (Boolean.parseBoolean(info[1]))
                        Globals.ClimbPositionList.addClimbPositionRow(info[0], info[2]);
                }
                else if (in_fileName.equals(getString(R.string.file_colors))) {
                    if (Boolean.parseBoolean(info[1]))
                        Globals.ColorList.addColorRow(info[0], info[2], info[3], info[4]);
                }
                else if (in_fileName.equals(getString(R.string.file_comments))) {
                    if (Boolean.parseBoolean(info[1]))
                        Globals.CommentList.addCommentRow(info[0], info[2], info[3]);
                }
                else if (in_fileName.equals(getString(R.string.file_competitions))) {
                    Globals.CompetitionList.addCompetitionRow(info[0], info[1]);
                }
                else if (in_fileName.equals(getString(R.string.file_devices))) {
                    Globals.DeviceList.addDeviceRow(info[0], info[1], info[2]);
                }
                else if (in_fileName.equals(getString(R.string.file_events_auto))) {
                    Globals.EventList.addEventRow(info[0], info[1], Constants.PHASE_AUTO, info[2], info[3], info[4]);
                }
                else if (in_fileName.equals(getString(R.string.file_events_teleop))) {
                    Globals.EventList.addEventRow(info[0], info[1], Constants.PHASE_TELEOP, info[2], info[3], info[4]);
                }
                else if (in_fileName.equals(getString(R.string.file_matches))) {
                    // Use only the match information that equals the competition we're in.
                    if (Integer.parseInt(info[0]) == Globals.sp.getInt(Constants.SP_COMPETITION_ID, -1)) {
                        for (int i = index; i < Integer.parseInt(info[1]); i++) {
                            Globals.MatchList.addMatchRow(Constants.NO_MATCH);
                        }
                        Globals.MatchList.addMatchRow(info[2], info[3], info[4], info[5], info[6], info[7]);
                        index = Integer.parseInt(info[1]) + 1;
                    }
                }
                else if (in_fileName.equals(getString(R.string.file_start_positions))) {
                    if (Boolean.parseBoolean(info[1]))
                        Globals.StartPositionList.addStartPositionRow(info[0], info[2]);
                }
                else if (in_fileName.equals(getString(R.string.file_teams))) {
                    // Need to make sure there's no gaps so the team number and index align
                    for (int i = index; i < Integer.parseInt(info[0]); i++) {
                        Globals.TeamList.add(Constants.NO_TEAM);
                    }
                    Globals.TeamList.add(info[1]);
                    index = Integer.parseInt(info[0]) + 1;
                }
                else if (in_fileName.equals(getString(R.string.file_trap_results))) {
                    if (Boolean.parseBoolean(info[1]))
                        Globals.TrapResultsList.addTrapResultRow(info[0], info[2]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}