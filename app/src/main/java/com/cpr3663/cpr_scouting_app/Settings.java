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
        // MUST CONVERT TO STRING or it crashes with out warning
        edit_CompetitionId.setText(String.valueOf(sp.getInt("CompetitionId", -1)));

        // Define a text box for the name of the Competition to appear in when you enter the ID
        TextView text_CompetitionName = settingsBinding.textCompetitionName;
        String compIdStr = String.valueOf(edit_CompetitionId.getText());
        if (!compIdStr.isEmpty()) {
            int compId = Integer.parseInt(compIdStr);
            Competitions.CompetitionRow competition = Globals.CompetitionList.getCompetitionRow(compId);
            if (competition != null) text_CompetitionName.setText(competition.getDescription());
            else text_CompetitionName.setText("");
        }

        // Define the edit Text for entering the Device Id
        EditText edit_DeviceId = settingsBinding.editDeviceId;
        // MUST CONVERT TO STRING or it crashes with out warning
        edit_DeviceId.setText(String.valueOf(sp.getInt("DeviceId", -1)));

        // Define a text box for the name of the Device to appear in when you enter the ID
        TextView text_DeviceName = settingsBinding.textDeviceName;
        String deviceIdStr = String.valueOf(edit_DeviceId.getText());
        if (!deviceIdStr.isEmpty()) {
            int deviceNumber = Integer.parseInt(deviceIdStr);
            Devices.DeviceRow device = Globals.DeviceList.getDeviceRow(deviceNumber);
            if (device != null) text_DeviceName.setText(device.getDescription());
            else text_DeviceName.setText("");
        }

        // Define the edit Text for entering the Device Id
        EditText edit_ScoutingTeam = settingsBinding.editScoutingTeam;
        // MUST CONVERT TO STRING or it crashes with out warning
        edit_ScoutingTeam.setText(String.valueOf(sp.getInt("ScoutingTeam", -1)));

        // Define a text box for the name of the Team to appear in when you enter the Number
        TextView text_ScoutingTeamName = settingsBinding.textScoutingTeamName;
        String ScoutingTeamNumStr = String.valueOf(edit_ScoutingTeam.getText());
        if (!ScoutingTeamNumStr.isEmpty()) {
            int ScoutingTeamNum = Integer.parseInt(ScoutingTeamNumStr);
            if (ScoutingTeamNum > 0 && ScoutingTeamNum < Globals.TeamList.size()) {
                // This will crash the app instead of returning null if you pass it an invalid num
                String ScoutingTeamName = Globals.TeamList.get(ScoutingTeamNum);
                text_ScoutingTeamName.setText(ScoutingTeamName);
            } else text_ScoutingTeamName.setText("");
        }

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
                String ScoutingTeam = String.valueOf(edit_ScoutingTeam.getText());
                if (!ScoutingTeam.isEmpty()) {
                    spe.putInt("ScoutingTeam", Integer.parseInt(ScoutingTeam));
                }
                spe.apply();
                Exit();
            }
        });

        edit_CompetitionId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (!focus) {
                    String compIdStr = String.valueOf(edit_CompetitionId.getText());
                    if (!compIdStr.isEmpty()) {
                        int compId = Integer.parseInt(compIdStr);
                        Competitions.CompetitionRow competition = Globals.CompetitionList.getCompetitionRow(compId);
                        if (competition != null) text_CompetitionName.setText(competition.getDescription());
                        else text_CompetitionName.setText("");
                    }
                }
            }
        });

        edit_DeviceId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (!focus) {
                    String deviceIdStr = String.valueOf(edit_DeviceId.getText());
                    if (!deviceIdStr.isEmpty()) {
                        int deviceNumber = Integer.parseInt(deviceIdStr);
                        Devices.DeviceRow device = Globals.DeviceList.getDeviceRow(deviceNumber);
                        if (device != null) {
                            text_DeviceName.setText(device.getDescription());
                            // MUST CONVERT TO STRING or it crashes with out warning
                            settingsBinding.editScoutingTeam.setText(String.valueOf(device.getTeamNumber()));
                            settingsBinding.textScoutingTeamName.setText(Globals.TeamList.get(device.getTeamNumber()));
                        } else text_DeviceName.setText("");
                    }
                }
            }
        });

        edit_ScoutingTeam.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if (!focus) {
                    String ScoutingTeamNumStr = String.valueOf(edit_ScoutingTeam.getText());
                    if (!ScoutingTeamNumStr.isEmpty()) {
                        int ScoutingTeamNum = Integer.parseInt(ScoutingTeamNumStr);
                        if (ScoutingTeamNum > 0 && ScoutingTeamNum < Globals.TeamList.size()) {
                            // This will crash the app instead of returning null if you pass it an invalid num
                            String ScoutingTeamName = Globals.TeamList.get(ScoutingTeamNum);
                            text_ScoutingTeamName.setText(ScoutingTeamName);
                        } else text_ScoutingTeamName.setText("");
                    }
                }
            }
        });
    }

    private void Exit() {
        Intent GoBackToLaunch = new Intent(Settings.this, AppLaunch.class);
        startActivity(GoBackToLaunch);
    }
}