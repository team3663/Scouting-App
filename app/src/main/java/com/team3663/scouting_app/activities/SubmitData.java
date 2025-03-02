package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
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
import androidx.documentfile.provider.DocumentFile;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.SubmitDataBinding;
import com.team3663.scouting_app.utility.achievements.Achievements;

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
    private static final ArrayList<String> Match_Types = Globals.MatchTypeList.getDescriptionList();

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

        // Initialize activity components that need the Logger
        initAchievements();

        // We're done with the logger (only if not null - it can be null if we're resubmitting data from Pre-Match)
        if (Globals.EventLogger != null) {
            Globals.EventLogger.close();
            Globals.EventLogger = null;
        }

        // Initialize activity components that don't log anything
        initMatchType();
        initMatch();
        initQR();
        initBluetooth();
        initQuit();
        initNext();
    }

    // =============================================================================================
    // Function:    initMatchType
    // Description: Initialize the Match Type field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initMatchType() {
        // Adds the items from the match type array to the list
        ArrayAdapter<String> adp_MatchType = new ArrayAdapter<>(this, R.layout.cpr_spinner, Match_Types);
        adp_MatchType.setDropDownViewResource(R.layout.cpr_spinner_item);
        submitDataBinding.spinnerMatchType.setAdapter(adp_MatchType);

        Globals.transmitMatchType = Globals.CurrentMatchType;

        // Search through the list of match types until you find the one that is correct then get its position in the list
        // and set that one as selected
        int start_Pos_DropId = 0;
        for (int i = 0; i < Match_Types.size(); i++) {
            if (Match_Types.get(i).equals(Globals.MatchTypeList.getMatchTypeDescription(Globals.transmitMatchType))) {
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
                int newMatchType = Globals.MatchTypeList.getMatchTypeId(submitDataBinding.spinnerMatchType.getSelectedItem().toString());

                if (newMatchType != Globals.transmitMatchType) {
                    Globals.transmitMatchType = newMatchType;
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

        // Get the list of files
        DocumentFile[] file_list = Globals.output_df.listFiles();

        // If there's no files, return nothing
        if ((file_list.length == 0)) return ret;

        // Parse out the match number from the filename.  If this is a "d" file from the right
        // competition (as defined in Settings) and matching device then add it to the list.
        for (DocumentFile df : file_list) {
            if (df.isFile() && df.getName().endsWith("_" + Globals.MatchTypeList.getMatchTypeShortForm(Globals.transmitMatchType) + "_d.csv")) {
                String[] file_parts = df.getName().split("_");
                if ((Integer.parseInt(file_parts[0]) == Globals.CurrentCompetitionId) &&
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
                achievement_timer.schedule(startNext, Constants.Achievements.IN_BETWEEN_DELAY);
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

        // If achievement need to be popped, first log them, and then set up a timer to show them.
        if (!poplist.isEmpty()) {
            String ach_sep_ID = "";
            for (Achievements.PoppedAchievement pa : poplist) {
                ach_sep_ID += ":" + pa.id;
            }
            if (!poplist.isEmpty()) {
                ach_sep_ID = ach_sep_ID.substring(1);
                Globals.EventLogger.LogData(Constants.Logger.LOGKEY_ACHIEVEMENT, ach_sep_ID);
            }

            achievement_timer.schedule(startOne, Constants.Achievements.START_DELAY);
        }
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
            Globals.isStartingGamePiece = true;
            Globals.isPractice = false;
            Globals.transmitMatchNum= Integer.parseInt(submitDataBinding.spinnerMatch.getSelectedItem().toString());

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
        submitDataBinding.butSendBT.setOnClickListener(view -> {
            // Reset pre-Match settings for next time
            Globals.isStartingGamePiece = true;
            Globals.isPractice = false;
            Globals.transmitMatchNum= Integer.parseInt(submitDataBinding.spinnerMatch.getSelectedItem().toString());


//            Intent GoToBluetooth = new Intent(SubmitData.this, Bluetooth.class);
//            startActivity(GoToBluetooth);

            finish();
        });
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
            Globals.isStartingGamePiece = true;
            Globals.isPractice = false;

            // Increases the team number so that it auto fills for the next match correctly
            Globals.CurrentMatchNumber++;

            Intent GoToPreMatch = new Intent(SubmitData.this, PreMatch.class);
            startActivity(GoToPreMatch);

            finish();
        });
    }
}