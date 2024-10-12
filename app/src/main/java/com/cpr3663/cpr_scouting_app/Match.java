package com.cpr3663.cpr_scouting_app;

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
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cpr3663.cpr_scouting_app.data.Colors;
import com.cpr3663.cpr_scouting_app.databinding.MatchBinding;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Match extends AppCompatActivity {
    // =============================================================================================
    // Define constants
    // =============================================================================================
    protected static final int TIMER_AUTO_LENGTH = 15; // in seconds
    protected static final int TIMER_TELEOP_LENGTH = 135; // in seconds
    private static final int TIMER_UPDATE_RATE = 1_000; // in milliseconds
    private static final int TIMER_AUTO_TELEOP_DELAY = 3; // in seconds
    private static final int BUTTON_FLASH_INTERVAL = 2_000; // in milliseconds
    private static final int BUTTON_FLASH_BLINK_INTERVAL = 250; // in milliseconds
    private static final int BUTTON_COLOR_FLASH = Color.RED;
    private static final int BUTTON_COLOR_NORMAL = Color.TRANSPARENT;
    private static final int BUTTON_TEXT_COLOR_DISABLED = Color.LTGRAY;
    private static final String ORIENTATION_LANDSCAPE = "l";
    private static final String ORIENTATION_LANDSCAPE_REVERSE = "lr";
    private static int IMAGE_HEIGHT;
    private static int IMAGE_WIDTH;

    // =============================================================================================
    // Class:       AutoTimerTask
    // Description: Defines the TimerTask trigger to when the Auto phase of the game is over.
    // =============================================================================================
    public class AutoTimerTask extends TimerTask {
        @Override
        public void run() {
            if (matchPhase.equals(Constants.PHASE_AUTO)) {
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
            if (matchPhase.equals(Constants.PHASE_TELEOP)) {
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

            if (matchPhase.equals(Constants.PHASE_AUTO)) {
                elapsedSeconds = (int) (TIMER_AUTO_LENGTH - Math.round((System.currentTimeMillis() - startTime) / 1_000.0));
            } else {
                elapsedSeconds = (int) (TIMER_TELEOP_LENGTH + TIMER_AUTO_LENGTH - Math.round((System.currentTimeMillis() - startTime) / 1_000.0));
            }
            if (elapsedSeconds < 0) elapsedSeconds = 0;
            text_Time.setText(elapsedSeconds / 60 + ":" + String.format("%02d", elapsedSeconds % 60));
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
            switch_NotMoving.setBackgroundColor(BUTTON_COLOR_NORMAL);
            switch_Defense.setBackgroundColor(BUTTON_COLOR_NORMAL);
            switch_Defended.setBackgroundColor(BUTTON_COLOR_NORMAL);

            try {
                Thread.sleep(BUTTON_FLASH_BLINK_INTERVAL);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // If it's checked, wait a bit then make it BUTTON_COLOR_FLASH
            if (switch_NotMoving.isChecked()) switch_NotMoving.setBackgroundColor(BUTTON_COLOR_FLASH);
            if (switch_Defense.isChecked()) switch_Defense.setBackgroundColor(BUTTON_COLOR_FLASH);
            if (switch_Defended.isChecked()) switch_Defended.setBackgroundColor(BUTTON_COLOR_FLASH);
        }
    }

    // =============================================================================================
    // Global variables
    // =============================================================================================
    private MatchBinding matchBinding;
    public static long startTime;
    public static String matchPhase = Constants.PHASE_NONE;
    private static int eventPrevious = -1;
    private static OrientationEventListener OEL; // needed to detect the screen being flipped around
    private static String currentOrientation = ORIENTATION_LANDSCAPE;
    private static double currentTouchTime = 0;
    private static boolean is_start_of_seq = true;
    private static float current_X_Relative = 0;
    private static float current_Y_Relative = 0;
    private static float current_X_Absolute = 0;
    private static float current_Y_Absolute = 0;

    // Define the buttons on the page
    Button but_MatchControl;
    Button but_Back;
    Button but_Undo;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch switch_NotMoving;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch switch_Defense;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch switch_Defended;
    // Define a TextView to display the match time
    TextView text_Time;
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

        // Set up a listener for Orientation changes so we can flip the field properly (which means we
        // ignore the flip for the field image in a sense)
        // This listener will get triggered for every slight movement so we'll need to be careful on how
        // we call the rotation.  Keeping track of the current orientation should help!
        // KEY: actually calling setRotation works, but severely messes up the context menu.  SO, we'll
        // hack it by just loading a "flipped" image to display.
        currentOrientation = ORIENTATION_LANDSCAPE;
        OEL = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onOrientationChanged(int rotation_degrees) {
                // If the device is in the 0 to 180 degree range, make it Landscape
                if ((rotation_degrees >= 0) && (rotation_degrees < 180) && !currentOrientation.equals(ORIENTATION_LANDSCAPE)) {
                    matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field_image));
                    currentOrientation = ORIENTATION_LANDSCAPE;
                }
                // If the device is in the 180 to 359 degree range, make it Landscape
                // We can get passed a -1 if the device can't tell (it's lying flat) and we want to ignore that
                else if ((rotation_degrees >= 180) && !currentOrientation.equals(ORIENTATION_LANDSCAPE_REVERSE)) {
                    matchBinding.imageFieldView.setImageDrawable(getDrawable(R.drawable.field_image_flipped));
                    currentOrientation = ORIENTATION_LANDSCAPE_REVERSE;
                }
            }
        };

        // Enable orientation listening if we can!
        if (OEL.canDetectOrientation()) {
            OEL.enable();
        }

        // Map the text box variable to the actual text box
        text_Time = matchBinding.textTime;
        // Initialize the match timer textbox settings
        text_Time.setVisibility(View.INVISIBLE);
        text_Time.setBackgroundColor(Color.TRANSPARENT);
        text_Time.setTextSize(24F);

        // If this is a practice, put a message in the Status and set the image to Practice mode
        if (Globals.isPractice) {
            matchBinding.textStatus.setTextColor(Color.YELLOW);
            matchBinding.textStatus.setText(getString(R.string.status_practice));
            matchBinding.textPractice.setText(getString(R.string.practice));
            matchBinding.textPractice.setTextSize(210);
        } else {
            matchBinding.textStatus.setTextColor(Color.LTGRAY);
            matchBinding.textPractice.setText("");
        }

        matchBinding.textTeam.setText(Globals.CurrentTeamToScout + " - " + Globals.TeamList.get(Globals.CurrentTeamToScout));
        matchBinding.textTeam.setTextColor(Color.WHITE);

        // Map the button variable to the actual button
        but_MatchControl = matchBinding.butMatchControl;
        // Initialize the match Control Button settings
        but_MatchControl.setText(getString(R.string.button_start_match));
        but_MatchControl.setBackgroundColor(ContextCompat.getColor(this.getApplicationContext(), R.color.dark_green));
        but_MatchControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Checks the current phase and makes the button press act accordingly
                switch (matchPhase) {
                    case Constants.PHASE_NONE:
                        start_Match();
                        break;
                    case Constants.PHASE_AUTO:
                        // If we're going to teleop manually, log the start time offset
                        Globals.EventLogger.LogData(Constants.LOGKEY_START_TIME_OFFSET, String.valueOf(Math.round((TIMER_AUTO_LENGTH * 1000.0 - System.currentTimeMillis() + startTime) / 10.0) / 100.0));
                        start_Teleop();
                        break;
                    case Constants.PHASE_TELEOP:
                        if (startTime + (TIMER_AUTO_LENGTH + TIMER_TELEOP_LENGTH) * 1000 > System.currentTimeMillis())
                            new AlertDialog.Builder(view.getContext())
                            .setTitle(getString(R.string.alert_endMatch_title))
                            .setMessage(getString(R.string.alert_endMatch_message))

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(getString(R.string.alert_endMatch_positive), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    end_Match();
                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(getString(R.string.alert_cancel), null)
                            // TODO make the icon work
//                          .setIcon(getDrawable(android.R.attr.alertDialogIcon))
                            .show();
                        else end_Match();
                        break;
                }
            }
        });

        // Map the button variable to the actual button
        // If clicked, go back to the previous page
        but_Back = matchBinding.butBack;
        but_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If this is a practice, we need to clear out the Logger since we have an instance of Logger, but during
                // practice, we don't open any files.  If the next time we get here from Pre-Match, if it's NOT a practice,
                // we'll try to write out to the same "dummy" Logger and crash.  Resetting the Logger here ensures we do the
                // right instantiation in Pre-Match.
                if (Globals.isPractice) Globals.EventLogger = null;

                // Go to the previous page
                Intent GoToPreviousPage = new Intent(Match.this, PreMatch.class);
                startActivity(GoToPreviousPage);

                finish();
            }
        });

        // Map the button variable to the actual button
        but_Undo = matchBinding.butUndo;
        but_Undo.setVisibility(View.INVISIBLE);
        but_Undo.setEnabled(false);

        // If clicked, undo the last event selected
        but_Undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventPrevious = Globals.EventLogger.UndoLastEvent();
                matchBinding.textStatus.setText(Globals.EventList.getEventDescription(eventPrevious));

                // If there are no events left to undo, hide the button
                if (eventPrevious == -1) {
                    // Certain actions can't be set from a non-UI thread
                    // So we need to make a Runner that will execute on the UI thread to set this.
                    Match.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            but_Undo.setVisibility(View.INVISIBLE);
                            but_Undo.setEnabled(false);
                        }
                    });
                }
            }
        });

        // Define a field image
        ImageView image_Field = matchBinding.imageFieldView;

        // Map the Not Moving Switch to the actual switch
        switch_NotMoving = matchBinding.switchNotMoving;
        // Initialize the Not Moving Switch settings
        switch_NotMoving.setTextColor(BUTTON_TEXT_COLOR_DISABLED);
        switch_NotMoving.setBackgroundColor(BUTTON_COLOR_NORMAL);
        switch_NotMoving.setVisibility(View.INVISIBLE);
        // Do this so that you can't mess with the switch during the wrong phases
        switch_NotMoving.setEnabled(false);

        // This gets called if either the switch is clicked on, or the slide toggle is flipped (covers both)
        switch_NotMoving.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If the button is being turned ON make it RED otherwise LTGRAY
                if (isChecked) {
                    Globals.EventLogger.LogEvent(Constants.EVENT_ID_NOT_MOVING_START, 0,0,true);
                    switch_NotMoving.setBackgroundColor(BUTTON_COLOR_FLASH);
                } else {
                    Globals.EventLogger.LogEvent(Constants.EVENT_ID_NOT_MOVING_END, 0,0,false);
                    switch_NotMoving.setBackgroundColor(BUTTON_COLOR_NORMAL);
                }
            }
        });

        switch_NotMoving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Need this listener or else the onCheckedChanged won't fire either.
            }
        });

        // Map the Defense Switch to the actual switch
        switch_Defense = matchBinding.switchDefense;
        // Initialize the Defense Switch settings
        switch_Defense.setTextColor(BUTTON_TEXT_COLOR_DISABLED);
        switch_Defense.setBackgroundColor(BUTTON_COLOR_NORMAL);
        switch_Defense.setVisibility(View.INVISIBLE);
        // Do this so that you can't mess with the switch during the wrong phases
        switch_Defense.setEnabled(false);

        // This gets called if either the switch is clicked on, or the slide toggle is flipped (covers both)
        switch_Defense.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If the button is being turned ON make it RED otherwise LTGRAY
                if (isChecked) {
                    Globals.EventLogger.LogEvent(Constants.EVENT_ID_DEFENSE_START, 0,0,true);
                    switch_Defense.setBackgroundColor(BUTTON_COLOR_FLASH);
                } else {
                    Globals.EventLogger.LogEvent(Constants.EVENT_ID_DEFENSE_END, 0,0,false);
                    switch_Defense.setBackgroundColor(BUTTON_COLOR_NORMAL);
                }
            }
        });

        switch_Defense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Need this listener or else the onCheckedChanged won't fire either.
            }
        });

        // Map the Defended Switch to the actual switch
        switch_Defended = matchBinding.switchDefended;
        // Initialize the Defended Switch settings
        switch_Defended.setTextColor(BUTTON_TEXT_COLOR_DISABLED);
        switch_Defended.setBackgroundColor(BUTTON_COLOR_NORMAL);
        switch_Defended.setVisibility(View.INVISIBLE);
        // Do this so that you can't mess with the switch during the wrong phases
        switch_Defended.setEnabled(false);

        // This gets called if either the switch is clicked on, or the slide toggle is flipped (covers both)
        switch_Defended.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Globals.EventLogger.LogEvent(Constants.EVENT_ID_DEFENDED_START, 0,0,true);
                    switch_Defended.setBackgroundColor(BUTTON_COLOR_FLASH);
                } else {
                    Globals.EventLogger.LogEvent(Constants.EVENT_ID_DEFENDED_END, 0,0,false);
                    switch_Defended.setBackgroundColor(BUTTON_COLOR_NORMAL);
                }
            }
        });

        switch_Defended.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Need this listener or else the onCheckedChanged won't fire either.
            }
        });

        // Define a context menu
        RelativeLayout ContextMenu = matchBinding.ContextMenu;

        // This is required it will not run without it
        registerForContextMenu(image_Field);
        // So that it activates on a normal click instead of a long click
        ContextMenu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Save where we touched the field image regardless of its orientation
                current_X_Absolute = motionEvent.getX();
                current_Y_Absolute = motionEvent.getY();

                // Save where we touched the field image relative to the fields orientation
                if (currentOrientation.equals(ORIENTATION_LANDSCAPE)) {
                    current_X_Relative = motionEvent.getX();
                    current_Y_Relative = motionEvent.getY();
                } else {
                    current_X_Relative = IMAGE_WIDTH - motionEvent.getX();
                    current_Y_Relative = IMAGE_HEIGHT - motionEvent.getY();
                }

                image_Field.showContextMenu(current_X_Absolute, current_Y_Absolute);
                return false;
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // Save off the time that this was touched
        currentTouchTime = System.currentTimeMillis();

        // Check to make sure the game is going
        if (!matchPhase.equals(Constants.PHASE_NONE)) {
            // Get the events
            ArrayList<String> events;
            is_start_of_seq = false;

            if (eventPrevious == -1) {
                events = Globals.EventList.getEventsForPhase(matchPhase);
                is_start_of_seq = true;
            } else {
                events = Globals.EventList.getNextEvents(eventPrevious);
                if ((events == null) || events.isEmpty()) {
                    events = Globals.EventList.getEventsForPhase(matchPhase);
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
                if ((ss.toString().contains("Miss")) || (ss.toString().contains("Failed"))) {
                    Colors.ColorRow cr = Globals.ColorList.getColorRow(Globals.CurrentColorId - 1);
                    if (cr != null) {
                        ss.setSpan(new ForegroundColorSpan(cr.getColorMiss()), 0, ss.length(), 0);
                    }
                } else if ((ss.toString().contains("Score")) || (ss.toString().contains("Success"))) {
                    Colors.ColorRow cr = Globals.ColorList.getColorRow(Globals.CurrentColorId - 1);
                    if (cr != null) {
                        ss.setSpan(new ForegroundColorSpan(cr.getColorScore()), 0, ss.length(), 0);
                    }
                }

                item.setTitle(ss);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (!Globals.isPractice) matchBinding.textStatus.setText(getString(R.string.status_text_label) + " " + Objects.requireNonNull(item.getTitle()));
        eventPrevious = Globals.EventList.getEventId(item.getTitle().toString());
        Globals.EventLogger.LogEvent(eventPrevious, current_X_Relative, current_Y_Relative, is_start_of_seq, currentTouchTime);
        but_Undo.setVisibility(View.VISIBLE);
        but_Undo.setEnabled(true);
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
        Globals.EventLogger.LogData(Constants.LOGKEY_START_TIME, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")));

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
        but_Back.setClickable(false);
        but_Back.setVisibility(View.INVISIBLE);

        // Clear out the team to override (kept it in case they hit the Back button)
        Globals.CurrentTeamOverrideNum = 0;

        // Show the time
        text_Time.setVisibility(View.VISIBLE);

        // Calculate the image dimensions
        IMAGE_WIDTH = matchBinding.imageFieldView.getWidth();
        IMAGE_HEIGHT = matchBinding.imageFieldView.getHeight();

        // Set timer tasks
        match_Timer.schedule(auto_timertask, (TIMER_AUTO_LENGTH + TIMER_AUTO_TELEOP_DELAY) * 1_000);
        match_Timer.scheduleAtFixedRate(gametime_timertask, 0, TIMER_UPDATE_RATE);
        match_Timer.scheduleAtFixedRate(flashing_timertask, 0, BUTTON_FLASH_INTERVAL);

        // Set match Phase to be correct and Button text
        matchPhase = Constants.PHASE_AUTO;
        but_MatchControl.setText(getString(R.string.button_start_teleop));
        but_MatchControl.setBackgroundColor(getColor(R.color.dark_yellow));
        but_MatchControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.start_teleop, 0);

        // If we logged that we started with a note, then set the eventPrevious to it.
        if (Globals.EventLogger.LookupEvent(Constants.EVENT_ID_AUTO_STARTNOTE)) {
            eventPrevious = Constants.EVENT_ID_AUTO_STARTNOTE;
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
        startTime = System.currentTimeMillis() - TIMER_AUTO_LENGTH * 1_000;
        text_Time.setText(TIMER_TELEOP_LENGTH / 60 + ":" + String.format("%02d", TIMER_TELEOP_LENGTH % 60));

        match_Timer.schedule(teleop_timertask, TIMER_TELEOP_LENGTH * 1_000);

        // Set match Phase to be correct and Button text
        matchPhase = Constants.PHASE_TELEOP;

        // Certain actions can't be set from a non-UI thread (like within a TimerTask that runs on a
        // separate thread). So we need to make a Runner that will execute on the UI thread to set this.
        Match.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch_NotMoving.setEnabled(true);
                switch_NotMoving.setTextColor(Color.WHITE);
                switch_NotMoving.setVisibility(View.VISIBLE);
                switch_Defense.setEnabled(true);
                switch_Defense.setTextColor(Color.WHITE);
                switch_Defense.setVisibility(View.VISIBLE);
                switch_Defended.setEnabled(true);
                switch_Defended.setTextColor(Color.WHITE);
                switch_Defended.setVisibility(View.VISIBLE);

                but_MatchControl.setText(getString(R.string.button_end_match));
                but_MatchControl.setBackgroundColor(getColor(R.color.dark_red));
                but_MatchControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.stop_match, 0);
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
                but_MatchControl.setText(getString(R.string.button_match_next));
                but_MatchControl.setTextColor(Color.TRANSPARENT);
                but_MatchControl.setBackgroundColor(getColor(R.color.white));
                but_MatchControl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.next_button, 0);
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
        matchPhase = Constants.PHASE_NONE;

        // If either of the toggles are on turn them off
        if (switch_NotMoving.isChecked()) Globals.EventLogger.LogEvent(Constants.EVENT_ID_NOT_MOVING_END, 0, 0, false);
        if (switch_Defense.isChecked()) Globals.EventLogger.LogEvent(Constants.EVENT_ID_DEFENSE_END, 0, 0, false);
        if (switch_Defended.isChecked()) Globals.EventLogger.LogEvent(Constants.EVENT_ID_DEFENDED_END, 0, 0, false);
        switch_NotMoving.setChecked(false);
        switch_Defense.setChecked(false);
        switch_Defended.setChecked(false);

        // Go to the next page
        Intent GoToPostMatch = new Intent(Match.this, PostMatch.class);
        startActivity(GoToPostMatch);

        finish();
    }
}