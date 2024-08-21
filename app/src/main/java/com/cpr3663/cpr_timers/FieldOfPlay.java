package com.cpr3663.cpr_timers;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Timer;
import java.util.TimerTask;

import com.cpr3663.cpr_timers.databinding.FieldOfPlayBinding;


public class FieldOfPlay extends AppCompatActivity {
    // =============================================================================================
    // Define constants
    // =============================================================================================
    private static final int TIMER_AUTO_LENGTH = 15; // in seconds
    private static final int TIMER_TELEOP_LENGTH = 135; // in seconds
    private static final int TIMER_UPDATE_RATE = 1_000; // in milliseconds
    private static final String TIMER_DEFAULT_NUM = "-:--"; // What the timer displays when there isn't a match going
    private static final int BUTTON_FLASH_INTERVAL = 1_000; // in milliseconds
    private static final int BUTTON_COLOR_FLASH = Color.RED;
    private static final int BUTTON_COLOR_NORMAL = Color.LTGRAY;
    private static final int BUTTON_TEXT_COLOR_DISABLED = Color.GRAY;
    private static final String PHASE_AUTO = "Auto";
    private static final String PHASE_TELEOP = "Teleop";
    private static final String PHASE_NONE = "";

    // =============================================================================================
    // Class:       AutoTimerTask
    // Description: Defines the TimerTask trigger to when the Auto phase of the game is over.
    // =============================================================================================
    public class AutoTimerTask extends TimerTask {
        @Override
        public void run() {
            if (matchPhase.equals(PHASE_AUTO)) {
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
            if (matchPhase.equals(PHASE_TELEOP)) {
                end_match();
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
            int elapsedSeconds = (int) Math.round((System.currentTimeMillis() - startTime) / 1_000.0);
            text_Time.setText("Time: " + elapsedSeconds / 60 + ":" + String.format("%02d", elapsedSeconds % 60));
        }
    }

    // =============================================================================================
    // Class:       FlashingTimerTask
    // Description: Defines the TimerTask trigger to when the Teleop phase of the game is over.
    // =============================================================================================
    public class FlashingTimerTask extends TimerTask {
        @Override
        public void run() {
            // Flashes both "switch_Defense" and "toggle_Defended"
            flash_button(switch_Defense);
            flash_button(switch_Defended);
        }
    }

    // =============================================================================================
    // Global variables
    // =============================================================================================
    private FieldOfPlayBinding fopBinding;
    public static long startTime;
    private static String matchPhase = PHASE_NONE;
    // Define a button that starts the match, skips to Teleop, and ends the match early
    Button but_MatchControl;
    // Define a TextView to display the match time
    TextView text_Time;
    // Define a Timer and TimerTasks so you can schedule things
    Timer match_Timer;
    TimerTask auto_timertask;
    TimerTask teleop_timertask;
    TimerTask gametime_timertask;
    TimerTask flashing_timertask;
    // Define the toggle switches
    Switch switch_Defense;
    Switch switch_Defended;

    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        fopBinding = FieldOfPlayBinding.inflate(getLayoutInflater());
        View page_root_view = fopBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(fopBinding.fieldOfPlay, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Map the text box variable to the actual text box
        text_Time = fopBinding.textTime;
        // Initialize the match timer textbox settings
        text_Time.setText("Time: " + TIMER_DEFAULT_NUM);
        text_Time.setTextSize(20F);
        text_Time.setTextColor(Color.BLACK);
        text_Time.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        text_Time.setX(2200F);
        text_Time.setY(0F);
        ViewGroup.LayoutParams text_Time_LP = new ViewGroup.LayoutParams(300, 100);
        text_Time.setLayoutParams(text_Time_LP);
        text_Time.setBackgroundColor(Color.TRANSPARENT);

        // Map the button variable to the actual button
        but_MatchControl = fopBinding.butMatchControl;
        // Initialize the match Control Button settings
        but_MatchControl.setText(getResources().getString(R.string.button_start_match));
        but_MatchControl.setTextSize(18F);
        but_MatchControl.setTextColor(Color.WHITE);
        but_MatchControl.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        but_MatchControl.setX(16F);
        but_MatchControl.setY(16F);
        ViewGroup.LayoutParams but_Update_LP = new ViewGroup.LayoutParams(300, 100);
        but_MatchControl.setLayoutParams(but_Update_LP);
        but_MatchControl.setBackgroundColor(getResources().getColor(R.color.dark_green));

        but_MatchControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Checks the current phase and makes the button press act accordingly
                if (matchPhase.isEmpty()) {
                    start_Match();
                } else if (matchPhase.equals(PHASE_AUTO)) {
                    start_Teleop();
                } else if (matchPhase.equals(PHASE_TELEOP)) {
                    end_match();
                }
            }
        });

        // TEST CODE FOR DETECTING A TOUCH EVENT ON THE BUTTON
        ConstraintLayout main = fopBinding.fieldOfPlay;
        main.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Check the motion type and if its correct then get the X and Y
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                }
                // Return false to not consume the click and have it also click the button
                return false;
            }
        });

        // Map the Defense Switch to the actual switch
        switch_Defense = fopBinding.switchDefense;
        // Initialize the Defense Switch settings
        switch_Defense.setText(getResources().getString(R.string.button_play_defense));
        switch_Defense.setTextSize(20F);
        switch_Defense.setTextColor(BUTTON_TEXT_COLOR_DISABLED);
        switch_Defense.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        switch_Defense.setX(1500F);
        switch_Defense.setY(16F);
        ViewGroup.LayoutParams switch_Defense_LP = new ViewGroup.LayoutParams(360, 100);
        switch_Defense.setLayoutParams(switch_Defense_LP);
        switch_Defense.setBackgroundColor(BUTTON_COLOR_NORMAL);

        switch_Defense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If the button is being turned ON make it RED otherwise LTGRAY
                if (switch_Defense.isChecked()) {
                    // Log EVENT
                    // <code goes here>
                    switch_Defense.setBackgroundColor(BUTTON_COLOR_FLASH);
                } else {
                    // Log EVENT
                    // <code goes here>
                    switch_Defense.setBackgroundColor(BUTTON_COLOR_NORMAL);
                }
            }
        });
        // Do this so that you can't mess with the switch during the wrong phases
        switch_Defense.setClickable(false);

        // Map the Defended Switch to the actual switch
        switch_Defended = fopBinding.switchDefended;
        // Initialize the Defended Switch settings
        switch_Defended.setText(getResources().getString(R.string.button_was_defended));
        switch_Defended.setTextSize(20F);
        switch_Defended.setTextColor(BUTTON_TEXT_COLOR_DISABLED);
        switch_Defended.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        switch_Defended.setX(1000F);
        switch_Defended.setY(16F);
        ViewGroup.LayoutParams switch_Defended_LP = new ViewGroup.LayoutParams(360, 100);
        switch_Defended.setLayoutParams(switch_Defended_LP);
        switch_Defended.setBackgroundColor(BUTTON_COLOR_NORMAL);

        switch_Defended.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If the button is being turned ON make it RED otherwise LTGRAY
                if (switch_Defended.isChecked()) {
                    // Log EVENT
                    // <code goes here>
                    switch_Defended.setBackgroundColor(BUTTON_COLOR_FLASH);
                } else {
                    // Log EVENT
                    // <code goes here>
                    switch_Defended.setBackgroundColor(BUTTON_COLOR_NORMAL);
                }
            }
        });
        // Do this so that you can't mess with the switch during the wrong phases
        switch_Defended.setClickable(false);
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

        // Create the Timers and timer tasks
        match_Timer = new Timer();
        auto_timertask = new AutoTimerTask();
        teleop_timertask = new TeleopTimerTask();
        gametime_timertask = new GameTimeTimerTask();
        flashing_timertask = new FlashingTimerTask();

        // Set timer tasks
        match_Timer.schedule(auto_timertask, TIMER_AUTO_LENGTH * 1_000);
        match_Timer.scheduleAtFixedRate(gametime_timertask, 0, TIMER_UPDATE_RATE);
        match_Timer.scheduleAtFixedRate(flashing_timertask, 0, BUTTON_FLASH_INTERVAL);

        // Default the toggles
        switch_Defense.setChecked(false);
        switch_Defended.setChecked(false);

        // Set match Phase to be correct and Button text
        matchPhase = PHASE_AUTO;
        but_MatchControl.setText(getResources().getString(R.string.button_start_teleop));
        but_MatchControl.setBackgroundColor(getResources().getColor(R.color.dark_yellow));
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
        text_Time.setText("Time: 0:" + String.format("%02d", TIMER_AUTO_LENGTH));

        // Set timer tasks
        match_Timer.schedule(teleop_timertask, TIMER_TELEOP_LENGTH * 1_000);

        // Set match Phase to be correct and Button text
        matchPhase = PHASE_TELEOP;
        but_MatchControl.setText(getResources().getString(R.string.button_end_match));
        but_MatchControl.setBackgroundColor(getResources().getColor((R.color.dark_red)));

        // Enable the Switches
        switch_Defense.setClickable(true);
        switch_Defense.setTextColor(Color.BLACK);
        switch_Defended.setClickable(true);
        switch_Defended.setTextColor(Color.BLACK);
    }

    // =============================================================================================
    // Function:    end_match
    // Description: Ends the match and all of the timers
    // Output:      void
    // Parameters:  N/A
    // =============================================================================================
    @SuppressLint("SetTextI18n")
    public void end_match() {
        // Get rid of the Scheduled events that are over/have ended
        // Need to set match_Timer and TimerTasks to null so we can create "new" ones at the start of the next match
        match_Timer.cancel();
        match_Timer.purge();
        match_Timer = null;
        auto_timertask = null;
        teleop_timertask = null;
        gametime_timertask = null;
        flashing_timertask = null;

        // Set the match Phase and button text
        matchPhase = PHASE_NONE;
        but_MatchControl.setText(getResources().getString(R.string.button_start_match));
        but_MatchControl.setBackgroundColor(getResources().getColor(R.color.dark_green));
        text_Time.setText("Time: " + TIMER_DEFAULT_NUM);

        // Disable the Switches and make sure they are off
        switch_Defense.setClickable(false);
        switch_Defense.setTextColor(BUTTON_TEXT_COLOR_DISABLED);
        switch_Defended.setClickable(false);
        switch_Defended.setTextColor(BUTTON_TEXT_COLOR_DISABLED);

        switch_Defense.setBackgroundColor(BUTTON_COLOR_NORMAL);
        switch_Defended.setBackgroundColor(BUTTON_COLOR_NORMAL);

        // Go to the next page
        Intent GoToNextPage = new Intent(FieldOfPlay.this, StartPage.class);
        startActivity(GoToNextPage);
    }

    // =============================================================================================
    // Function:    flash_button
    // Description: Flash the background color of the button.  Since all toggle-able buttons extend
    //              from CompoundButton, we can use that as the param type.
    // Output:      void
    // Parameters:  button - specific the button you want to flash.
    // =============================================================================================
    public void flash_button(CompoundButton button) {
        // If the button is ON then toggle the background color between COLOR_FLASH and COLOR_NORMAL
        if (button.isChecked()) {
            if (System.currentTimeMillis() / BUTTON_FLASH_INTERVAL % 2 == 0) {
                button.setBackgroundColor(BUTTON_COLOR_NORMAL);
            } else {
                button.setBackgroundColor(BUTTON_COLOR_FLASH);
            }
        }
    }
}
