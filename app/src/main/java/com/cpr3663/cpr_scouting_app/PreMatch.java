package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.ArrayList;

public class PreMatch extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private PreMatchBinding preMatchBinding;
    // To store the inputted name
    protected static String ScouterName;
    protected CheckBox checkbox_StartNote; // This needs to be global so that Match.java can access it
    private static final String[] Start_Positions = Globals.StartPositionList.getDescriptionList();
    public static int CurrentTeamToScoutPosition;

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
        if (Globals.sp == null) Globals.sp = this.getSharedPreferences(getString(R.string.preference_setting_file_key), Context.MODE_PRIVATE);
        Globals.CurrentScoutingTeam = Globals.sp.getInt(Constants.SP_SCOUTING_TEAM, 0);
        Globals.CurrentCompetitionId = Globals.sp.getInt(Constants.SP_COMPETITION_ID, 0);
        Globals.CurrentDeviceId = Globals.sp.getInt(Constants.SP_DEVICE_ID, 0);
        Globals.CurrentColorId = Globals.sp.getInt(Constants.SP_COLOR_CONTEXT_MENU, 1);
        Globals.CurrentPrefTeamPos = Globals.sp.getInt(Constants.SP_PREF_TEAM_POS, 0);
        Globals.isPractice = false;

        // Create components
        EditText edit_Match = preMatchBinding.editMatch;
        Spinner spinner_Team = preMatchBinding.spinnerTeamToScout;
        Spinner spinner_StartPos = preMatchBinding.spinnerStartingPosition;
        TextView text_TeamName = preMatchBinding.textTeamToScoutName;
        Button but_AddOverrideTeamNum = preMatchBinding.butAddOverrideTeamNum;
        CheckBox checkbox_DidPlay = preMatchBinding.checkboxDidPlay;
        checkbox_StartNote = preMatchBinding.checkboxStartNote;
        CheckBox checkbox_Override = preMatchBinding.checkboxOverride;
        CheckBox checkbox_Practice = preMatchBinding.checkboxPractice;
        CheckBox checkbox_ReSubmit = preMatchBinding.checkboxResubmit;
        EditText edit_OverrideTeamNum = preMatchBinding.editOverrideTeamNum;
        EditText edit_Name = preMatchBinding.editScouterName;
        TextView text_Override = preMatchBinding.textOverride;

        // Default the override button to disabled
        checkbox_Override.setEnabled(false);

        // Adds the items from the starting positions array to the list
        ArrayAdapter<String> adp_StartPos = new ArrayAdapter<String>(this, R.layout.cpr_spinner, Start_Positions);
        adp_StartPos.setDropDownViewResource(R.layout.cpr_spinner_item);
        spinner_StartPos.setAdapter(adp_StartPos);
        // Search through the list of Start Positions till you find the one that is correct then get its position in the list
        //  and set that one as selected
        int start_Pos_DropId = 0;
        for (int i = 0; i < Start_Positions.length; i++) {
            if (Start_Positions[i].equals(Globals.StartPositionList.getStartPositionDescription(Globals.CurrentStartPosition))) {
                start_Pos_DropId = i;
                break;
            }
        }
        spinner_StartPos.setSelection(start_Pos_DropId);

        // adds teams in match to the spinner
        // Create an ArrayAdapter using the string array and a default spinner layout.
        Matches.MatchRow match = Globals.MatchList.getMatchInfoRow(Globals.CurrentMatchNumber);
        ArrayList<String> teams;
        if (match == null) {
            teams = new ArrayList<String>();
            teams.add(getString(R.string.dropdown_no_items));
        } else {
            teams = match.getListOfTeams();
        }
        ArrayAdapter<String> adp_Team = new ArrayAdapter<String>(this, R.layout.cpr_spinner, teams);
        adp_Team.setDropDownViewResource(R.layout.cpr_spinner_item);
        spinner_Team.setAdapter(adp_Team);

        // Run it from a handler because it doesn't like to work and it will just do absolutely nothing if you don't
        // Create a new variable so it wont change before the handler is called cause that will mess it up (Even though its only 1 millisecond)
        new Handler().postDelayed(new Runnable() {
            public void run() {
                spinner_Team.setSelection(CurrentTeamToScoutPosition);
            }
        }, 1);

        // Since we are putting the checkbox on the RIGHT side of the text, the checkbox doesn't honor padding.
        // So we need to use 7 spaces, but you can't when using a string resource (it ignores the trailing spaces)
        // So add it in now.
        checkbox_DidPlay.setText(checkbox_DidPlay.getText() + Globals.CheckBoxTextPadding);
        checkbox_StartNote.setText(checkbox_StartNote.getText() + Globals.CheckBoxTextPadding);

        // Create a text box to input the scouters name
        edit_Name.setText(ScouterName);

        // When we load the page, if we have a match number (can happen if we hit the BACK button from Match or it's the next match)
        // load the team information for the match
        if (Globals.CurrentMatchNumber > 0) {
            // If we have a match number, enable the override
            checkbox_Override.setEnabled(true);

            // MUST CONVERT TO STRING or it crashes with out warning
            edit_Match.setText(String.valueOf(Globals.CurrentMatchNumber));
            // TODO it seems we do this twice.  once at load if we saved a match number, and again
            // TODO if they type in a match number.  should be in a function?
            Matches.MatchRow Match = Globals.MatchList.getMatchInfoRow(Globals.CurrentMatchNumber);
            ArrayList<String> teamsInMatch;

            // If we got match data, get the list of teams from it
            if (Match != null)
                teamsInMatch = Match.getListOfTeams();
            else {
                teamsInMatch = new ArrayList<>();
                teamsInMatch.add(getString(R.string.dropdown_no_items));
            }

            // If there's an override set, add it to the spinner
            if (Globals.CurrentTeamOverrideNum > 0) teamsInMatch.add(String.valueOf(Globals.CurrentTeamOverrideNum));

            // Create and apply the adapter to the spinner.
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.cpr_spinner, teamsInMatch);
            adapter.setDropDownViewResource(R.layout.cpr_spinner_item);
            spinner_Team.setAdapter(adapter);

            // Set the spinner to the right selection
            if (CurrentTeamToScoutPosition > 0) spinner_Team.setSelection(CurrentTeamToScoutPosition);
            else spinner_Team.setSelection(teamsInMatch.size() - 1);
        } else edit_Match.setText("");

        // Default checkboxes
        // TODO shouldn't we save these values off as well in case they hit the BACK button?
        checkbox_DidPlay.setChecked(true);
        checkbox_StartNote.setChecked(true);
        checkbox_ReSubmit.setChecked(false);
        checkbox_Practice.setChecked(false);

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

        checkbox_Practice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Globals.isPractice = checkbox_Practice.isChecked();
            }
        });

        but_AddOverrideTeamNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String teamNum = String.valueOf(preMatchBinding.editOverrideTeamNum.getText());
                ArrayList<String> teamsInMatch;
                if (!teamNum.isEmpty()) {
                    Matches.MatchRow Match = Globals.MatchList.getMatchInfoRow(Integer.parseInt(preMatchBinding.editMatch.getText().toString()));
                    if (Match != null)
                        teamsInMatch = Match.getListOfTeams();
                    else {
                        teamsInMatch = new ArrayList<>();
                        teamsInMatch.add(getString(R.string.dropdown_no_items));
                    }

                    teamsInMatch.add(teamNum);
                    ArrayAdapter<String> adp_Team = new ArrayAdapter<String>(view.getContext(), R.layout.cpr_spinner, teamsInMatch);
                    adp_Team.setDropDownViewResource(R.layout.cpr_spinner_item);
                    // Apply the adapter to the spinner
                    spinner_Team.setAdapter(adp_Team);
                    spinner_Team.setSelection(teamsInMatch.size() - 1);
                    // Set CurrentTeamToScoutPosition to be the overridden value
                    CurrentTeamToScoutPosition = spinner_Team.getSelectedItemPosition();
                    Globals.CurrentTeamOverrideNum = Integer.parseInt(teamNum);
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

                    finish();
                } else {
                    // Check we have all the fields entered that are needed.  Otherwise, pop a TOAST message instead
                    if (String.valueOf(edit_Match.getText()).isEmpty() || spinner_Team.getSelectedItem().toString().equals(getString(R.string.dropdown_no_items))
                            || spinner_Team.getSelectedItem().toString().isEmpty() || String.valueOf(edit_Name.getText()).isEmpty()
                            || (spinner_StartPos.getSelectedItem().toString().equals(Globals.StartPositionList.getStartPositionDescription(Constants.DATA_ID_START_POS_DEFAULT)) && checkbox_DidPlay.isChecked())) {
                        Toast.makeText(PreMatch.this, R.string.missing_data, Toast.LENGTH_SHORT).show();
                    } else {
                        Globals.CurrentMatchNumber = Integer.parseInt(preMatchBinding.editMatch.getText().toString());
                        Globals.NumberMatchFilesKept = Globals.sp.getInt(Constants.SP_NUM_MATCHES, 5);
                        CurrentTeamToScoutPosition = spinner_Team.getSelectedItemPosition();

                        // Set up the Logger
                        // null it out first in case we have one set up already (could be there if BACK button was hit on Match)
                        Globals.EventLogger = null;
                        Globals.EventLogger = new Logger(getApplicationContext());

                        // Log all of the data from this page
                        Globals.CurrentTeamToScout = Integer.parseInt(preMatchBinding.spinnerTeamToScout.getSelectedItem().toString());
                        Globals.EventLogger.LogData(Constants.LOGKEY_TEAM_TO_SCOUT, String.valueOf(Globals.CurrentTeamToScout));
                        Globals.EventLogger.LogData(Constants.LOGKEY_SCOUTER, preMatchBinding.editScouterName.getText().toString().toUpperCase().replace(" ",""));
                        Globals.EventLogger.LogData(Constants.LOGKEY_DID_PLAY, String.valueOf(preMatchBinding.checkboxDidPlay.isChecked()));
                        Globals.EventLogger.LogData(Constants.LOGKEY_TEAM_SCOUTING, String.valueOf(Globals.CurrentScoutingTeam));
                        if (checkbox_DidPlay.isChecked()) {
                            int startPos = Globals.StartPositionList.getStartPositionId(spinner_StartPos.getSelectedItem().toString());
                            Globals.EventLogger.LogData(Constants.LOGKEY_START_POSITION, String.valueOf(startPos));
                            Globals.CurrentStartPosition = startPos;
                        }

                        // Log if they started with a note (the match hasn't started so we need to specify a "0" time
                        // or else it will log as max match time which wastes 2 log characters for no benefit)
                        if (checkbox_StartNote.isChecked()) {
                            Globals.EventLogger.LogEvent(Constants.EVENT_ID_AUTO_STARTNOTE, 0, 0, true, 0);
                        }

                        // Save off some fields for next time or later usage
                        ScouterName = String.valueOf(edit_Name.getText());

                        // If they didn't play skip everything else
                        if (preMatchBinding.checkboxDidPlay.isChecked()) {
                            Intent GoToMatch = new Intent(PreMatch.this, Match.class);
                            startActivity(GoToMatch);
                        } else {
                            // Since we're jumping to the Submit page, we need to clear the Logger first.
                            Globals.EventLogger.clear();
                            Globals.EventLogger = null;

                            // Increases the match number so that it auto fills for the next match correctly
                            //  and do it after the logger is closed so that this can't mess the logger up
                            Globals.CurrentMatchNumber++;

                            // Reset the Saved Start position so that you have to choose it again
                            Globals.CurrentStartPosition = 0;

                            Intent GoToSubmitData = new Intent(PreMatch.this, SubmitData.class);
                            startActivity(GoToSubmitData);
                        }

                        finish();
                    }
                }
            }
        });

        edit_Match.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (!focus) {
                    String MatchNumStr = String.valueOf(edit_Match.getText());
                    int MatchNum = -1;
                    if (!MatchNumStr.isEmpty()) MatchNum = Integer.parseInt(MatchNumStr);

                    // We need to do SOMETHING if:
                    // 1. they blanked out the match number
                    // 2. they changed the match number from what it was before
                    // 3. we didn't have a match number before.
                    if (MatchNumStr.isEmpty()) {
                        // Disable the override
                        checkbox_Override.setEnabled(false);
                        Globals.CurrentMatchNumber = -1;
                        Globals.CurrentTeamOverrideNum = 0;
                        CurrentTeamToScoutPosition = -1;

                        ArrayList<String> noTeams = new ArrayList<>();
                        noTeams.add(getString(R.string.dropdown_no_items));
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), R.layout.cpr_spinner, noTeams);
                        adapter.setDropDownViewResource(R.layout.cpr_spinner_item);
                        spinner_Team.setAdapter(adapter);
                    } else if (MatchNum != Globals.CurrentMatchNumber) {
                        // Enable the override
                        checkbox_Override.setEnabled(true);
                        Globals.CurrentMatchNumber = MatchNum;
                        Globals.CurrentTeamOverrideNum = 0;

                        if (MatchNum > 0 && MatchNum < Globals.MatchList.size()) {
                            Matches.MatchRow Match = Globals.MatchList.getMatchInfoRow(MatchNum);
                            ArrayList<String> teamsInMatch = Match.getListOfTeams();
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), R.layout.cpr_spinner, teamsInMatch);
                            adapter.setDropDownViewResource(R.layout.cpr_spinner_item);

                            // Apply the adapter to the spinner.
                            spinner_Team.setAdapter(adapter);

                            // If there's a default position, select the right team
                            if (Globals.CurrentPrefTeamPos > 0) {
                                String prefTeam = Match.getTeamInPosition(Constants.SETTINGS_PREF_TEAM_POS[Globals.CurrentPrefTeamPos]);
                                int prefPos = teamsInMatch.indexOf(prefTeam);
                                spinner_Team.setSelection(prefPos);
                            }

                            CurrentTeamToScoutPosition = spinner_Team.getSelectedItemPosition();
                        } else {
                            ArrayList<String> noTeams = new ArrayList<>();
                            noTeams.add(getString(R.string.dropdown_no_items));
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), R.layout.cpr_spinner, noTeams);
                            adapter.setDropDownViewResource(R.layout.cpr_spinner_item);
                            spinner_Team.setAdapter(adapter);
                        }
                    }
                }
            }
        });

        spinner_Team.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String TeamToScoutStr = spinner_Team.getSelectedItem().toString();
                if (!TeamToScoutStr.isEmpty() && !TeamToScoutStr.equals(getString(R.string.dropdown_no_items))) {
                    int TeamToScout = Integer.parseInt(TeamToScoutStr);
                    if (TeamToScout > 0 && TeamToScout < Globals.TeamList.size()) {
                        // This will crash the app instead of returning null if you pass it an invalid num
                        String ScoutingTeamName = Globals.TeamList.get(TeamToScout);
                        text_TeamName.setText(ScoutingTeamName);
                    } else {
                        text_TeamName.setText("");
                    }

                    // Save off what you selected for if you go to the match and then back
                    CurrentTeamToScoutPosition = spinner_Team.getSelectedItemPosition();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }
}