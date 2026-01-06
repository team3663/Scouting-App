package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

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

public class PostMatch extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private PostMatchBinding postMatchBinding;
    boolean[] selectedComment;
    //Creating an array list for the Comments
    ArrayList<Integer> CommentList = new ArrayList<>();
    ArrayList<String> CommentArray = Globals.CommentList.getDescriptionList();

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
        initDidLeave();
        initComments();
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
    // Function:    initDidLeave
    // Description: Initialize the Did Leave field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initDidLeave() {
        // Default values
        postMatchBinding.checkboxDidLeave.setChecked(true);

        // Since we are putting the checkbox on the RIGHT side of the text, the checkbox doesn't honor padding.
        // So we need to use 7 spaces, but you can't when using a string resource (it ignores the trailing spaces)
        // So add it in now.
        String new_text = postMatchBinding.checkboxDidLeave.getText() + Globals.CheckBoxTextPadding;
        postMatchBinding.checkboxDidLeave.setText(new_text);
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
                Globals.CurrentTeamOverrideNum = "";

                Intent GoToPreMatch = new Intent(PostMatch.this, PreMatch.class);
                startActivity(GoToPreMatch);
            } else {
                // Log all of the data from this page
                Globals.EventLogger.LogData(Constants.Logger.LOGKEY_DID_LEAVE_START, String.valueOf(postMatchBinding.checkboxDidLeave.isChecked()));
                StringBuilder comment_sep_ID = new StringBuilder();
                for (Integer comment_dropID : CommentList) {
                    String comment = CommentArray.get(comment_dropID);
                    comment_sep_ID.append(":").append(Globals.CommentList.getCommentId(comment));
                }
                if (comment_sep_ID.length() > 0) comment_sep_ID = new StringBuilder(comment_sep_ID.substring(1));
                Globals.EventLogger.LogData(Constants.Logger.LOGKEY_COMMENTS, comment_sep_ID.toString());

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
        StringBuilder statsCoral = new StringBuilder();
        StringBuilder statsAlgae = new StringBuilder();

        for (int i = 4; i >0; --i) {
            statsCoral.append("Placed L").append(i).append(": ").append(Achievements.data_match_CoralLevel[i]).append("\n");
        }
        statsCoral.append("Dropped: ").append(Achievements.data_match_CoralDropped);

        statsAlgae.append("Net: ").append(Achievements.data_match_AlgaeInNet).append("\n")
            .append("Processor: ").append(Achievements.data_match_AlgaeInProcessor);

        postMatchBinding.textStatsCoral.setText(statsCoral.toString());
        postMatchBinding.textStatsAlgae.setText(statsAlgae.toString());
        //use achievement data
    }
}