package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
        if (Globals.sp == null) Globals.sp = this.getSharedPreferences(getString(R.string.preference_setting_file_key), Context.MODE_PRIVATE);
        if (Globals.spe == null) Globals.spe = Globals.sp.edit();

        // Restore number of files to keep from saved preferences
        settingsBinding.editNumMatches.setText(String.valueOf(Globals.sp.getInt(Constants.Prefs.NUM_MATCHES, 5)));

        // Adds Competition information to spinner
        ArrayAdapter<String> adp_Competition = new ArrayAdapter<String>(this,
                R.layout.cpr_spinner, Globals.CompetitionList.getCompetitionList());
        adp_Competition.setDropDownViewResource(R.layout.cpr_spinner_item);
        settingsBinding.spinnerCompetition.setAdapter(adp_Competition);

        // Set the selection (if there is one) to the saved one
        savedCompetitionId = Globals.sp.getInt(Constants.Prefs.COMPETITION_ID, -1);
        if ((savedCompetitionId > -1) && (adp_Competition.getCount() > 0))
            settingsBinding.spinnerCompetition.setSelection(adp_Competition.getPosition(Globals.CompetitionList.getCompetitionDescription(savedCompetitionId)), true);

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

        // Adds Device information to spinner
        ArrayAdapter<String> adp_Device = new ArrayAdapter<String>(this,
                R.layout.cpr_spinner, Globals.DeviceList.getDeviceList());
        adp_Device.setDropDownViewResource(R.layout.cpr_spinner_item);
        settingsBinding.spinnerDevice.setAdapter(adp_Device);

        // Set the selection (if there is one) to the saved one
        int savedDeviceId = Globals.sp.getInt(Constants.Prefs.DEVICE_ID, -1);
        if ((savedDeviceId > -1) && (adp_Device.getCount() >0)) {
            settingsBinding.spinnerDevice.setSelection(adp_Device.getPosition(Globals.DeviceList.getDeviceDescription(savedDeviceId)), true);
            settingsBinding.editScoutingTeam.setText(String.valueOf(Globals.DeviceList.getTeamNumberByDeviceId(savedDeviceId)));
            settingsBinding.textScoutingTeamName.setText(Globals.TeamList.get(Globals.DeviceList.getTeamNumberByDeviceId(savedDeviceId)));
        }

        // Define the actions when an item is selected.  Set text color and set description text
        settingsBinding.spinnerDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Settings.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int team_num = Globals.DeviceList.getTeamNumberByDescription(settingsBinding.spinnerDevice.getSelectedItem().toString());
                        settingsBinding.editScoutingTeam.setText(String.valueOf(team_num));
                        settingsBinding.textScoutingTeamName.setText(Globals.TeamList.get(team_num));
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Adds PreferredTeamPosition information to spinner
        ArrayAdapter<String> adp_PrefTeamPos = new ArrayAdapter<String>(this,
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
                Settings.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Adds Color information to spinner
        ArrayAdapter<String> adp_Color = new ArrayAdapter<String>(this,
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

        // Define the edit Text for entering the Device Id
        EditText edit_ScoutingTeam = settingsBinding.editScoutingTeam;
        // MUST CONVERT TO STRING or it crashes with out warning
        edit_ScoutingTeam.setText(String.valueOf(Globals.sp.getInt(Constants.Prefs.SCOUTING_TEAM, -1)));

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

        but_Cancel.setOnClickListener(view -> finish());

        // Define a Save Button
        Button but_Save = settingsBinding.butSave;

        but_Save.setOnClickListener(view -> {
            int CompetitionId = Globals.CompetitionList.getCompetitionId(settingsBinding.spinnerCompetition.getSelectedItem().toString());
            if (CompetitionId > 0) {
                // If we changed the CompetitionId, set Global flag to reload some of the data
                if (CompetitionId != savedCompetitionId) Globals.NeedToLoadData = true;
                Globals.spe.putInt(Constants.Prefs.COMPETITION_ID, CompetitionId);
            }
            int DeviceId = Globals.DeviceList.getDeviceId(settingsBinding.spinnerDevice.getSelectedItem().toString());
            if (DeviceId > 0) {
                Globals.spe.putInt(Constants.Prefs.DEVICE_ID, DeviceId);
            }
            String ScoutingTeam = String.valueOf(edit_ScoutingTeam.getText());
            if (!ScoutingTeam.isEmpty()) {
                Globals.spe.putInt(Constants.Prefs.SCOUTING_TEAM, Integer.parseInt(ScoutingTeam));
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
            finish();
        });

        edit_ScoutingTeam.setOnFocusChangeListener((view, focus) -> {
            if (!focus) {
                String ScoutingTeamNumStr1 = String.valueOf(edit_ScoutingTeam.getText());
                if (!ScoutingTeamNumStr1.isEmpty()) {
                    int ScoutingTeamNum = Integer.parseInt(ScoutingTeamNumStr1);
                    if (ScoutingTeamNum > 0 && ScoutingTeamNum < Globals.TeamList.size()) {
                        // This will crash the app instead of returning null if you pass it an invalid num
                        String ScoutingTeamName = Globals.TeamList.get(ScoutingTeamNum);
                        text_ScoutingTeamName.setText(ScoutingTeamName);
                    } else text_ScoutingTeamName.setText("");
                }
            }
        });
    }
}