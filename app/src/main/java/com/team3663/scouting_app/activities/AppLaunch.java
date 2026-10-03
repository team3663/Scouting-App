package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.UriPermission;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.AppLaunchBinding;
import com.team3663.scouting_app.utility.CPR_Network;
import com.team3663.scouting_app.dataFile.*;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AppLaunch extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private AppLaunchBinding appLaunchBinding;
    public static Timer appLaunch_timer = new Timer();

    // Installed app version as {major, minor, patch}, used to detect a newer APK on Google Drive
    private int[] currentVersion = new int[]{0, 0, 0};
    // Guards against kicking off the data-file load more than once across the async update flow
    private boolean dataLoadStarted = false;

    ActivityResultLauncher<Intent> settingsActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    if (Objects.requireNonNull(data).getIntExtra(Constants.Settings.RELOAD_DATA_KEY, 0) == 1) {
                        loadDataFiles();
                    }
                }
            });

    ActivityResultLauncher<Intent> storageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    Uri treeUri = Objects.requireNonNull(data).getData();
                    if (treeUri != null) {
                        getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        Globals.spe.putString(Constants.Prefs.STORAGE_URI, treeUri.toString());
                        Globals.spe.apply();
                        Globals.baseStorageURI = treeUri;
                        initDataFiles();
                    }

                    loadDataFiles();
                }
            });

    // Receives the result of the interactive Google sign-in flow used by the update check.
    // The sign-in handling lives in CPR_Network; here we only react to success/failure: on success
    // run the update check, otherwise report the status code and continue loading data.
    ActivityResultLauncher<IntentSenderRequest> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                int status = Globals.network.completeSignIn(result.getData());
                if (status == CPR_Network.SIGN_IN_OK) {
                    runUpdateCheck();
                } else {
                    Toast.makeText(this, getString(R.string.applaunch_update_signin_failed, status), Toast.LENGTH_LONG).show();
                    proceedToDataLoad();
                }
            });

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

        // Default to NO internet
        appLaunchBinding.imageInternet.setVisibility(View.INVISIBLE);
        Globals.network = new CPR_Network(this);

        // Display app version
        PackageInfo pInfo;
        try {
            pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        appLaunchBinding.textVersion.setText(getString(R.string.app_version) + " " + pInfo.versionName);

        // Parse the installed version (e.g. "3.6.2") into {major, minor, patch} for update comparison
        currentVersion = parseVersion(pInfo.versionName);

        // Get the Shared Preferences where we save off app settings to use next time
        if (Globals.sp == null)
            Globals.sp = this.getSharedPreferences(getString(R.string.preference_setting_file_key), Context.MODE_PRIVATE);
        if (Globals.spe == null) Globals.spe = Globals.sp.edit();

        // Initialize activity components
        initSettings();
        initScouting();

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        appLaunchBinding.textBanner.startAnimation(fadeIn);

        // check if we have access to the internet
        if (Globals.network.hasActiveInternet())
            appLaunchBinding.imageInternet.setVisibility(View.VISIBLE);
        else
            appLaunchBinding.imageInternet.setVisibility(View.INVISIBLE);

        // Before reading the data files, check Google Drive for a newer version of this app.
        // When the check resolves (no update, can't check, or the download fails), the data files
        // are loaded.  If a newer version is found it is downloaded and the installer is launched.
        startUpdateCheckThenLoad();

        // Ensure we have VPN client running.
        Globals.network.ensureVPN();
    }

    // =============================================================================================
    // Function:    parseVersion
    // Description: Parse a "major.minor.patch" version string into an int[]{major, minor, patch}.
    //              Any missing or non-numeric component defaults to 0.
    // Parameters:  in_versionName  the version string (e.g. "3.6.2")
    // Output:      int[] of length 3
    // =============================================================================================
    private int[] parseVersion(String in_versionName) {
        int[] version = new int[]{0, 0, 0};
        if (in_versionName == null) return version;

        String[] parts = in_versionName.split("\\.");
        for (int i = 0; i < 3 && i < parts.length; i++) {
            try {
                version[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                version[i] = 0;
            }
        }
        return version;
    }

    // =============================================================================================
    // Function:    startUpdateCheckThenLoad
    // Description: Entry point for the "check for newer APK before loading data" flow.  Requires
    //              internet and a Google account with the Drive scope.  Reuses an existing sign-in
    //              when possible; otherwise launches the interactive sign-in.  If we can't check
    //              (offline), we skip straight to loading the data files.
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void startUpdateCheckThenLoad() {
        // No internet -> nothing to check, just load the data
        if (!Globals.network.hasActiveInternet()) {
            proceedToDataLoad();
            return;
        }

        // Authorize for Drive (silently if the scope was already granted); run the update check once
        // ready, or launch the consent flow (its result is handled by googleSignInLauncher above).
        Globals.network.authorizeDrive(new CPR_Network.AuthCallback() {
            @Override
            public void onAuthorized() {
                runUpdateCheck();
            }

            @Override
            public void onNeedsConsent(@NonNull IntentSender in_intentSender) {
                googleSignInLauncher.launch(new IntentSenderRequest.Builder(in_intentSender).build());
            }

            @Override
            public void onFailed(int in_statusCode) {
                Toast.makeText(AppLaunch.this, getString(R.string.applaunch_update_signin_failed, in_statusCode), Toast.LENGTH_LONG).show();
                proceedToDataLoad();
            }
        });
    }

    // =============================================================================================
    // Function:    runUpdateCheck
    // Description: Ask CPR_Network to scan the Google Drive download folder for a newer APK.  If one
    //              is found, download it and launch the system installer; otherwise load the data.
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void runUpdateCheck() {
        Toast.makeText(this, R.string.applaunch_update_checking, Toast.LENGTH_SHORT).show();

        Globals.network.checkForAppUpdate(currentVersion, new CPR_Network.UpdateCheckCallback() {
            @Override
            public void onUpdateAvailable(@NonNull CPR_Network.AppUpdateInfo in_info) {
                String versionText = in_info.version[0] + "." + in_info.version[1] + "." + in_info.version[2];
                Toast.makeText(AppLaunch.this, getString(R.string.applaunch_update_found, versionText), Toast.LENGTH_LONG).show();

                Globals.network.downloadApkToDownloads(in_info, in_apkUri -> {
                    if (in_apkUri != null) installApk(in_apkUri);
                    else Toast.makeText(AppLaunch.this, R.string.applaunch_update_download_failed, Toast.LENGTH_LONG).show();

                    // Load the data regardless, so the app is usable if the user cancels the installation.
                    // If the installation succeeds the app is replaced and restarted anyway.
                    proceedToDataLoad();
                });
            }

            @Override
            public void onNoUpdate() {
                proceedToDataLoad();
            }
        });
    }

    // =============================================================================================
    // Function:    installApk
    // Description: Launch the system package installer for a downloaded APK.  The installer shows
    //              its own confirmation prompt.  If this app isn't allowed to install unknown apps,
    //              send the user to the system setting to grant it.
    // Parameters:  in_apkUri  the content Uri of the downloaded APK
    // Output:      void
    // =============================================================================================
    private void installApk(Uri in_apkUri) {
        // On modern Android the user must grant this app permission to install unknown apps
        if (!getPackageManager().canRequestPackageInstalls()) {
            Toast.makeText(this, R.string.applaunch_update_install_sources, Toast.LENGTH_LONG).show();
            Intent sources = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
            sources.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(sources);
            return;
        }

        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(in_apkUri, Constants.AppLaunch.APK_MIME_TYPE);
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(install);
    }

    // =============================================================================================
    // Function:    proceedToDataLoad
    // Description: Load the data files (or request storage permissions first).  Safe to call from
    //              any of the async update-flow callbacks; it only runs once per launch.
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void proceedToDataLoad() {
        if (dataLoadStarted) return;
        dataLoadStarted = true;

        // Find out if we have permissions.  Since our app is only requesting one location, if it's not empty, we're good
        List<UriPermission> perm_list = getContentResolver().getPersistedUriPermissions();
        boolean havePerms = !perm_list.isEmpty();

        // If we have storage permissions go ahead and load the data.
        // Otherwise, initiate getting permissions (which will then load the data afterward)
        if (havePerms) {
            Globals.baseStorageURI = Uri.parse(Globals.sp.getString(Constants.Prefs.STORAGE_URI, null));
            initDataFiles();
            loadDataFiles();
        } else
            initStoragePermissions();

        // While loading Matches, we messed with Globals.CurrentMatchType, so reset it
        Globals.CurrentMatchType = Constants.PreMatch.DEFAULT_MATCH_TYPE;
    }

    // =============================================================================================
    // Function:    initSettings
    // Description: Initialize the Settings button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initSettings() {
        // Define an Image Button to open up the Settings
        appLaunchBinding.imgButSettings.setImageResource(R.drawable.settings_icon);
        appLaunchBinding.imgButSettings.setVisibility(View.INVISIBLE);
        appLaunchBinding.imgButSettings.setClickable(false);
        appLaunchBinding.imgButSettings.setOnClickListener(view -> {
            Intent GoToSettings = new Intent(AppLaunch.this, Settings.class);
            settingsActivityResultLauncher.launch(GoToSettings);
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
            Globals.CurrentOverrideTeamNum = "";

            if ((Globals.sp == null) ||
                    (Globals.sp.getInt(Constants.Prefs.COMPETITION_ID, -1) == -1) ||
                    (Globals.sp.getInt(Constants.Prefs.DEVICE_ID, -1) == -1) ||
                    (Globals.sp.getInt(Constants.Prefs.QR_SIZE, -1) == -1)) {
                Toast.makeText(AppLaunch.this, R.string.applaunch_not_configured, Toast.LENGTH_SHORT).show();
            } else {
                // Pin the app to help prevent it from being closed mid-match
                startLockTask();

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
        storageActivityResultLauncher.launch(intent);
    }

    // =============================================================================================
    // Function:    initDataFiles
    // Description: Initialize the Global variables for the data files
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initDataFiles() {
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
            // The base dir is there, but we should check the two subdirectories are there.
            Globals.input_df = Globals.base_df.findFile(Constants.Data.PUBLIC_INPUT_DIR);
            if (Globals.input_df == null)
                Globals.input_df = Globals.base_df.createDirectory(Constants.Data.PUBLIC_INPUT_DIR);
            Globals.output_df = Globals.base_df.findFile(Constants.Data.PUBLIC_OUTPUT_DIR);
            if (Globals.output_df == null)
                Globals.output_df = Globals.base_df.createDirectory(Constants.Data.PUBLIC_OUTPUT_DIR);
        }

        // Instantiate the Global variables or reset their context
        if (Globals.AccuracyList == null) Globals.AccuracyList = new AccuracyFile(this); else Globals.AccuracyList.setContext(this);
        if (Globals.ClimbLevelList == null) Globals.ClimbLevelList = new ClimbLevelFile(this); else Globals.ClimbLevelList.setContext(this);
        if (Globals.ClimbPositionList == null) Globals.ClimbPositionList = new ClimbPositionFile(this); else Globals.ClimbPositionList.setContext(this);
        if (Globals.ColorList == null) Globals.ColorList = new ColorsFile(this); else Globals.ColorList.setContext(this);
        if (Globals.CommentList == null) Globals.CommentList = new CommentsFile(this); else Globals.CommentList.setContext(this);
        if (Globals.CompetitionList == null) Globals.CompetitionList = new CompetitionsFile(this); else Globals.CompetitionList.setContext(this);
        if (Globals.DeviceList == null) Globals.DeviceList = new DevicesFile(this); else Globals.DeviceList.setContext(this);
        if (Globals.EventGroupList == null) Globals.EventGroupList = new EventGroupsFile(this); else Globals.EventGroupList.setContext(this);
        if (Globals.EventList == null) Globals.EventList = new EventsFile(this); else Globals.EventList.setContext(this);
        if (Globals.MatchTypeList == null) Globals.MatchTypeList = new MatchTypesFile(this); else Globals.MatchTypeList.setContext(this);
        if (Globals.MatchList == null) Globals.MatchList = new MatchesFile(this); else Globals.MatchList.setContext(this);
        if (Globals.TeamList == null) Globals.TeamList = new TeamsFile(this); else Globals.TeamList.setContext(this);
    }

    // =============================================================================================
    // Function:    loadDataFiles
    // Description: Load the data files into the Global variables
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void loadDataFiles() {
        // Ensure the loading file objects are visible
        appLaunchBinding.progressBarOverall.setVisibility(View.VISIBLE);
        appLaunchBinding.progressBarFile.setVisibility(View.VISIBLE);
        appLaunchBinding.textStatusOverall.setVisibility(View.VISIBLE);
        appLaunchBinding.textPercentOverall.setVisibility(View.VISIBLE);
        appLaunchBinding.textStatusFile.setVisibility(View.VISIBLE);
        appLaunchBinding.textPercentFile.setVisibility(View.VISIBLE);
        appLaunchBinding.butStartScouting.setVisibility(View.INVISIBLE);
        appLaunchBinding.imgButSettings.setVisibility(View.INVISIBLE);
        appLaunchBinding.butStartScouting.setClickable(false);
        appLaunchBinding.imgButSettings.setClickable(false);

        // Clear out the lists, just in case
        _DataFile.clearAllLists();

        // Load the data files
        // Set a TimerTask to load the data shortly AFTER this OnCreate finishes
        appLaunch_timer.schedule(new TimerTask() {
            @Override
            public void run() {
                appLaunchBinding.progressBarOverall.setMax(Globals.CompetitionList.getNumberOfFiles());
                appLaunchBinding.progressBarOverall.setProgress(0);
                appLaunchBinding.textStatusOverall.setText(getString(R.string.applaunch_loading_prefix));
                appLaunchBinding.textPercentOverall.setText(getString(R.string.applaunch_percent, 0));

                // Load all the data with a BRIEF delay between.  :)
                _DataFile.LoadAllDataFiles(appLaunchBinding.textStatusFile, appLaunchBinding.progressBarFile, appLaunchBinding.textPercentFile, appLaunchBinding.progressBarOverall, appLaunchBinding.textPercentOverall);

                // After loading all the data, we need to build the set of "next events"
                Globals.EventList.buildNextEvents();

                // Setting the Visibility attribute can't be set from a non-UI thread (like withing a TimerTask
                // that runs on a separate thread).  So we need to make a Runner that will execute on the UI thread
                // to set these.
                AppLaunch.this.runOnUiThread(() -> {
                    // Sleep a tiny bit to help the UI not glitch. Otherwise, this block of code doesn't
                    // do what it's trying to do (things don't become visible, etc.).
                    try {
                        Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);
                    }
                    catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    // Hide the loading file objects
                    appLaunchBinding.progressBarOverall.setVisibility(View.INVISIBLE);
                    appLaunchBinding.progressBarFile.setVisibility(View.INVISIBLE);
                    appLaunchBinding.textStatusOverall.setVisibility(View.INVISIBLE);
                    appLaunchBinding.textPercentOverall.setVisibility(View.INVISIBLE);
                    appLaunchBinding.textStatusFile.setVisibility(View.INVISIBLE);
                    appLaunchBinding.textPercentFile.setVisibility(View.INVISIBLE);
                    appLaunchBinding.butStartScouting.setVisibility(View.VISIBLE);
                    appLaunchBinding.imgButSettings.setVisibility(View.VISIBLE);
                    appLaunchBinding.butStartScouting.setClickable(true);
                    appLaunchBinding.imgButSettings.setClickable(true);

                    // Erase the status text
                    appLaunchBinding.textStatusFile.setText("");
                    appLaunchBinding.textStatusOverall.setText("");
                    appLaunchBinding.textPercentFile.setText("");
                    appLaunchBinding.textPercentOverall.setText("");

                    // Enable the start scouting button and settings button
                    appLaunchBinding.butStartScouting.setClickable(true);
                    appLaunchBinding.imgButSettings.setClickable(true);
                });
            }
        }, Constants.AppLaunch.SPLASH_SCREEN_DELAY);
    }
}