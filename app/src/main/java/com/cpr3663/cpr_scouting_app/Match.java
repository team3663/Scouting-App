package com.cpr3663.cpr_scouting_app;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.text.Layout;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cpr3663.cpr_scouting_app.databinding.MatchBinding;
import com.cpr3663.cpr_scouting_app.databinding.MatchBinding;

import java.util.Timer;
import java.util.TimerTask;


public class Match extends AppCompatActivity {
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
    public static final String PHASE_AUTO = "Auto";
    public static final String PHASE_TELEOP = "Teleop";
    public static final String PHASE_NONE = "";

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
    private MatchBinding matchBinding;
    public static long startTime;
    private static String matchPhase = PHASE_NONE;
    private static String eventPrevious;
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
        // Capture screen size. Need to use WindowManager to populate a Point that holds the screen size.
        Display myscreen = getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        myscreen.getSize(screen_size);

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

        // Map the text box variable to the actual text box
        text_Time = matchBinding.textTime;
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
        but_MatchControl = matchBinding.butMatchControl;
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
                if (matchPhase == PHASE_NONE) {
                    start_Match();
                } else if (matchPhase.equals(PHASE_AUTO)) {
                    start_Teleop();
                } else if (matchPhase.equals(PHASE_TELEOP)) {
                    end_match();
                }
            }
        });

        // Define a field image
        ImageView image_Field = matchBinding.imageFieldView;
        // Initialize the fields settings
        int image_Field_height = screen_size.x * 1297 / 2560;
        image_Field.setX(0F);
        image_Field.setY(screen_size.y - image_Field_height);
        ViewGroup.LayoutParams image_Field_LP = new ViewGroup.LayoutParams(screen_size.x, image_Field_height);
        image_Field.setLayoutParams(image_Field_LP);

        // Listens for a click/touch on the screen
        image_Field.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Check the motion type and the phase and if its correct then get the X and Y
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && matchPhase != PHASE_NONE) {
                    double x = motionEvent.getX();
                    double y = motionEvent.getY();
                    matchBinding.textClickXY.setText(x + "," + y);
                    // Get current time, elapsed time, or tell the logger that the initial click happened now, so it doesn't log the second click's time instead
                    // Also make a Popup Context Menu to ask what the event was
                }
                // This decides if it consumes the click and stops it
                return false;
            }
        });

        // Map the Defense Switch to the actual switch
        switch_Defense = matchBinding.switchDefense;
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
        switch_Defended = matchBinding.switchDefended;
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

        // Define a context menu
        RelativeLayout ContextMenu = matchBinding.ContextMenu;
        // Initialize the Context Menu's settings
        ContextMenu.setX(0F);
        ContextMenu.setY(screen_size.y - image_Field_height);
        ViewGroup.LayoutParams ContextMenu_LP = new ViewGroup.LayoutParams(screen_size.x, image_Field_height);
        ContextMenu.setLayoutParams(ContextMenu_LP);
//        ContextMenu.setBackgroundColor(getResources().getColor(R.color.red_highlight)); // For checking it's location
        ContextMenu.setBackgroundColor(Color.TRANSPARENT);
        // This is required it will not run without it
        registerForContextMenu(image_Field);
        // So that it activates on a normal click instead of a long click
        ContextMenu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                image_Field.showContextMenu(motionEvent.getX(), motionEvent.getY());
                return false;
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // Check to make sure the game is going
        if (matchPhase != PHASE_NONE) {
            // Create a fake text file of events and their order
//            String[] events = {"Auto Pickup", "Auto Speaker Score", "Auto Speaker Miss", "Auto Amp Score", "Auto Amp Miss", "Auto Drop", "Cancel"};
            // Get the events (Doesn't work yet because it isnt fully set up yet)
            String[][] events = {{""}};
            if (eventPrevious != null) {
                events = AppLaunch.EventList.getNextEvents(AppLaunch.EventList.getEventId(eventPrevious));
            } else {
                events = AppLaunch.EventList.getEventsForPhase(matchPhase);
            }
            // Add all the events
            for (String[] event : events) {
                menu.add(event[1]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle() != "Cancel") {
            matchBinding.textClickXY.setText(item.getTitle());
        }
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
        text_Time.setText(getResources().getString(R.string.timer_label) + "0:" + String.format("%02d", TIMER_AUTO_LENGTH));

        // Set timer tasks
        match_Timer.schedule(teleop_timertask, TIMER_TELEOP_LENGTH * 1_000);

        // Set match Phase to be correct and Button text
        matchPhase = PHASE_TELEOP;
        but_MatchControl.setText(getResources().getString(R.string.button_end_match));
        but_MatchControl.setBackgroundColor(getResources().getColor(R.color.dark_red));

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
        Intent GoToNextPage = new Intent(Match.this, PreMatch.class);
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
