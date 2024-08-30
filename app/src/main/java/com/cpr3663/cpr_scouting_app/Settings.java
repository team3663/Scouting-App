package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Layout;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
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

        // Define a Label for the Competition
        TextView text_Competition = settingsBinding.textCompetition;
        text_Competition.setText(getResources().getString(R.string.settings_competition));
        text_Competition.setTextSize(20F);
        text_Competition.setTextColor(Color.BLACK);
        text_Competition.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        text_Competition.setX(0F);
        text_Competition.setY(0F);
        ViewGroup.LayoutParams text_Competition_LP = new ViewGroup.LayoutParams(300, 100);
        text_Competition.setLayoutParams(text_Competition_LP);
        text_Competition.setBackgroundColor(Color.TRANSPARENT);

        // Define the edit Text for entering the Competition Id
        EditText edit_CompetitionId = settingsBinding.editCompetitionId;
        edit_CompetitionId.setText(sp.getInt("CompetitionId", -1));
        edit_CompetitionId.setTextColor(Color.BLACK);
        edit_CompetitionId.setHint("Enter Competition ID Here:");
        edit_CompetitionId.setHintTextColor(Color.GRAY);
        edit_CompetitionId.setTextSize(20F);
        edit_CompetitionId.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        edit_CompetitionId.setX(0F);
        edit_CompetitionId.setY(0F);
        ViewGroup.LayoutParams edit_Competition_LP = new ViewGroup.LayoutParams(500, 200);
        edit_CompetitionId.setLayoutParams(edit_Competition_LP);
        edit_CompetitionId.setBackgroundColor(Color.TRANSPARENT);

        // Define a text box for the name of the Competition to appear in when you enter the ID
        TextView text_CompetitionName = settingsBinding.textCompetition;
        text_CompetitionName.setText("");
        text_CompetitionName.setTextSize(20F);
        text_CompetitionName.setTextColor(Color.BLACK);
        text_CompetitionName.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        text_CompetitionName.setX(0F);
        text_CompetitionName.setY(0F);
        ViewGroup.LayoutParams text_CompetitionName_LP = new ViewGroup.LayoutParams(300, 100);
        text_CompetitionName.setLayoutParams(text_CompetitionName_LP);
        text_CompetitionName.setBackgroundColor(Color.TRANSPARENT);

        // Define a Label for the Device
        TextView text_Device = settingsBinding.textDevice;
        text_Device.setText(getResources().getString(R.string.settings_device));
        text_Device.setTextSize(20F);
        text_Device.setTextColor(Color.BLACK);
        text_Device.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        text_Device.setX(0F);
        text_Device.setY(0F);
        ViewGroup.LayoutParams text_Device_LP = new ViewGroup.LayoutParams(300, 100);
        text_Device.setLayoutParams(text_Device_LP);
        text_Device.setBackgroundColor(Color.TRANSPARENT);

        // Define the edit Text for entering the Device Id
        EditText edit_DeviceId = settingsBinding.editDeviceId;
        edit_DeviceId.setText(sp.getInt("DeviceId", -1));
        edit_DeviceId.setTextColor(Color.BLACK);
        edit_DeviceId.setHint("Enter Device ID Here:");
        edit_DeviceId.setHintTextColor(Color.GRAY);
        edit_DeviceId.setTextSize(20F);
        edit_DeviceId.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        edit_DeviceId.setX(0F);
        edit_DeviceId.setY(0F);
        ViewGroup.LayoutParams edit_DeviceId_LP = new ViewGroup.LayoutParams(500, 200);
        edit_DeviceId.setLayoutParams(edit_DeviceId_LP);
        edit_DeviceId.setBackgroundColor(Color.TRANSPARENT);

        // Define a text box for the name of the Device to appear in when you enter the ID
        TextView text_DeviceName = settingsBinding.textDeviceName;
        text_DeviceName.setText("");
        text_DeviceName.setTextSize(20F);
        text_DeviceName.setTextColor(Color.BLACK);
        text_DeviceName.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        text_DeviceName.setX(0F);
        text_DeviceName.setY(0F);
        ViewGroup.LayoutParams text_DeviceName_LP = new ViewGroup.LayoutParams(300, 100);
        text_DeviceName.setLayoutParams(text_DeviceName_LP);
        text_DeviceName.setBackgroundColor(Color.TRANSPARENT);

        // Define a Cancel Button
        Button but_Cancel = settingsBinding.butCancel;
        but_Cancel.setText(getResources().getString(R.string.settings_cancel));
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
        but_Save.setText(getResources().getString(R.string.settings_save));
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
                String CompetitionId = String.valueOf(edit_DeviceId.getText());
                if (!CompetitionId.isEmpty()) {
                    spe.putInt("DeviceId", Integer.parseInt(CompetitionId));
                    spe.apply();
                }
                String DeviceId = String.valueOf(edit_DeviceId.getText());
                if (!DeviceId.isEmpty()) {
                    spe.putInt("DeviceId", Integer.parseInt(DeviceId));
                    spe.apply();
                }
                Exit();
            }
        });
    }

    private void Exit() {
        Intent GoBackToLaunch = new Intent(Settings.this, AppLaunch.class);
        startActivity(GoBackToLaunch);
    }
}