package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.*;
import com.team3663.scouting_app.databinding.SettingsBinding;

public class Settings extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    SettingsBinding settingsBinding;
    int savedCompetitionId;

    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility", "ResourceType"})
    @Override
    protected void onCreate(Bundle in_savedInstanceState) {
        super.onCreate(in_savedInstanceState);
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
        if (Globals.sp == null) Globals.sp = this.getSharedPreferences(getString(R.string.preference_setting_file_key), Context.MODE_PRIVATE);
        if (Globals.spe == null) Globals.spe = Globals.sp.edit();

        // Initialize activity components
        initCompetition();
        initDevice();
        initPrefTeamPos();
        initFieldOrientation();
        initScoutingTeam();
        initNumMatches();
        initColors();
        initShadowMode();

        // Define a Cancel Button
        settingsBinding.butCancel.setOnClickListener(view -> finish());

        // Define a Save Button
        settingsBinding.butSave.setOnClickListener(view -> SaveSettings());
    }

    // =============================================================================================
    // Function:    SaveSettings
    // Description: Save off the settings before closing this activity
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void SaveSettings() {
        Intent intent = new Intent();
        int CompetitionId = Globals.CompetitionList.getCompetitionId(settingsBinding.spinnerCompetition.getSelectedItem().toString());

        if (CompetitionId > 0) {
            // If we changed the CompetitionId, set Global flag to reload some of the data
            if (CompetitionId != savedCompetitionId)
                intent.putExtra(Constants.Settings.RELOAD_DATA_KEY, 1);
            Globals.spe.putInt(Constants.Prefs.COMPETITION_ID, CompetitionId);
        }

        int DeviceId = Globals.DeviceList.getDeviceId(settingsBinding.spinnerDevice.getSelectedItem().toString());
        if (DeviceId > 0) {
            Globals.spe.putInt(Constants.Prefs.DEVICE_ID, DeviceId);
        }

        String ScoutingTeam = String.valueOf(settingsBinding.editScoutingTeam.getText());
        if (!ScoutingTeam.isEmpty()) {
            Globals.spe.putString(Constants.Prefs.SCOUTING_TEAM, ScoutingTeam);
        }

        int NumMatches = Integer.parseInt(settingsBinding.editNumMatches.getText().toString());
        if (NumMatches < 1) NumMatches = 1;
        Globals.spe.putInt(Constants.Prefs.NUM_MATCHES, NumMatches);

        int ColorId = Globals.ColorList.getColorId(settingsBinding.spinnerColor.getSelectedItem().toString());
        if (ColorId > 0) {
            Globals.spe.putInt(Constants.Prefs.COLOR_CONTEXT_MENU, ColorId);
        }

        Globals.CurrentPrefTeamPos = settingsBinding.spinnerPrefTeamPos.getSelectedItemPosition();
        Globals.spe.putInt(Constants.Prefs.PREF_TEAM_POS, Globals.CurrentPrefTeamPos);
        Globals.spe.apply();

        Globals.CurrentFieldOrientationPos = settingsBinding.spinnerOrientation.getSelectedItemPosition();
        Globals.spe.putInt(Constants.Prefs.PREF_ORIENTATION, Globals.CurrentFieldOrientationPos);
        Globals.spe.apply();

        setResult(RESULT_OK, intent);
        finish();
    }

    // =============================================================================================
    // Function:    initCompetition
    // Description: Initialize the competition id field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initCompetition() {
        // Adds Competition information to spinner
        ArrayAdapter<String> adp_Competition = new ArrayAdapter<>(this,
                R.layout.cpr_spinner, Globals.CompetitionList.getCompetitionList());
        adp_Competition.setDropDownViewResource(R.layout.cpr_spinner_item);
        settingsBinding.spinnerCompetition.setAdapter(adp_Competition);

        // Set the selection (if there is one) to the saved one
        savedCompetitionId = Globals.sp.getInt(Constants.Prefs.COMPETITION_ID, -1);
        if ((savedCompetitionId > -1) && (adp_Competition.getCount() > 0)) {
            int pos = adp_Competition.getPosition(Globals.CompetitionList.getCompetitionDescription(savedCompetitionId));
            if (pos > -1) settingsBinding.spinnerCompetition.setSelection(pos, true);
        }

        // Define the actions when an item is selected.  Set text color and set description text
        settingsBinding.spinnerCompetition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(getColor(R.color.cpr_bkgnd));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // =============================================================================================
    // Function:    initDevice
    // Description: Initialize the device id field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initDevice() {
        // Adds Device information to spinner
        ArrayAdapter<String> adp_Device = new ArrayAdapter<>(this,
                R.layout.cpr_spinner, Globals.DeviceList.getDeviceList());
        adp_Device.setDropDownViewResource(R.layout.cpr_spinner_item);
        settingsBinding.spinnerDevice.setAdapter(adp_Device);

        // Set the selection (if there is one) to the saved one
        int savedDeviceId = Globals.sp.getInt(Constants.Prefs.DEVICE_ID, -1);
        if ((savedDeviceId > -1) && (adp_Device.getCount() >0)) {
            settingsBinding.spinnerDevice.setSelection(adp_Device.getPosition(Globals.DeviceList.getDeviceDescription(savedDeviceId)), true);
            settingsBinding.editScoutingTeam.setText(String.valueOf(Globals.DeviceList.getTeamNumberByDeviceId(savedDeviceId)));
            settingsBinding.textScoutingTeamName.setText(Globals.TeamList.getOrDefault(Globals.DeviceList.getTeamNumberByDeviceId(savedDeviceId), ""));
        }

        // Define the actions when an item is selected.  Set text color and set description text
        settingsBinding.spinnerDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Settings.this.runOnUiThread(() -> {
                    String team_num = Globals.DeviceList.getTeamNumberByDescription(settingsBinding.spinnerDevice.getSelectedItem().toString());
                    settingsBinding.editScoutingTeam.setText(team_num);
                    settingsBinding.textScoutingTeamName.setText(Globals.TeamList.getOrDefault(team_num, ""));
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // =============================================================================================
    // Function:    initPrefTeamPos
    // Description: Initialize the Preferred Team Position field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initPrefTeamPos() {
        // Adds PreferredTeamPosition information to spinner
        ArrayAdapter<String> adp_PrefTeamPos = new ArrayAdapter<>(this,
                R.layout.cpr_spinner, Constants.Settings.PREF_TEAM_POS);
        adp_PrefTeamPos.setDropDownViewResource(R.layout.cpr_spinner_item);
        settingsBinding.spinnerPrefTeamPos.setAdapter(adp_PrefTeamPos);

        // Set the selection (if there is one) to the saved one
        int savedPrefTeamPos = Globals.sp.getInt(Constants.Prefs.PREF_TEAM_POS, 0);
        settingsBinding.spinnerPrefTeamPos.setSelection(savedPrefTeamPos, true);

        // Define the actions when an item is selected.  Set text color and set description text
        settingsBinding.spinnerPrefTeamPos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Settings.this.runOnUiThread(() -> {

                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // =============================================================================================
    // Function:    initFieldOrientation
    // Description: Initialize the Preferred Field Orientation field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initFieldOrientation() {
        // Adds PreferredFieldOrientation information to spinner
        ArrayAdapter<String> adp_PrefOrientation = new ArrayAdapter<>(this,
                R.layout.cpr_spinner, Constants.Settings.PREF_FIELD_ORIENTATION);
        adp_PrefOrientation.setDropDownViewResource(R.layout.cpr_spinner_item);
        settingsBinding.spinnerOrientation.setAdapter(adp_PrefOrientation);

        // Set the selection (if there is one) to the saved one
        int savedPrefOrientation = Globals.sp.getInt(Constants.Prefs.PREF_ORIENTATION, 0);
        settingsBinding.spinnerOrientation.setSelection(savedPrefOrientation, true);

        // Define the actions when an item is selected.  Set text color and set description text
        settingsBinding.spinnerOrientation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Settings.this.runOnUiThread(() -> {

                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // =============================================================================================
    // Function:    initScoutingTeam
    // Description: Initialize the Scouting Team field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initScoutingTeam() {
        // MUST CONVERT TO STRING or it crashes with out warning
        settingsBinding.editScoutingTeam.setText(Globals.sp.getString(Constants.Prefs.SCOUTING_TEAM, ""));

        // Define a text box for the name of the Team to appear in when you enter the Number
        String ScoutingTeamNumStr = String.valueOf(settingsBinding.editScoutingTeam.getText());
        if (!ScoutingTeamNumStr.isEmpty()) {
            settingsBinding.textScoutingTeamName.setText(Globals.TeamList.getOrDefault(ScoutingTeamNumStr, ""));
        }

        settingsBinding.editScoutingTeam.setOnFocusChangeListener((view, focus) -> {
            if (!focus) {
                String ScoutingTeamNumStr1 = String.valueOf(settingsBinding.editScoutingTeam.getText());
                if (!ScoutingTeamNumStr1.isEmpty()) {
                    settingsBinding.textScoutingTeamName.setText(Globals.TeamList.getOrDefault(ScoutingTeamNumStr1, ""));
                }
            }
        });
    }

    // =============================================================================================
    // Function:    initNumMatches
    // Description: Initialize the Number of Matches to Keep field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initNumMatches() {
        // Restore number of files to keep from saved preferences
        settingsBinding.editNumMatches.setText(String.valueOf(Globals.sp.getInt(Constants.Prefs.NUM_MATCHES, 5)));
    }

    // =============================================================================================
    // Function:    initColors
    // Description: Initialize the Color Palette field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initColors() {
        // Adds Color information to spinner
        ArrayAdapter<String> adp_Color = new ArrayAdapter<>(this,
                R.layout.cpr_spinner, Globals.ColorList.getDescriptionList());
        adp_Color.setDropDownViewResource(R.layout.cpr_spinner_item);
        settingsBinding.spinnerColor.setAdapter(adp_Color);

        // Set the selection (if there is one) to the saved one
        int savedColorId = Globals.sp.getInt(Constants.Prefs.COLOR_CONTEXT_MENU, -1);
        if ((savedColorId > -1) && (adp_Color.getCount() > 0))
            settingsBinding.spinnerColor.setSelection(adp_Color.getPosition(Globals.ColorList.getColorDescription(savedColorId)), true);

        // Define the actions when an item is selected.  Set text color and set description text
        settingsBinding.spinnerColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(getColor(R.color.cpr_bkgnd));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // =============================================================================================
    // Function:    initShadowMode
    // Description: Initialize the Shadow Mode field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initShadowMode() {
        // Since we are putting the checkbox on the RIGHT side of the text, the checkbox doesn't honor padding.
        // So we need to use 7 spaces, but you can't when using a string resource (it ignores the trailing spaces)
        // So add it in now.
        String paddedText = settingsBinding.checkboxShadowMode.getText() + Globals.CheckBoxTextPadding;
        settingsBinding.checkboxShadowMode.setText(paddedText);

        // Default checkboxes
        settingsBinding.checkboxShadowMode.setChecked(false);

        settingsBinding.checkboxShadowMode.setOnCheckedChangeListener((buttonView, isChecked) -> Globals.isShadowMode = isChecked);
    }
}