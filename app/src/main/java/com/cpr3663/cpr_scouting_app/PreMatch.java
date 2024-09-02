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
    public static String NAME_SCOUTER;

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

        // creates the single select menu for the robot starting positions
        Spinner spinner=findViewById(R.id.spinnerStartingPosition);

        // adds the items from the starting positions array to the list
        ArrayAdapter<CharSequence> adapter= ArrayAdapter.createFromResource(this,R.array.starting_positions_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Create a input text box for the scouter name
        EditText edit_Name = preMatchBinding.editScouterName;
        edit_Name.setText(NAME_SCOUTER);
        edit_Name.setHint("Input your name");
        edit_Name.setHintTextColor(Color.WHITE);

        // Defualt them to playing
        preMatchBinding.didPlay.setChecked(true);

        CheckBox check_Override = preMatchBinding.checkOverride;
        check_Override.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int state = View.VISIBLE;
                if (!check_Override.isChecked()) state = View.INVISIBLE;
                preMatchBinding.textOverride.setVisibility(state);
                preMatchBinding.editOverrideTeamNum.setVisibility(state);
                preMatchBinding.butAddOverrideTeamNum.setVisibility(state);
            }
        });

        Button but_AddTeamNum = preMatchBinding.butAddOverrideTeamNum;
        but_AddTeamNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String teamNum = String.valueOf(preMatchBinding.editOverrideTeamNum.getText());
                if (!teamNum.isEmpty()) {
                    // TODO make it add teamNum to the options after converting to int and
                    //      have it auto select that one
                }
                check_Override.setChecked(false);
                preMatchBinding.textOverride.setVisibility(View.INVISIBLE);
                preMatchBinding.editOverrideTeamNum.setVisibility(View.INVISIBLE);
                preMatchBinding.butAddOverrideTeamNum.setVisibility(View.INVISIBLE);
            }
        });

        // Create a button for when you are done inputting info
        Button but_SubmitPage = preMatchBinding.butNext;
        but_SubmitPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NAME_SCOUTER = String.valueOf(edit_Name.getText());
                // If they didn't play skip everything else
                if (preMatchBinding.didPlay.isChecked()) {
                    Intent GoToMatch = new Intent(PreMatch.this, Match.class);
                    startActivity(GoToMatch);
                } else {
                    Intent GoToSubmitData = new Intent(PreMatch.this, SubmitData.class);
                    startActivity(GoToSubmitData);
                }
            }
        });

        // Create an EditText for entering the match your on
        EditText edit_Match = preMatchBinding.editMatch;

        // Create an EditText for entering the team you are scouting
        EditText edit_TeamToScout = preMatchBinding.editTeamToScout;

        // Create a text box for the name of the team your scouting to appear in
        TextView text_TeamToScoutName = preMatchBinding.textTeamToScoutName;

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
                            for (int team : Teams) ; // TODO Add "team" to the options in the single select dropdown
                        }
                    }
                }
            }
        });

        edit_TeamToScout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (!focus) {
                    String TeamToScoutStr = String.valueOf(edit_TeamToScout.getText());
                    if (!TeamToScoutStr.isEmpty()) {
                        int TeamToScout = Integer.parseInt(TeamToScoutStr);
                        if (TeamToScout > 0 && TeamToScout < Globals.TeamList.size()) {
                            // This will crash the app instead of returning null if you pass it an invalid num
                            String ScoutingTeamName = Globals.TeamList.get(TeamToScout);
                            text_TeamToScoutName.setText(ScoutingTeamName);
                        } else {
                            text_TeamToScoutName.setText("");
                        }
                    }
                }
            }
        });
    }
}