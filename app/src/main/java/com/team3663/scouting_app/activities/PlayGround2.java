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
    float angle;
    float positiveAngle;
    int direction;
    int lastSector;
    float lastPositiveAngle;
    int tens;
    int sector;
    int textValue;

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

        TextView textView = findViewById(R.id.textView);
        ImageView imageView = findViewById(R.id.imageView);

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
                        angle = (float) Math.toDegrees(atan2(touchY - centerY, touchX - centerX));
                        positiveAngle = (angle + 90 + 360) % 360;
                        sector = (int) positiveAngle / 36;
                        if (lastPositiveAngle > 350 && positiveAngle < 10) {
                            direction = 1;
                        }
                        else if (positiveAngle > lastPositiveAngle) {
                            direction = 1;
                        }
                        else {
                            direction = -1;
                        }
                        if (sector == 0 && direction == 1 && lastSector != 0) {
                            tens ++;
                        }
                        else if (sector == 9 && lastSector != 9 && direction == -1) {
                            if (tens >= 1) {
                                tens --;
                            }
                            else {
                                tens = 0;
                            }
                        }
                        textValue = tens * 10 + sector;
                        textView.setText(String.valueOf(textValue));
                        lastSector = sector;
                        lastPositiveAngle = (int) positiveAngle;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // Handle movement
                        touchX = event.getX();
                        touchY = event.getY();
                        angle = (float) Math.toDegrees(atan2(touchY - centerY, touchX - centerX));
                        positiveAngle = (angle + 90 + 360) % 360;
                        sector = (int) positiveAngle / 36;
                        if (lastPositiveAngle > 350 && positiveAngle < 10) {
                            direction = 1;
                        }
                        else if (positiveAngle > lastPositiveAngle) {
                            direction = 1;
                        }
                        else {
                            direction = -1;
                        }
                        if (sector == 0 && direction == 1 && lastSector != 0) {
                            tens ++;
                        }
                        else if (sector == 9 && lastSector != 9 && direction == -1) {
                            if (tens >= 1) {
                                tens --;
                            }
                            else {
                                tens = 0;
                            }
                        }
                        textValue = tens * 10 + sector;
                        textView.setText(String.valueOf(textValue));
                        lastSector = sector;
                        lastPositiveAngle = (int) positiveAngle;
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

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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