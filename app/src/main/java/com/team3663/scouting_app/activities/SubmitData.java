package com.team3663.scouting_app.activities;

import static com.team3663.scouting_app.config.Constants.Achievements.ANIMATION_SCALE_DURATION;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.google.android.material.button.MaterialButton;
import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.SubmitDataBinding;
import com.team3663.scouting_app.utility.CPR_Network;
import com.team3663.scouting_app.utility.Logger;
import com.team3663.scouting_app.utility.achievements.Achievements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class SubmitData extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private SubmitDataBinding submitDataBinding;
    private static final Timer achievement_timer = new Timer();
    private static MediaPlayer media;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ActivityResultLauncher<IntentSenderRequest> googleSignInLauncher;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle in_savedInstanceState) {
        super.onCreate(in_savedInstanceState);
        EdgeToEdge.enable(this);
        submitDataBinding = SubmitDataBinding.inflate(getLayoutInflater());
        View page_root_view = submitDataBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(submitDataBinding.submitData, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Register the Google sign-in launcher before the activity is STARTED
        initGoogleSignIn();

        // Initialize activity components that need the Logger
        initAchievements();

        // We're done with the logger (only if not null - it can be null if we're resubmitting data from Pre-Match)
        if (Globals.EventLogger != null) {
            Globals.EventLogger.WriteOutFiles();
            Globals.EventLogger.close();
            Globals.EventLogger = null;
        }

        // Initialize activity components that don't log anything
        initNetwork();
        initMatchType();
        initMatch();
        initQR();
        initBluetooth();
        initGoogle();
        initDatabase();
        initQuit();
        initNext();
        initOverride();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Important: Unregister to avoid memory leaks
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    // =============================================================================================
    // Function:    initMatchType
    // Description: Initialize the Match Type field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initMatchType() {
        // If there's no files, return nothing
        if (Globals.FileList.isEmpty()) {
            Logger.SearchForFiles();
            if (Globals.FileList.isEmpty()) return;
        }

        // Add the match types from the log files to the list.  Use a hashmap so we don't have to track
        // what we already found.
        HashMap<String, Integer> file_types = new HashMap<>();

        for (String file_name : Globals.FileList.keySet()) {
            String[] file_parts = file_name.split("_");
            file_types.put(Globals.MatchTypeList.getMatchTypeDescription(file_parts[3].substring(0,1)), 1);
        }

        // Convert HashMap to an ArrayList and use in the spinner
        ArrayList<String> match_types = new ArrayList<>(file_types.keySet());
        ArrayAdapter<String> adp_MatchType = new ArrayAdapter<>(this, R.layout.cpr_spinner, match_types);
        adp_MatchType.setDropDownViewResource(R.layout.cpr_spinner_item);
        submitDataBinding.spinnerMatchType.setAdapter(adp_MatchType);

        Globals.TransmitMatchType = Globals.CurrentMatchType;

        // Search through the list of match types until you find the one that is correct then get its position in the list
        // and set that one as selected
        int start_Pos_DropId = 0;
        for (int i = 0; i < match_types.size(); i++) {
            if (match_types.get(i).equals(Globals.MatchTypeList.getMatchTypeDescription(Globals.TransmitMatchType))) {
                start_Pos_DropId = i;
                break;
            }
        }
        submitDataBinding.spinnerMatchType.setSelection(start_Pos_DropId);

        // Set up a listener to handle any changes to the dropdown
        submitDataBinding.spinnerMatchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Save off what you selected to be used until changed again
                String newMatchType = Globals.MatchTypeList.getMatchTypeShortForm(submitDataBinding.spinnerMatchType.getSelectedItem().toString());

                if (!Objects.equals(newMatchType, Globals.TransmitMatchType)) {
                    Globals.TransmitMatchType = newMatchType;
                    initMatch();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    // =============================================================================================
    // Function:    FindMatches
    // Description: Search through the output csv files (logger files) and parse through them to get
    //              a list of matches that we can submit from.
    // Parameters:  void
    // Output:      ArrayList<String> - list of match numbers sorts numerically
    // =============================================================================================
    private ArrayList<String> FindMatches() {
        ArrayList<Integer> ret_int = new ArrayList<>();
        ArrayList<String> ret = new ArrayList<>();

        // If there's no files, return nothing
        if (Globals.FileList.isEmpty()) {
            Logger.SearchForFiles();
            if (Globals.FileList.isEmpty()) return ret;
        }

        // Parse out the match number from the filename.  If this is a "d" file from the right
        // competition (as defined in Settings) and matching device then add it to the list.
        // Use a regular expression to ensure the file_part IS numeric.  Otherwise parseInt() will
        // throw an exception.
        for (String file_name : Globals.FileList.keySet()) {
            if (file_name.endsWith("_" + Globals.TransmitMatchType + ".csv")) {
                String[] file_parts = file_name.split("_");
                if (file_parts[0].trim().matches("-?\\d+(\\.\\d+)?") &&
                        file_parts[1].trim().matches("-?\\d+(\\.\\d+)?") &&
                        file_parts[2].trim().matches("-?\\d+(\\.\\d+)?") &&
                        (Integer.parseInt(file_parts[0]) == Globals.CurrentCompetitionId) &&
                        (Integer.parseInt(file_parts[2]) == Globals.CurrentDeviceId))
                    ret_int.add(Integer.parseInt(file_parts[1]));
            }
        }

        // Sort the list (numerically) and then copy into the String version
        Collections.sort(ret_int);
        for (Integer i : ret_int) ret.add(i.toString());
        return ret;
    }

    // =============================================================================================
    // Class:       popOneAndGo_TimerTask
    // Description: Called within a timer, pop one achievement with it's own timing for showing and
    //              removing.
    // =============================================================================================
    private class popOneAndGo_TimerTask extends TimerTask {
        private final Achievements.PoppedAchievement myAchievement;
        private final boolean isLast;


        popOneAndGo_TimerTask(Achievements.PoppedAchievement in_pa, boolean in_isLast) {
            this.myAchievement = in_pa;
            this.isLast = in_isLast;
        }

        @Override
        public void run() {
            achievement_timer.schedule(new AchievementTimerTaskStart(myAchievement), 500);
            achievement_timer.schedule(new AchievementTimerTaskEnd(isLast), Constants.Achievements.DISPLAY_TIME + 500);
        }
    }

    // =============================================================================================
    // Class:       AchievementTimerTaskStart
    // Description: Defines the TimerTask trigger for when we need to start showing an achievement
    // =============================================================================================
    private class AchievementTimerTaskStart extends TimerTask {
        private final Achievements.PoppedAchievement myAchievement;

        AchievementTimerTaskStart(Achievements.PoppedAchievement in_Achievement) {
            myAchievement = in_Achievement;
        }

        @Override
        public void run() {
            SubmitData.this.runOnUiThread(() -> {
                submitDataBinding.textAchievementTitle.setText(myAchievement.title);
                submitDataBinding.textAchievementDesc.setText(myAchievement.description);

                animateAchievementStart();
            });

//                in_submitDataBinding.imageAchievement.startAnimation(animation);
//                in_submitDataBinding.textAchievement.startAnimation(animation);
//                in_submitDataBinding.imageAchievement.clearAnimation();
//                in_submitDataBinding.textAchievement.clearAnimation();

            media.start();
        }
    }

    // =============================================================================================
    // Class:       AchievementTimerTaskEnd
    // Description: Defines the TimerTask trigger for when we need to end showing an achievement
    // =============================================================================================
    private class AchievementTimerTaskEnd extends TimerTask {
        private final boolean isLast;

        AchievementTimerTaskEnd(boolean in_isLast) {
            this.isLast = in_isLast;
        }

        @Override
        public void run() {
            SubmitData.this.runOnUiThread(SubmitData.this::animateAchievementEnd);

            if (isLast) closeAchievements();
        }
    }

    // =============================================================================================
    // Function:    closeAchievements
    // Description: Close and finish the activity
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void closeAchievements() {
        if (media.isPlaying()) media.stop();
        media.reset();
        media.release();
    }

    // =============================================================================================
    // Function:    updateWifiIcon
    // Description: Update the Wi-Fi signal icon with the correct signal strength level
    // Parameters:  in_level    signal strength level
    // Output:      void
    // =============================================================================================
    private void updateWifiIcon(int in_level) {
        // You would typically swap icons here based on level
        // 0: No signal, 1-4: Signal bars
        switch (in_level) {
            case 4: submitDataBinding.imageWifiSignal.setImageResource(R.drawable.wifi_bar_4); break;
            case 3: submitDataBinding.imageWifiSignal.setImageResource(R.drawable.wifi_bar_3); break;
            case 2: submitDataBinding.imageWifiSignal.setImageResource(R.drawable.wifi_bar_2); break;
            case 1: submitDataBinding.imageWifiSignal.setImageResource(R.drawable.wifi_bar_1); break;
            default: submitDataBinding.imageWifiSignal.setImageResource(R.drawable.wifi_bar_0); break;
        }

        // check if we have access to the internet
        if (Globals.network.hasActiveInternet()) {
            submitDataBinding.imageInternet.setVisibility(View.VISIBLE);
            submitDataBinding.butSendGoogle.setEnabled(true);
            submitDataBinding.butSendDatabase.setEnabled(true);
        } else {
            submitDataBinding.imageInternet.setVisibility(View.INVISIBLE);
            submitDataBinding.butSendGoogle.setEnabled(false);
            submitDataBinding.butSendDatabase.setEnabled(false);
        }
    }

    // =============================================================================================
    // Function:    initOverride
    // Description: Override team number reset for next match
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initOverride() {
        Globals.CurrentOverrideTeamNum = "";
    }

    // =============================================================================================
    // Function:    initAchievements
    // Description: Initialize the Achievements system
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initAchievements() {
        ArrayList<Achievements.PoppedAchievement> pop_list = Achievements.popAchievements();

        // Keep Achievements invisible
        submitDataBinding.imageAchievementOpen.setVisibility(View.INVISIBLE);
        submitDataBinding.imageAchievement.setVisibility(View.INVISIBLE);
        submitDataBinding.textAchievementTitle.setVisibility(View.INVISIBLE);
        submitDataBinding.textAchievementDesc.setVisibility(View.INVISIBLE);

        media = MediaPlayer.create(this, R.raw.achievement);
        media.setVolume(1,1);

        // If achievement need to be popped, first log them, and then set up a timer to show them.
        // If the logger doesn't exist, wait to pop the achievement until we do. (next match?)
        // Because we try to log the achievement which will fail if there's no valid logger.
        if (!pop_list.isEmpty() && Globals.EventLogger != null) {
            StringBuilder ach_sep_ID = new StringBuilder();

            for (int achievement_index = 0; achievement_index < pop_list.size(); achievement_index++) {
                Achievements.PoppedAchievement pa = pop_list.get(achievement_index);
                ach_sep_ID.append(":").append(pa.id);
                achievement_timer.schedule(new popOneAndGo_TimerTask(pa, achievement_index == pop_list.size() - 1), Constants.Achievements.START_DELAY + (long) Constants.Achievements.DISPLAY_TIME * achievement_index + (long) Constants.Achievements.IN_BETWEEN_DELAY * achievement_index);
            }

            if (!pop_list.isEmpty()) {
                ach_sep_ID = new StringBuilder(ach_sep_ID.substring(1));
                Globals.EventLogger.LogData(Constants.Logger.LOGKEY_ACHIEVEMENT, ach_sep_ID.toString());
            }
        }
    }

    public void animateAchievementStart() {
        // Show the opening logo overlay
        submitDataBinding.imageAchievementOpen.setVisibility(View.VISIBLE);

        //scale opening image at beginning
        submitDataBinding.imageAchievementOpen.animate().scaleX(Constants.Achievements.openingAnimationScaleValue).scaleY(Constants.Achievements.openingAnimationScaleValue).setDuration(ANIMATION_SCALE_DURATION)
                .withEndAction(() -> submitDataBinding.imageAchievementOpen.animate().scaleX(1.0f).scaleY(1.0f).setDuration(ANIMATION_SCALE_DURATION));

        // set pivot to a little bit in from the left
        submitDataBinding.imageAchievement.setPivotX(20f);

        // Set the achievement image visibility and shrink it to 0 immediately
        submitDataBinding.imageAchievement.setScaleX(0.0f);
        submitDataBinding.imageAchievement.setVisibility(View.VISIBLE);

        // Scale it up to full size
        submitDataBinding.imageAchievement.animate()
                .scaleX(1.0f)
                .setDuration((long)(ANIMATION_SCALE_DURATION * 1.5))
                .withEndAction(() -> {
                    // Reveal the text details after the scaling finishes
                    submitDataBinding.imageAchievement.setVisibility(View.VISIBLE);
                    submitDataBinding.textAchievementTitle.setVisibility(View.VISIBLE);
                    submitDataBinding.textAchievementDesc.setVisibility(View.VISIBLE);
                })
                .start();
    }

    // hiding achievements
    public void animateAchievementEnd() {
        // hide all achievement elements while keeping opener visible
        submitDataBinding.imageAchievementOpen.setVisibility(View.VISIBLE);
        submitDataBinding.imageAchievementOpen.animate().scaleX(Constants.Achievements.openingAnimationScaleValue).scaleY(Constants.Achievements.openingAnimationScaleValue).setDuration(ANIMATION_SCALE_DURATION / 2);
        submitDataBinding.textAchievementDesc.setVisibility(View.INVISIBLE);
        submitDataBinding.textAchievementTitle.setVisibility(View.INVISIBLE);
        submitDataBinding.imageAchievement.animate()
                .scaleX(0.18f)
                .setDuration(ANIMATION_SCALE_DURATION + (ANIMATION_SCALE_DURATION / 2))
                        .withEndAction(() -> {
                            submitDataBinding.imageAchievement.setVisibility(View.INVISIBLE);
                            submitDataBinding.imageAchievementOpen.animate().scaleX(0.0f).scaleY(0.0f).setDuration(ANIMATION_SCALE_DURATION / 2).start();
                        });

    }

    // =============================================================================================
    // Function:    initMatch
    // Description: Initialize the Match field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initMatch() {
        submitDataBinding.spinnerMatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Save off what you selected to be used until changed again
                Globals.TransmitMatchNum = Integer.parseInt(submitDataBinding.spinnerMatch.getSelectedItem().toString());
                submitDataBinding.imageGoogleResult.setImageResource(0);
                submitDataBinding.imageDatabaseResult.setImageResource(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Adds the items from the match log files array to the list
        ArrayAdapter<String> adp_Match = new ArrayAdapter<>(this,
                R.layout.cpr_spinner, FindMatches());
        adp_Match.setDropDownViewResource(R.layout.cpr_spinner_item);
        submitDataBinding.spinnerMatch.setAdapter(adp_Match);
        // Set the selection (if there are any) to the latest match (largest value in the list)
        if (adp_Match.getCount() > 0) {
            submitDataBinding.spinnerMatch.setSelection(adp_Match.getCount() - 1, true);
            Globals.TransmitMatchNum = Integer.parseInt(submitDataBinding.spinnerMatch.getSelectedItem().toString());
        }
    }

    // =============================================================================================
    // Function:    initNetwork
    // Description: Initialize the network related fields and process
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initNetwork() {
        // setup Wi-Fi monitoring
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(@NonNull Network in_network, @NonNull NetworkCapabilities in_capabilities) {
                int rssi = 0;

                // On API 31+, WifiInfo is part of the capabilities (requires location permission)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    WifiInfo wifiInfo = (WifiInfo) in_capabilities.getTransportInfo();
                    if (wifiInfo != null) rssi = wifiInfo.getRssi();
                } else {
                    // Fallback for API 30
                    WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    rssi = wm.getConnectionInfo().getRssi();
                }

                // Convert RSSI to a signal level (0 to 4)
                int level = WifiManager.calculateSignalLevel(rssi, 5);

                // Update UI on the main thread
                runOnUiThread(() -> updateWifiIcon(level));
            }

            @Override
            public void onLost(@NonNull Network in_network) {
                // Signal lost or Wi-Fi turned off
                runOnUiThread(() -> updateWifiIcon(-1));
            }
        };

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    // =============================================================================================
    // Function:    initQR
    // Description: Initialize the QR Code field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initQR() {
        submitDataBinding.butQRCode.setOnClickListener(view -> {
            // Reset pre-Match settings for next time
            Globals.numStartingGamePiece = Constants.PreMatch.STARTING_GAME_PIECES;
            Globals.isPractice = false;
            Globals.TransmitMatchNum = Integer.parseInt(submitDataBinding.spinnerMatch.getSelectedItem().toString());

            Intent GoToQRCode = new Intent(SubmitData.this, QRCode.class);
            startActivity(GoToQRCode);

            finish();
        });
    }

    // =============================================================================================
    // Function:    initBluetooth
    // Description: Initialize the Bluetooth field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initBluetooth() {
        // Until we have BT working...
        submitDataBinding.butSendBT.setEnabled(false);

        submitDataBinding.butSendBT.setOnClickListener(view -> {
            // Reset pre-Match settings for next time
            Globals.numStartingGamePiece = Constants.PreMatch.STARTING_GAME_PIECES;
            Globals.isPractice = false;
            Globals.TransmitMatchNum = Integer.parseInt(submitDataBinding.spinnerMatch.getSelectedItem().toString());

//            Intent GoToBluetooth = new Intent(SubmitData.this, Bluetooth.class);
//            startActivity(GoToBluetooth);

            finish();
        });
    }

    // =============================================================================================
    // Function:    initGoogle
    // Description: Initialize the Google field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initGoogle() {
        if (!Globals.network.hasActiveInternet()) {
            submitDataBinding.butSendGoogle.setEnabled(false);
            return;
        }

        submitDataBinding.butSendGoogle.setOnClickListener(view -> {
            Globals.TransmitMatchNum = Integer.parseInt(submitDataBinding.spinnerMatch.getSelectedItem().toString());
            buttonClickUx(submitDataBinding.butSendGoogle, submitDataBinding.imageGoogleResult);

            // Authorize for Drive (silently if the scope was already granted); upload once ready,
            // or launch the consent flow whose result is handled by googleSignInLauncher.
            Globals.network.authorizeDrive(new CPR_Network.AuthCallback() {
                @Override
                public void onAuthorized() {
                    Globals.network.uploadToGoogle(SubmitData.this::handleGoogleUploadResult);
                }

                @Override
                public void onNeedsConsent(@NonNull IntentSender in_intentSender) {
                    googleSignInLauncher.launch(new IntentSenderRequest.Builder(in_intentSender).build());
                }

                @Override
                public void onFailed(int in_statusCode) {
                    Toast.makeText(SubmitData.this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                    handleGoogleUploadResult(CPR_Network.Result.TRANSMISSION_FAILURE);
                }
            });
        });
    }

    // =============================================================================================
    // Function:    handleGoogleUploadResult
    // Description: Handle the result of a Google Drive upload
    // Parameters:  result  the result of the upload
    // Output:      void
    // =============================================================================================
    private void handleGoogleUploadResult(CPR_Network.Result result) {
        showGoogleButtonResult(result == CPR_Network.Result.TRANSMISSION_SUCCESS);
    }

    // =============================================================================================
    // Function:    initGoogleSignIn
    // Description: Register the launcher that receives the result of the Google sign-in flow.
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initGoogleSignIn() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (Globals.network.completeSignIn(result.getData()) == CPR_Network.SIGN_IN_OK) {
                        Globals.network.uploadToGoogle(this::handleGoogleUploadResult);
                    } else {
                        Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                        submitDataBinding.imageGoogleResult.setImageResource(R.drawable.x);
                    }
                });
    }

    // =============================================================================================
    // Function:    initDatabase
    // Description: Initialize the Database field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initDatabase() {
        if (!Globals.network.hasActiveInternet()) {
            submitDataBinding.butSendDatabase.setEnabled(false);
            submitDataBinding.butSendDatabase.setClickable(false);
            submitDataBinding.butSendDatabase.setBackgroundColor(getColor(R.color.light_grey));
            return;
        }

        submitDataBinding.butSendDatabase.setOnClickListener(view -> {
            buttonClickUx(submitDataBinding.butSendDatabase, submitDataBinding.imageDatabaseResult);

            Globals.network.sendFileToSQLServer(result -> {
                switch (result) {
                    case TRANSMISSION_SUCCESS:
                        showDatabaseButtonResult("Successfully transmitted!", true);
                        break;
                    case NO_NETWORK:
                        showDatabaseButtonResult("No network connection", false);
                        break;
                    case HOST_UNREACHABLE:
                        showDatabaseButtonResult("SQL Server is unreachable", false);
                        break;
                    case NO_DATA:
                        showDatabaseButtonResult("No data to send", false);
                        break;
                    case SQL_EXCEPTION:
                        showDatabaseButtonResult("SQL Server Exception", false);
                        break;
                    case TRANSMISSION_FAILURE:
                    default:
                        showDatabaseButtonResult("Transmission failed", false);
                        break;
                }
            });
        });
    }

    // =============================================================================================
    // Function:    buttonClickUx
    // Description: Setup the UX when a button is clicked
    // Parameters:  The button, The label for the button
    // Output:      void
    // =============================================================================================
    private void buttonClickUx(MaterialButton button, ImageView label) {
        button.setEnabled(false);
        button.setClickable(false);
        button.setBackgroundColor(getColor(R.color.light_grey));

        CircularProgressDrawable spinner = new CircularProgressDrawable(this);
        spinner.setStrokeWidth(4f);
        spinner.setCenterRadius(18f);
        spinner.setColorSchemeColors(getColor(R.color.light_grey));
        label.setImageDrawable(spinner);
        spinner.start();
    }

    /**
     * Show the Check or X on the label for the button
     * @param button The button to modify
     * @param label The image label to modify
     * @param message The text for the toast message
     * @param success Whether it was a success or not
     */
    private void showButtonResult(MaterialButton button, ImageView label, String message, boolean success) {
        if (message != null && !message.isBlank())
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        button.setEnabled(true);
        button.setClickable(true);
        button.setBackgroundColor(getColor(R.color.white));

        Drawable current = label.getDrawable();
        if (current instanceof Animatable) {
            ((Animatable) current).stop();
        }
        label.setImageResource(success ? R.drawable.checkmark : R.drawable.x);
    }

    /**
     * An Overload method for showButtonResult for when it is for Google
     * @param success Whether it was a success or not
     */
    private void showGoogleButtonResult(boolean success) {
        showButtonResult(submitDataBinding.butSendGoogle, submitDataBinding.imageGoogleResult, "", success);
    }


    /**
     * An Overload method for showButtonResult for when it is for the Database
     * @param message The text for the toast message
     * @param success Whether it was a success or not
     */
    private void showDatabaseButtonResult(String message, boolean success) {
        showButtonResult(submitDataBinding.butSendDatabase, submitDataBinding.imageDatabaseResult, message, success);
    }

    // =============================================================================================
    // Function:    initQuit
    // Description: Initialize the Quit button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initQuit() {
        submitDataBinding.butQuit.setOnClickListener(view -> new AlertDialog.Builder(view.getContext())
            .setTitle(getString(R.string.submit_alert_quit_title))
            .setMessage(getString(R.string.submit_alert_quit_message))

            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton(getString(R.string.submit_alert_quit_positive), (dialog, which) -> {
                SubmitData.this.finishAffinity();
                stopLockTask();
                Globals.network.shutdown();
                System.exit(0);
            })

            // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton(getString(R.string.submit_alert_cancel), null)
            .show()
        );
    }

    // =============================================================================================
    // Function:    initNext
    // Description: Initialize the Next Match button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initNext() {
        submitDataBinding.butNext.setOnClickListener(view -> {
            // Reset pre-Match settings for next time
            Globals.numStartingGamePiece = Constants.PreMatch.STARTING_GAME_PIECES;
            Globals.CurrentAccuracy = Constants.PostMatch.ACCURACY_NOT_SELECTED;
            Globals.CurrentClimbLevel = Constants.PostMatch.CLIMB_LEVEL_NOT_SELECTED;
            Globals.CurrentClimbPosition = Constants.PostMatch.CLIMB_POSITION_NOT_SELECTED;
            Globals.stealFuelValue = Constants.PostMatch.STEAL_FUEL_NOT_SELECTED;
            Globals.affectedByDefenseValue = Constants.PostMatch.AFFECTED_BY_DEFENSE_NOT_SELECTED;
            Globals.isPractice = false;

            // Increases the team number so that it will autofill for the next match correctly
            Globals.CurrentMatchNumber++;

            Intent GoToPreMatch = new Intent(SubmitData.this, PreMatch.class);
            startActivity(GoToPreMatch);

            finish();
        });
    }
}