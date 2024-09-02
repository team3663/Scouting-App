package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cpr3663.cpr_scouting_app.databinding.PostMatchBinding;

import java.util.ArrayList;
import java.util.Collections;

public class PostMatch extends AppCompatActivity {
    // =============================================================================================
    // Constants
    // =============================================================================================


    // =============================================================================================
    // Global variables
    // =============================================================================================
    private PostMatchBinding postMatchBinding;
    TextView textView;
    TextView DNPTextView;
    boolean[] selectedComment;
    boolean[] selectedDNPReasons;
    ArrayList<Integer> CommentList = new ArrayList<>();
    String[] CommentArray = {"Robot became disabled or stopped moving", "Robot (or part of it) broke",
            "Robot didn't contribute much (no auto, low scoring, no defense)", "Poor human player (source)",
            "Poor human player (amp)", "Robot got note(s) stuck in it"};
    ArrayList<Integer> DNPReasonsList = new ArrayList<>();
    String[] DNPReasonsArray = {"Fouled excessively", "Red/Yellow card", 
            "Never contributing to match", "no show", "e", "f"};

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        postMatchBinding = PostMatchBinding.inflate(getLayoutInflater());
        View page_root_view = postMatchBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(postMatchBinding.postMatch, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setContentView(R.layout.post_match);

        // assign variable
        textView = findViewById(R.id.textViewComments);
        DNPTextView = findViewById(R.id.textViewDNP);

        // initialize selected language array
        selectedComment = new boolean[CommentArray.length];
        selectedDNPReasons = new boolean[DNPReasonsArray.length];

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PostMatch.this);

                // set title
                builder.setTitle("Select All That Apply");


                // set dialog non cancelable
                builder.setCancelable(false);

                builder.setMultiChoiceItems(CommentArray, selectedComment, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // check condition
                        if (b) {
                            // when checkbox selected
                            // Add position  in lang list
                            CommentList.add(i);
                            // Sort array list
                            Collections.sort(CommentList);
                        } else {
                            // when checkbox unselected
                            // Remove position from langList
                            CommentList.remove(Integer.valueOf(i));
                        }
                    }
                });


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Initialize string builder
                        StringBuilder stringBuilder = new StringBuilder();
                        // use for loop
                        for (int j = 0; j < CommentList.size(); j++) {
                            // concat array value
                            stringBuilder.append(CommentArray[CommentList.get(j)]);
                            // check condition
                            if (j != CommentList.size() - 1) {
                                // When j value  not equal
                                // to lang list size - 1
                                // add comma
                                stringBuilder.append(", ");
                            }
                        }
                        // set text on textView
                        textView.setText(stringBuilder.toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dismiss dialog
                        dialogInterface.dismiss();
                    }
                });
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // use for loop
                        for (int j = 0; j < selectedComment.length; j++) {
                            // remove all selection
                            selectedComment[j] = false;
                            // clear language list
                            CommentList.clear();
                            // clear text view value
                            textView.setText("");
                        }
                    }
                });
                // show dialog
                builder.show();
            }
        });
        DNPTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(PostMatch.this);

                // set title
                builder.setTitle("Select Reason(s)");


                // set dialog non cancelable
                builder.setCancelable(false);

                builder.setMultiChoiceItems(DNPReasonsArray, selectedDNPReasons, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // check condition
                        if (b) {
                            // when checkbox selected
                            // Add position  in lang list
                            DNPReasonsList.add(i);
                            // Sort array list
                            Collections.sort(DNPReasonsList);
                        } else {
                            // when checkbox unselected
                            // Remove position from langList
                            DNPReasonsList.remove(Integer.valueOf(i));
                        }
                    }
                });


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Initialize string builder
                        StringBuilder stringBuilder = new StringBuilder();
                        // use for loop
                        for (int j = 0; j < DNPReasonsList.size(); j++) {
                            // concat array value
                            stringBuilder.append(DNPReasonsArray[DNPReasonsList.get(j)]);
                            // check condition
                            if (j != DNPReasonsList.size() - 1) {
                                // When j value  not equal
                                // to lang list size - 1
                                // add comma
                                stringBuilder.append(", ");
                            }
                        }
                        // set text on textView
                        DNPTextView.setText(stringBuilder.toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dismiss dialog
                        dialogInterface.dismiss();
                    }
                });
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // use for loop
                        for (int j = 0; j < selectedDNPReasons.length; j++) {
                            // remove all selection
                            selectedDNPReasons[j] = false;
                            // clear language list
                            DNPReasonsList.clear();
                            // clear text view value
                            DNPTextView.setText("");
                        }
                    }
                });
                // show dialog
                builder.show();

            }
        });


        // Create a button for when you are done inputting info
        Button but_Next = postMatchBinding.butNext;
        but_Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent GoToSubmitData = new Intent(PostMatch.this, SubmitData.class);
                startActivity(GoToSubmitData);
            }
        });
    }
}






