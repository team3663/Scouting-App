package com.team3663.scouting_app.activities;

import static java.lang.Math.atan2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.databinding.Playground2Binding;

public class PlayGround2 extends AppCompatActivity {

    private Playground2Binding playgroundBinding;
    float centerX;
    float centerY;
    float touchX;
    float touchY;
    int angle;
    double positiveAngle;
    int numSectors;
    int startAngle;
    int endAngle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        playgroundBinding = Playground2Binding.inflate(getLayoutInflater());
        View page_root_view = playgroundBinding.getRoot();
        setContentView(page_root_view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initQuit();
        initNext();

        SeekBar seekbar = findViewById(R.id.seekBar);
        seekbar.setHapticFeedbackEnabled(true);
        TextView textView = findViewById(R.id.textView);
        ImageView imageView = findViewById(R.id.imageView);

        seekbar.setProgress(8);
        seekbar.setMax(60);
        textView.setText(String.valueOf(seekbar.getProgress()));

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Handle the initial press
                        int[] location = new int[2];
                        imageView.getLocationOnScreen(location);


                        centerX = (float) playgroundBinding.imageView.getWidth() / 2;
                        centerY = (float) playgroundBinding.imageView.getHeight() / 2;
                        touchX = event.getX();
                        touchY = event.getY();
                        angle = (int) Math.toDegrees(atan2(touchY - centerY, touchX - centerX));
                        positiveAngle = (angle + 90 + 360) % 360;
                        textView.setText(String.valueOf(positiveAngle));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // Handle movement
                        touchX = event.getX();
                        touchY = event.getY();
                        angle = (int) Math.toDegrees(atan2(touchY - centerY, touchX - centerX));
                        positiveAngle = (angle + 90 + 360) % 360;
                        numSectors = 10;
                        for (int i = 0; i < 10; i++) {
                            startAngle = i * 36;
                            endAngle = (i + 1) * 36;
                            if (startAngle )
                        }
                        textView.setText(String.valueOf(numSectors));
                        break;
                    case MotionEvent.ACTION_UP:
                        // Handle release
                        textView.setText("up");
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        // Handle cancellation
                        textView.setText("cancel");
                        break;
                }

                // Return true to indicate that you have handled the event
                return true;
            }
        });
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
                    PlayGround2.this.finishAffinity();
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
            Intent GoToSubmitData = new Intent(PlayGround2.this, PlayGround3.class);
            startActivity(GoToSubmitData);

            finish();
        });
    }
}