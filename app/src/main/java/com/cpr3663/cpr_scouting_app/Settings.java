package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cpr3663.cpr_scouting_app.data.Competitions;
import com.cpr3663.cpr_scouting_app.data.Devices;
import com.cpr3663.cpr_scouting_app.databinding.SettingsBinding;

public class Settings extends AppCompatActivity {
    // =============================================================================================
    // Define Constants
    // =============================================================================================
    public static final String SP_COMPETITION_ID = "CompetitionId";
    public static final String SP_DEVICE_ID = "DeviceId";
    public static final String SP_SCOUTING_TEAM = "ScoutingTeam";

    // =============================================================================================
    // Global variables
    // =============================================================================================
    SettingsBinding settingsBinding;
    SharedPreferences sp;
    SharedPreferences.Editor spe;
    Spinner spinner_CompetitionId;
    Spinner spinner_DeviceId;


    // Doesn't appear to be needed on Tablet but helps on Virtual Devices.
    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility", "ResourceAsColor"})
    @Override
    protected void onResume() {
        super.onResume();

        // Hide the status and action bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) actionBar.hide();
    }

    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // Get the Shared Preferences where we save off app settings to use next time
        sp = this.getSharedPreferences(getResources().getString(R.string.preference_setting_file_key), Context.MODE_PRIVATE);
        spe = sp.edit();

        // Adds Competition information to spinner
        spinner_CompetitionId = settingsBinding.spinnerCompetitionId;
        ArrayAdapter<String> adp_CompetitionId = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, Globals.CompetitionList.getCompetitionIdList());
        adp_CompetitionId.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_CompetitionId.setAdapter(adp_CompetitionId);

        // Set the selection (if there is one) to the saved one
        int savedCompetitionId = sp.getInt(SP_COMPETITION_ID, -1);
        if ((savedCompetitionId > -1) && (adp_CompetitionId.getCount() >= savedCompetitionId)) {
            spinner_CompetitionId.setSelection(savedCompetitionId - 1, true);
            settingsBinding.textCompetitionName.setText(Globals.CompetitionList.getCompetitionDescriptionById(savedCompetitionId));
        }

            // Define the actions when an item is selected.  Set text color and set description text
        spinner_CompetitionId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.cpr_bkgnd));

                Settings.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        settingsBinding.textCompetitionName.setText(Globals.CompetitionList.getCompetitionDescriptionById(position + 1));
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Adds Device information to spinner
        spinner_DeviceId = settingsBinding.spinnerDeviceId;
        ArrayAdapter<String> adp_DeviceId = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, Globals.DeviceList.getDeviceIdList());
        adp_DeviceId.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_DeviceId.setAdapter(adp_DeviceId);

        // Set the selection (if there is one) to the saved one
        int savedDeviceId = sp.getInt(SP_DEVICE_ID, -1);
        if ((savedDeviceId > -1) && (adp_DeviceId.getCount() >= savedDeviceId)) {
            spinner_DeviceId.setSelection(savedDeviceId - 1, true);
            Devices.DeviceRow dr = Globals.DeviceList.getDeviceRow(savedDeviceId);
            settingsBinding.textDeviceName.setText(dr.getDescription());
            settingsBinding.editScoutingTeam.setText(String.valueOf(dr.getTeamNumber()));
            settingsBinding.textScoutingTeamName.setText(Globals.TeamList.get(dr.getTeamNumber()));
        }

        // Define the actions when an item is selected.  Set text color and set description text
        spinner_DeviceId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.cpr_bkgnd));

                Settings.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Devices.DeviceRow dr = Globals.DeviceList.getDeviceRow(position + 1);
                        settingsBinding.textDeviceName.setText(dr.getDescription());
                        settingsBinding.editScoutingTeam.setText(String.valueOf(dr.getTeamNumber()));
                        settingsBinding.textScoutingTeamName.setText(Globals.TeamList.get(dr.getTeamNumber()));
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Define the edit Text for entering the Device Id
        EditText edit_ScoutingTeam = settingsBinding.editScoutingTeam;
        // MUST CONVERT TO STRING or it crashes with out warning
        edit_ScoutingTeam.setText(String.valueOf(sp.getInt(SP_SCOUTING_TEAM, -1)));

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
                String CompetitionId = spinner_CompetitionId.getSelectedItem().toString();
                if (!CompetitionId.isEmpty()) {
                    spe.putInt(SP_COMPETITION_ID, Integer.parseInt(CompetitionId));
                }
                String DeviceId = spinner_DeviceId.getSelectedItem().toString();
                if (!DeviceId.isEmpty()) {
                    spe.putInt(SP_DEVICE_ID, Integer.parseInt(DeviceId));
                }
                String ScoutingTeam = String.valueOf(edit_ScoutingTeam.getText());
                if (!ScoutingTeam.isEmpty()) {
                    spe.putInt(SP_SCOUTING_TEAM, Integer.parseInt(ScoutingTeam));
                }
                spe.apply();
                Exit();
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