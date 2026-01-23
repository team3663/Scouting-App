package com.team3663.scouting_app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.databinding.PlaygroundBinding;

public class PlayGround extends AppCompatActivity {

    private PlaygroundBinding playgroundBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        playgroundBinding = PlaygroundBinding.inflate(getLayoutInflater());
        View page_root_view = playgroundBinding.getRoot();
        setContentView(page_root_view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initQuit();
        initNext();
        initTap();
        initSeekBar();
        initTeleAllianceZone();
        initTeleNeutralZone();
        initTeleOpponentZone();

        playgroundBinding.imageFieldView.bringToFront();
        playgroundBinding.textRobot.bringToFront();
        playgroundBinding.textPractice.bringToFront();
        playgroundBinding.butAllianceZone.bringToFront();
        playgroundBinding.butNeutralZone.bringToFront();
        playgroundBinding.butOpponentZone.bringToFront();
        playgroundBinding.imageFieldView.invalidate();
        playgroundBinding.imageFieldView.requestLayout();
    }

    // =============================================================================================
    // Function:    initSeekBar
    // Description: Initialize the SeekBar
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initSeekBar() {
        SeekBar seekbar = findViewById(R.id.seekBar);
        TextView textView = findViewById(R.id.textView);

        seekbar.setProgress(8);
        seekbar.setMax(60);
        textView.setText(String.valueOf(seekbar.getProgress()));

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(progress));

            }
        });
    }

    // =============================================================================================
    // Function:    initTeleAllianceZone
    // Description: Initialize the Teleop Alliance Zone selection
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initTeleAllianceZone() {
        Button but_Zone = playgroundBinding.butAllianceZone;

        but_Zone.setOnClickListener(view -> {
            playgroundBinding.butAllianceZone.setBackgroundColor(getColor(R.color.transparent_orange));
            playgroundBinding.butNeutralZone.setBackgroundColor(getColor(R.color.transparent));
            playgroundBinding.butOpponentZone.setBackgroundColor(getColor(R.color.transparent));
        });
    }

    // =============================================================================================
    // Function:    initTeleNeutralZone
    // Description: Initialize the Teleop Neutral Zone selection
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initTeleNeutralZone() {
        Button but_Zone = playgroundBinding.butNeutralZone;

        but_Zone.setOnClickListener(view -> {
            playgroundBinding.butAllianceZone.setBackgroundColor(getColor(R.color.transparent));
            playgroundBinding.butNeutralZone.setBackgroundColor(getColor(R.color.transparent_orange));
            playgroundBinding.butOpponentZone.setBackgroundColor(getColor(R.color.transparent));
        });
    }

    // =============================================================================================
    // Function:    initTeleOpponentZone
    // Description: Initialize the Teleop Opponent Zone selection
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initTeleOpponentZone() {
        Button but_Zone = playgroundBinding.butOpponentZone;

        but_Zone.setOnClickListener(view -> {
            playgroundBinding.butAllianceZone.setBackgroundColor(getColor(R.color.transparent));
            playgroundBinding.butNeutralZone.setBackgroundColor(getColor(R.color.transparent));
            playgroundBinding.butOpponentZone.setBackgroundColor(getColor(R.color.transparent_orange));
        });
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
                    PlayGround.this.finishAffinity();
                    System.exit(0);
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(getString(R.string.submit_alert_cancel), null)
                .show()
        );
    }

    // =============================================================================================
    // Function:    initTap
    // Description: Initialize the Tap button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initTap() {
        Button but_Tap = playgroundBinding.butShootTap;
        but_Tap.setOnClickListener(view -> {
            playgroundBinding.textView.setText(String.valueOf(Integer.valueOf(playgroundBinding.textView.getText().toString())+1));
        });
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
                Intent GoToSubmitData = new Intent(PlayGround.this, PlayGround2.class);
                startActivity(GoToSubmitData);

                finish();
            });
    }
}