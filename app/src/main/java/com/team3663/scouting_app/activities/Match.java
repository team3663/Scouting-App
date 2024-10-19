package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.ImageView;
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
    public static long startTime;
    private static int eventPrevious = -1;
    private static OrientationEventListener OEL; // needed to detect the screen being flipped around
    private static String currentOrientation = Constants.Match.ORIENTATION_LANDSCAPE;
    private static double currentTouchTime = 0;
    private static boolean is_start_of_seq = true;
    private static float current_X_Relative = 0;
    private static float current_Y_Relative = 0;
    private static float current_X_Absolute = 0;
    private static float current_Y_Absolute = 0;

    // Define a Timer and TimerTasks so you can schedule things
    Timer match_Timer;
    TimerTask auto_timertask;
    TimerTask teleop_timertask;
    TimerTask gametime_timertask;
    TimerTask flashing_timertask;

    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // Set up a listener for Orientation changes so we can flip the field properly (which means we
        // ignore the flip for the field image in a sense)
        // This listener will get triggered for every slight movement so we'll need to be careful on how
        // we call the rotation.  Keeping track of the current orientation should help!
        // KEY: actually calling setRotation works, but severely messes up the context menu.  SO, we'll
        // hack it by just loading a "flipped" image to display.
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
            }
        };

        // Enable orientation listening if we can!
        if (OEL.canDetectOrientation()) {
            OEL.enable();
        }

        // Initialize the match timer textbox settings
        matchBinding.textTime.setVisibility(View.INVISIBLE);
        matchBinding.textTime.setBackgroundColor(Color.TRANSPARENT);
        matchBinding.textTime.setTextSize(24F);

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

        matchBinding.textTeam.setText(Globals.CurrentTeamToScout + " - " + Globals.TeamList.get(Globals.CurrentTeamToScout));
        matchBinding.textTeam.setTextColor(Color.WHITE);

        // Initialize the match Control Button settings
        matchBinding.butMatchControl.setText(getString(R.string.button_start_match));
        matchBinding.butMatchControl.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.dark_green));
        matchBinding.butMatchControl.setOnClickListener(view -> {
            // Checks the current phase and makes the button press act accordingly
            switch (Globals.CurrentMatchPhase) {
                case Constants.Phases.NONE:
                    start_Match();
                    break;
                case Constants.Phases.AUTO:
                    // If we're going to teleop manually, log the start time offset
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_START_TIME_OFFSET, String.valueOf(Math.round((Constants.Match.TIMER_AUTO_LENGTH * 1000.0 - System.currentTimeMillis() + startTime) / 10.0) / 100.0));
                    start_Teleop();
                    break;
                case Constants.Phases.TELEOP:
                    if (startTime + (Constants.Match.TIMER_AUTO_LENGTH + Constants.Match.TIMER_TELEOP_LENGTH) * 1000 > System.currentTimeMillis())
                        new AlertDialog.Builder(view.getContext())
                        .setTitle(getString(R.string.match_alert_endMatch_title))
                        .setMessage(getString(R.string.match_alert_endMatch_message))

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(getString(R.string.match_alert_endMatch_positive), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                end_Match();
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(getString(R.string.match_alert_cancel), null)
                        // TODO make the icon work
//                          .setIcon(getDrawable(android.R.attr.alertDialogIcon))
                        .show();
                    else end_Match();
                    break;
            }
        });

        // Map the button variable to the actual button
        // If clicked, go back to the previous page
        matchBinding.butBack.setOnClickListener(view -> {
            // If this is a practice, we need to clear out the Logger since we have an instance of Logger, but during
            // practice, we don't open any files.  If the next time we get here from Pre-Match, if it's NOT a practice,
            // we'll try to write out to the same "dummy" Logger and crash.  Resetting the Logger here ensures we do the
            // right instantiation in Pre-Match.
            if (Globals.isPractice) Globals.EventLogger = null;

            // Go to the previous page
            Intent GoToPreviousPage = new Intent(Match.this, PreMatch.class);
            startActivity(GoToPreviousPage);

            finish();
        });

        matchBinding.butUndo.setVisibility(View.INVISIBLE);
        matchBinding.butUndo.setEnabled(false);

        // If clicked, undo the last event selected
        matchBinding.butUndo.setOnClickListener(view -> {
            eventPrevious = Globals.EventLogger.UndoLastEvent();

            // If there are no events left to undo, hide the button
            if ((eventPrevious == -1) || (eventPrevious == Constants.Events.ID_AUTO_STARTNOTE)) {
                // Certain actions can't be set from a non-UI thread
                // So we need to make a Runner that will execute on the UI thread to set this.
                Match.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        matchBinding.butUndo.setVisibility(View.INVISIBLE);
                        matchBinding.butUndo.setEnabled(false);
                        matchBinding.textStatus.setText("");
                    }
                });
            }
            else {
                SpannableString ss = new SpannableString(Globals.EventList.getEventDescription(eventPrevious));
                ss.setSpan(new AbsoluteSizeSpan(24), 0, ss.length(), 0);

                // If this menuItem has "Miss"/"Failed" or "Score"/"Success" in the text, see if we should use a special color
                if (Globals.ColorList.isColorValid(Globals.CurrentColorId - 1)) {
                    if ((ss.toString().contains("Miss")) || (ss.toString().contains("Failed")))
                        ss.setSpan(new ForegroundColorSpan(Globals.ColorList.getColorMiss(Globals.CurrentColorId - 1)), 0, ss.length(), 0);
                    else if ((ss.toString().contains("Score")) || (ss.toString().contains("Success")))
                        ss.setSpan(new ForegroundColorSpan(Globals.ColorList.getColorScore(Globals.CurrentColorId - 1)), 0, ss.length(), 0);
                }

                matchBinding.textStatus.setText(ss);
            }
        });

        // Define a field image
        ImageView image_Field = matchBinding.imageFieldView;

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
                Globals.EventLogger.LogEvent(Constants.Events.ID_NOT_MOVING_START, 0,0,true);
                matchBinding.switchNotMoving.setBackgroundColor(Constants.Match.BUTTON_COLOR_FLASH);
            } else {
                Globals.EventLogger.LogEvent(Constants.Events.ID_NOT_MOVING_END, 0,0,false);
                matchBinding.switchNotMoving.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);
            }
        });

        matchBinding.switchNotMoving.setOnClickListener(view -> {
            // Need this listener or else the onCheckedChanged won't fire either.
        });

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
                Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENSE_START, 0,0,true);
                matchBinding.switchDefense.setBackgroundColor(Constants.Match.BUTTON_COLOR_FLASH);
            } else {
                Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENSE_END, 0,0,false);
                matchBinding.switchDefense.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);
            }
        });

        matchBinding.switchDefense.setOnClickListener(view -> {
            // Need this listener or else the onCheckedChanged won't fire either.
        });

        // Initialize the Defended Switch settings
        matchBinding.switchDefended.setTextColor(Constants.Match.BUTTON_TEXT_COLOR_DISABLED);
        matchBinding.switchDefended.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);
        matchBinding.switchDefended.setVisibility(View.INVISIBLE);
        // Do this so that you can't mess with the switch during the wrong phases
        matchBinding.switchDefended.setEnabled(false);

        // This gets called if either the switch is clicked on, or the slide toggle is flipped (covers both)
        matchBinding.switchDefended.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENDED_START, 0,0,true);
                matchBinding.switchDefended.setBackgroundColor(Constants.Match.BUTTON_COLOR_FLASH);
            } else {
                Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENDED_END, 0,0,false);
                matchBinding.switchDefended.setBackgroundColor(Constants.Match.BUTTON_COLOR_NORMAL);
            }
        });

        matchBinding.switchDefended.setOnClickListener(view -> {
            // Need this listener or else the onCheckedChanged won't fire either.
        });

        // Define a context menu
        RelativeLayout ContextMenu = matchBinding.ContextMenu;

        // This is required it will not run without it
        registerForContextMenu(image_Field);
        // So that it activates on a normal click instead of a long click
        ContextMenu.setOnTouchListener((view, motionEvent) -> {
            // Save where we touched the field image regardless of its orientation
            current_X_Absolute = motionEvent.getX();
            current_Y_Absolute = motionEvent.getY();

            // Save where we touched the field image relative to the fields orientation
            if (currentOrientation.equals(Constants.Match.ORIENTATION_LANDSCAPE)) {
                current_X_Relative = motionEvent.getX();
                current_Y_Relative = motionEvent.getY();
            } else {
                current_X_Relative = Constants.Match.IMAGE_WIDTH - motionEvent.getX();
                current_Y_Relative = Constants.Match.IMAGE_HEIGHT - motionEvent.getY();
            }

            image_Field.showContextMenu(current_X_Absolute, current_Y_Absolute);
            return false;
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // Save off the time that this was touched
        currentTouchTime = System.currentTimeMillis();

        // Check to make sure the game is going
        if (!Globals.CurrentMatchPhase.equals(Constants.Phases.NONE)) {
            // Get the events
            ArrayList<String> events;
            is_start_of_seq = false;

            if (eventPrevious == -1) {
                events = Globals.EventList.getEventsForPhase(Globals.CurrentMatchPhase);
                is_start_of_seq = true;
            } else {
                events = Globals.EventList.getNextEvents(eventPrevious);
                if ((events == null) || events.isEmpty()) {
                    events = Globals.EventList.getEventsForPhase(Globals.CurrentMatchPhase);
                    is_start_of_seq = true;
                }
            }

            // Add all the events
            for (String event : events) {
                menu.add(event);
            }

            // Go through all of the items and see if we want to customize the text using a SpannableString
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                SpannableString ss = new SpannableString(item.getTitle());
                ss.setSpan(new AbsoluteSizeSpan(24), 0, ss.length(), 0);

                // If this menuItem has "Miss"/"Failed" or "Score"/"Success" in the text, see if we should use a special color
                if (Globals.ColorList.isColorValid(Globals.CurrentColorId - 1)) {
                    if ((ss.toString().contains("Miss")) || (ss.toString().contains("Failed")))
                        ss.setSpan(new ForegroundColorSpan(Globals.ColorList.getColorMiss(Globals.CurrentColorId - 1)), 0, ss.length(), 0);
                    else if ((ss.toString().contains("Score")) || (ss.toString().contains("Success")))
                        ss.setSpan(new ForegroundColorSpan(Globals.ColorList.getColorScore(Globals.CurrentColorId - 1)), 0, ss.length(), 0);
                }

                item.setTitle(ss);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (!Globals.isPractice) matchBinding.textStatus.setText(Objects.requireNonNull(item.getTitle()));
        eventPrevious = Globals.EventList.getEventId(Objects.requireNonNull(item.getTitle()).toString());
        Globals.EventLogger.LogEvent(eventPrevious, current_X_Relative, current_Y_Relative, is_start_of_seq, currentTouchTime);
        matchBinding.butUndo.setVisibility(View.VISIBLE);
        matchBinding.butUndo.setEnabled(true);
        return true;
    }

    // =============================================================================================
    // Function:    start_match
    // Description: Starts the match Timer and all the scheduled events and repeat schedule events
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint({"DiscouragedApi", "SetTextI18n"})
    public void start_Match() {
        // Record the current/start time of the match to calculate elapsed time
        startTime = System.currentTimeMillis();

        // Log the starting time
        Globals.EventLogger.LogData(Constants.Logger.LOGKEY_START_TIME, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")));

        // Disable orientation listening if we can!  Once we start the match don't allow rotation anymore
        if (OEL.canDetectOrientation()) {
            OEL.disable();
        }

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
        Globals.CurrentTeamOverrideNum = 0;

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

        // If we logged that we started with a note, then set the eventPrevious to it.
        if (Globals.EventLogger.LookupEvent(Constants.Events.ID_AUTO_STARTNOTE)) {
            eventPrevious = Constants.Events.ID_AUTO_STARTNOTE;
        }
    }

    // =============================================================================================
    // Function:    start_Teleop
    // Description: Starts Teleop phase by setting timer to 15 sec and adjust start time to
    //              compensate and update button text
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void start_Teleop() {
        // Set the start Time so that the Display Time will be correct
        startTime = System.currentTimeMillis() - Constants.Match.TIMER_AUTO_LENGTH * 1_000;
        matchBinding.textTime.setText(Constants.Match.TIMER_TELEOP_LENGTH / 60 + ":" + String.format("%02d", Constants.Match.TIMER_TELEOP_LENGTH % 60));

        match_Timer.schedule(teleop_timertask, Constants.Match.TIMER_TELEOP_LENGTH * 1_000);

        // Set match Phase to be correct and Button text
        Globals.CurrentMatchPhase = Constants.Phases.TELEOP;

        // Certain actions can't be set from a non-UI thread (like within a TimerTask that runs on a
        // separate thread). So we need to make a Runner that will execute on the UI thread to set this.
        Match.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }

    // =============================================================================================
    // Function:    end_Teleop
    // Description: Ends teleop, but not the match so that you can still finish up
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint("SetTextI18n")
    public void end_Teleop() {
        // Certain actions can't be set from a non-UI thread (like within a TimerTask that runs on a
        // separate thread). So we need to make a Runner that will execute on the UI thread to set this.
        Match.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                matchBinding.butMatchControl.setText(getString(R.string.button_match_next));
                matchBinding.butMatchControl.setTextColor(Color.TRANSPARENT);
                matchBinding.butMatchControl.setBackgroundColor(getColor(R.color.white));
                matchBinding.butMatchControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.next_button, 0);
            }
        });
    }

    // =============================================================================================
    // Function:    end_match
    // Description: Ends the match and all of the timers
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint("SetTextI18n")
    public void end_Match() {
        // Get rid of the Scheduled events that are over/have ended
        // Need to set match_Timer and TimerTasks to null so we can create "new" ones at the start of the next match
        if (match_Timer != null) {
            match_Timer.cancel();
            match_Timer.purge();
        }
        match_Timer = null;
        auto_timertask = null;
        teleop_timertask = null;
        gametime_timertask = null;
        flashing_timertask = null;

        // Reset match phase so that the next time we hit Start Match we do the right thing
        Globals.CurrentMatchPhase = Constants.Phases.NONE;

        // If either of the toggles are on turn them off
        if (matchBinding.switchNotMoving.isChecked()) Globals.EventLogger.LogEvent(Constants.Events.ID_NOT_MOVING_END, 0, 0, false);
        if (matchBinding.switchDefense.isChecked()) Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENSE_END, 0, 0, false);
        if (matchBinding.switchDefended.isChecked()) Globals.EventLogger.LogEvent(Constants.Events.ID_DEFENDED_END, 0, 0, false);
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
                start_Teleop();
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
                end_Teleop();
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
                elapsedSeconds = (int) (Constants.Match.TIMER_AUTO_LENGTH - Math.round((System.currentTimeMillis() - startTime) / 1_000.0));
            } else {
                elapsedSeconds = (int) (Constants.Match.TIMER_TELEOP_LENGTH + Constants.Match.TIMER_AUTO_LENGTH - Math.round((System.currentTimeMillis() - startTime) / 1_000.0));
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

}