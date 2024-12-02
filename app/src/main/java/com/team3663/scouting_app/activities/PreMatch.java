package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.team3663.scouting_app.utility.achievements.Achievements;

import java.util.ArrayList;

public class PreMatch extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private PreMatchBinding preMatchBinding;
    // To store the inputted name
    protected static String ScouterName;
    private static final ArrayList<String> Start_Positions = Globals.StartPositionList.getDescriptionList();
    private static final ArrayList<String> Match_Types = Globals.MatchTypeList.getDescriptionList();
    public static int CurrentTeamToScoutPosition;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle in_savedInstanceState) {
        super.onCreate(in_savedInstanceState);
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

        // Initialize activity components
        initMatchNumber();
        initMatchType();
        initTeamNumber();
        initScouterName();
        initDidPlay();
        initStartingNote();
        initStartingPos();
        initOverride();
        initResubmit();
        initPractice();
        initNext();
        initAchievements();
        initShadowMode();
    }

    // =============================================================================================
    // Function:    initMatchNumber
    // Description: Initialize the Match Number field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initMatchNumber() {
        if (Globals.CurrentMatchNumber > 0)
            // MUST CONVERT TO STRING or it crashes with out warning
            preMatchBinding.editMatch.setText(String.valueOf(Globals.CurrentMatchNumber));
        else
            preMatchBinding.editMatch.setText("");

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
                } else if (MatchNum != Globals.CurrentMatchNumber) {
                    // Enable the override
                    preMatchBinding.checkboxOverride.setEnabled(true);
                    Globals.CurrentMatchNumber = MatchNum;
                    Globals.CurrentTeamOverrideNum = 0;
                }

                loadTeamToScout();
            }
        });
    }

    // =============================================================================================
    // Function:    initMatchType
    // Description: Initialize the Match Type field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initMatchType() {
        // Adds the items from the match type array to the list
        ArrayAdapter<String> adp_MatchType = new ArrayAdapter<>(this, R.layout.cpr_spinner, Match_Types);
        adp_MatchType.setDropDownViewResource(R.layout.cpr_spinner_item);
        preMatchBinding.spinnerMatchType.setAdapter(adp_MatchType);

        // Search through the list of match types until you find the one that is correct then get its position in the list
        // and set that one as selected
        int start_Pos_DropId = 0;
        for (int i = 0; i < Match_Types.size(); i++) {
            if (Match_Types.get(i).equals(Globals.MatchTypeList.getMatchTypeDescription(Globals.CurrentMatchType))) {
                start_Pos_DropId = i;
                break;
            }
        }
        preMatchBinding.spinnerMatchType.setSelection(start_Pos_DropId);

        // Set up a listener to handle any changes to the dropdown
        preMatchBinding.spinnerMatchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Save off what you selected to be used until changed again
                int newMatchType = Globals.MatchTypeList.getMatchTypeId(preMatchBinding.spinnerMatchType.getSelectedItem().toString());

                if (newMatchType != Globals.CurrentMatchType) {
                    Globals.CurrentMatchType = Globals.MatchTypeList.getMatchTypeId(preMatchBinding.spinnerMatchType.getSelectedItem().toString());
                    Globals.CurrentTeamOverrideNum = 0;
                    Globals.CurrentMatchNumber = 0;
                    Globals.CurrentTeamToScout = 0;
                    preMatchBinding.editMatch.setText("");
                    preMatchBinding.textTeamToScoutName.setText("");
                    loadTeamToScout();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    // =============================================================================================
    // Function:    initTeamNumber
    // Description: Initialize the Team Number To Scout field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initTeamNumber() {
        loadTeamToScout();

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

    // =============================================================================================
    // Function:    initScouterName
    // Description: Initialize the Scouter Name field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initScouterName() {
        // Create a text box to input the scouters name
        preMatchBinding.editScouterName.setText(ScouterName);
    }

    // =============================================================================================
    // Function:    initDidPlay
    // Description: Initialize the Did Play field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initDidPlay() {
        // Since we are putting the checkbox on the RIGHT side of the text, the checkbox doesn't honor padding.
        // So we need to use 7 spaces, but you can't when using a string resource (it ignores the trailing spaces)
        // So add it in now.
        String paddedText = preMatchBinding.checkboxDidPlay.getText() + Globals.CheckBoxTextPadding;
        preMatchBinding.checkboxDidPlay.setText(paddedText);

        // Default checkboxes
        // TODO shouldn't we save these values off as well in case they hit the BACK button?
        preMatchBinding.checkboxDidPlay.setChecked(true);
    }

    // =============================================================================================
    // Function:    initStartingNote
    // Description: Initialize the Starting Note field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initStartingNote() {
        // Since we are putting the checkbox on the RIGHT side of the text, the checkbox doesn't honor padding.
        // So we need to use 7 spaces, but you can't when using a string resource (it ignores the trailing spaces)
        // So add it in now.
        String paddedText = preMatchBinding.checkboxStartNote.getText() + Globals.CheckBoxTextPadding;
        preMatchBinding.checkboxStartNote.setText(paddedText);

        // Default checkboxes
        // TODO shouldn't we save these values off as well in case they hit the BACK button?
        preMatchBinding.checkboxStartNote.setChecked(true);
    }

    // =============================================================================================
    // Function:    initStartingPos
    // Description: Initialize the Start Position field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initStartingPos() {
        // Adds the items from the starting positions array to the list
        ArrayAdapter<String> adp_StartPos = new ArrayAdapter<>(this, R.layout.cpr_spinner, Start_Positions);
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
    }

    // =============================================================================================
    // Function:    initOverride
    // Description: Initialize the Override fields
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initOverride() {
        // Default the override button to disabled
        // If we have a match number, enable the override
        preMatchBinding.checkboxOverride.setEnabled(Globals.CurrentMatchNumber > 0);

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

        preMatchBinding.butAddOverrideTeamNum.setOnClickListener(view -> {
            // Set the global override number
            String new_override = preMatchBinding.editOverrideTeamNum.getText().toString();

            if (!new_override.isEmpty())
                Globals.CurrentTeamOverrideNum = Integer.parseInt(new_override);

            // Load the team data again
            loadTeamToScout();

            preMatchBinding.checkboxOverride.setChecked(false);
            preMatchBinding.textOverride.setVisibility(View.INVISIBLE);
            preMatchBinding.editOverrideTeamNum.setVisibility(View.INVISIBLE);
            preMatchBinding.butAddOverrideTeamNum.setVisibility(View.INVISIBLE);
        });
    }

    // =============================================================================================
    // Function:    initResubmit
    // Description: Initialize the Resubmit field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initResubmit() {
        // Default checkboxes
        // TODO shouldn't we save these values off as well in case they hit the BACK button?
        preMatchBinding.checkboxResubmit.setChecked(false);
    }

    // =============================================================================================
    // Function:    initPractice
    // Description: Initialize the Practice field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initPractice() {
        preMatchBinding.checkboxPractice.setOnClickListener(view -> Globals.isPractice = preMatchBinding.checkboxPractice.isChecked());

        // Default checkboxes
        // TODO shouldn't we save these values off as well in case they hit the BACK button?
        preMatchBinding.checkboxPractice.setChecked(false);
    }

    // =============================================================================================
    // Function:    initNext
    // Description: Initialize the Next button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initNext() {
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
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_MATCH_TYPE, Globals.MatchTypeList.getMatchTypeShortForm(Globals.CurrentMatchType));
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_SHADOW_MODE, String.valueOf(Globals.isShadowMode));

                    Achievements.data_TeamToScout = Globals.CurrentTeamToScout;

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
                    if ((ScouterName != null) && (!ScouterName.isEmpty()) && !ScouterName.equals(String.valueOf(preMatchBinding.editScouterName.getText())))
                        Globals.myAchievements.clearAllData();

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
    }

    // =============================================================================================
    // Function:    loadTeamToScout
    // Description: Load the teams to scout into the spinner (including an override)
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void loadTeamToScout() {
        // If we have a match number load the team information for the match
        ArrayList<String> teamsInMatch = new ArrayList<>();
        if (Globals.CurrentMatchNumber > 0) {
            if (Globals.MatchList.isCurrentMatchValid())
                teamsInMatch = Globals.MatchList.getListOfTeams();
            else {
                teamsInMatch = new ArrayList<>();
                teamsInMatch.add(getString(R.string.pre_dropdown_no_items));
            }
        }

        // If there's an override set, add it to the spinner
        if (Globals.CurrentTeamOverrideNum > 0) {
            if (teamsInMatch.size() == 1)
                teamsInMatch.set(0, String.valueOf(Globals.CurrentTeamOverrideNum));
            else
                teamsInMatch.add(String.valueOf(Globals.CurrentTeamOverrideNum));
        }

        // Create and apply the adapter to the spinner.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.cpr_spinner, teamsInMatch);
        adapter.setDropDownViewResource(R.layout.cpr_spinner_item);
        preMatchBinding.spinnerTeamToScout.setAdapter(adapter);

        // Choose the right team in the list to select.
        // If there's an override, we want that one and it'll be the last in the list
        // If there's a preferred position, choose it
        // Lastly, if there is no previously chosen one, set it to the first one
        if (Globals.CurrentTeamOverrideNum > 0) {
            CurrentTeamToScoutPosition = teamsInMatch.size() - 1;
        } else if (Globals.CurrentPrefTeamPos > 0) {
            String prefTeam = Globals.MatchList.getTeamInPosition(Constants.Settings.PREF_TEAM_POS[Globals.CurrentPrefTeamPos]);
            CurrentTeamToScoutPosition = teamsInMatch.indexOf(prefTeam);
        } else if (CurrentTeamToScoutPosition < 0)
            CurrentTeamToScoutPosition = 0;

        preMatchBinding.spinnerTeamToScout.setSelection(CurrentTeamToScoutPosition);
    }

    // =============================================================================================
    // Function:    initAchievements
    // Description: Initialize the Achievements
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initAchievements() {
        if (Globals.myAchievements == null)
            Globals.myAchievements = new Achievements();
    }

    // =============================================================================================
    // Function:    initShadowMode
    // Description: Initialize the Shadow Mode fields
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initShadowMode() {
        if (Globals.isShadowMode) {
            preMatchBinding.textShadowModeL.setText(getString(R.string.pre_shadow_mode));
            preMatchBinding.textShadowModeR.setText(getString(R.string.pre_shadow_mode));
        }
    }
}