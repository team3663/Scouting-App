package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cpr3663.cpr_scouting_app.databinding.PreMatchBinding;

public class PreMatch extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private PreMatchBinding preMatchBinding;
    // To store the inputted name
    protected static String ScouterName;
    protected static int MatchNum = -1;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        preMatchBinding = PreMatchBinding.inflate(getLayoutInflater());
        View page_root_view = preMatchBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(preMatchBinding.preMatch, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Create components
        EditText edit_Match = preMatchBinding.editMatch;
        TextView text_Match = preMatchBinding.textMatch;
        EditText edit_Team = preMatchBinding.editTeamToScout;
        TextView text_Team = preMatchBinding.textTeamToScout;
        TextView text_TeamName = preMatchBinding.textTeamToScoutName;

        // creates the single select menu for the robot starting positions
//        Spinner spinner = findViewById(R.id.spinnerStartingPosition);

        // adds the items from the starting positions array to the list
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.starting_positions_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);

        EditText edit_Name = preMatchBinding.editScouterName;
        CheckBox checkbox_DidPlay = preMatchBinding.checkboxDidPlay;
        CheckBox checkbox_Override = preMatchBinding.checkboxOverride;
        TextView text_Override = preMatchBinding.textOverride;
        EditText edit_OverrideTeamNum = preMatchBinding.editOverrideTeamNum;
        Button but_AddOverrideTeamNum = preMatchBinding.butAddOverrideTeamNum;

        // Since we are putting the checkbox on the RIGHT side of the text, the checkbox doesn't honor padding.
        // So we need to use 7 spaces, but you can't when using a string resource (it ignores the trailing spaces)
        // So add it in now.
        checkbox_DidPlay.setText(preMatchBinding.checkboxDidPlay.getText() + Globals.CheckBoxTextPadding);

        // Create a text box to input the scouters name
        edit_Name.setText(ScouterName);
        edit_Name.setHint("Input your name");
        edit_Name.setHintTextColor(Color.WHITE);

        MatchNum++;
        if (MatchNum > 0) {
            // MUST CONVERT TO STRING or it crashes with out warning
            edit_Match.setText(String.valueOf(MatchNum));
            Matches.MatchRow Match = Globals.MatchList.getMatchInfoRow(MatchNum);
            if (Match != null) {
                int[] Teams = Match.getListOfTeams();
                for (int team : Teams)
                    ; // TODO Add "team" to the options in the single select dropdown
            }
        } else edit_Match.setText("");
        edit_Match.setHint("Input the match number");
        edit_Match.setHintTextColor(Color.GRAY);

        // Default them to playing
        checkbox_DidPlay.setChecked(true);

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
                    // TODO make it add teamNum to the options after converting to int and
                    //      have it auto select that one
                }
                checkbox_Override.setChecked(false);
                text_Override.setVisibility(View.INVISIBLE);
                edit_OverrideTeamNum.setVisibility(View.INVISIBLE);
                but_AddOverrideTeamNum.setVisibility(View.INVISIBLE);
            }
        });

        // Create a button for when you are done inputting info
        Button but_SubmitPage = preMatchBinding.butNext;
        but_SubmitPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check we have all the fields entered that are needed.  Otherwise, pop a TOAST message instead
                if (String.valueOf(edit_Match.getText()).isEmpty() || String.valueOf(edit_Team.getText()).isEmpty() || String.valueOf(edit_Name.getText()).isEmpty()) {
                    Toast.makeText(PreMatch.this, R.string.missing_data, Toast.LENGTH_SHORT).show();
                } else {
                    ScouterName = String.valueOf(edit_Name.getText());
                    MatchNum = Integer.parseInt(String.valueOf(edit_Match.getText()));
                    // If they didn't play skip everything else
                    if (preMatchBinding.checkboxDidPlay.isChecked()) {
                        // TODO log here
                        Intent GoToMatch = new Intent(PreMatch.this, Match.class);
                        startActivity(GoToMatch);
                    } else {
                        // TODO log here
                        Intent GoToSubmitData = new Intent(PreMatch.this, SubmitData.class);
                        startActivity(GoToSubmitData);
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
                            // TODO Add "team" to the options in the single select dropdown
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