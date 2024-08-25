package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Layout;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cpr3663.cpr_scouting_app.databinding.SettingsBinding;

public class Settings extends AppCompatActivity {
    // =============================================================================================
    // Define Constants
    // =============================================================================================


    // =============================================================================================
    // Global variables
    // =============================================================================================
    SettingsBinding settingsBinding;

    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Capture screen size. Need to use WindowManager to populate a Point that holds the screen size.
        Display screen = getWindowManager().getDefaultDisplay();
        Point screen_size = new Point();
        screen.getSize(screen_size);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        settingsBinding = SettingsBinding.inflate(getLayoutInflater());
        View page_root_view = settingsBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(settingsBinding.settings, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Define a Cancel Button
        Button but_Cancel = settingsBinding.butCancel;
        but_Cancel.setText(getResources().getString(R.string.button_start_match));
        but_Cancel.setTextSize(18F);
        but_Cancel.setTextColor(Color.WHITE);
        but_Cancel.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        ViewGroup.LayoutParams but_Cancel_LP = new ViewGroup.LayoutParams(300, 100);
        but_Cancel.setLayoutParams(but_Cancel_LP);
        but_Cancel.setX(screen_size.x - but_Cancel.getWidth());
        but_Cancel.setY(screen_size.y - but_Cancel.getHeight());
        but_Cancel.setBackgroundColor(Color.BLACK);

        but_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Exit();
            }
        });

        // Define a Save Button
        Button but_Save = settingsBinding.butSave;
        but_Save.setText("Save and Exit");
        but_Save.setTextSize(18F);
        but_Save.setTextColor(Color.WHITE);
        but_Save.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        ViewGroup.LayoutParams but_Save_LP = new ViewGroup.LayoutParams(300, 100);
        but_Save.setLayoutParams(but_Save_LP);
        but_Save.setX(but_Cancel.getX() - but_Save.getWidth());
        but_Save.setY(but_Cancel.getY() - but_Save.getHeight());
        but_Save.setBackgroundColor(Color.BLACK);

        but_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO record data
                Exit();
            }
        });
    }

    private void Exit() {
        Intent GoBackToLaunch = new Intent(Settings.this, AppLaunch.class);
        startActivity(GoBackToLaunch);
    }
}