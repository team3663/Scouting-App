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
import android.widget.SeekBar;
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
import com.team3663.scouting_app.utility.Logger;
import com.team3663.scouting_app.utility.achievements.Achievements;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class MatchTally extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private MatchTallyBinding matchBinding;
    private static OrientationEventListener OEL = null; // needed to detect the screen being flipped around
    private static String currentOrientation = Constants.Match.ORIENTATION_LANDSCAPE;
    private static float current_X_Relative = 0;
    private static float current_Y_Relative = 0;
    private static float current_X_Absolute = 0;
    private static float current_Y_Absolute = 0;
    private static float starting_X_Relative = 0;
    private static float starting_Y_Relative = 0;
    private static float starting_X_Absolute = 0;
    private static float starting_Y_Absolute = 0;
    private static long start_time_not_moving;
    private static long currentTouchTime = 0;
    private static float tele_button_position_x = 0;
    private static float tele_button_position_y = 0;;

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
        Globals.EventLogger.LogEvent(Constants.Events.ID_AUTO_START_GAME_PIECE, starting_X_Relative, starting_Y_Relative, 0);

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

        // Calculate the image dimensions
        Constants.Match.IMAGE_WIDTH = matchBinding.FieldTouch.getWidth();
        Constants.Match.IMAGE_HEIGHT = matchBinding.FieldTouch.getHeight();

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

        // Hide the Pickup Fuel button
        matchBinding.butPickup.setClickable(false);
        matchBinding.butPickup.setVisibility(View.INVISIBLE);

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
                        Achievements.data_OrphanEvents++;
                        Achievements.data_match_OrphanEvents++;
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
            currentOrientation = Constants.Match.ORIENTATION_LANDSCAPE;
            OEL = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onOrientationChanged(int rotation_degrees) {
                    // If the device is in the 0 to 180 degree range, make it Landscape
                    if ((rotation_degrees >= 0) && (rotation_degrees < 180) && !currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE)) {
                        matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field2026));
                        currentOrientation = Constants.Match.ORIENTATION_LANDSCAPE;

                        // Set the robot location based on the new rotation
                        setRobotLocation(matchBinding.FieldTouch.getWidth() - starting_X_Absolute, matchBinding.FieldTouch.getHeight() - starting_Y_Absolute);
                    }
                    // If the device is in the 180 to 359 degree range, make it Landscape
                    // We can get passed a -1 if the device can't tell (it's lying flat) and we want to ignore that
                    else if ((rotation_degrees >= 180) && !currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE_REVERSE)) {
                        matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field2026_flipped));
                        currentOrientation = Constants.Match.ORIENTATION_LANDSCAPE_REVERSE;

                        // Set the robot location based on the new rotation
                        setRobotLocation(matchBinding.FieldTouch.getWidth() - starting_X_Absolute, matchBinding.FieldTouch.getHeight() - starting_Y_Absolute);
                    }
                }
            };

            // Enable orientation listening if we can!
            if (OEL.canDetectOrientation()) {
                OEL.enable();
            }
        } else if (Globals.CurrentFieldOrientationPos == 1) {   // Blue On Left
            currentOrientation = Constants.Match.ORIENTATION_LANDSCAPE;
            matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field2026));
        } else {    // Red On Left
            matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field2026_flipped));
            currentOrientation = Constants.Match.ORIENTATION_LANDSCAPE_REVERSE;
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
            matchBinding.textStatus.setText(getString(R.string.match_status_practice));
            matchBinding.textPractice.setText(getString(R.string.match_practice_watermark));
            matchBinding.textPractice.setTextSize(210);
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
            int last_event_id;
            last_event_id = Globals.EventLogger.UndoLastEvent();

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
        String alliance = Globals.MatchList.getAllianceForTeam(Globals.CurrentTeamToScout);

        // If we don't know the alliance (didn't have match schedule?) then default to the PREFERRED position
        if (alliance.isEmpty()) alliance = Constants.Settings.PREF_TEAM_POS[Globals.CurrentPrefTeamPos];
        boolean blue_alliance = alliance.substring(0,1).equalsIgnoreCase("B");

        if (in_X > 0) {
            starting_X_Absolute = in_X;
            starting_Y_Absolute = in_Y;
        } else if ((blue_alliance && currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE)) ||
                (!blue_alliance && currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE_REVERSE))) {
            starting_X_Absolute = matchBinding.FieldTouch.getWidth() - starting_X_Absolute;
            starting_Y_Absolute = matchBinding.FieldTouch.getHeight() - starting_Y_Absolute;
        }

        // Snap the robot to the correct starting line if we haven't started the match
        if (Globals.CurrentMatchPhase.equals(Constants.Phases.NONE)) {
            if ((blue_alliance && currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE)) ||
                    (!blue_alliance && currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE_REVERSE))) {
                starting_X_Absolute = Math.min(Math.max(in_X, matchBinding.FieldTouch.getWidth() * Constants.Match.START_LINE_X / 100.0f - offset), matchBinding.FieldTouch.getWidth() * Constants.Match.START_LINE_X / 100.0f + offset);
            } else {
                starting_X_Absolute = Math.min(Math.max(in_X, matchBinding.FieldTouch.getWidth() * (100.0f - Constants.Match.START_LINE_X) / 100.0f - offset), matchBinding.FieldTouch.getWidth() * (100.0f - Constants.Match.START_LINE_X) / 100.0f + offset);
            }

            // Save off the correct relative values based on the orientation
            if (currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE)) {
                starting_X_Relative = starting_X_Absolute;
                starting_Y_Relative = matchBinding.FieldTouch.getHeight() - starting_Y_Absolute;
            } else {
                starting_X_Relative = matchBinding.FieldTouch.getWidth() - starting_X_Absolute;
                starting_Y_Relative = starting_Y_Absolute;
            }
        }

        // Make sure we see the location of the robot
        matchBinding.textRobot.setX(starting_X_Absolute - offset);
        matchBinding.textRobot.setY(starting_Y_Absolute - offset);

        // Enable the robot
        // This will be called initially as well as during screen rotations.  in_X will be 0 at that time.  Make it a no-op
        if (in_X > 0) {
            matchBinding.textRobot.setVisibility(View.VISIBLE);
            matchBinding.butMatchControl.setEnabled(true);
            matchBinding.butMatchControl.setClickable(true);
            matchBinding.textStatus.setText("");
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
            current_X_Absolute = motionEvent.getX() + tele_button_position_x;
            current_Y_Absolute = motionEvent.getY() + tele_button_position_y;

            // Save where we touched the field image relative to the fields orientation
            // Since the App has (0,0) in the top left, but our reporting will have (0,0) in the bottom left,
            // we need to flip the Y coordinate.
            if (currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE)) {
                current_X_Relative = current_X_Absolute;
                current_Y_Relative = matchBinding.FieldTouch.getHeight() - current_Y_Absolute;
            } else {
                current_X_Relative = matchBinding.FieldTouch.getWidth() - current_X_Absolute;
                current_Y_Relative = current_Y_Absolute;
            }

            setRobotLocation(current_X_Absolute, current_Y_Absolute);

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
        matchBinding.butAllianceZone.bringToFront();
        matchBinding.butNeutralZone.bringToFront();
        matchBinding.butOpponentZone.bringToFront();
        matchBinding.FieldTouch.invalidate();
        matchBinding.FieldTouch.requestLayout();
    }

    // =============================================================================================
    // Function:    initSeekBar
    // Description: Initialize the SeekBar
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initSeekBar() {
        SeekBar seekbar = findViewById(R.id.seekBar);
        TextView seekbarProgress = findViewById(R.id.text_SeekBarProgress);

        seekbar.setProgress(Globals.numStartingGamePiece);
        seekbar.setMax(Constants.Match.SEEKBAR_MAX);
        seekbarProgress.setText(String.valueOf(seekbar.getProgress()));

        matchBinding.butPass.setText(getString(R.string.button_pass).replace("!#!", String.valueOf(Globals.numStartingGamePiece)));
        matchBinding.butShoot.setText(getString(R.string.button_shoot).replace("!#!", String.valueOf(Globals.numStartingGamePiece)));

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String progress_str = String.valueOf(progress);

                seekbarProgress.setText(progress_str);
                matchBinding.butPass.setText(getString(R.string.button_pass).replace("!#!", progress_str));
                matchBinding.butShoot.setText(getString(R.string.button_shoot).replace("!#!", progress_str));
            }
        });
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
        matchBinding.butAllianceZone.setOnClickListener(view -> {
            if (!Globals.CurrentMatchPhase.equals(Constants.Phases.TELEOP)) return;

            matchBinding.butAllianceZone.setBackgroundColor(getColor(R.color.transparent_orange));
            matchBinding.butNeutralZone.setBackgroundColor(getColor(R.color.transparent));
            matchBinding.butOpponentZone.setBackgroundColor(getColor(R.color.transparent));

            // Allow shooting
            matchBinding.butShoot.setEnabled(true);
            matchBinding.butShoot.setClickable(true);
            matchBinding.butShootTap.setEnabled(true);
            matchBinding.butShootTap.setClickable(true);

            current_X_Relative = matchBinding.butAllianceZone.getX() + (matchBinding.butAllianceZone.getWidth() / 2f);
            current_Y_Relative = matchBinding.butAllianceZone.getY() + (matchBinding.butAllianceZone.getHeight() / 2f);
        });

        matchBinding.butNeutralZone.setOnClickListener(view -> {
            if (!Globals.CurrentMatchPhase.equals(Constants.Phases.TELEOP)) return;

            matchBinding.butAllianceZone.setBackgroundColor(getColor(R.color.transparent));
            matchBinding.butNeutralZone.setBackgroundColor(getColor(R.color.transparent_orange));
            matchBinding.butOpponentZone.setBackgroundColor(getColor(R.color.transparent));

            // Disallow shooting
            matchBinding.butShoot.setEnabled(false);
            matchBinding.butShoot.setClickable(false);
            matchBinding.butShootTap.setEnabled(false);
            matchBinding.butShootTap.setClickable(false);

            current_X_Relative = matchBinding.butNeutralZone.getX() + (matchBinding.butNeutralZone.getWidth() / 2f);
            current_Y_Relative = matchBinding.butNeutralZone.getY() + (matchBinding.butNeutralZone.getHeight() / 2f);
        });

        matchBinding.butOpponentZone.setOnClickListener(view -> {
            if (!Globals.CurrentMatchPhase.equals(Constants.Phases.TELEOP)) return;

            matchBinding.butAllianceZone.setBackgroundColor(getColor(R.color.transparent));
            matchBinding.butNeutralZone.setBackgroundColor(getColor(R.color.transparent));
            matchBinding.butOpponentZone.setBackgroundColor(getColor(R.color.transparent_orange));

            // Disallow shooting
            matchBinding.butShoot.setEnabled(false);
            matchBinding.butShoot.setClickable(false);
            matchBinding.butShootTap.setEnabled(false);
            matchBinding.butShootTap.setClickable(false);

            current_X_Relative = matchBinding.butOpponentZone.getX() + (matchBinding.butOpponentZone.getWidth() / 2f);
            current_Y_Relative = matchBinding.butOpponentZone.getY() + (matchBinding.butOpponentZone.getHeight() / 2f);
        });

        matchBinding.butAllianceZone.setOnTouchListener((view, motionEvent) -> {
            tele_button_position_x = matchBinding.butAllianceZone.getX();
            tele_button_position_y = matchBinding.butAllianceZone.getY();
            matchBinding.FieldTouch.dispatchTouchEvent(motionEvent);
            return false; });
        matchBinding.butNeutralZone.setOnTouchListener((view, motionEvent) -> {
            tele_button_position_x = matchBinding.butNeutralZone.getX();
            tele_button_position_y = matchBinding.butNeutralZone.getY();
            matchBinding.FieldTouch.dispatchTouchEvent(motionEvent);
            return false; });
        matchBinding.butOpponentZone.setOnTouchListener((view, motionEvent) -> {
            tele_button_position_x = matchBinding.butOpponentZone.getX();
            tele_button_position_y = matchBinding.butOpponentZone.getY();
            matchBinding.FieldTouch.dispatchTouchEvent(motionEvent);
            return false; });
    }


    // =============================================================================================
    // Function:    initActionButtons
    // Description: Initialize the Buttons that highlight zones on the field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initActionButtons() {
        matchBinding.butClimb.setOnClickListener(view -> {
            logEvent(Globals.EventList.getEventId(Globals.CurrentMatchPhase, "Climb"), 1);
        });

        matchBinding.butPickup.setOnClickListener(view -> {
            if (!Globals.CurrentMatchPhase.equals(Constants.Phases.AUTO)) return;

            logEvent(Globals.EventList.getEventId(Constants.Phases.AUTO, "Pickup Fuel"), 1);
        });

        matchBinding.butPassTap.setOnClickListener(view -> {
            logEvent(Globals.EventList.getEventId(Globals.CurrentMatchPhase, "Pass 1 Fuel"), 1);
        });

        matchBinding.butPass.setOnClickListener(view -> {
            logEvent(Globals.EventList.getEventId(Globals.CurrentMatchPhase, "Pass Many Fuel"), matchBinding.seekBar.getProgress());
        });

        matchBinding.butShootTap.setOnClickListener(view -> {
            logEvent(Globals.EventList.getEventId(Globals.CurrentMatchPhase, "Shoot 1 Fuel"), 1);
        });

        matchBinding.butShoot.setOnClickListener(view -> {
            logEvent(Globals.EventList.getEventId(Globals.CurrentMatchPhase, "Shoot Many Fuel"),  matchBinding.seekBar.getProgress());
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
    // Function:    logEvent
    // Description: Log an event into the logger
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void logEvent(int in_event_id, int in_Count) {
        // If we get called with no valid event, just return
        if (in_event_id == Constants.Events.ID_NO_EVENT) return;

        // Set the status text for the UNDO button.
        setEventStatus(in_event_id);

        // Log the event to the Logger and ensure the UNDO button is enabled
        Globals.EventLogger.LogEvent(in_event_id, current_X_Relative, current_Y_Relative, game_Timer.getElapsedMilliSeconds(), in_Count);
        matchBinding.butUndo.setVisibility(View.VISIBLE);
        matchBinding.butUndo.setEnabled(true);
    }
}