package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.team3663.scouting_app.utility.Logger;
import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.PreMatchBinding;

import java.util.ArrayList;

public class PreMatch extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private PreMatchBinding preMatchBinding;
    // To store the inputted name
    protected static String ScouterName;
    private static final ArrayList<String> Start_Positions = Globals.StartPositionList.getDescriptionList();
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
        Globals.CurrentScoutingTeam = Globals.sp.getInt(Constants.Prefs.SCOUTING_TEAM, 0);
        Globals.CurrentCompetitionId = Globals.sp.getInt(Constants.Prefs.COMPETITION_ID, 0);
        Globals.CurrentDeviceId = Globals.sp.getInt(Constants.Prefs.DEVICE_ID, 0);
        Globals.CurrentColorId = Globals.sp.getInt(Constants.Prefs.COLOR_CONTEXT_MENU, 1);
        Globals.CurrentPrefTeamPos = Globals.sp.getInt(Constants.Prefs.PREF_TEAM_POS, 0);
        Globals.isPractice = false;

        // Default the override button to disabled
        preMatchBinding.checkboxOverride.setEnabled(false);

        // Adds the items from the starting positions array to the list
        ArrayAdapter<String> adp_StartPos = new ArrayAdapter<String>(this, R.layout.cpr_spinner, Start_Positions);
        adp_StartPos.setDropDownViewResource(R.layout.cpr_spinner_item);
        preMatchBinding.spinnerStartingPosition.setAdapter(adp_StartPos);
        // Search through the list of Start Positions till you find the one that is correct then get its position in the list
        //  and set that one as selected
        int start_Pos_DropId = 0;
        for (int i = 0; i < Start_Positions.size(); i++) {
            if (Start_Positions.get(i).equals(Globals.StartPositionList.getStartPositionDescription(Globals.CurrentStartPosition))) {
                start_Pos_DropId = i;
                break;
            }
        }
        preMatchBinding.spinnerStartingPosition.setSelection(start_Pos_DropId);

        // adds teams in match to the spinner
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayList<String> teams;
        if (Globals.MatchList.isMatchValid(Globals.CurrentMatchNumber)) {
            teams = new ArrayList<String>();
            teams.add(getString(R.string.pre_dropdown_no_items));
        } else {
            teams = Globals.MatchList.getListOfTeams(Globals.CurrentMatchNumber);
        }
        ArrayAdapter<String> adp_Team = new ArrayAdapter<String>(this, R.layout.cpr_spinner, teams);
        adp_Team.setDropDownViewResource(R.layout.cpr_spinner_item);
        preMatchBinding.spinnerTeamToScout.setAdapter(adp_Team);

        // Run it from a handler because it doesn't like to work and it will just do absolutely nothing if you don't
        // Create a new variable so it wont change before the handler is called cause that will mess it up (Even though its only 1 millisecond)
        new Handler().postDelayed(() -> preMatchBinding.spinnerTeamToScout.setSelection(CurrentTeamToScoutPosition), 1);

        // Since we are putting the checkbox on the RIGHT side of the text, the checkbox doesn't honor padding.
        // So we need to use 7 spaces, but you can't when using a string resource (it ignores the trailing spaces)
        // So add it in now.
        preMatchBinding.checkboxDidPlay.setText(preMatchBinding.checkboxDidPlay.getText() + Globals.CheckBoxTextPadding);
        preMatchBinding.checkboxStartNote.setText(preMatchBinding.checkboxStartNote.getText() + Globals.CheckBoxTextPadding);

        // Create a text box to input the scouters name
        preMatchBinding.editScouterName.setText(ScouterName);

        // When we load the page, if we have a match number (can happen if we hit the BACK button from Match or it's the next match)
        // load the team information for the match
        if (Globals.CurrentMatchNumber > 0) {
            // If we have a match number, enable the override
            preMatchBinding.checkboxOverride.setEnabled(true);

            // MUST CONVERT TO STRING or it crashes with out warning
            preMatchBinding.editMatch.setText(String.valueOf(Globals.CurrentMatchNumber));

            ArrayList<String> teamsInMatch;
            if (Globals.MatchList.isMatchValid(Globals.CurrentMatchNumber)) {
                teamsInMatch = new ArrayList<String>();
                teamsInMatch.add(getString(R.string.pre_dropdown_no_items));
            } else {
                teamsInMatch = Globals.MatchList.getListOfTeams(Globals.CurrentMatchNumber);
            }

            // If there's an override set, add it to the spinner
            if (Globals.CurrentTeamOverrideNum > 0) teamsInMatch.add(String.valueOf(Globals.CurrentTeamOverrideNum));

            // Create and apply the adapter to the spinner.
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.cpr_spinner, teamsInMatch);
            adapter.setDropDownViewResource(R.layout.cpr_spinner_item);
            preMatchBinding.spinnerTeamToScout.setAdapter(adapter);

            // Set the spinner to the right selection
            if (CurrentTeamToScoutPosition > 0) preMatchBinding.spinnerTeamToScout.setSelection(CurrentTeamToScoutPosition);
            else preMatchBinding.spinnerTeamToScout.setSelection(teamsInMatch.size() - 1);
        } else {
            preMatchBinding.checkboxOverride.setEnabled(false);
            preMatchBinding.editMatch.setText("");
        }

        // Default checkboxes
        // TODO shouldn't we save these values off as well in case they hit the BACK button?
        preMatchBinding.checkboxDidPlay.setChecked(true);
        preMatchBinding.checkboxStartNote.setChecked(true);
        preMatchBinding.checkboxResubmit.setChecked(false);
        preMatchBinding.checkboxPractice.setChecked(false);

        // Hide override components initially
        preMatchBinding.textOverride.setVisibility(View.INVISIBLE);
        preMatchBinding.editOverrideTeamNum.setVisibility(View.INVISIBLE);
        preMatchBinding.butAddOverrideTeamNum.setVisibility(View.INVISIBLE);

        preMatchBinding.checkboxOverride.setOnClickListener(view -> {
            int state = View.VISIBLE;
            if (!preMatchBinding.checkboxOverride.isChecked()) state = View.INVISIBLE;                    preMatchBinding.textOverride.setVisibility(state);
            preMatchBinding.editOverrideTeamNum.setVisibility(state);
            preMatchBinding.butAddOverrideTeamNum.setVisibility(state);
        });

        preMatchBinding.checkboxPractice.setOnClickListener(view -> Globals.isPractice = preMatchBinding.checkboxPractice.isChecked());

        preMatchBinding.butAddOverrideTeamNum.setOnClickListener(view -> {
            String teamNum = String.valueOf(preMatchBinding.editOverrideTeamNum.getText());
            ArrayList<String> teamsInMatch;
            if (!teamNum.isEmpty()) {
                if (Globals.MatchList.isMatchValid(Globals.CurrentMatchNumber)) {
                    teamsInMatch = new ArrayList<String>();
                    teamsInMatch.add(getString(R.string.pre_dropdown_no_items));
                } else {
                    teamsInMatch = Globals.MatchList.getListOfTeams(Globals.CurrentMatchNumber);
                }

                teamsInMatch.add(teamNum);
                ArrayAdapter<String> adp_Team1 = new ArrayAdapter<String>(view.getContext(), R.layout.cpr_spinner, teamsInMatch);
                adp_Team1.setDropDownViewResource(R.layout.cpr_spinner_item);
                // Apply the adapter to the spinner
                preMatchBinding.spinnerTeamToScout.setAdapter(adp_Team1);
                preMatchBinding.spinnerTeamToScout.setSelection(teamsInMatch.size() - 1);
                // Set CurrentTeamToScoutPosition to be the overridden value
                CurrentTeamToScoutPosition = preMatchBinding.spinnerTeamToScout.getSelectedItemPosition();
                Globals.CurrentTeamOverrideNum = Integer.parseInt(teamNum);
            }

            preMatchBinding.checkboxOverride.setChecked(false);
            preMatchBinding.textOverride.setVisibility(View.INVISIBLE);
            preMatchBinding.editOverrideTeamNum.setVisibility(View.INVISIBLE);
            preMatchBinding.butAddOverrideTeamNum.setVisibility(View.INVISIBLE);
        });

        // Create a button for when you are done inputting info
        Button but_Next = preMatchBinding.butNext;
        but_Next.setOnClickListener(view -> {
            // If we should re-submit data, go to the submit page immediately
            if (preMatchBinding.checkboxResubmit.isChecked()) {
                Intent GoToSubmitData = new Intent(PreMatch.this, SubmitData.class);
                startActivity(GoToSubmitData);

                finish();
            } else {
                // Check we have all the fields entered that are needed.  Otherwise, pop a TOAST message instead
                if (String.valueOf(preMatchBinding.editMatch.getText()).isEmpty() || preMatchBinding.spinnerTeamToScout.getSelectedItem().toString().equals(getString(R.string.pre_dropdown_no_items))
                        || preMatchBinding.spinnerTeamToScout.getSelectedItem().toString().isEmpty() || String.valueOf(preMatchBinding.editScouterName.getText()).isEmpty()
                        || (preMatchBinding.spinnerStartingPosition.getSelectedItem().toString().equals(Globals.StartPositionList.getStartPositionDescription(Constants.Data.ID_START_POS_DEFAULT))
                        && preMatchBinding.checkboxDidPlay.isChecked())) {
                    Toast.makeText(PreMatch.this, R.string.pre_missing_data, Toast.LENGTH_SHORT).show();
                } else {
                    Globals.CurrentMatchNumber = Integer.parseInt(preMatchBinding.editMatch.getText().toString());
                    Globals.NumberMatchFilesKept = Globals.sp.getInt(Constants.Prefs.NUM_MATCHES, 5);
                    CurrentTeamToScoutPosition = preMatchBinding.spinnerTeamToScout.getSelectedItemPosition();

                    // Set up the Logger
                    // null it out first in case we have one set up already (could be there if BACK button was hit on Match)
                    Globals.EventLogger = null;
                    Globals.EventLogger = new Logger(getApplicationContext());

                    // Log all of the data from this page
                    Globals.CurrentTeamToScout = Integer.parseInt(preMatchBinding.spinnerTeamToScout.getSelectedItem().toString());
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_TEAM_TO_SCOUT, String.valueOf(Globals.CurrentTeamToScout));
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_SCOUTER, preMatchBinding.editScouterName.getText().toString().toUpperCase().replace(" ",""));
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_DID_PLAY, String.valueOf(preMatchBinding.checkboxDidPlay.isChecked()));
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_TEAM_SCOUTING, String.valueOf(Globals.CurrentScoutingTeam));
                    if (preMatchBinding.checkboxDidPlay.isChecked()) {
                        int startPos = Globals.StartPositionList.getStartPositionId(preMatchBinding.spinnerStartingPosition.getSelectedItem().toString());
                        Globals.EventLogger.LogData(Constants.Logger.LOGKEY_START_POSITION, String.valueOf(startPos));
                        Globals.CurrentStartPosition = startPos;
                    }

                    // Log if they started with a note (the match hasn't started so we need to specify a "0" time
                    // or else it will log as max match time which wastes 2 log characters for no benefit)
                    if (preMatchBinding.checkboxStartNote.isChecked()) {
                        Globals.EventLogger.LogEvent(Constants.Events.ID_AUTO_STARTNOTE, 0, 0, true, 0);
                    }

                    // Save off some fields for next time or later usage
                    ScouterName = String.valueOf(preMatchBinding.editScouterName.getText());

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
        });

        preMatchBinding.editMatch.setOnFocusChangeListener((view, focus) -> {
            if (!focus) {
                String MatchNumStr = String.valueOf(preMatchBinding.editMatch.getText());
                int MatchNum = -1;
                if (!MatchNumStr.isEmpty()) MatchNum = Integer.parseInt(MatchNumStr);

                // We need to do SOMETHING if:
                // 1. they blanked out the match number
                // 2. they changed the match number from what it was before
                // 3. we didn't have a match number before.
                if (MatchNumStr.isEmpty()) {
                    // Disable the override
                    preMatchBinding.checkboxOverride.setEnabled(false);
                    Globals.CurrentMatchNumber = -1;
                    Globals.CurrentTeamOverrideNum = 0;
                    CurrentTeamToScoutPosition = -1;

                    ArrayList<String> noTeams = new ArrayList<>();
                    noTeams.add(getString(R.string.pre_dropdown_no_items));
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), R.layout.cpr_spinner, noTeams);
                    adapter.setDropDownViewResource(R.layout.cpr_spinner_item);
                    preMatchBinding.spinnerTeamToScout.setAdapter(adapter);
                } else if (MatchNum != Globals.CurrentMatchNumber) {
                    // Enable the override
                    preMatchBinding.checkboxOverride.setEnabled(true);
                    Globals.CurrentMatchNumber = MatchNum;
                    Globals.CurrentTeamOverrideNum = 0;

                    if (MatchNum > 0 && MatchNum < Globals.MatchList.size()) {
                        ArrayList<String> teamsInMatch = Globals.MatchList.getListOfTeams(MatchNum);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), R.layout.cpr_spinner, teamsInMatch);
                        adapter.setDropDownViewResource(R.layout.cpr_spinner_item);

                        // Apply the adapter to the spinner.
                        preMatchBinding.spinnerTeamToScout.setAdapter(adapter);

                        // If there's a default position, select the right team
                        if (Globals.CurrentPrefTeamPos > 0) {
                            String prefTeam = Globals.MatchList.getTeamInPosition(MatchNum, Constants.Settings.PREF_TEAM_POS[Globals.CurrentPrefTeamPos]);
                            int prefPos = teamsInMatch.indexOf(prefTeam);
                            preMatchBinding.spinnerTeamToScout.setSelection(prefPos);
                        }

                        CurrentTeamToScoutPosition = preMatchBinding.spinnerTeamToScout.getSelectedItemPosition();
                    } else {
                        ArrayList<String> noTeams = new ArrayList<>();
                        noTeams.add(getString(R.string.pre_dropdown_no_items));
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), R.layout.cpr_spinner, noTeams);
                        adapter.setDropDownViewResource(R.layout.cpr_spinner_item);
                        preMatchBinding.spinnerTeamToScout.setAdapter(adapter);
                    }
                }
            }
        });

        preMatchBinding.spinnerTeamToScout.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String TeamToScoutStr = preMatchBinding.spinnerTeamToScout.getSelectedItem().toString();
                if (!TeamToScoutStr.isEmpty() && !TeamToScoutStr.equals(getString(R.string.pre_dropdown_no_items))) {
                    int TeamToScout = Integer.parseInt(TeamToScoutStr);
                    if (TeamToScout > 0 && TeamToScout < Globals.TeamList.size()) {
                        // This will crash the app instead of returning null if you pass it an invalid num
                        String ScoutingTeamName = Globals.TeamList.get(TeamToScout);
                        preMatchBinding.textTeamToScoutName.setText(ScoutingTeamName);
                    } else {
                        preMatchBinding.textTeamToScoutName.setText("");
                    }

                    // Save off what you selected for if you go to the match and then back
                    CurrentTeamToScoutPosition = preMatchBinding.spinnerTeamToScout.getSelectedItemPosition();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }
}