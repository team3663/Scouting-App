package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.PostMatchBinding;
import com.team3663.scouting_app.utility.achievements.Achievements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class PostMatch extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private PostMatchBinding postMatchBinding;
    boolean[] selectedComment;
    //Creating an array list for the Comments
    ArrayList<Integer> CommentList = new ArrayList<>();
    ArrayList<String> CommentArray = Globals.CommentList.getDescriptionList();
    ArrayList<String> Accuracy = Globals.AccuracyTypeList.getDescriptionList();
    ArrayList<String> ClimbLevel = Globals.ClimbLevelList.getDescriptionList();
    ArrayList<String> ClimbPosition = Globals.ClimbPositionList.getDescriptionList();

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle in_savedInstanceState) {
        super.onCreate(in_savedInstanceState);
        EdgeToEdge.enable(this);
        postMatchBinding = PostMatchBinding.inflate(getLayoutInflater());
        setContentView(postMatchBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(postMatchBinding.postMatch, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize activity components
        initComments();
        initAccuracy();
        initClimbLevel();
        initClimbPosition();
        initStealFuel();
        initAffectedByDefense();
        initReset();
        initSubmit();
        initStats();
    }

    // =============================================================================================
    // Function:    initComments
    // Description: Initialize the Comments field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initComments() {
        // initialize comment reasons arrays
        selectedComment = new boolean[CommentArray.size()];

        String new_text = "0 " + getString(R.string.post_dropdown_items_selected);
        postMatchBinding.dropComments.setText(new_text);
        //code for how to open the dropdown menu when clicked and select items
        postMatchBinding.dropComments.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(PostMatch.this)
                .setTitle("Select All That Apply")
                .setCancelable(false)
                .setNeutralButton("Clear All", null);

            // Puts to comments from the array into the dropdown menu
            String[] CA = new String[CommentArray.size()];
            CommentArray.toArray(CA);
            builder.setMultiChoiceItems(CA, selectedComment, (dialogInterface, i, b) -> {
                // check condition
                if (b) {
                    // when checkbox selected
                    // Add position  in comment list
                    CommentList.add(i);
                    // Sort array list
                    Collections.sort(CommentList);
                } else {
                    // when checkbox unselected
                    // Remove position from comment list
                    CommentList.remove(Integer.valueOf(i));
                }
            });

            //adds the "ok" button to the dropdown menu
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                // Initialize string builder
                StringBuilder stringBuilder = new StringBuilder();
                // use for loop
                for (int j = 0; j < CommentList.size(); j++) {
                    // concat array value
                    stringBuilder.append(CommentArray.get(CommentList.get(j)));
                    // check condition
                    if (j != CommentList.size() - 1) {
                        // When j value  not equal
                        // to comment list size - 1
                        // add comma
                        stringBuilder.append(", ");
                    }
                }
                // set number of selected on CommentsTextView
                String new_text2 = CommentList.size() + " " + getString(R.string.post_dropdown_items_selected);
                postMatchBinding.dropComments.setText(new_text2);
            });

            //adds the "cancel" button to the dropdown menu
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
                // dismiss dialog
                dialogInterface.dismiss();
            });

            // show dialog
            final AlertDialog dialog = builder.create();
            dialog.show();

            //Overriding the handler for the neutral button
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                // remove all selection
                Arrays.fill(selectedComment, false);
                // clear comment list and uncheck entries
                CommentList.clear();
                for (int i = 0; i < dialog.getListView().getCount(); ++i) {
                    dialog.getListView().setItemChecked(i, false);
                }
            });
        });
    }

    // =============================================================================================
    // Function:    initAccuracy
    // Description: Initialize the Accuracy field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initAccuracy() {
        ArrayAdapter<String> adp_Accuracy = new ArrayAdapter<>(this, R.layout.cpr_spinner, Accuracy);
        adp_Accuracy.setDropDownViewResource(R.layout.cpr_spinner_item);
        postMatchBinding.spinnerAccuracy.setAdapter(adp_Accuracy);

        // Set starting selection
        int start_Pos = 0;
        for (int i = 0; i < Accuracy.size(); i++) {
            if (Accuracy.get(i).equals(Globals.AccuracyTypeList.getAccuracyDescription(Globals.CurrentAccuracy))) {
                start_Pos = i;
                break;
            }
        }
        postMatchBinding.spinnerAccuracy.setSelection(start_Pos);

        postMatchBinding.spinnerAccuracy.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                        int newAccuracy = Globals.AccuracyTypeList.getAccuracyValue(postMatchBinding.spinnerAccuracy.getSelectedItem().toString());

                        if (!Objects.equals(newAccuracy, Globals.CurrentAccuracy)) {
                            Globals.CurrentAccuracy = newAccuracy;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        Globals.CurrentAccuracy = Constants.PostMatch.ACCURACY_NOT_SELECTED;
                    }
                });
    }

    // =============================================================================================
    // Function:    initClimbLevel
    // Description: Initialize the Climb Level field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initClimbLevel() {
        ArrayAdapter<String> adp_ClimbLevel = new ArrayAdapter<>(this, R.layout.cpr_spinner, ClimbLevel);
        adp_ClimbLevel.setDropDownViewResource(R.layout.cpr_spinner_item);
        postMatchBinding.spinnerClimbLevel.setAdapter(adp_ClimbLevel);

        // Set starting selection
        int start_Pos = 0;
        for (int i = 0; i < ClimbLevel.size(); i++) {
            if (ClimbLevel.get(i).equals(Globals.ClimbLevelList.getClimbLevelDescription(Globals.CurrentClimbLevel))) {
                start_Pos = i;
                break;
            }
        }
        postMatchBinding.spinnerClimbLevel.setSelection(start_Pos);

        postMatchBinding.spinnerClimbLevel.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                        String newClimbLevel = Globals.ClimbLevelList.getClimbLevelValue(postMatchBinding.spinnerClimbLevel.getSelectedItem().toString());

                        if (!Objects.equals(newClimbLevel, Globals.CurrentClimbLevel)) {
                            Globals.CurrentClimbLevel = newClimbLevel;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        Globals.CurrentClimbLevel = "-1";
                    }
                });
    }


    // =============================================================================================
    // Function:    initClimbPosition
    // Description: Initialize the  field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initClimbPosition() {
        ArrayAdapter<String> adp_ClimbPosition = new ArrayAdapter<>(this, R.layout.cpr_spinner, ClimbPosition);
        adp_ClimbPosition.setDropDownViewResource(R.layout.cpr_spinner_item);
        postMatchBinding.spinnerClimbPosition.setAdapter(adp_ClimbPosition);

        // Set starting selection
        int start_Pos = 0;
        for (int i = 0; i < ClimbPosition.size(); i++) {
            if (ClimbPosition.get(i).equals(Globals.ClimbPositionList.getClimbPositionDescription(Globals.CurrentClimbPosition))) {
                start_Pos = i;
                break;
            }
        }
        postMatchBinding.spinnerClimbPosition.setSelection(start_Pos);

        // Set up a listener to handle any changes to the dropdown
        postMatchBinding.spinnerClimbPosition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                        String newClimbPosition = Globals.ClimbPositionList.getClimbPositionValue(postMatchBinding.spinnerClimbPosition.getSelectedItem().toString());

                        if (!Objects.equals(newClimbPosition, Globals.CurrentClimbPosition)) {
                            Globals.CurrentClimbPosition = newClimbPosition;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        Globals.CurrentClimbPosition = "-1";
                    }
            });
    }

    // =============================================================================================
    // Function:    initStealFuel
    // Description: Initialize the Steal Fuel field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initStealFuel() {
        RadioGroup stealFuelGroup = findViewById(R.id.radiogroup_StealFuel);

        stealFuelGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                Globals.stealFuelValue = "-1";
            } else if (checkedId == R.id.radiobutton_StealFuelYes) {
                Globals.stealFuelValue = "yes";
            } else if (checkedId == R.id.radiobutton_StealFuelNo) {
                Globals.stealFuelValue = "no";
            }
        });
    }

    // =============================================================================================
    // Function:    initAffectedByDefense
    // Description: Initialize the  field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initAffectedByDefense() {
        RadioGroup affectByDefenseGroup = findViewById(R.id.radiogroup_AffectedByDefense);

        affectByDefenseGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                Globals.affectedByDefenseValue = "-1";
            } else if (checkedId == R.id.radiobutton_NoDefense) {
                Globals.affectedByDefenseValue = "none";
            } else if (checkedId == R.id.radiobutton_Low) {
                Globals.affectedByDefenseValue = "low";
            } else if (checkedId == R.id.radiobutton_Medium) {
                Globals.affectedByDefenseValue = "medium";
            } else if (checkedId == R.id.radiobutton_High) {
                Globals.affectedByDefenseValue = "high";
            }
        });
    }

    // =============================================================================================
    // Function:    initReset
    // Description: Initialize the Reset Match field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initReset() {
        // Default values
        postMatchBinding.checkboxReset.setChecked(false);

        postMatchBinding.checkboxReset.setOnClickListener(view -> {
            if (postMatchBinding.checkboxReset.isChecked()) {
                postMatchBinding.butNext.setText(getString(R.string.post_but_reset));
            } else {
                postMatchBinding.butNext.setText(getString(R.string.post_but_submit));
            }
        });
    }

    // =============================================================================================
    // Function:    initSubmit
    // Description: Initialize the Submit button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initSubmit() {
        // Create a button for when you are done inputting info
        // finishes scouting the team and submits info
        postMatchBinding.butNext.setOnClickListener(view -> {
            // If we need to reset the match, abort it all and go back
            if (postMatchBinding.checkboxReset.isChecked()) {
                Globals.EventLogger.close();
                Achievements.data_FieldReset++;

                // Reset post-Match values if match is reset
                Globals.CurrentAccuracy = Constants.PostMatch.ACCURACY_NOT_SELECTED;
                Globals.CurrentClimbLevel = Constants.PostMatch.CLIMB_LEVEL_NOT_SELECTED;
                Globals.CurrentClimbPosition = Constants.PostMatch.CLIMB_POSITION_NOT_SELECTED;
                Globals.stealFuelValue = Constants.PostMatch.STEAL_FUEL_NOT_SELECTED;
                Globals.affectedByDefenseValue = Constants.PostMatch.AFFECTED_BY_DEFENSE_NOT_SELECTED;

                Intent GoToPreMatch = new Intent(PostMatch.this, PreMatch.class);
                startActivity(GoToPreMatch);
            } else {
                // Log all of the data from this page
                StringBuilder comment_sep_ID = new StringBuilder();
                for (Integer comment_dropID : CommentList) {
                    String comment = CommentArray.get(comment_dropID);
                    comment_sep_ID.append(":").append(Globals.CommentList.getCommentId(comment));
                }
                if (comment_sep_ID.length() > 0) comment_sep_ID = new StringBuilder(comment_sep_ID.substring(1));
                Globals.EventLogger.LogData(Constants.Logger.LOGKEY_COMMENTS, comment_sep_ID.toString());

                // If any spinner data is left blank
                if ((Constants.PostMatch.ACCURACY_NOT_SELECTED == Globals.CurrentAccuracy) ||
                        Constants.PostMatch.CLIMB_LEVEL_NOT_SELECTED.equals(Globals.CurrentClimbLevel) ||
                        Constants.PostMatch.CLIMB_POSITION_NOT_SELECTED.equals(Globals.CurrentClimbPosition)) {

                    Toast.makeText(this, R.string.post_missing_data, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    // Log all the spinner data
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_ACCURACY, String.valueOf(Globals.CurrentAccuracy));
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_CLIMB_LEVEL, String.valueOf(Globals.CurrentClimbLevel));
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_CLIMB_POSITION, String.valueOf(Globals.CurrentClimbPosition));
                }

                // If any radio button is left blank
                if (Objects.equals(Globals.stealFuelValue, "-1") || Objects.equals(Globals.affectedByDefenseValue, "-1")) {
                    Toast.makeText(this, R.string.post_missing_data, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    // Log all the spinner data
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_STEAL_FUEL,String.valueOf(Globals.stealFuelValue));
                    Globals.EventLogger.LogData(Constants.Logger.LOGKEY_AFFECTED_BY_DEFENSE, String.valueOf(Globals.affectedByDefenseValue));
                }

                Intent GoToSubmitData = new Intent(PostMatch.this, SubmitData.class);
                startActivity(GoToSubmitData);
            }

            Achievements.data_NumMatches++;
            Achievements.data_NumMatchesByCompetition.put(Globals.CurrentCompetitionId, Achievements.data_NumMatchesByCompetition.getOrDefault(Globals.CurrentCompetitionId, 0) + 1);
            if (Globals.MatchTypeList.getMatchTypeDescription(Globals.CurrentMatchType)
                    .startsWith(Constants.Achievements.EVENT_TYPE_PRACTICE)) Achievements.data_PracticeType++;
            if (Globals.MatchTypeList.getMatchTypeDescription(Globals.CurrentMatchType)
                    .startsWith(Constants.Achievements.EVENT_TYPE_SEMI)) Achievements.data_SemiFinalType++;
            if (Globals.MatchTypeList.getMatchTypeDescription(Globals.CurrentMatchType)
                    .startsWith(Constants.Achievements.EVENT_TYPE_FINAL)) Achievements.data_FinalType++;

            finish();

        });
    }

    // =============================================================================================
    // Function:    initStats
    // Description: Initialize the Stats field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initStats() {
        String statsFuelShot = "Fuel Shot: " + Achievements.data_match_FuelShot;
        String statsFuelPassed = "Fuel Passes: " + Achievements.data_match_FuelPassed;

        postMatchBinding.textStatsFuelShot.setText(statsFuelShot);
        postMatchBinding.textStatsFuelPassed.setText(statsFuelPassed);
    }
}