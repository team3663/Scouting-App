package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.SubmitDataBinding;
import com.team3663.scouting_app.utility.achievements.Achievements;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class SubmitData extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private SubmitDataBinding submitDataBinding;
    private static final Timer achievement_timer = new Timer();
    private static MediaPlayer media;
    private static ArrayList<Achievements.PoppedAchievement> poplist;
    private static int currentAchievement = 0;

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

        // Initialize activity components
        initAchievements();
        initMatch();
        initQR();
        initBluetooth();
        initQuit();
        initNext();
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

        // Open the output directory and get all filenames that end with "*d.csv"
        String path = getString(R.string.logger_path);

        // Ensure the path (if it's not blank) has a trailing delimiter
        if (!path.isEmpty()) {
            if (!path.endsWith("/")) path = path + "/";
        }

        // Define the filenames/files to be used for this logger
        File parent = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), path + "dummy").getParentFile();

        // Ensure the directory structure exists first - if not, return nothing
        if ((parent == null) || !parent.isDirectory()) return ret;

        // Get the list of files
        File[] file_list = parent.listFiles();

        // If there's no files, return nothing
        if ((file_list == null) || (file_list.length == 0)) return ret;

        // Parse out the match number from the filename.  If this is a "d" file from the right
        // competition (as defined in Settings) then add it to the list.
        for (File file : file_list) {
            if (file.isFile() && file.getName().endsWith("d.csv")) {
                String[] file_parts = file.getName().split("_");
                if (Integer.parseInt(file_parts[0]) == Globals.CurrentCompetitionId)
                    ret_int.add(Integer.parseInt(file_parts[1]));
            }
        }

        // Sort the list (numerically) and then copy into the String version
        Collections.sort(ret_int);
        for (Integer i : ret_int) ret.add(i.toString());
        return ret;
    }

    // =============================================================================================
    // Function:    checkDataFiles
    // Description: Ensures that there is a data file "e" (we already looked for all "d" ones).
    //              We assume that we already called FindMatches() which did a lot of error checking
    //              so we'll skip ones that have been checked already.
    // Parameters:  void
    // Output:      Boolean - return true if "e" file found.  False if no "e" file found.
    // =============================================================================================
    private Boolean checkDataFiles(String in_match) {
        // Open the output directory and get all filenames that end with "*d.csv"
        String path = getString(R.string.logger_path);

        // Ensure the path (if it's not blank) has a trailing delimiter
        if (!path.isEmpty()) {
            if (!path.endsWith("/")) path = path + "/";
        }

        // Define the filename to check
        File e_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), path + Globals.CurrentCompetitionId + "_" + in_match + "_" + Globals.CurrentDeviceId + "_e.csv" );

        // Ensure the directory structure exists first - if not, return nothing
        return e_file.exists() && e_file.isFile();
    }

    private class popOneAndGo_TimerTask extends TimerTask {
        @Override
        public void run() {
            TimerTask achievement_timerTask_Start;
            TimerTask achievement_timerTask_End;

            if (!poplist.isEmpty() && currentAchievement < poplist.size()) {
                achievement_timerTask_Start = new AchievementTimerTaskStart();
                achievement_timerTask_End = new AchievementTimerTaskEnd();
                achievement_timer.schedule(achievement_timerTask_Start, 1);
                achievement_timer.schedule(achievement_timerTask_End, Constants.Achievements.DISPLAY_TIME);
            }
        }
    }

    // =============================================================================================
    // Class:       AchievementTimerTaskStart
    // Description: Defines the TimerTask trigger for when we need to start showing an achievement
    // =============================================================================================
    private class AchievementTimerTaskStart extends TimerTask {
        @Override
        public void run() {
            SubmitData.this.runOnUiThread(() -> {
                submitDataBinding.textAchievementTitle.setText(poplist.get(currentAchievement).title);
                submitDataBinding.textAchievementDesc.setText(poplist.get(currentAchievement).description);

                Animation animation = AnimationUtils.loadAnimation(SubmitData.this, R.anim.blink);

                submitDataBinding.imageAchievement.setVisibility(View.VISIBLE);
                submitDataBinding.textAchievementTitle.setVisibility(View.VISIBLE);
                submitDataBinding.textAchievementDesc.setVisibility(View.VISIBLE);
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
        @Override
        public void run() {
            TimerTask startNext;

            SubmitData.this.runOnUiThread(() -> {
                submitDataBinding.imageAchievement.setVisibility(View.INVISIBLE);
                submitDataBinding.textAchievementTitle.setVisibility(View.INVISIBLE);
                submitDataBinding.textAchievementDesc.setVisibility(View.INVISIBLE);
            });

            currentAchievement++;
            if (currentAchievement < poplist.size()) {
                startNext = new popOneAndGo_TimerTask();
                achievement_timer.schedule(startNext, Constants.Achievements.INBETWEEN_DELAY);
            }
            else closeAchievements();
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
    // Function:    initAchievements
    // Description: Initialize the Achievements system
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initAchievements() {
        TimerTask startOne;
        startOne = new popOneAndGo_TimerTask();

        // Keep Achievements invisible
        submitDataBinding.imageAchievement.setVisibility(View.INVISIBLE);
        submitDataBinding.textAchievementTitle.setVisibility(View.INVISIBLE);
        submitDataBinding.textAchievementDesc.setVisibility(View.INVISIBLE);

        media = MediaPlayer.create(this, R.raw.achievement);
        media.setVolume(1,1);

        poplist = Achievements.popAchievements();
        achievement_timer.schedule(startOne,Constants.Achievements.START_DELAY);
    }

    // =============================================================================================
    // Function:    initMatch
    // Description: Initialize the Match field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initMatch() {
        // Adds the items from the match log files array to the list
        ArrayAdapter<String> adp_Match = new ArrayAdapter<>(this,
                R.layout.cpr_spinner, FindMatches());
        adp_Match.setDropDownViewResource(R.layout.cpr_spinner_item);
        submitDataBinding.spinnerMatch.setAdapter(adp_Match);
        // Set the selection (if there are any) to the latest match (largest value in the list)
        if (adp_Match.getCount() > 0) submitDataBinding.spinnerMatch.setSelection(adp_Match.getCount() - 1, true);

        submitDataBinding.spinnerMatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // If we don't have both data files, display a message.  Otherwise clear it.
                if (!checkDataFiles(submitDataBinding.spinnerMatch.getSelectedItem().toString()))
                    submitDataBinding.textMatchMessage.setText(getString(R.string.submit_match_error));
                else
                    submitDataBinding.textMatchMessage.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // =============================================================================================
    // Function:    initQR
    // Description: Initialize the QR Code field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initQR() {

    }

    // =============================================================================================
    // Function:    initBluetooth
    // Description: Initialize the Bluetooth field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initBluetooth() {

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
                System.exit(0);
            })

            // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton(getString(R.string.submit_alert_cancel), null)
            // TODO make the icon work
//                .setIcon(getDrawable(android.R.attr.alertDialogIcon))
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
            Intent GoToPreMatch = new Intent(SubmitData.this, PreMatch.class);
            startActivity(GoToPreMatch);

            finish();
        });
    }
}