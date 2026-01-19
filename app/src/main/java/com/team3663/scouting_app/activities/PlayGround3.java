package com.team3663.scouting_app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.databinding.Playground3Binding;

public class PlayGround3 extends AppCompatActivity {

    private Playground3Binding playgroundBinding;
    private int tens = 0;
    private int ones = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        playgroundBinding = Playground3Binding.inflate(getLayoutInflater());
        View page_root_view = playgroundBinding.getRoot();
        setContentView(page_root_view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initQuit();
        initNext();
        initButtons();
        initTap();

        playgroundBinding.textView.setText(String.valueOf(0));
    }

    // =============================================================================================
    // Function:    initQuit
    // Description: Initialize the Quit button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initQuit() {
        playgroundBinding.butQuit.setOnClickListener(view -> new AlertDialog.Builder(view.getContext())
                .setTitle(getString(R.string.submit_alert_quit_title))
                .setMessage(getString(R.string.submit_alert_quit_message))

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(getString(R.string.submit_alert_quit_positive), (dialog, which) -> {
                    PlayGround3.this.finishAffinity();
                    System.exit(0);
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(getString(R.string.submit_alert_cancel), null)
                .show()
        );
    }

    // =============================================================================================
    // Function:    initNext
    // Description: Initialize the Next button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initNext() {
        Button but_Next = playgroundBinding.butNext;
        but_Next.setOnClickListener(view -> {
            Intent GoToSubmitData = new Intent(PlayGround3.this, PlayGround.class);
            startActivity(GoToSubmitData);

            finish();
        });
    }

    // =============================================================================================
    // Function:    initTap
    // Description: Initialize the Tap button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initTap() {
        Button but_Tap = playgroundBinding.butTap;
        but_Tap.setOnClickListener(view -> {
            playgroundBinding.textView.setText(String.valueOf(Integer.valueOf(playgroundBinding.textView.getText().toString())+1));
        });
    }

    // =============================================================================================
    // Function:    initButtons
    // Description: Initialize the Number buttons
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initButtons() {
        initButton(playgroundBinding.but90, 90);
        initButton(playgroundBinding.but80, 80);
        initButton(playgroundBinding.but70, 70);
        initButton(playgroundBinding.but60, 60);
        initButton(playgroundBinding.but50, 50);
        initButton(playgroundBinding.but40, 40);
        initButton(playgroundBinding.but30, 30);
        initButton(playgroundBinding.but20, 20);
        initButton(playgroundBinding.but10, 10);
        initButton(playgroundBinding.but100, 100);
        initButton(playgroundBinding.but9, 9);
        initButton(playgroundBinding.but8, 8);
        initButton(playgroundBinding.but7, 7);
        initButton(playgroundBinding.but6, 6);
        initButton(playgroundBinding.but5, 5);
        initButton(playgroundBinding.but4, 4);
        initButton(playgroundBinding.but3, 3);
        initButton(playgroundBinding.but2, 2);
        initButton(playgroundBinding.but1, 1);
        initButton(playgroundBinding.but0, 0);
    }

    // =============================================================================================
    // Function:    initButton
    // Description: Initialize an individual buttons
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initButton(Button in_button, int in_id) {
        in_button.setBackgroundColor(getColor(R.color.dark_grey));

        in_button.setId(in_id);
        in_button.setOnClickListener(view1 -> processButton(in_button));
    }

    // =============================================================================================
    // Function:    processButton
    // Description: Process a button click
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void processButton(Button in_button) {
        int in_id = in_button.getId();

        if (in_id > 9) {
            if (in_id > 90) tens = 0;
            else tens = in_id;
        }
        else ones = in_id;

        in_button.setBackgroundColor(getColor(R.color.cpr_green));
        playgroundBinding.textView.setText(String.valueOf(tens + ones));
    }
}