package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cpr3663.cpr_scouting_app.data.Matches;
import com.cpr3663.cpr_scouting_app.databinding.PreMatchBinding;

import java.io.IOException;

public class PreMatch extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private PreMatchBinding preMatchBinding;
    // To store the inputted name
    protected static String ScouterName;
    protected static CheckBox checkbox_StartNote; // This needs to be global so that Match.java can access it

    // Doesn't appear to be needed on Tablet but helps on Virtual Devices.
    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility", "ResourceAsColor"})
    @Override
    protected void onResume() {
        super.onResume();

        // Hide the status and action bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.hide();
    }

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        preMatchBinding = PreMatchBinding.inflate(getLayoutInflater());
        setContentView(preMatchBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(preMatchBinding.preMatch, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Now that we are starting to scout data, set the Global values
        SharedPreferences sp;
        sp = this.getSharedPreferences(getString(R.string.preference_setting_file_key), Context.MODE_PRIVATE);
        Globals.CurrentScoutingTeam = sp.getInt(Settings.SP_SCOUTING_TEAM, 0);
        Globals.CurrentCompetitionId = sp.getInt(Settings.SP_COMPETITION_ID, 0);
        Globals.CurrentDeviceId = sp.getInt(Settings.SP_DEVICE_ID, 0);

        // Create components
        EditText edit_Match = preMatchBinding.editMatch;
        EditText edit_Team = preMatchBinding.editTeamToScout;
        Spinner spinner_StartPos = preMatchBinding.spinnerStartingPosition;
        TextView text_Match = preMatchBinding.textMatch;
        TextView text_TeamName = preMatchBinding.textTeamToScoutName;

        // adds the items from the starting positions array to the list
        ArrayAdapter<String> adp_StartPos = new ArrayAdapter<String>(this,
                R.layout.cpr_spinner, Globals.StartPositionList.getDescriptionList());
        adp_StartPos.setDropDownViewResource(R.layout.cpr_spinner_item);
        spinner_StartPos.setAdapter(adp_StartPos);

        Button but_AddOverrideTeamNum = preMatchBinding.butAddOverrideTeamNum;
        CheckBox checkbox_DidPlay = preMatchBinding.checkboxDidPlay;
        checkbox_StartNote = preMatchBinding.checkboxStartNote;
        CheckBox checkbox_Override = preMatchBinding.checkboxOverride;
        CheckBox checkbox_ReSubmit = preMatchBinding.checkboxResubmit;
        EditText edit_OverrideTeamNum = preMatchBinding.editOverrideTeamNum;
        EditText edit_Name = preMatchBinding.editScouterName;
        TextView text_Override = preMatchBinding.textOverride;

        // Since we are putting the checkbox on the RIGHT side of the text, the checkbox doesn't honor padding.
        // So we need to use 7 spaces, but you can't when using a string resource (it ignores the trailing spaces)
        // So add it in now.
        checkbox_DidPlay.setText(checkbox_DidPlay.getText() + Globals.CheckBoxTextPadding);
        checkbox_StartNote.setText(checkbox_StartNote.getText() + Globals.CheckBoxTextPadding);
        checkbox_ReSubmit.setText(checkbox_ReSubmit.getText() + Globals.CheckBoxTextPadding);

        // Create a text box to input the scouters name
        edit_Name.setText(ScouterName);

        if (Globals.CurrentMatchNumber > 0) {
            // MUST CONVERT TO STRING or it crashes with out warning
            edit_Match.setText(String.valueOf(Globals.CurrentMatchNumber));
            Matches.MatchRow Match = Globals.MatchList.getMatchInfoRow(Globals.CurrentMatchNumber);
            if (Match != null) {
                int[] Teams = Match.getListOfTeams();
                // TODO Set "Teams" to the options in the single select dropdown
            }
        } else edit_Match.setText("");

        // Default checkboxes
        checkbox_DidPlay.setChecked(true);
        checkbox_StartNote.setChecked(true);
        checkbox_ReSubmit.setChecked(false);

        // Hide override components initially
        text_Override.setVisibility(View.INVISIBLE);
        edit_OverrideTeamNum.setVisibility(View.INVISIBLE);
        but_AddOverrideTeamNum.setVisibility(View.INVISIBLE);

        checkbox_Override.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int state = View.VISIBLE;
                if (!checkbox_Override.isChecked()) state = View.INVISIBLE;
                text_Override.setVisibility(state);
                edit_OverrideTeamNum.setVisibility(state);
                but_AddOverrideTeamNum.setVisibility(state);
            }
        });

        but_AddOverrideTeamNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String teamNum = String.valueOf(preMatchBinding.editOverrideTeamNum.getText());
                if (!teamNum.isEmpty()) {
                    // TODO make it add teamNum to the options of single select dropdown after converting to int and
                    //      then have it auto select that one
                }
                checkbox_Override.setChecked(false);
                text_Override.setVisibility(View.INVISIBLE);
                edit_OverrideTeamNum.setVisibility(View.INVISIBLE);
                but_AddOverrideTeamNum.setVisibility(View.INVISIBLE);
            }
        });

        // Create a button for when you are done inputting info
        Button but_Next = preMatchBinding.butNext;
        but_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If we should re-submit data, go to the submit page immediately
                if (checkbox_ReSubmit.isChecked()) {
                    Intent GoToSubmitData = new Intent(PreMatch.this, SubmitData.class);
                    startActivity(GoToSubmitData);
                } else {
                    // Check we have all the fields entered that are needed.  Otherwise, pop a TOAST message instead
                    if (String.valueOf(edit_Match.getText()).isEmpty() || String.valueOf(edit_Team.getText()).isEmpty() || String.valueOf(edit_Name.getText()).isEmpty()) {
                        Toast.makeText(PreMatch.this, R.string.missing_data, Toast.LENGTH_SHORT).show();
                    } else {
                        // Set up the Logger - if it fails, we better stop now, or we won't capture any data!
                        try {
                            Globals.EventLogger = new Logger(getApplicationContext());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        // Save off the current match number (Logger needs this)
                        Globals.CurrentMatchNumber = Integer.parseInt(preMatchBinding.editMatch.getText().toString());

                        // Log all of the data from this page
                        Globals.EventLogger.LogData(Constants.LOGKEY_TEAM_TO_SCOUT, preMatchBinding.editTeamToScout.getText().toString());
                        Globals.EventLogger.LogData(Constants.LOGKEY_SCOUTER, preMatchBinding.editScouterName.getText().toString().toUpperCase());
                        Globals.EventLogger.LogData(Constants.LOGKEY_DID_PLAY, String.valueOf(preMatchBinding.checkboxDidPlay.isChecked()));
                        Globals.EventLogger.LogData(Constants.LOGKEY_TEAM_SCOUTING, String.valueOf(Globals.CurrentScoutingTeam));
                        //                    Globals.EventLogger.LogData(Constants.LOGKEY_START_POSITION, preMatchBinding.spinnerStartingPosition.getSelectedItem().toString());

                        // Save off some fields for next time or later usage
                        ScouterName = String.valueOf(edit_Name.getText());

                        // If they didn't play skip everything else
                        if (preMatchBinding.checkboxDidPlay.isChecked()) {
                            Intent GoToMatch = new Intent(PreMatch.this, Match.class);
                            startActivity(GoToMatch);
                        } else {
                            // Since we're jumping to the Submit page, we need to close the Logger first.
                            Globals.EventLogger.close();
                            Globals.EventLogger = null;

                            // Increases the team number so that it auto fills for the next match correctly
                            //  and do it after the logger is closed so that this can't mess the logger up
                            Globals.CurrentMatchNumber++;

                            Intent GoToSubmitData = new Intent(PreMatch.this, SubmitData.class);
                            startActivity(GoToSubmitData);
                        }
                    }
                }
            }
        });

        edit_Match.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (!focus) {
                    String MatchNumStr = String.valueOf(edit_Match.getText());
                    if (!MatchNumStr.isEmpty()) {
                        int MatchNum = Integer.parseInt(MatchNumStr);
                        Matches.MatchRow Match = Globals.MatchList.getMatchInfoRow(MatchNum);
                        if (Match != null) {
                            // MUST CONVERT TO STRING or it crashes with out warning
                            int[] Teams = Match.getListOfTeams();
                            // TODO Set "Teams" to the options in the single select dropdown
                        }
                    }
                }
            }
        });

        edit_Team.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (!focus) {
                    String TeamToScoutStr = String.valueOf(edit_Team.getText());
                    if (!TeamToScoutStr.isEmpty()) {
                        int TeamToScout = Integer.parseInt(TeamToScoutStr);
                        if (TeamToScout > 0 && TeamToScout < Globals.TeamList.size()) {
                            // This will crash the app instead of returning null if you pass it an invalid num
                            String ScoutingTeamName = Globals.TeamList.get(TeamToScout);
                            text_TeamName.setText(ScoutingTeamName);
                        } else {
                            text_TeamName.setText("");
                        }
                    }
                }
            }
        });

        edit_Team.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (!focus) {
                    String TeamToScoutStr = String.valueOf(edit_Team.getText());
                    if (!TeamToScoutStr.isEmpty()) {
                        int TeamToScout = Integer.parseInt(TeamToScoutStr);
                        if (TeamToScout > 0 && TeamToScout < Globals.TeamList.size()) {
                            // This will crash the app instead of returning null if you pass it an invalid num
                            String ScoutingTeamName = Globals.TeamList.get(TeamToScout);
                            text_TeamName.setText(ScoutingTeamName);
                        } else text_TeamName.setText("");
                    }
                }
            }
        });
    }
}