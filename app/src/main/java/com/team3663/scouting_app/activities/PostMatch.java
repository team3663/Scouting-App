package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

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

import java.util.ArrayList;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        postMatchBinding = PostMatchBinding.inflate(getLayoutInflater());
        setContentView(postMatchBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(postMatchBinding.postMatch, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Default values
        postMatchBinding.checkboxDidLeave.setChecked(true);
        postMatchBinding.checkboxReset.setChecked(false);

        //Creating the single select dropdown menu for the trap outcomes
        Spinner spinner_Trap = findViewById(R.id.spinnerTrap);
        //accessing the array in strings.xml
        ArrayAdapter<String> adp_Trap = new ArrayAdapter<String>(this,
                R.layout.cpr_spinner, Globals.TrapResultsList.getDescriptionList());
        adp_Trap.setDropDownViewResource(R.layout.cpr_spinner_item);
        spinner_Trap.setAdapter(adp_Trap);

        //Creating the single select dropdown menu for the climb positions
        Spinner spinner_ClimbPos = findViewById(R.id.spinnerClimbPosition);
        //accessing the array in strings.xml
        ArrayAdapter<String> adp_ClimbPos = new ArrayAdapter<String> (this,
                R.layout.cpr_spinner, Globals.ClimbPositionList.getDescriptionList());
        adp_ClimbPos.setDropDownViewResource(R.layout.cpr_spinner_item);
        spinner_ClimbPos.setAdapter(adp_ClimbPos);

        // initialize comment reasons arrays
        selectedComment = new boolean[CommentArray.size()];

        postMatchBinding.dropComments.setText("0 " + getString(R.string.post_dropdown_items_selected));
        //code for how to open the dropdown menu when clicked and select items
        postMatchBinding.dropComments.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(PostMatch.this);

            // set title for the dropdown menu
            builder.setTitle("Select All That Apply");

            // set dialog non cancelable
            builder.setCancelable(false);

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
                postMatchBinding.dropComments.setText(CommentList.size() + " " + getString(R.string.post_dropdown_items_selected));
            });

            //adds the "cancel" button to the dropdown menu
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
                // dismiss dialog
                dialogInterface.dismiss();
            });

            //adds the "clear all" button to the dropdown menu
            // to clear all previously selected items
            builder.setNeutralButton("Clear All", (dialogInterface, i) -> {
                // use for loop
                for (int j = 0; j < selectedComment.length; j++) {
                    // remove all selection
                    selectedComment[j] = false;
                    // clear comment list
                    CommentList.clear();
                    // clear text view value
                    postMatchBinding.dropComments.setText("0 " + getString(R.string.post_dropdown_items_selected));
                }
            });
            // show dialog
            builder.show();
        });

        // Since we are putting the checkbox on the RIGHT side of the text, the checkbox doesn't honor padding.
        // So we need to use 7 spaces, but you can't when using a string resource (it ignores the trailing spaces)
        // So add it in now.
        postMatchBinding.checkboxDidLeave.setText(postMatchBinding.checkboxDidLeave.getText() + Globals.CheckBoxTextPadding);

        // Create a button for when you are done inputting info
        // finishes scouting the team and submits info
        Button but_Next = postMatchBinding.butNext;
        but_Next.setOnClickListener(view -> {
            // If we need to reset the match, abort it all and go back
            if (postMatchBinding.checkboxReset.isChecked()) {
                Globals.EventLogger.clear();

                Intent GoToPreMatch = new Intent(PostMatch.this, PreMatch.class);
                startActivity(GoToPreMatch);
            } else {
                // Log all of the data from this page
                Globals.EventLogger.LogData(Constants.Logger.LOGKEY_DID_LEAVE_START, String.valueOf(postMatchBinding.checkboxDidLeave.isChecked()));
                Globals.EventLogger.LogData(Constants.Logger.LOGKEY_CLIMB_POSITION, String.valueOf(Globals.ClimbPositionList.getClimbPositionId(postMatchBinding.spinnerClimbPosition.getSelectedItem().toString())));
                Globals.EventLogger.LogData(Constants.Logger.LOGKEY_TRAP, String.valueOf(Globals.TrapResultsList.getTrapResultId(postMatchBinding.spinnerTrap.getSelectedItem().toString())));
                String comment_sep_ID = "";
                for (Integer comment_dropID : CommentList) {
                    String comment = CommentArray.get(comment_dropID);
                    comment_sep_ID += ":" + Globals.CommentList.getCommentId(comment);
                }
                if (!comment_sep_ID.isEmpty()) comment_sep_ID = comment_sep_ID.substring(1);
                Globals.EventLogger.LogData(Constants.Logger.LOGKEY_COMMENTS, comment_sep_ID);

                // We're done with the logger
                Globals.EventLogger.close();
                Globals.EventLogger = null;

                // Increases the team number so that it auto fills for the next match correctly
                //  and do it after the logger is closed so that this can't mess the logger up
                Globals.CurrentMatchNumber++;

                // Reset the Saved Start position so that you have to choose it again
                Globals.CurrentStartPosition = 0;

                Intent GoToSubmitData = new Intent(PostMatch.this, SubmitData.class);
                startActivity(GoToSubmitData);
            }

            finish();
        });

        postMatchBinding.checkboxReset.setOnClickListener(view -> {
            if (postMatchBinding.checkboxReset.isChecked()) {
                postMatchBinding.butNext.setText(getString(R.string.post_but_reset));
            } else {
                postMatchBinding.butNext.setText(getString(R.string.post_but_submit));
            }
        });
    }
}