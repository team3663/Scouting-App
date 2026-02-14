package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.MatchTallyBinding;
import com.team3663.scouting_app.utility.CPR_Chronometer;
import com.team3663.scouting_app.utility.CPR_VerticalSeekBar;
import com.team3663.scouting_app.utility.Logger;
import com.team3663.scouting_app.utility.achievements.Achievements;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MatchTally extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private MatchTallyBinding matchBinding;
    private static OrientationEventListener OEL = null; // needed to detect the screen being flipped around
    private static String currentAllianceOnLeft = Constants.Match.ORIENTATION_RED_ON_LEFT;
    private static float BlueView_X = 0;
    private static float BlueView_Y = 0;
    private static float Screen_X = 0;
    private static float Screen_Y = 0;
    private static long start_time_not_moving;
    private static float tele_button_position_x = 0;
    private static float tele_button_position_y = 0;
    private static String team_alliance;
    private static boolean climb_button_pressed = false;
    private static boolean in_alliance_zone = false;
    private static boolean in_opponent_zone = false;
    private static boolean in_neutral_zone = false;

    public static float NeutralZone_StartX = -1;
    public static float RightZone_StartX = -1;

    // Define the view id's for elements we need to reference
    private static int ID_BUT_CLIMB;
    private static int ID_BUT_SHOOT_TAP;
    private static int ID_BUT_SHOOT;
    private static int ID_BUT_PASS_TAP;
    private static int ID_BUT_PASS;
    private static int ID_BUT_PICKUP;

    // Define a Timer and TimerTasks so you can schedule things
    private CPR_Chronometer game_Timer;
    private CPR_Chronometer delay_Timer;
    private static Timer flashing_Timer;
    private static TimerTask flashing_timertask;

    private static final ColorDrawable[] switch_color_drawable = {
        new ColorDrawable(Constants.Match.BUTTON_COLOR_NORMAL),
        new ColorDrawable(Constants.Match.BUTTON_COLOR_FLASH)
    };
    private static TransitionDrawable switch_notMoving_transition;
    private static TransitionDrawable switch_defended_transition;
    private static TransitionDrawable switch_defense_transition;

    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle in_savedInstanceState) {
        super.onCreate(in_savedInstanceState);
        EdgeToEdge.enable(this);
        matchBinding = MatchTallyBinding.inflate(getLayoutInflater());
        View page_root_view = matchBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(matchBinding.matchTally, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Globals.CurrentMatchPhase = Constants.Phases.NONE;

        // Initialize activity components
        initViewIDs();
        initRotation();
        initStartButton();
        initTeam();
        initUndoButton();
        initStatus();
        initNotMoving();
        initDefense();
        initDefended();
        initTime();
        initBackButton();
        initRobotStartLocation();
        initZoneButtons();
        initActionButtons();
        initZOrder();
        initSeekBar();
    }

    // =============================================================================================
    // Function:    startMatch
    // Description: Starts the match Timer and all the scheduled events and repeat schedule events
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint({"DiscouragedApi", "SetTextI18n"})
    public void startMatch() {
        // Achievements
        Globals.myAchievements.clearMatchData();

        // Log the starting time
        Globals.EventLogger.LogData(Constants.Logger.LOGKEY_START_TIME, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")));
        // Log the starting location (the match hasn't started so we need to specify a "0" time
        // or else it will log as max match time which wastes extra log characters for no benefit)
        Globals.EventLogger.LogEvent(Constants.Events.ID_AUTO_START_GAME_PIECE, BlueView_X, BlueView_Y, 0);

        // Disable orientation listening if we can!  Once we start the match don't allow rotation anymore
        if (OEL != null && OEL.canDetectOrientation()) {
            OEL.disable();
        }

        // Hide Back Button (too late to go back now!)
        matchBinding.butBack.setClickable(false);
        matchBinding.butBack.setVisibility(View.INVISIBLE);

        // Show the switch for "Not Moving" in Auto
        matchBinding.switchNotMoving.setEnabled(true);
        matchBinding.switchNotMoving.setTextColor(Color.WHITE);
        matchBinding.switchNotMoving.setVisibility(View.VISIBLE);

        // Enable action buttons
        matchBinding.butClimb.setEnabled(true);
        matchBinding.butClimb.setClickable(true);
        matchBinding.butPickup.setEnabled(true);
        matchBinding.butPickup.setClickable(true);
        matchBinding.butPass.setEnabled(true);
        matchBinding.butPass.setClickable(true);
        matchBinding.butPassTap.setEnabled(true);
        matchBinding.butPassTap.setClickable(true);
        matchBinding.butShoot.setEnabled(true);
        matchBinding.butShoot.setClickable(true);
        matchBinding.butShootTap.setEnabled(true);
        matchBinding.butShootTap.setClickable(true);
        matchBinding.seekBar.setEnabled(true);

        // Calculate the image dimensions
        Constants.Match.IMAGE_WIDTH = matchBinding.FieldTouch.getWidth();
        Constants.Match.IMAGE_HEIGHT = matchBinding.FieldTouch.getHeight();
        NeutralZone_StartX = matchBinding.butCenterZone.getX();
        RightZone_StartX = matchBinding.butRightZone.getX();

        // Set match Phase to be correct and Button text
        Globals.CurrentMatchPhase = Constants.Phases.AUTO;
        matchBinding.butMatchControl.setText(getString(R.string.button_start_teleop));
        matchBinding.butMatchControl.setBackgroundColor(getColor(R.color.dark_yellow));
        matchBinding.butMatchControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.start_teleop, 0);

        // Create the Timers and timer tasks
        game_Timer.start();
        flashing_Timer = new Timer();
        flashing_timertask = new MatchTally.FlashingTimerTask();

        // Set timer tasks
        game_Timer.setOnChronometerTickListener(chronometer -> game_Timer_tick());
        if (Achievements.data_StartTime == 0) Achievements.data_StartTime = game_Timer.getStartTime();
        flashing_Timer.scheduleAtFixedRate(flashing_timertask, 0, Constants.Match.BUTTON_FLASH_INTERVAL);
    }

    // =============================================================================================
    // Function:    startTeleopDelay
    // Description: Starts the small delay before we start Teleop.
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void startTeleopDelay() {
        // Set match Phase to be correct
        Globals.CurrentMatchPhase = Constants.Phases.TELEOP;

        // Disable Zone Buttons during delay
        matchBinding.butLeftZone.setEnabled(false);
        matchBinding.butLeftZone.setClickable(false);
        matchBinding.butCenterZone.setEnabled(false);
        matchBinding.butCenterZone.setClickable(false);
        matchBinding.butRightZone.setEnabled(false);
        matchBinding.butRightZone.setClickable(false);

        game_Timer.pause();
        delay_Timer.start();
        delay_Timer.setOnChronometerTickListener(chronometer -> {
            if (delay_Timer.getElapsedSeconds() >= Constants.Match.TIMER_AUTO_TELEOP_DELAY)
                startTeleop();
        });
    }

    // =============================================================================================
    // Function:    startTeleop
    // Description: Starts Teleop phase by setting timer to 15 sec and adjust start time to
    //              compensate and update button text
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void startTeleop() {
        // Set the game Time so that the Display Time will be correct
        game_Timer.setTime(Constants.Match.TIMER_AUTO_LENGTH);
        game_Timer.resume();
        delay_Timer.stop();

        // Set match Phase to be correct and Button text
        Globals.CurrentMatchPhase = Constants.Phases.TELEOP;

        // Hide the location of the robot
        matchBinding.textRobot.setVisibility(View.INVISIBLE);
        // Highlight the right zone instead
        if (Screen_X < matchBinding.butCenterZone.getX())
            matchBinding.butLeftZone.callOnClick();
        else if (Screen_X < matchBinding.butRightZone.getX())
            matchBinding.butCenterZone.callOnClick();
        else matchBinding.butRightZone.callOnClick();

        // Hide the Pickup Fuel button
        matchBinding.butPickup.setClickable(false);
        matchBinding.butPickup.setVisibility(View.INVISIBLE);

        // Enable the climb button
        matchBinding.butClimb.setEnabled(in_alliance_zone);
        matchBinding.butClimb.setClickable(in_alliance_zone);
        climb_button_pressed = false;

        // Enable Zone Buttons
        matchBinding.butLeftZone.setEnabled(true);
        matchBinding.butLeftZone.setClickable(true);
        matchBinding.butCenterZone.setEnabled(true);
        matchBinding.butCenterZone.setClickable(true);
        matchBinding.butRightZone.setEnabled(true);
        matchBinding.butRightZone.setClickable(true);

        // Certain actions can't be set from a non-UI thread (like within a TimerTask that runs on a
        // separate thread). So we need to make a Runner that will execute on the UI thread to set this.
        MatchTally.this.runOnUiThread(() -> {
            matchBinding.switchDefense.setEnabled(true);
            matchBinding.switchDefense.setTextColor(Color.WHITE);
            matchBinding.switchDefense.setVisibility(View.VISIBLE);
            matchBinding.switchDefended.setEnabled(true);
            matchBinding.switchDefended.setTextColor(Color.WHITE);
            matchBinding.switchDefended.setVisibility(View.VISIBLE);

            matchBinding.butMatchControl.setText(getString(R.string.button_end_match));
            matchBinding.butMatchControl.setBackgroundColor(getColor(R.color.dark_red));
            matchBinding.butMatchControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.stop_match, 0);
        });
    }

    // =============================================================================================
    // Function:    endTeleop
    // Description: Ends teleop, but not the match so that you can still finish up
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint("SetTextI18n")
    public void endTeleop() {
        // Certain actions can't be set from a non-UI thread (like within a TimerTask that runs on a
        // separate thread). So we need to make a Runner that will execute on the UI thread to set this.
        MatchTally.this.runOnUiThread(() -> {
            matchBinding.butMatchControl.setText(getString(R.string.button_match_next));
            matchBinding.butMatchControl.setTextColor(getColor(R.color.cpr_bkgnd));
            matchBinding.butMatchControl.setBackgroundColor(getColor(R.color.white));
            matchBinding.butMatchControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.next_button, 0);
        });

        // Stop the game timer
        game_Timer.stop();
    }

    // =============================================================================================
    // Function:    endMatchCheck
    // Description: Check if the match CAN end.  Check for orphaned events.
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint("SetTextI18n")
    public void endMatchCheck() {
        // See if there's an orphaned event.  If so, double check we REALLY want to end the match
        if (Globals.EventLogger.isLastEventAnOrphan()) {
            new AlertDialog.Builder(MatchTally.this)
                    .setTitle(getString(R.string.match_alert_orphanedEvent_title))
                    .setMessage(getString(R.string.match_alert_orphanedEvent_message))

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(getString(R.string.match_alert_orphanedEvent_positive), (dialog, which) -> {
                        dialog.dismiss();
                        endMatch();
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(getString(R.string.match_alert_orphanedEvent_negative), null)
                    .show();
        }
        else endMatch();
    }

    // =============================================================================================
    // Function:    endMatch
    // Description: Ends the match and all of the timers
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint("SetTextI18n")
    public void endMatch() {
        // Get rid of the Scheduled events that are over/have ended
        // Need to set flashing_Timer and TimerTasks to null so we can create "new" ones at the start of the next match
        if (flashing_Timer != null) {
            flashing_Timer.cancel();
            flashing_Timer.purge();
        }

        flashing_Timer = null;
        flashing_timertask = null;

        // Reset match phase so that the next time we hit Start Match we do the right thing
        Globals.CurrentMatchPhase = Constants.Phases.NONE;

        // If either of the toggles are on turn them off
        matchBinding.switchNotMoving.setChecked(false);
        matchBinding.switchDefense.setChecked(false);
        matchBinding.switchDefended.setChecked(false);

        // Go to the next page
        Intent GoToPostMatch = new Intent(MatchTally.this, PostMatch.class);
        startActivity(GoToPostMatch);

        finish();
    }

    // =============================================================================================
    // Class:       FlashingTimerTask
    // Description: Defines the TimerTask trigger to when the Teleop phase of the game is over.
    // =============================================================================================
    public class FlashingTimerTask extends TimerTask {
        @Override
        public void run() {
            // If the button is ON then toggle the background color between COLOR_FLASH and COLOR_NORMAL
            // Always start by setting it to "normal" to avoid the case where you toggle it off during the Thread.sleep
            // since it will remain BUTTON_COLOR_FLASH even though it's off.
            matchBinding.switchNotMoving.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);
            matchBinding.switchDefense.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);
            matchBinding.switchDefended.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);

            // Only sleep the thread if at least one switch is checked.
            if (matchBinding.switchNotMoving.isChecked() || matchBinding.switchDefense.isChecked() || matchBinding.switchDefended.isChecked()) {
                try {
                    Thread.sleep(Constants.Match.BUTTON_FLASH_BLINK_INTERVAL);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // If it's checked, wait a bit then make it BUTTON_COLOR_FLASH
                if (matchBinding.switchNotMoving.isChecked()) animateSwitchColor(matchBinding.switchNotMoving);
                if (matchBinding.switchDefense.isChecked()) animateSwitchColor(matchBinding.switchDefense);
                if (matchBinding.switchDefended.isChecked()) animateSwitchColor(matchBinding.switchDefended);
            }
        }
    }

    // =============================================================================================
    // Function:    animateSwitchColor
    // Description: Use some animation to slowly fade in the color of the background of a switch
    // Parameters:  in_switch - The switch to change the color of
    // Output:      void
    // =============================================================================================
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private void animateSwitchColor(Switch in_switch) {
        String switch_text = in_switch.getText().toString();

        // Based on the switch, set the background to the proper transition and start it.
        // Objects can't share a transition but we don't want to create a new one every time. (memory leak?)
        // Transition can only run once, it appears, so need to set the background to it each time.
        if (switch_text.equals(getString(R.string.switch_on_defense))) {
            in_switch.setBackground(switch_defense_transition);
            switch_defense_transition.startTransition(Constants.Match.BUTTON_FLASH_FADE_IN_TIME);
        } else if (switch_text.equals(getString(R.string.switch_is_defended))) {
            in_switch.setBackground(switch_defended_transition);
            switch_defended_transition.startTransition(Constants.Match.BUTTON_FLASH_FADE_IN_TIME);
        } else if (switch_text.equals(getString(R.string.switch_not_moving))) {
            in_switch.setBackground(switch_notMoving_transition);
            switch_notMoving_transition.startTransition(Constants.Match.BUTTON_FLASH_FADE_IN_TIME);
        }
    }

    // =============================================================================================
    // Function:    initRotation
    // Description: Initialize the field rotation mechanics
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    @SuppressLint("UseCompatLoadingForDrawables")
    private void initRotation() {
        // Set up a listener for Orientation changes so we can flip the field properly (which means we
        // ignore the flip for the field image in a sense)
        // This listener will get triggered for every slight movement so we'll need to be careful on how
        // we call the rotation.  Keeping track of the current orientation should help!
        // KEY: actually calling setRotation works, but severely messes up the context menu.  SO, we'll
        // hack it by just loading a "flipped" image to display.
        if (Globals.CurrentFieldOrientationPos == 0) {  // Automatic
            currentAllianceOnLeft = Constants.Match.ORIENTATION_RED_ON_LEFT;

            OEL = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onOrientationChanged(int rotation_degrees) {
                    // If the device is in the 0 to 180 degree range, make it Landscape
                    if ((rotation_degrees >= 0) && (rotation_degrees < 180) && !currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_RED_ON_LEFT)) {
                        matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field2026));
                        currentAllianceOnLeft = Constants.Match.ORIENTATION_RED_ON_LEFT;

                        // Set the robot location based on the new rotation
                        setRobotLocation(matchBinding.FieldTouch.getWidth() - Screen_X, matchBinding.FieldTouch.getHeight() - Screen_Y);
                    }
                    // If the device is in the 180 to 359 degree range, make it Landscape
                    // We can get passed a -1 if the device can't tell (it's lying flat) and we want to ignore that
                    else if ((rotation_degrees >= 180) && !currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_BLUE_ON_LEFT)) {
                        matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field2026_flipped));
                        currentAllianceOnLeft = Constants.Match.ORIENTATION_BLUE_ON_LEFT;

                        // Set the robot location based on the new rotation
                        setRobotLocation(matchBinding.FieldTouch.getWidth() - Screen_X, matchBinding.FieldTouch.getHeight() - Screen_Y);
                    }
                }
            };

            // Enable orientation listening if we can!
            if (OEL.canDetectOrientation()) {
                OEL.enable();
            }
        } else if (Globals.CurrentFieldOrientationPos == 1) {   // Blue On Left
            matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field2026_flipped));
            currentAllianceOnLeft = Constants.Match.ORIENTATION_BLUE_ON_LEFT;
        } else {    // Red On Left
            matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field2026));
            currentAllianceOnLeft = Constants.Match.ORIENTATION_RED_ON_LEFT;
        }
    }

    // =============================================================================================
    // Function:    initTime
    // Description: Initialize the Time / Clock field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initTime() {
        // Initialize the match timer textbox settings
        Chronometer game_Chronometer = matchBinding.chronometer;
        game_Chronometer.setBackgroundColor(Color.TRANSPARENT);
        game_Chronometer.setTextSize(24F);
        game_Timer = new CPR_Chronometer(game_Chronometer);
        game_Timer.setTime(0);

        Chronometer delay_Chronometer = matchBinding.chronometerDelay;
        delay_Timer = new CPR_Chronometer(delay_Chronometer);
    }

    // =============================================================================================
    // Function:    initStatus
    // Description: Initialize the Status field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initStatus() {
        // If this is a practice, put a message in the Status and set the image to Practice mode
        if (Globals.isPractice) {
            matchBinding.textStatus.setTextColor(Color.YELLOW);
            matchBinding.textPractice.setText(getString(R.string.match_practice_watermark));
            matchBinding.textPractice.setTextSize(180);
        } else {
            matchBinding.textStatus.setTextColor(Color.LTGRAY);
            matchBinding.textPractice.setText("");
        }
    }

    // =============================================================================================
    // Function:    initTeam
    // Description: Initialize the field rotation mechanics
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initTeam() {
        String new_team = Globals.CurrentTeamToScout + " - " + Globals.TeamList.getOrDefault(Globals.CurrentTeamToScout, "");
        matchBinding.textTeam.setText(new_team);
        matchBinding.textTeam.setTextColor(Color.WHITE);

        team_alliance = Globals.MatchList.getAllianceForTeam(Globals.CurrentTeamToScout);
        if (team_alliance.isEmpty()) team_alliance = Constants.Settings.PREF_TEAM_POS[Globals.CurrentPrefTeamPos];
    }

    // =============================================================================================
    // Function:    initStartButton
    // Description: Initialize the Start button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initStartButton() {
        // Initialize the match Control Button settings
        matchBinding.butMatchControl.setEnabled(false);
        matchBinding.butMatchControl.setClickable(false);
        matchBinding.butMatchControl.setText(getString(R.string.button_start_match));
        matchBinding.butMatchControl.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.dark_green));
        matchBinding.butMatchControl.setOnClickListener(view -> {
            // Checks the current phase and makes the button press act accordingly
            switch (Globals.CurrentMatchPhase) {
                case Constants.Phases.NONE:
                    startMatch();
                    break;
                case Constants.Phases.AUTO:
                    // If we're going to teleop manually, log the start time offset
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_START_TIME_OFFSET, String.valueOf(Math.round((Constants.Match.TIMER_AUTO_LENGTH * 1000.0 - game_Timer.getElapsedMilliSeconds()) / 10.0) / 100.0));
                    startTeleop();
                    break;
                case Constants.Phases.TELEOP:
                    if (game_Timer.getElapsedSeconds() < Constants.Match.TIMER_AUTO_LENGTH + Constants.Match.TIMER_TELEOP_LENGTH)
                        new AlertDialog.Builder(view.getContext())
                                .setTitle(getString(R.string.match_alert_endMatch_title))
                                .setMessage(getString(R.string.match_alert_endMatch_message))

                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                // The dialog is automatically dismissed when a dialog button is clicked.
                                .setPositiveButton(getString(R.string.match_alert_endMatch_positive), (dialog, which) -> {
                                    dialog.dismiss();
                                    endMatchCheck();
                                })

                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setNegativeButton(getString(R.string.match_alert_cancel), null)
                                .show();
                    else endMatchCheck();
                    break;
            }
        });
    }

    // =============================================================================================
    // Function:    initBackButton
    // Description: Initialize the Back button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initBackButton() {
        // Map the button variable to the actual button
        // If clicked, go back to the previous page
        matchBinding.butBack.setOnClickListener(view -> {
            // If this is a practice, we need to clear out the Logger since we have an instance of Logger, but during
            // practice, we don't open any files.  If the next time we get here from Pre-Match, if it's NOT a practice,
            // we'll try to write out to the same "dummy" Logger and crash.  Resetting the Logger here ensures we do the
            // right instantiation in Pre-Match.
            if (Globals.isPractice) Globals.EventLogger = null;

            if (OEL != null) {
                OEL.disable();
                OEL = null;
            }

            // Go to the previous page
            Intent GoToPreviousPage = new Intent(MatchTally.this, PreMatch.class);
            startActivity(GoToPreviousPage);

            finish();
        });
    }

    // =============================================================================================
    // Function:    initUndoButton
    // Description: Initialize the Undo button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initUndoButton() {
        matchBinding.butUndo.setVisibility(View.INVISIBLE);
        matchBinding.butUndo.setEnabled(false);

        // If clicked, undo the last event selected
        matchBinding.butUndo.setOnClickListener(view -> {
            // If the most recent event was a climb and we're going to undo it, re-enable the climb button
            if (matchBinding.textStatus.getText().toString().equalsIgnoreCase("Climb")) {
                climb_button_pressed = false;
                if (in_alliance_zone) matchBinding.butClimb.setEnabled(true);
                if (in_alliance_zone) matchBinding.butClimb.setClickable(true);
            }

            int last_event_id =Globals.EventLogger.UndoLastEvent();

            // If there are no events left to undo, hide the button
            if ((last_event_id == -1) || (Logger.current_event[Globals.EventList.getEventGroup(last_event_id)] == Constants.Events.ID_AUTO_START_GAME_PIECE)) {
                // Certain actions can't be set from a non-UI thread
                // So we need to make a Runner that will execute on the UI thread to set this.
                MatchTally.this.runOnUiThread(() -> {
                    matchBinding.butUndo.setVisibility(View.INVISIBLE);
                    matchBinding.butUndo.setEnabled(false);
                    matchBinding.textStatus.setText("");
                });
            }
            else {
                setEventStatus(last_event_id);
            }
        });
    }

    // =============================================================================================
    // Function:    game_Timer_tick
    // Description: Process what needs to happen when the game timer ticks up a second.
    //              Update the clock, check if we need to change game phase, etc.
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void game_Timer_tick() {
        int elapsedSeconds = game_Timer.getElapsedSeconds();

        // Check if we need to move to Teleop
        if (Globals.CurrentMatchPhase.equals(Constants.Phases.AUTO) && (elapsedSeconds >= Constants.Match.TIMER_AUTO_LENGTH)) {
            startTeleopDelay();
        }

        // Check if we need to move to Teleop
        if (Globals.CurrentMatchPhase.equals(Constants.Phases.TELEOP) && elapsedSeconds >= Constants.Match.TIMER_AUTO_LENGTH + Constants.Match.TIMER_TELEOP_LENGTH) {
            endTeleop();
        }
    }

    // =============================================================================================
    // Function:    setEventStatus
    // Description: Set the status text for a given event text
    // Parameters:  in_event_id
    //                  The ID value for the event we want to display
    // Output:      void
    // =============================================================================================
    private void setEventStatus(int in_event_id) {
        if (Globals.isPractice) return;

        SpannableString ss = new SpannableString(Globals.EventList.getEventDescription(in_event_id));

        // Ensure the text will fit, based on the length of the string.
        if (ss.length() > Constants.Match.STATUS_TEXT_LONG_LENGTH) ss.setSpan(new AbsoluteSizeSpan(Constants.Match.STATUS_TEXT_LONG_SIZE), 0, ss.length(), 0);
        else if (ss.length() > Constants.Match.STATUS_TEXT_MED_LENGTH) ss.setSpan(new AbsoluteSizeSpan(Constants.Match.STATUS_TEXT_MED_SIZE), 0, ss.length(), 0);
        else ss.setSpan(new AbsoluteSizeSpan(Constants.Match.STATUS_TEXT_DEFAULT_SIZE), 0, ss.length(), 0);

        // If this menuItem has a color to use, then use it
        if (Globals.ColorList.isColorValid(Globals.CurrentColorId - 1)) {
            if (Globals.EventList.hasEventColor(in_event_id))
                ss.setSpan(new ForegroundColorSpan(Globals.ColorList.getColor(Globals.CurrentColorId - 1, Globals.EventList.getEventColor(in_event_id))), 0, ss.length(), 0);
        }

        matchBinding.textStatus.setText(ss);
    }

    // =============================================================================================
    // Function:    initNotMoving
    // Description: Initialize the Not Moving switch
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initNotMoving() {
        switch_notMoving_transition = new TransitionDrawable(switch_color_drawable);

        // Initialize the Not Moving Switch settings
        matchBinding.switchNotMoving.setTextColor(Constants.Match.BUTTON_TEXT_COLOR_DISABLED);
        matchBinding.switchNotMoving.setBackground(switch_notMoving_transition);
        matchBinding.switchNotMoving.setVisibility(View.INVISIBLE);
        // Do this so that you can't mess with the switch during the wrong phases
        matchBinding.switchNotMoving.setEnabled(false);

        // This gets called if either the switch is clicked on, or the slide toggle is flipped (covers both)
        matchBinding.switchNotMoving.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // If the button is being turned ON make it RED otherwise LTGRAY
            if (isChecked) {
                Globals.EventLogger.LogEvent(Constants.Events.ID_NOT_MOVING_START, game_Timer.getElapsedMilliSeconds());
                matchBinding.switchNotMoving.setBackgroundColor(Constants.Match.BUTTON_COLOR_FLASH);
                start_time_not_moving = System.currentTimeMillis();
                Achievements.data_match_Toggle_NotMoving++;
                Achievements.data_Toggle_NotMoving++;
            } else {
                Globals.EventLogger.LogEvent(Constants.Events.ID_NOT_MOVING_END, game_Timer.getElapsedMilliSeconds());
                matchBinding.switchNotMoving.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);
                Achievements.data_IdleTime += (int)(System.currentTimeMillis() - start_time_not_moving);
            }
        });

        matchBinding.switchNotMoving.setOnClickListener(view -> {
            // Need this listener or else the onCheckedChanged won't fire either.
        });
    }

    // =============================================================================================
    // Function:    initDefense
    // Description: Initialize the Playing Defense switch
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initDefense() {
        switch_defense_transition = new TransitionDrawable(switch_color_drawable);

        // Initialize the Defense Switch settings
        matchBinding.switchDefense.setTextColor(Constants.Match.BUTTON_TEXT_COLOR_DISABLED);
        matchBinding.switchDefense.setBackground(switch_defense_transition);
        matchBinding.switchDefense.setVisibility(View.INVISIBLE);
        // Do this so that you can't mess with the switch during the wrong phases
        matchBinding.switchDefense.setEnabled(false);

        // This gets called if either the switch is clicked on, or the slide toggle is flipped (covers both)
        matchBinding.switchDefense.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // If the button is being turned ON make it RED otherwise LTGRAY
            if (isChecked) {
                Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENSE_START, game_Timer.getElapsedMilliSeconds());
                matchBinding.switchDefense.setBackgroundColor(Constants.Match.BUTTON_COLOR_FLASH);
                Achievements.data_Toggle_Defense++;
            } else {
                Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENSE_END, game_Timer.getElapsedMilliSeconds());
                matchBinding.switchDefense.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);
            }
        });

        matchBinding.switchDefense.setOnClickListener(view -> {
            // Need this listener or else the onCheckedChanged won't fire either.
        });
    }

    // =============================================================================================
    // Function:    initDefended
    // Description: Initialize the Is Defended switch
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initDefended() {
        switch_defended_transition = new TransitionDrawable(switch_color_drawable);

        // Initialize the Defended Switch settings
        matchBinding.switchDefended.setTextColor(Constants.Match.BUTTON_TEXT_COLOR_DISABLED);
        matchBinding.switchDefended.setBackground(switch_defended_transition);
        matchBinding.switchDefended.setVisibility(View.INVISIBLE);
        // Do this so that you can't mess with the switch during the wrong phases
        matchBinding.switchDefended.setEnabled(false);

        // This gets called if either the switch is clicked on, or the slide toggle is flipped (covers both)
        matchBinding.switchDefended.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Globals.isDefended = isChecked;

            if (isChecked) {
                Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENDED_START, game_Timer.getElapsedMilliSeconds());
                matchBinding.switchDefended.setBackgroundColor(Constants.Match.BUTTON_COLOR_FLASH);
                Achievements.data_Toggle_Defended++;
            } else {
                Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENDED_END, game_Timer.getElapsedMilliSeconds());
                matchBinding.switchDefended.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);
            }
        });

        matchBinding.switchDefended.setOnClickListener(view -> {
            // Need this listener or else the onCheckedChanged won't fire either.
        });
    }

    // =============================================================================================
    // Function:    setRobotLocation
    // Description: Set the Robot location whether at the start or throughout the match
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    @SuppressLint("ClickableViewAccessibility")
    private void setRobotLocation(float in_X, float in_Y) {
        float offset = (float) (matchBinding.textRobot.getWidth() / 2);

        // Force all action buttons to be un-pressed
        handleActionButtonTouch(ID_BUT_CLIMB, MotionEvent.ACTION_UP);
        handleActionButtonTouch(ID_BUT_PICKUP, MotionEvent.ACTION_UP);
        handleActionButtonTouch(ID_BUT_PASS, MotionEvent.ACTION_UP);
        handleActionButtonTouch(ID_BUT_PASS_TAP, MotionEvent.ACTION_UP);
        handleActionButtonTouch(ID_BUT_SHOOT, MotionEvent.ACTION_UP);
        handleActionButtonTouch(ID_BUT_SHOOT_TAP, MotionEvent.ACTION_UP);

        // If we don't know the alliance (didn't have match schedule?) then default to the PREFERRED position
        boolean blue_alliance = team_alliance.substring(0,1).equalsIgnoreCase("B");

        if (in_X > 0) {
            Screen_X = in_X;
            Screen_Y = in_Y;
        }

        // Snap the robot to the correct starting line if we haven't started the match
        if (Globals.CurrentMatchPhase.equals(Constants.Phases.NONE)) {
            if ((blue_alliance && currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_BLUE_ON_LEFT)) ||
                    (!blue_alliance && currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_RED_ON_LEFT))) {
                Screen_X = Math.min(Math.max(in_X, matchBinding.FieldTouch.getWidth() * Constants.Match.START_LINE_X / 100.0f - offset), matchBinding.FieldTouch.getWidth() * Constants.Match.START_LINE_X / 100.0f + offset);
            } else {
                Screen_X = Math.min(Math.max(in_X, matchBinding.FieldTouch.getWidth() * (100.0f - Constants.Match.START_LINE_X) / 100.0f - offset), matchBinding.FieldTouch.getWidth() * (100.0f - Constants.Match.START_LINE_X) / 100.0f + offset);
            }

            // Ensure we aren't placing a robot where it can't be located - 2026 Season
            if ((Screen_Y > (Constants.Match.HUB_TOP_Y_PERCENT * matchBinding.FieldTouch.getHeight())) && (Screen_Y < (Constants.Match.HUB_BOTTOM_Y_PERCENT * matchBinding.FieldTouch.getHeight()))) {
                if ((blue_alliance && currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_BLUE_ON_LEFT)) ||
                        (!blue_alliance && currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_RED_ON_LEFT)))
                    Screen_X = matchBinding.FieldTouch.getWidth() * Constants.Match.START_LINE_X / 100.0f - offset;
                else
                    Screen_X = matchBinding.FieldTouch.getWidth() * (100.0f - Constants.Match.START_LINE_X) / 100.0f + offset;
            }

            // Save off the correct relative values based on the orientation
            if (blue_alliance && currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_RED_ON_LEFT) ||
                    !blue_alliance && currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_BLUE_ON_LEFT)) {
                BlueView_X = matchBinding.FieldTouch.getWidth() - Screen_X;
                BlueView_Y = Screen_Y;
            } else {
                BlueView_X = Screen_X;
                BlueView_Y = matchBinding.FieldTouch.getHeight() - Screen_Y;
            }
        }

        // Ensure we can't place the robot off the field
        if (Screen_X < offset) Screen_X = offset;
        else if (Screen_X > matchBinding.FieldTouch.getWidth() - offset) Screen_X = matchBinding.FieldTouch.getWidth() - offset;
        if (Screen_Y < offset) Screen_Y = offset;
        else if (Screen_Y > matchBinding.FieldTouch.getHeight() - offset) Screen_Y = matchBinding.FieldTouch.getHeight() - offset;

        // Make sure we see the location of the robot
        matchBinding.textRobot.setX(Screen_X - offset);
        matchBinding.textRobot.setY(Screen_Y - offset);

        // Enable the robot
        // This will be called initially as well as during screen rotations.  in_X will be 0 at that time.  Make it a no-op
        if (in_X > 0) {
            matchBinding.textRobot.setVisibility(View.VISIBLE);
            matchBinding.butMatchControl.setEnabled(true);
            matchBinding.butMatchControl.setClickable(true);
            // Reset the status text only if the match hasn't started.
            if (Globals.CurrentMatchPhase.equals(Constants.Phases.NONE)) matchBinding.textStatus.setText("");
        }
    }

    // =============================================================================================
    // Function:    initRobotStartLocation
    // Description: Initialize the Robot starting location
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    @SuppressLint("ClickableViewAccessibility")
    private void initRobotStartLocation() {
        matchBinding.FieldTouch.setOnTouchListener((view, motionEvent) -> {
            if (Globals.CurrentMatchPhase.equals(Constants.Phases.TELEOP)) return false;
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) return true;

            // Save where we touched the field image regardless of its orientation
            Screen_X = motionEvent.getX() + tele_button_position_x;
            Screen_Y = motionEvent.getY() + tele_button_position_y;

            // If we don't know the alliance (didn't have match schedule?) then default to the PREFERRED position
            boolean blue_alliance = team_alliance.substring(0,1).equalsIgnoreCase("B");

            // Save where we touched the field image relative to the fields orientation
            // Since the App has (0,0) in the top left, but our reporting will have (0,0) in the bottom left,
            // we need to flip the Y coordinate.
            if (blue_alliance && currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_RED_ON_LEFT) ||
                    !blue_alliance && currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_BLUE_ON_LEFT)) {
                BlueView_X = matchBinding.FieldTouch.getWidth() - Screen_X;
                BlueView_Y = Screen_Y;
            } else {
                BlueView_X = Screen_X;
                BlueView_Y = matchBinding.FieldTouch.getHeight() - Screen_Y;
            }

            setRobotLocation(Screen_X, Screen_Y);

            return true;
        });

        matchBinding.textRobot.setVisibility(View.INVISIBLE);
        matchBinding.textStatus.setText(R.string.match_select_start_location);
    }

    // =============================================================================================
    // Function:    initZOrder
    // Description: Initialize the Z-Order of the views in the Activity
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initZOrder() {
        matchBinding.FieldTouch.bringToFront();
        matchBinding.textRobot.bringToFront();
        matchBinding.textPractice.bringToFront();
        matchBinding.butLeftZone.bringToFront();
        matchBinding.butCenterZone.bringToFront();
        matchBinding.butRightZone.bringToFront();
        matchBinding.FieldTouch.invalidate();
        matchBinding.FieldTouch.requestLayout();
    }

    // =============================================================================================
    // Function:    initViewIDs
    // Description: Initialize the IDs for the views we need to keep track of
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initViewIDs() {
        ID_BUT_CLIMB = matchBinding.butClimb.getId();
        ID_BUT_PICKUP = matchBinding.butPickup.getId();
        ID_BUT_PASS = matchBinding.butPass.getId();
        ID_BUT_PASS_TAP = matchBinding.butPassTap.getId();
        ID_BUT_SHOOT = matchBinding.butShoot.getId();
        ID_BUT_SHOOT_TAP = matchBinding.butShootTap.getId();
    }


    // =============================================================================================
    // Function:    initSeekBar
    // Description: Initialize the SeekBar
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initSeekBar() {
        CPR_VerticalSeekBar seekbar = findViewById(R.id.seekBar);
        TextView seekbarProgress = findViewById(R.id.text_SeekBarProgress);

        seekbar.setProgress(Globals.numStartingGamePiece);
        seekbar.setMax(Constants.Match.SEEKBAR_MAX);
        seekbarProgress.setText(String.valueOf(seekbar.getProgress()));

        matchBinding.butPass.setText(getString(R.string.button_pass, Globals.numStartingGamePiece));
        matchBinding.butShoot.setText(getString(R.string.button_shoot, Globals.numStartingGamePiece));

        seekbar.setOnSeekBarChangeListener(new CPR_VerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(CPR_VerticalSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(CPR_VerticalSeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(CPR_VerticalSeekBar seekBar, int progress, boolean fromUser) {
                seekbarProgress.setText(String.valueOf(progress));
                matchBinding.butPass.setText(getString(R.string.button_pass, progress));
                matchBinding.butShoot.setText(getString(R.string.button_shoot, progress));
            }
        });

        // At the start of the match, we'll disabled the seekBar so it can't be changed.
        // When the match starts, we'll enable it again.
        seekbar.setEnabled(false);
    }

    // =============================================================================================
    // Function:    initZoneButtons
    // Description: Initialize the Buttons that highlight zones on the field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    @SuppressLint("ClickableViewAccessibility")
    private void initZoneButtons() {
        // Set up an OnClick listener per button
        matchBinding.butLeftZone.setOnClickListener(view -> {
            // If the match hasn't even started yet, just return
            if (Globals.CurrentMatchPhase.equals(Constants.Phases.NONE)) return;

            matchBinding.butShoot.setEnabled(in_alliance_zone);
            matchBinding.butShoot.setClickable(in_alliance_zone);
            matchBinding.butShootTap.setEnabled(in_alliance_zone);
            matchBinding.butShootTap.setClickable(in_alliance_zone);
            if (!climb_button_pressed) matchBinding.butClimb.setEnabled(in_alliance_zone);
            if (!climb_button_pressed) matchBinding.butClimb.setClickable(in_alliance_zone);

            // Force all action buttons to be un-pressed
            handleActionButtonTouch(ID_BUT_CLIMB, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_PICKUP, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_PASS, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_PASS_TAP, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_SHOOT, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_SHOOT_TAP, MotionEvent.ACTION_UP);

            // The rest of the code needs to be in TELEOP phase.  So if it's not, just return.
            if (!Globals.CurrentMatchPhase.equals(Constants.Phases.TELEOP)) return;

            matchBinding.butLeftZone.setBackgroundColor(getColor(R.color.transparent_orange));
            matchBinding.butCenterZone.setBackgroundColor(getColor(R.color.transparent));
            matchBinding.butRightZone.setBackgroundColor(getColor(R.color.transparent));

            BlueView_X = matchBinding.butLeftZone.getX() + (matchBinding.butLeftZone.getWidth() / 2f);
            BlueView_Y = matchBinding.butLeftZone.getY() + (matchBinding.butLeftZone.getHeight() / 2f);
        });

        matchBinding.butCenterZone.setOnClickListener(view -> {
            // If the match hasn't even started yet, just return
            if (Globals.CurrentMatchPhase.equals(Constants.Phases.NONE)) return;

            // Disallow shooting / climbing
            matchBinding.butShoot.setEnabled(in_alliance_zone);
            matchBinding.butShoot.setClickable(in_alliance_zone);
            matchBinding.butShootTap.setEnabled(in_alliance_zone);
            matchBinding.butShootTap.setClickable(in_alliance_zone);
            matchBinding.butClimb.setEnabled(in_alliance_zone);
            matchBinding.butClimb.setClickable(in_alliance_zone);

            // Force all action buttons to be un-pressed
            handleActionButtonTouch(ID_BUT_CLIMB, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_PICKUP, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_PASS, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_PASS_TAP, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_SHOOT, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_SHOOT_TAP, MotionEvent.ACTION_UP);

            // The rest of the code needs to be in TELEOP phase.  So if it's not, just return.
            if (!Globals.CurrentMatchPhase.equals(Constants.Phases.TELEOP)) return;

            matchBinding.butLeftZone.setBackgroundColor(getColor(R.color.transparent));
            matchBinding.butCenterZone.setBackgroundColor(getColor(R.color.transparent_orange));
            matchBinding.butRightZone.setBackgroundColor(getColor(R.color.transparent));

            BlueView_X = matchBinding.butCenterZone.getX() + (matchBinding.butCenterZone.getWidth() / 2f);
            BlueView_Y = matchBinding.butCenterZone.getY() + (matchBinding.butCenterZone.getHeight() / 2f);
        });

        matchBinding.butRightZone.setOnClickListener(view -> {
            // If the match hasn't even started yet, just return
            if (Globals.CurrentMatchPhase.equals(Constants.Phases.NONE)) return;

            matchBinding.butShoot.setEnabled(in_alliance_zone);
            matchBinding.butShoot.setClickable(in_alliance_zone);
            matchBinding.butShootTap.setEnabled(in_alliance_zone);
            matchBinding.butShootTap.setClickable(in_alliance_zone);
            if (!climb_button_pressed) matchBinding.butClimb.setEnabled(in_alliance_zone);
            if (!climb_button_pressed) matchBinding.butClimb.setClickable(in_alliance_zone);

            // Force all action buttons to be un-pressed
            handleActionButtonTouch(ID_BUT_CLIMB, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_PICKUP, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_PASS, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_PASS_TAP, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_SHOOT, MotionEvent.ACTION_UP);
            handleActionButtonTouch(ID_BUT_SHOOT_TAP, MotionEvent.ACTION_UP);

            // The rest of the code needs to be in TELEOP phase.  So if it's not, just return.
            if (!Globals.CurrentMatchPhase.equals(Constants.Phases.TELEOP)) return;

            matchBinding.butLeftZone.setBackgroundColor(getColor(R.color.transparent));
            matchBinding.butCenterZone.setBackgroundColor(getColor(R.color.transparent));
            matchBinding.butRightZone.setBackgroundColor(getColor(R.color.transparent_orange));

            BlueView_X = matchBinding.butRightZone.getX() + (matchBinding.butRightZone.getWidth() / 2f);
            BlueView_Y = matchBinding.butRightZone.getY() + (matchBinding.butRightZone.getHeight() / 2f);
        });

        matchBinding.butLeftZone.setOnTouchListener((view, motionEvent) -> {
            // only handle DOWN actions
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) return false;

            // We'll use the button click to determine if we should allow or disallow shooting / climbing but won't process
            // anything else if we're not in Tele mode.
            in_alliance_zone = ((currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_RED_ON_LEFT)) && team_alliance.substring(0, 1).equalsIgnoreCase("R")) ||
                    ((currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_BLUE_ON_LEFT)) && team_alliance.substring(0, 1).equalsIgnoreCase("B"));

            tele_button_position_x = matchBinding.butLeftZone.getX();
            tele_button_position_y = matchBinding.butLeftZone.getY();
            matchBinding.FieldTouch.dispatchTouchEvent(motionEvent);
            return false; });
        matchBinding.butCenterZone.setOnTouchListener((view, motionEvent) -> {
            // only handle DOWN actions
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) return false;

            // if the alliance zone is on the LEFT, check the left edge of the robot
            in_alliance_zone = false;
            if (((currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_RED_ON_LEFT))
                    && team_alliance.substring(0, 1).equalsIgnoreCase("R"))
                    || ((currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_BLUE_ON_LEFT))
                    && team_alliance.substring(0, 1).equalsIgnoreCase("B"))) {
                if (motionEvent.getX() <= (matchBinding.textRobot.getWidth() / 2f))
                    in_alliance_zone = true;
                // otherwise the alliance zone is on the RIGHT, check the right edge of the robot
            } else
                if (motionEvent.getX() >= (matchBinding.butCenterZone.getWidth() - matchBinding.textRobot.getWidth() / 2f))
                    in_alliance_zone = true;

            tele_button_position_x = matchBinding.butCenterZone.getX();
            tele_button_position_y = matchBinding.butCenterZone.getY();
            matchBinding.FieldTouch.dispatchTouchEvent(motionEvent);
            return false; });
        matchBinding.butRightZone.setOnTouchListener((view, motionEvent) -> {
            // only handle DOWN actions
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) return false;

            // We'll use the button click to determine if we should allow or disallow shooting / climbing but won't process
            // anything else if we're not in Tele mode.
            in_alliance_zone = ((!currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_RED_ON_LEFT)) || !team_alliance.substring(0, 1).equalsIgnoreCase("R")) &&
                    ((!currentAllianceOnLeft.equals(Constants.Match.ORIENTATION_BLUE_ON_LEFT)) || !team_alliance.substring(0, 1).equalsIgnoreCase("B"));

            tele_button_position_x = matchBinding.butRightZone.getX();
            tele_button_position_y = matchBinding.butRightZone.getY();
            matchBinding.FieldTouch.dispatchTouchEvent(motionEvent);
            return false; });
    }


    // =============================================================================================
    // Function:    initActionButtons
    // Description: Initialize the Buttons that highlight zones on the field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    @SuppressLint("ClickableViewAccessibility")
    private void initActionButtons() {
        matchBinding.butClimb.setOnTouchListener((view, motionEvent) -> {
            handleActionButtonTouch(view.getId(), motionEvent.getAction());
            return false;
        });

        matchBinding.butClimb.setOnClickListener(view -> {
            logEvent(Globals.EventList.getEventId(Globals.CurrentMatchPhase, "Climb"), 1);
            matchBinding.butClimb.setEnabled(false);
            matchBinding.butClimb.setClickable(false);
            climb_button_pressed = true;
            if (Objects.equals(Globals.CurrentMatchPhase, "AUTO")) Achievements.data_match_ClimbSuccessAuto++;
            if (Objects.equals(Globals.CurrentMatchPhase, "TELEOP")) Achievements.data_match_ClimbSuccessTele++;
        });

        matchBinding.butPickup.setOnTouchListener((view, motionEvent) -> {
            handleActionButtonTouch(view.getId(), motionEvent.getAction());
            return false;
        });

        matchBinding.butPickup.setOnClickListener(view -> {
            if (!Globals.CurrentMatchPhase.equals(Constants.Phases.AUTO)) return;

            logEvent(Globals.EventList.getEventId(Constants.Phases.AUTO, "Pickup Fuel"), 1);
            // if pickup fuel is at the depot
            if (BlueView_X <= matchBinding.FieldTouch.getWidth() * (Constants.Field.STRUCTURE_WIDTH_PERCENTAGE / 100)
                    && BlueView_Y >= matchBinding.FieldTouch.getHeight() * (Constants.Field.DEPOT_BOTTOM_PERCENTAGE / 100)
                    && BlueView_Y <= matchBinding.FieldTouch.getHeight() * (Constants.Field.DEPOT_TOP_PERCENTAGE / 100)) {
                Achievements.data_match_FuelPickUpDepot++;
            }
            // if pickup fuel is at the outpost
            if (BlueView_X <= matchBinding.FieldTouch.getWidth() * (Constants.Field.STRUCTURE_WIDTH_PERCENTAGE / 100)
                    && BlueView_Y >= Constants.Field.OUTPOST_BOTTOM_PERCENTAGE
                    && BlueView_Y <= matchBinding.FieldTouch.getHeight() * (Constants.Field.OUTPOST_TOP_PERCENTAGE / 100)) {
                Achievements.data_match_FuelPickUpOutpost++;
            }
        });

        matchBinding.butPassTap.setOnTouchListener((view, motionEvent) -> {
            handleActionButtonTouch(view.getId(), motionEvent.getAction());
            return false;
        });

        matchBinding.butPassTap.setOnClickListener(view -> {
            logEvent(Globals.EventList.getEventId(Globals.CurrentMatchPhase, "Pass 1 Fuel"), 1);
            // if pass is from alliance zone
            if (in_alliance_zone) Achievements.data_match_FuelPassAlliance += 1;
            // if pass is from neutral zone
            if (BlueView_X > matchBinding.butCenterZone.getX() && BlueView_X < matchBinding.butRightZone.getX()) Achievements.data_match_FuelPassNeutral += 1;
            // if pass is from opponent zone
            if (BlueView_X > matchBinding.butRightZone.getX()) Achievements.data_match_FuelPassOpponent += 1;
        });

        matchBinding.butPass.setOnTouchListener((view, motionEvent) -> {
            handleActionButtonTouch(view.getId(), motionEvent.getAction());
            return false;
        });

        matchBinding.butPass.setOnClickListener(view -> {
            logEvent(Globals.EventList.getEventId(Globals.CurrentMatchPhase, "Pass Many Fuel"), matchBinding.seekBar.getProgress());
            // if pass is from alliance zone
            if (in_alliance_zone) Achievements.data_match_FuelPassAlliance += matchBinding.seekBar.getProgress();
            // if pass is from neutral zone
            if (BlueView_X > matchBinding.butCenterZone.getX() && BlueView_X < matchBinding.butRightZone.getX()) Achievements.data_match_FuelPassNeutral += matchBinding.seekBar.getProgress();
            // if pass is from opponent zone
            if (BlueView_X > matchBinding.butRightZone.getX()) Achievements.data_match_FuelPassOpponent += matchBinding.seekBar.getProgress();
        });

        matchBinding.butShootTap.setOnTouchListener((view, motionEvent) -> {
            handleActionButtonTouch(view.getId(), motionEvent.getAction());
            return false;
        });

        matchBinding.butShootTap.setOnClickListener(view -> {
            logEvent(Globals.EventList.getEventId(Globals.CurrentMatchPhase, "Shoot 1 Fuel"), 1);
            Achievements.data_match_FuelShoot += 1;
        });

        matchBinding.butShoot.setOnTouchListener((view, motionEvent) -> {
            handleActionButtonTouch(view.getId(), motionEvent.getAction());
            return false;
        });

        matchBinding.butShoot.setOnClickListener(view -> {
            logEvent(Globals.EventList.getEventId(Globals.CurrentMatchPhase, "Shoot Many Fuel"),  matchBinding.seekBar.getProgress());
            Achievements.data_match_FuelShoot += matchBinding.seekBar.getProgress();
        });

        matchBinding.butClimb.setEnabled(false);
        matchBinding.butClimb.setClickable(false);
        matchBinding.butPickup.setEnabled(false);
        matchBinding.butPickup.setClickable(false);
        matchBinding.butPass.setEnabled(false);
        matchBinding.butPass.setClickable(false);
        matchBinding.butPassTap.setEnabled(false);
        matchBinding.butPassTap.setClickable(false);
        matchBinding.butShoot.setEnabled(false);
        matchBinding.butShoot.setClickable(false);
        matchBinding.butShootTap.setEnabled(false);
        matchBinding.butShootTap.setClickable(false);
    }

    // =============================================================================================
    // Function:    handleActionButtonTouch
    // Description: Handle the action button(s) being touched.  We need this not only to clean up
    //              code, but will need to call this explicitly when a Zone button is pressed to
    //              force the action button "unpressed".  Since it will be disabled, the onTouch
    //              listener for the action button won't be called.
    // Parameters:  in_viewID
    //                  The view ID representing the action button
    //              in_motionEvent
    //                  The motion event that was triggered
    // Output:      void
    // =============================================================================================
    private void handleActionButtonTouch(int in_viewID, int in_motionEvent) {
        View in_view = findViewById(in_viewID);

        int color_down = getColor(R.color.dark_grey);
        int color_up;

        if (in_viewID == ID_BUT_PICKUP) {
            color_up = getColor(R.color.dark_yellow);
        } else if ((in_viewID == ID_BUT_PASS) || (in_viewID == ID_BUT_PASS_TAP)) {
            color_up = getColor(R.color.light_blue);
        } else if ((in_viewID == ID_BUT_SHOOT) || (in_viewID == ID_BUT_SHOOT_TAP) || in_viewID == ID_BUT_CLIMB) {
            color_up = getColor(R.color.dark_green);
        } else return;

        if (in_motionEvent == MotionEvent.ACTION_DOWN)
            in_view.setBackgroundColor(color_down);
        else if (in_motionEvent == MotionEvent.ACTION_UP)
            in_view.setBackgroundColor(color_up);
    }

    // =============================================================================================
    // Function:    logEvent
    // Description: Log an event into the logger
    // Parameters:  in_event_id
    //                  The ID value for the event we want to display
    //              in_Count
    //                  The number of times this event has occurred with one entry
    //                  eg: Shot 32 balls "at once".
    // Output:      void
    // =============================================================================================
    private void logEvent(int in_event_id, int in_Count) {
        // If we get called with no valid event, just return
        if (in_event_id == Constants.Events.ID_NO_EVENT) return;

        // Set the status text for the UNDO button.
        setEventStatus(in_event_id);

        // Log the event to the Logger and ensure the UNDO button is enabled
        Globals.EventLogger.LogEvent(in_event_id, BlueView_X, BlueView_Y, game_Timer.getElapsedMilliSeconds(), in_Count);
        matchBinding.butUndo.setVisibility(View.VISIBLE);
        matchBinding.butUndo.setEnabled(true);
    }
}