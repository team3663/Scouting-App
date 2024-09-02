package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    SharedPreferences sp;
    SharedPreferences.Editor spe;

    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility", "ResourceType"})
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

        sp = this.getSharedPreferences(getResources().getString(R.string.preference_setting_file_key), Context.MODE_PRIVATE);
        spe = sp.edit();

        // Define the edit Text for entering the Competition Id
        EditText edit_CompetitionId = settingsBinding.editCompetitionId;
        // Make sure you convert it to a String it won't warn you or give an error about it needing to be but it will crash
        edit_CompetitionId.setText(String.valueOf(sp.getInt("CompetitionId", -1)));

        // Define a text box for the name of the Competition to appear in when you enter the ID
        TextView text_CompetitionName = settingsBinding.textCompetitionName;
        text_CompetitionName.setText("");

        // Define the edit Text for entering the Device Id
        EditText edit_DeviceId = settingsBinding.editDeviceId;
        // Make sure you convert it to a String it won't warn you or give an error about it needing to be but it will crash
        edit_DeviceId.setText(String.valueOf(sp.getInt("DeviceId", -1)));

        // Define a text box for the name of the Device to appear in when you enter the ID
        TextView text_DeviceName = settingsBinding.textDeviceName;
        text_DeviceName.setText("");

        // Define a Cancel Button
        Button but_Cancel = settingsBinding.butCancel;

        but_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Exit();
            }
        });

        // Define a Save Button
        Button but_Save = settingsBinding.butSave;

        but_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String CompetitionId = String.valueOf(edit_CompetitionId.getText());
                if (!CompetitionId.isEmpty()) {
                    spe.putInt("CompetitionId", Integer.parseInt(CompetitionId));
                }
                String DeviceId = String.valueOf(edit_DeviceId.getText());
                if (!DeviceId.isEmpty()) {
                    spe.putInt("DeviceId", Integer.parseInt(DeviceId));
                }
                spe.apply();
                Exit();
            }
        });
    }

    private void Exit() {
        Intent GoBackToLaunch = new Intent(Settings.this, AppLaunch.class);
        startActivity(GoBackToLaunch);
    }
}