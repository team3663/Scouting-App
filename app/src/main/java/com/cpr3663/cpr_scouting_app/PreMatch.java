package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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

        // Create components
        EditText edit_Match = preMatchBinding.editMatch;
        TextView text_Match = preMatchBinding.textMatch;
        EditText edit_Team = preMatchBinding.editTeamToScout;
        TextView text_Team = preMatchBinding.textTeamToScout;
        EditText edit_Name = preMatchBinding.editScouterName;

        // Since we are putting the checkbox on the RIGHT side of the text, the checkbox doesn't honor padding.
        // So we need to use 7 spaces, but you can't when using a string resource (it ignores the trailing spaces)
        // So add it in now.
        preMatchBinding.checkboxDidPlay.setText(preMatchBinding.checkboxDidPlay.getText() + Globals.CheckBoxTextPadding);

        // Create a input text box for the scouter name
        edit_Name.setText(NAME_SCOUTER);
        edit_Name.setHint("Input your name");
        edit_Name.setHintTextColor(Color.GRAY);

        // Default them to playing
        preMatchBinding.checkboxDidPlay.setChecked(true);

        // Hide override components initially
        preMatchBinding.textOverride.setVisibility(View.INVISIBLE);
        preMatchBinding.editOverrideTeamNum.setVisibility(View.INVISIBLE);
        preMatchBinding.butAddOverrideTeamNum.setVisibility(View.INVISIBLE);

        CheckBox check_Override = preMatchBinding.checkboxOverride;
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
                // Check we have all the fields entered that are needed.  Otherwise, pop a TOAST message instead
                if (String.valueOf(edit_Match.getText()).isEmpty() || String.valueOf(edit_Team.getText()).isEmpty() || String.valueOf(edit_Name.getText()).isEmpty()) {
                    Toast.makeText(PreMatch.this, R.string.missing_data, Toast.LENGTH_SHORT).show();
                } else {
                    NAME_SCOUTER = String.valueOf(edit_Name.getText());
                    // If they didn't play skip everything else
                    if (preMatchBinding.checkboxDidPlay.isChecked()) {
                        Intent GoToMatch = new Intent(PreMatch.this, Match.class);
                        startActivity(GoToMatch);
                    } else {
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
                            for (int team : Teams) ; // TODO Add "team" to the options in the single select dropdown
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
                            text_Team.setText(ScoutingTeamName);
                        } else {
                            text_Team.setText("");
                        }
                    }
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