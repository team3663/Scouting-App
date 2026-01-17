package com.team3663.scouting_app.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
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

        SeekBar seekbar = findViewById(R.id.seekBar);
        seekbar.setHapticFeedbackEnabled(true);
        TextView textView = findViewById(R.id.textView);

        seekbar.setProgress(8);
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
}