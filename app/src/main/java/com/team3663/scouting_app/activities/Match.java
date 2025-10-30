package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.MatchBinding;
import com.team3663.scouting_app.utility.Logger;
import com.team3663.scouting_app.utility.achievements.Achievements;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Match extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private MatchBinding matchBinding;
    private static OrientationEventListener OEL = null; // needed to detect the screen being flipped around
    private static String currentOrientation = Constants.Match.ORIENTATION_LANDSCAPE;
    private static double currentTouchTime = 0;
    private static float current_X_Relative = 0;
    private static float current_Y_Relative = 0;
    private static float current_X_Absolute = 0;
    private static float current_Y_Absolute = 0;
    private static float starting_X_Relative = 0;
    private static float starting_Y_Relative = 0;
    private static float starting_X_Absolute = 0;
    private static float starting_Y_Absolute = 0;
    private static long start_time_not_moving;
    private static boolean showing_event_detail_menu = false;
    private static int showing_event_detail_group;

    // Define a Timer and TimerTasks so you can schedule things
    Timer match_Timer;
    Timer robot_Timer;
    TimerTask robot_timertask;
    TimerTask auto_timertask;
    TimerTask teleop_timertask;
    TimerTask gametime_timertask;
    TimerTask flashing_timertask;

    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle in_savedInstanceState) {
        super.onCreate(in_savedInstanceState);
        EdgeToEdge.enable(this);
        matchBinding = MatchBinding.inflate(getLayoutInflater());
        View page_root_view = matchBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(matchBinding.match, (v, insets) -> {
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
        initContextMenu();
        initRobotStartLocation();
    }

    @Override
    public void onCreateContextMenu(ContextMenu in_menu, View in_v, ContextMenu.ContextMenuInfo in_menuInfo) {
        super.onCreateContextMenu(in_menu, in_v, in_menuInfo);

        // Check to make sure the game is going
        if (!Globals.CurrentMatchPhase.equals(Constants.Phases.NONE)) {
            // Save off the time that this was touched
            currentTouchTime = System.currentTimeMillis();

            // Call the correct context menu
            if (in_v.getId() == R.id.image_FieldView)
                if (Globals.MaxEventGroups > 1) {
                    showing_event_detail_menu = false;
                    createEventContextMenu(in_menu);
                }
                else {
                    showing_event_detail_menu = true;
                    showing_event_detail_group = Globals.MaxEventGroups;
                    createEventContextSubMenu(in_menu);
                }
            else if (in_v.getId() == R.id.view_ContextSubMenuView) {
                showing_event_detail_menu = true;
                createEventContextSubMenu(in_menu);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem in_item) {
        // Take action on whether we're showing the detail event menu or the group event menu
        if (showing_event_detail_menu) {
            int event_id = Globals.EventList.getEventId(Objects.requireNonNull(in_item.getTitle()).toString());

            if (!Globals.isPractice) setEventStatus(event_id);

            Globals.EventLogger.LogEvent(event_id, current_X_Relative, current_Y_Relative, currentTouchTime);
            matchBinding.butUndo.setVisibility(View.VISIBLE);
            matchBinding.butUndo.setEnabled(true);
            showing_event_detail_menu = false;
        }
        else {
            // Set the group we clicked on so the sub-menu will be the right one, then show the sub-menu
            showing_event_detail_group = Globals.EventList.getGroupId(Objects.requireNonNull(in_item.getTitle()).toString());
            matchBinding.viewContextSubMenuView.showContextMenu(current_X_Absolute, current_Y_Absolute);
        }

        return true;
    }

    // =============================================================================================
    // Function:    createEventContextMenu
    // Description: Create the context menu for the Event Group list
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint({"DiscouragedApi", "SetTextI18n"})
    public void createEventContextMenu(ContextMenu in_menu) {
        // Loop through the groups, and if there are events for this group in this phase of the game,
        // add the group to the menu
        for (int i = 1; i <= Globals.MaxEventGroups; ++i) {
            if (Globals.EventList.hasEventsForGroup(i, Globals.CurrentMatchPhase))
                in_menu.add(Globals.EventList.getGroupName(i));
        }

        // Go through all of the items and see if we want to customize the text using a SpannableString
        for (int i = 0; i < in_menu.size(); i++) {
            MenuItem item = in_menu.getItem(i);
            SpannableString ss = new SpannableString(item.getTitle());
            ss.setSpan(new AbsoluteSizeSpan(24), 0, ss.length(), 0);

            // If this menuItem has a color to use, then use it
            if (Globals.ColorList.isColorValid(Globals.CurrentColorId - 1)) {
                int groupID = Globals.EventList.getGroupId(Objects.requireNonNull(item.getTitle()).toString());
                if (Globals.EventList.hasGroupColor(groupID))
                    ss.setSpan(new ForegroundColorSpan(Globals.ColorList.getColor(Globals.CurrentColorId - 1, Globals.EventList.getGroupColor(groupID))), 0, ss.length(), 0);
            }

            item.setTitle(ss);
        }
    }

    // =============================================================================================
    // Function:    createEventContextSubMenu
    // Description: Create the context menu for the Event Group list
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint({"DiscouragedApi", "SetTextI18n"})
    public void createEventContextSubMenu(ContextMenu in_menu) {
        // In case there's not a valid group, exit out.  This shouldn't happen.
        if (showing_event_detail_group < 1) return;

        // Get the events
        ArrayList<String> events;
        events = Globals.EventList.getNextEvents(Logger.current_event[showing_event_detail_group]);

        if ((events == null) || events.isEmpty()) {
            events = Globals.EventList.getEventsForPhase(Globals.CurrentMatchPhase, showing_event_detail_group);
        }

        // Add all the events
        for (String event : events) {
            in_menu.add(event);
        }

        // Go through all of the items and see if we want to customize the text using a SpannableString
        for (int i = 0; i < in_menu.size(); i++) {
            MenuItem item = in_menu.getItem(i);
            SpannableString ss = new SpannableString(item.getTitle());
            ss.setSpan(new AbsoluteSizeSpan(24), 0, ss.length(), 0);

            // If this menuItem has a color to use, then use it
            if (Globals.ColorList.isColorValid(Globals.CurrentColorId - 1)) {
                int eventID = Globals.EventList.getEventId(Objects.requireNonNull(item.getTitle()).toString());
                if (Globals.EventList.hasEventColor(eventID))
                    ss.setSpan(new ForegroundColorSpan(Globals.ColorList.getColor(Globals.CurrentColorId - 1, Globals.EventList.getEventColor(eventID))), 0, ss.length(), 0);
            }

            item.setTitle(ss);
        }
    }

    // =============================================================================================
    // Function:    startMatch
    // Description: Starts the match Timer and all the scheduled events and repeat schedule events
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint({"DiscouragedApi", "SetTextI18n"})
    public void startMatch() {
        // Record the current/start time of the match to calculate elapsed time
        Globals.StartTime = System.currentTimeMillis();

        // Achievements
        if (Achievements.data_StartTime == 0) Achievements.data_StartTime = Globals.StartTime;
        Globals.myAchievements.clearMatchData();

        // Log the starting time
        Globals.EventLogger.LogData(Constants.Logger.LOGKEY_START_TIME, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")));

        // Disable orientation listening if we can!  Once we start the match don't allow rotation anymore
        if (OEL != null && OEL.canDetectOrientation()) {
            OEL.disable();
        }

        // Hide the starting location of the robot
        matchBinding.textRobot.setVisibility(View.INVISIBLE);

        // Log the starting location (the match hasn't started so we need to specify a "0" time
        // or else it will log as max match time which wastes extra log characters for no benefit)
        Globals.EventLogger.LogEvent(Constants.Events.ID_AUTO_START_GAME_PIECE, starting_X_Relative, starting_Y_Relative, 0);

        // Create the Timers and timer tasks
        match_Timer = new Timer();
        auto_timertask = new AutoTimerTask();
        teleop_timertask = new TeleopTimerTask();
        gametime_timertask = new GameTimeTimerTask();
        flashing_timertask = new FlashingTimerTask();

        // Hide Back Button (too late to go back now!)
        matchBinding.butBack.setClickable(false);
        matchBinding.butBack.setVisibility(View.INVISIBLE);

        // Clear out the team to override (kept it in case they hit the Back button)
        Globals.CurrentTeamOverrideNum = "";

        // Show the time
        matchBinding.textTime.setVisibility(View.VISIBLE);

        // Calculate the image dimensions
        Constants.Match.IMAGE_WIDTH = matchBinding.imageFieldView.getWidth();
        Constants.Match.IMAGE_HEIGHT = matchBinding.imageFieldView.getHeight();

        // Set timer tasks
        match_Timer.schedule(auto_timertask, (Constants.Match.TIMER_AUTO_LENGTH + Constants.Match.TIMER_AUTO_TELEOP_DELAY) * 1_000);
        match_Timer.scheduleAtFixedRate(gametime_timertask, 0, Constants.Match.TIMER_UPDATE_RATE);
        match_Timer.scheduleAtFixedRate(flashing_timertask, 0, Constants.Match.BUTTON_FLASH_INTERVAL);

        // Set match Phase to be correct and Button text
        Globals.CurrentMatchPhase = Constants.Phases.AUTO;
        matchBinding.butMatchControl.setText(getString(R.string.button_start_teleop));
        matchBinding.butMatchControl.setBackgroundColor(getColor(R.color.dark_yellow));
        matchBinding.butMatchControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.start_teleop, 0);
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
        // Set the start Time so that the Display Time will be correct
        Globals.StartTime = System.currentTimeMillis() - Constants.Match.TIMER_AUTO_LENGTH * 1_000;
        matchBinding.textTime.setText(Constants.Match.TIMER_TELEOP_LENGTH / 60 + ":" + String.format("%02d", Constants.Match.TIMER_TELEOP_LENGTH % 60));

        match_Timer.schedule(teleop_timertask, Constants.Match.TIMER_TELEOP_LENGTH * 1_000);

        // Set match Phase to be correct and Button text
        Globals.CurrentMatchPhase = Constants.Phases.TELEOP;

        // Certain actions can't be set from a non-UI thread (like within a TimerTask that runs on a
        // separate thread). So we need to make a Runner that will execute on the UI thread to set this.
        Match.this.runOnUiThread(() -> {
            matchBinding.switchNotMoving.setEnabled(true);
            matchBinding.switchNotMoving.setTextColor(Color.WHITE);
            matchBinding.switchNotMoving.setVisibility(View.VISIBLE);
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
        Match.this.runOnUiThread(() -> {
            matchBinding.butMatchControl.setText(getString(R.string.button_match_next));
            matchBinding.butMatchControl.setTextColor(getColor(R.color.cpr_bkgnd));
            matchBinding.butMatchControl.setBackgroundColor(getColor(R.color.white));
            matchBinding.butMatchControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.next_button, 0);
        });
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
            new AlertDialog.Builder(Match.this)
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
        // Need to set match_Timer and TimerTasks to null so we can create "new" ones at the start of the next match
        if (match_Timer != null) {
            match_Timer.cancel();
            match_Timer.purge();
        }
        if (robot_Timer != null) {
            robot_Timer.cancel();
            robot_Timer.purge();
        }

        match_Timer = null;
        robot_Timer = null;
        robot_timertask = null;
        auto_timertask = null;
        teleop_timertask = null;
        gametime_timertask = null;
        flashing_timertask = null;

        // Reset match phase so that the next time we hit Start Match we do the right thing
        Globals.CurrentMatchPhase = Constants.Phases.NONE;

        // If either of the toggles are on turn them off
        matchBinding.switchNotMoving.setChecked(false);
        matchBinding.switchDefense.setChecked(false);
        matchBinding.switchDefended.setChecked(false);

        // Go to the next page
        Intent GoToPostMatch = new Intent(Match.this, PostMatch.class);
        startActivity(GoToPostMatch);

        finish();
    }

    // =============================================================================================
    // Class:       AutoTimerTask
    // Description: Defines the TimerTask trigger to when the Auto phase of the game is over.
    // =============================================================================================
    public class AutoTimerTask extends TimerTask {
        @Override
        public void run() {
            if (Globals.CurrentMatchPhase.equals(Constants.Phases.AUTO)) {
                startTeleop();
            }
        }
    }

    // =============================================================================================
    // Class:       TeleopTimerTask
    // Description: Defines the TimerTask trigger to when the Teleop phase of the game is over.
    // =============================================================================================
    public class TeleopTimerTask extends TimerTask {
        @Override
        public void run() {
            if (Globals.CurrentMatchPhase.equals(Constants.Phases.TELEOP)) {
                endTeleop();
            }
        }
    }

    // =============================================================================================
    // Class:       GameTimeTimerTask
    // Description: Defines the TimerTask trigger to when the Teleop phase of the game is over.
    // =============================================================================================
    public class GameTimeTimerTask extends TimerTask {
        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        @Override
        public void run() {
            // Get elapsed time in seconds without decimal and round to make it more accurate and not skip numbers
            int elapsedSeconds;

            if (Globals.CurrentMatchPhase.equals(Constants.Phases.AUTO)) {
                elapsedSeconds = (int) (Constants.Match.TIMER_AUTO_LENGTH - Math.round((System.currentTimeMillis() - Globals.StartTime) / 1_000.0));
            } else {
                elapsedSeconds = (int) (Constants.Match.TIMER_TELEOP_LENGTH + Constants.Match.TIMER_AUTO_LENGTH - Math.round((System.currentTimeMillis() - Globals.StartTime) / 1_000.0));
            }
            if (elapsedSeconds < 0) elapsedSeconds = 0;
            matchBinding.textTime.setText(elapsedSeconds / 60 + ":" + String.format("%02d", elapsedSeconds % 60));
        }
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

            try {
                Thread.sleep(Constants.Match.BUTTON_FLASH_BLINK_INTERVAL);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // If it's checked, wait a bit then make it BUTTON_COLOR_FLASH
            if (matchBinding.switchNotMoving.isChecked()) matchBinding.switchNotMoving.setBackgroundColor(Constants.Match.BUTTON_COLOR_FLASH);
            if (matchBinding.switchDefense.isChecked()) matchBinding.switchDefense.setBackgroundColor(Constants.Match.BUTTON_COLOR_FLASH);
            if (matchBinding.switchDefended.isChecked()) matchBinding.switchDefended.setBackgroundColor(Constants.Match.BUTTON_COLOR_FLASH);
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
                        matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field_image));
                        currentOrientation = Constants.Match.ORIENTATION_LANDSCAPE;
                    }
                    // If the device is in the 180 to 359 degree range, make it Landscape
                    // We can get passed a -1 if the device can't tell (it's lying flat) and we want to ignore that
                    else if ((rotation_degrees >= 180) && !currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE_REVERSE)) {
                        matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field_image_flipped));
                        currentOrientation = Constants.Match.ORIENTATION_LANDSCAPE_REVERSE;
                    }

                    // Set the robot starting location based on the new rotation
                    setRobotStartLocation(0, 0);
                }
            };

            // Enable orientation listening if we can!
            if (OEL.canDetectOrientation()) {
                OEL.enable();
            }
        } else if (Globals.CurrentFieldOrientationPos == 1) {   // Blue On Left
            currentOrientation = Constants.Match.ORIENTATION_LANDSCAPE;
            matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field_image));
        } else {    // Red On Left
            matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field_image_flipped));
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
        matchBinding.textTime.setVisibility(View.INVISIBLE);
        matchBinding.textTime.setBackgroundColor(Color.TRANSPARENT);
        matchBinding.textTime.setTextSize(24F);
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
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_START_TIME_OFFSET, String.valueOf(Math.round((Constants.Match.TIMER_AUTO_LENGTH * 1000.0 - System.currentTimeMillis() + Globals.StartTime) / 10.0) / 100.0));
                    startTeleop();
                    break;
                case Constants.Phases.TELEOP:
                    if (Globals.StartTime + (Constants.Match.TIMER_AUTO_LENGTH + Constants.Match.TIMER_TELEOP_LENGTH) * 1000 > System.currentTimeMillis())
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
            Intent GoToPreviousPage = new Intent(Match.this, PreMatch.class);
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
                Match.this.runOnUiThread(() -> {
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
    // Function:    setEventStatus
    // Description: Set the status text for a given event text
    // Parameters:  in_event_id
    //                  The ID value for the event we want to display
    // Output:      void
    // =============================================================================================
    private void setEventStatus(int in_event_id) {
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
        // Initialize the Not Moving Switch settings
        matchBinding.switchNotMoving.setTextColor(Constants.Match.BUTTON_TEXT_COLOR_DISABLED);
        matchBinding.switchNotMoving.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);
        matchBinding.switchNotMoving.setVisibility(View.INVISIBLE);
        // Do this so that you can't mess with the switch during the wrong phases
        matchBinding.switchNotMoving.setEnabled(false);

        // This gets called if either the switch is clicked on, or the slide toggle is flipped (covers both)
        matchBinding.switchNotMoving.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // If the button is being turned ON make it RED otherwise LTGRAY
            if (isChecked) {
                Globals.EventLogger.LogEvent(Constants.Events.ID_NOT_MOVING_START);
                matchBinding.switchNotMoving.setBackgroundColor(Constants.Match.BUTTON_COLOR_FLASH);
                start_time_not_moving = System.currentTimeMillis();
                Achievements.data_match_Toggle_NotMoving++;
                Achievements.data_Toggle_NotMoving++;
            } else {
                Globals.EventLogger.LogEvent(Constants.Events.ID_NOT_MOVING_END);
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
        // Initialize the Defense Switch settings
        matchBinding.switchDefense.setTextColor(Constants.Match.BUTTON_TEXT_COLOR_DISABLED);
        matchBinding.switchDefense.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);
        matchBinding.switchDefense.setVisibility(View.INVISIBLE);
        // Do this so that you can't mess with the switch during the wrong phases
        matchBinding.switchDefense.setEnabled(false);

        // This gets called if either the switch is clicked on, or the slide toggle is flipped (covers both)
        matchBinding.switchDefense.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // If the button is being turned ON make it RED otherwise LTGRAY
            if (isChecked) {
                Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENSE_START);
                matchBinding.switchDefense.setBackgroundColor(Constants.Match.BUTTON_COLOR_FLASH);
                Achievements.data_Toggle_Defense++;
            } else {
                Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENSE_END);
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
        // Initialize the Defended Switch settings
        matchBinding.switchDefended.setTextColor(Constants.Match.BUTTON_TEXT_COLOR_DISABLED);
        matchBinding.switchDefended.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);
        matchBinding.switchDefended.setVisibility(View.INVISIBLE);
        // Do this so that you can't mess with the switch during the wrong phases
        matchBinding.switchDefended.setEnabled(false);

        // This gets called if either the switch is clicked on, or the slide toggle is flipped (covers both)
        matchBinding.switchDefended.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Globals.isDefended = isChecked;

            if (isChecked) {
                Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENDED_START);
                matchBinding.switchDefended.setBackgroundColor(Constants.Match.BUTTON_COLOR_FLASH);
                Achievements.data_Toggle_Defended++;
            } else {
                Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENDED_END);
                matchBinding.switchDefended.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);
            }
        });

        matchBinding.switchDefended.setOnClickListener(view -> {
            // Need this listener or else the onCheckedChanged won't fire either.
        });
    }

    // =============================================================================================
    // Function:    initContextMenu
    // Description: Initialize the Field of Play image
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    @SuppressLint("ClickableViewAccessibility")
    private void initContextMenu() {
        // Define a context menu
        RelativeLayout ContextMenu = matchBinding.ContextMenu;

        // This is required it will not run without it
        registerForContextMenu(matchBinding.imageFieldView);
        registerForContextMenu(matchBinding.viewContextSubMenuView);

        // So that it activates on a normal click instead of a long click
        ContextMenu.setOnTouchListener((view, motionEvent) -> {
            // Save where we touched the field image regardless of its orientation
            current_X_Absolute = motionEvent.getX();
            current_Y_Absolute = motionEvent.getY();

            // Save where we touched the field image relative to the fields orientation
            // Since the App has (0,0) in the top left, but our reporting will have (0,0) in the bottom left,
            // we need to flip the Y coordinate.
            if (currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE)) {
                current_X_Relative = current_X_Absolute;
                current_Y_Relative = Constants.Match.IMAGE_HEIGHT - current_Y_Absolute;
            } else {
                current_X_Relative = Constants.Match.IMAGE_WIDTH - current_X_Absolute;
                current_Y_Relative = current_Y_Absolute;
            }

            if (Globals.CurrentMatchPhase.equals(Constants.Phases.NONE))
                setRobotStartLocation(current_X_Absolute, current_Y_Absolute);
            else
                matchBinding.imageFieldView.showContextMenu(current_X_Absolute, current_Y_Absolute);

            return false;
        });
    }

    // =============================================================================================
    // Function:    setRobotStartLocation
    // Description: Initialize the Robot starting location
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    @SuppressLint("ClickableViewAccessibility")
    private void setRobotStartLocation(float in_X, float in_Y) {
        float offset = (float) (matchBinding.textRobot.getWidth() / 2);
        boolean blue_alliance = Globals.MatchList.getAllianceForTeam(Globals.CurrentTeamToScout).equals("BLUE");

        if (in_X > 0) {
            starting_X_Absolute = in_X;
            starting_Y_Absolute = in_Y;
        } else if ((blue_alliance && currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE)) ||
                (!blue_alliance && currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE_REVERSE))) {
            starting_X_Absolute = matchBinding.imageFieldView.getWidth() - starting_X_Absolute;
            starting_Y_Absolute = matchBinding.imageFieldView.getHeight() - starting_Y_Absolute;
        }

        // Snap the robot to the correct starting line
        if ((blue_alliance && currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE)) ||
            (!blue_alliance && currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE_REVERSE))) {
            starting_X_Absolute = Math.min(Math.max(in_X, matchBinding.imageFieldView.getWidth() * Constants.Match.START_LINE_X / 100.0f - offset), matchBinding.imageFieldView.getWidth() * Constants.Match.START_LINE_X / 100.0f + offset);
        }
        else {
            starting_X_Absolute = Math.min(Math.max(in_X, matchBinding.imageFieldView.getWidth() * (100.0f - Constants.Match.START_LINE_X) / 100.0f - offset), matchBinding.imageFieldView.getWidth() * (100.0f - Constants.Match.START_LINE_X) / 100.0f + offset);
        }

        // Save off the correct relative values based on the orientation
        if (currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE_REVERSE)) {
            starting_X_Relative = matchBinding.imageFieldView.getWidth() - starting_X_Absolute;
            starting_Y_Relative = matchBinding.imageFieldView.getHeight() - starting_Y_Absolute;
        } else {
            starting_X_Relative = starting_X_Absolute;
            starting_Y_Relative = starting_Y_Absolute;
        }

        // Make sure we see the starting location of the robot
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
        // Hide the starting location of the robot
        matchBinding.textRobot.setVisibility(View.INVISIBLE);

        matchBinding.textStatus.setText(R.string.match_select_start_location);
    }
}