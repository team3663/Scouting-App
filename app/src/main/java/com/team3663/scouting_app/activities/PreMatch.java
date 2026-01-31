package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class PreMatch extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private PreMatchBinding preMatchBinding;
    // To store the inputted name
    protected static String ScouterName;
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
        Globals.CurrentScoutingTeam = Globals.sp.getString(Constants.Prefs.SCOUTING_TEAM, "");
        Globals.CurrentCompetitionId = Globals.sp.getInt(Constants.Prefs.COMPETITION_ID, 0);
        Globals.CurrentDeviceId = Globals.sp.getInt(Constants.Prefs.DEVICE_ID, 0);
        Globals.CurrentColorId = Globals.sp.getInt(Constants.Prefs.COLOR_CONTEXT_MENU, 1);
        Globals.CurrentPrefTeamPos = Globals.sp.getInt(Constants.Prefs.PREF_TEAM_POS, 0);
        Globals.CurrentFieldOrientationPos = Globals.sp.getInt(Constants.Prefs.PREF_ORIENTATION, 0);
        Globals.CurrentQRSize = Globals.sp.getInt(Constants.Prefs.QR_SIZE, 0);

        // Initialize activity components
        initMatchNumber();
        initMatchType();
        initTeamNumber();
        initScouterName();
        initDidPlay();
        initStartingGamePiece();
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
            // MUST CONVERT TO STRING or it crashes without warning
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
                    Globals.CurrentMatchNumber = 0;
                    Globals.CurrentTeamOverrideNum = "";
                    CurrentTeamToScoutPosition = -1;
                } else if (MatchNum != Globals.CurrentMatchNumber) {
                    // Enable the override
                    preMatchBinding.checkboxOverride.setEnabled(true);
                    Globals.CurrentMatchNumber = MatchNum;
                    Globals.CurrentTeamOverrideNum = "";
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
                String newMatchType = Globals.MatchTypeList.getMatchTypeShortForm(preMatchBinding.spinnerMatchType.getSelectedItem().toString());

                if (!Objects.equals(newMatchType, Globals.CurrentMatchType)) {
                    Globals.CurrentMatchType = newMatchType;
                    Globals.CurrentTeamOverrideNum = "";
                    Globals.CurrentMatchNumber = 0;
                    Globals.CurrentTeamToScout = "";
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
                    String ScoutingTeamName = Globals.TeamList.getOrDefault(TeamToScoutStr, "");
                    preMatchBinding.textTeamToScoutName.setText(ScoutingTeamName);

                    // Save off what you selected for if you go to the match and then back
                    CurrentTeamToScoutPosition = preMatchBinding.spinnerTeamToScout.getSelectedItemPosition();
                }
                else preMatchBinding.textTeamToScoutName.setText("");
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
        preMatchBinding.checkboxDidPlay.setChecked(true);
    }

    // =============================================================================================
    // Function:    initStartingGamePiece
    // Description: Initialize the Starting Game Piece field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initStartingGamePiece() {
        // Default starting number of game pieces
        Globals.numStartingGamePiece = Constants.PreMatch.STARTING_GAME_PIECES;

        // Create text box to input robot's starting number of game pieces
        preMatchBinding.editNumStartingGamePiece.setText(String.valueOf(Globals.numStartingGamePiece));
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
                Globals.CurrentTeamOverrideNum = new_override;

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
        preMatchBinding.checkboxPractice.setChecked(Globals.isPractice);
    }

    // =============================================================================================
    // Function:    processNextButton
    // Description: Process the Next button action - assumes all checks have been made
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void processNextButton() {
        Globals.CurrentMatchNumber = Integer.parseInt(preMatchBinding.editMatch.getText().toString());
        Globals.NumberMatchFilesKept = Globals.sp.getInt(Constants.Prefs.NUM_MATCHES, 5);
        Globals.numStartingGamePiece = Integer.parseInt(preMatchBinding.editNumStartingGamePiece.getText().toString());
        CurrentTeamToScoutPosition = preMatchBinding.spinnerTeamToScout.getSelectedItemPosition();

        // Set up the Logger
        // Clear and null it out first if we have one set up already (could be there if BACK button was hit on Match)
        if (Globals.EventLogger != null) {
            Globals.EventLogger.close();
            Globals.EventLogger = null;
        }
        Globals.EventLogger = new Logger(getApplicationContext());

        // Log all of the data from this page
        Globals.CurrentTeamToScout = preMatchBinding.spinnerTeamToScout.getSelectedItem().toString();
        Globals.EventLogger.LogData(Constants.Logger.LOGKEY_TEAM_TO_SCOUT, Globals.CurrentTeamToScout);
        Globals.EventLogger.LogData(Constants.Logger.LOGKEY_SCOUTER, preMatchBinding.editScouterName.getText().toString().toUpperCase().replace(" ",""));
        Globals.EventLogger.LogData(Constants.Logger.LOGKEY_DID_PLAY, String.valueOf(preMatchBinding.checkboxDidPlay.isChecked()));
        Globals.EventLogger.LogData(Constants.Logger.LOGKEY_START_WITH_GAME_PIECE, String.valueOf(Integer.valueOf(Globals.numStartingGamePiece)));
        Globals.EventLogger.LogData(Constants.Logger.LOGKEY_TEAM_SCOUTING, Globals.CurrentScoutingTeam);
        Globals.EventLogger.LogData(Constants.Logger.LOGKEY_MATCH_TYPE, Globals.MatchTypeList.getMatchTypeShortForm(Globals.CurrentMatchType));
        Globals.EventLogger.LogData(Constants.Logger.LOGKEY_SHADOW_MODE, String.valueOf(Globals.isShadowMode));
        Globals.EventLogger.LogData(Constants.Logger.LOGKEY_START_TIME, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")));

        Achievements.data_TeamToScout = Globals.CurrentTeamToScout;

        // Save off some fields for next time or later usage
        if ((ScouterName != null) && (!ScouterName.isEmpty()) && !ScouterName.equals(String.valueOf(preMatchBinding.editScouterName.getText())))
            Globals.myAchievements.clearAllData();

        ScouterName = String.valueOf(preMatchBinding.editScouterName.getText());

        // If they didn't play skip everything else
        if (preMatchBinding.checkboxDidPlay.isChecked()) {
            Intent GoToMatch = new Intent(PreMatch.this, MatchTally.class);
            startActivity(GoToMatch);
        } else {
            Intent GoToSubmitData = new Intent(PreMatch.this, SubmitData.class);
            startActivity(GoToSubmitData);
        }

        finish();
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
                // Decrement the team number since we're only going to resubmit data.  When we return,
                // it will increment it for the "next match" will set it back (in this case) to the
                // original value.
                Globals.CurrentMatchNumber--;

                // Since we're only resubmitting data, we should close the current logger
                if (Globals.EventLogger != null) {
                    Globals.EventLogger.close();
                    Globals.EventLogger = null;
                }

                Intent GoToSubmitData = new Intent(PreMatch.this, SubmitData.class);
                startActivity(GoToSubmitData);

                finish();
                return;
            }

            if (String.valueOf(preMatchBinding.editMatch.getText()).isEmpty() || preMatchBinding.spinnerTeamToScout.getSelectedItem().toString().equals(getString(R.string.pre_dropdown_no_items))
                    || preMatchBinding.spinnerTeamToScout.getSelectedItem().toString().isEmpty() || String.valueOf(preMatchBinding.editScouterName.getText()).isEmpty()) {
                Toast.makeText(PreMatch.this, R.string.pre_missing_data, Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLogFileExisting()) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle(getString(R.string.pre_already_scouted_title))
                        .setMessage(getString(R.string.pre_already_scouted_message))

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(getString(R.string.pre_already_scouted_button_positive), (dialog, which) -> {
                            dialog.dismiss();
                            backupLogFile(Globals.CurrentCompetitionId + "_" + Globals.CurrentMatchNumber + "_" + Globals.CurrentDeviceId + "_" + Globals.MatchTypeList.getMatchTypeShortForm(Globals.CurrentMatchType) + ".csv");
                            Globals.FileList.remove(Globals.CurrentCompetitionId + "_" + Globals.CurrentMatchNumber + "_" + Globals.CurrentDeviceId + "_" + Globals.MatchTypeList.getMatchTypeShortForm(Globals.CurrentMatchType) + ".csv");
                            processNextButton();
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(getString(R.string.pre_already_scouted_button_negative), null)
                        .show();
            } else
                processNextButton();
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
        ArrayList<String> teamsInMatch;

        // See if this is a valid match number.  If not, default team list to "None" otherwise fill it
        if (Globals.MatchList.isCurrentMatchValid())
            teamsInMatch = Globals.MatchList.getListOfTeams();
        else {
            teamsInMatch = new ArrayList<>();
            teamsInMatch.add(getString(R.string.pre_dropdown_no_items));
        }

        // If there's an override set, add it to the spinner
        if (!Globals.CurrentTeamOverrideNum.isEmpty()) {
            if (teamsInMatch.size() == 1)
                teamsInMatch.set(0, Globals.CurrentTeamOverrideNum);
            else
                teamsInMatch.add(Globals.CurrentTeamOverrideNum);
        }

        // Create and apply the adapter to the spinner.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.cpr_spinner, teamsInMatch);
        adapter.setDropDownViewResource(R.layout.cpr_spinner_item);
        preMatchBinding.spinnerTeamToScout.setAdapter(adapter);

        // Choose the right team in the list to select.
        // If there's an override, we want that one and it'll be the last in the list
        // If there's a preferred position, choose it
        // Lastly, if there is no previously chosen one, set it to the first one
        if (!Globals.CurrentTeamOverrideNum.isEmpty()) {
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

    // =============================================================================================
    // Function:    isLogFileExisting
    // Description: Check if the Log file(s) already exist
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private boolean isLogFileExisting() {
        String filename = Globals.CurrentCompetitionId + "_" + Globals.CurrentMatchNumber + "_" + Globals.CurrentDeviceId + "_" + Globals.MatchTypeList.getMatchTypeShortForm(Globals.CurrentMatchType) + ".csv";

        return Globals.output_df.findFile(filename) != null;
    }

    // =============================================================================================
    // Function:    backupLogFile
    // Description: Rename the Log files so we keep a backup rather than losing the data altogether
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void backupLogFile(String in_filename) {
        String backupFilename;

        if (Globals.output_df.findFile(in_filename) != null) {
            for (int i = 1; ; ++i) {
                backupFilename = in_filename + "(" + i + ")";
                if (Globals.output_df.findFile(backupFilename) == null) break;
            }

            Objects.requireNonNull(Globals.output_df.findFile(in_filename)).renameTo(backupFilename);
        }
    }
}