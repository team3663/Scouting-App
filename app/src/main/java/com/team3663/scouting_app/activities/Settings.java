package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.*;
import com.team3663.scouting_app.databinding.SettingsBinding;
import com.team3663.scouting_app.fragments.SettingsPagerAdapter;
import com.team3663.scouting_app.fragments.SettingsPage1;
import com.team3663.scouting_app.fragments.SettingsPage2;
import com.google.android.material.tabs.TabLayoutMediator;

public class Settings extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    SettingsBinding settingsBinding;
    SettingsPagerAdapter adapter;

    @SuppressLint({"DiscouragedApi", "SetTextI18n", "ClickableViewAccessibility", "ResourceType"})
    @Override
    protected void onCreate(Bundle in_savedInstanceState) {
        super.onCreate(in_savedInstanceState);
        EdgeToEdge.enable(this);
        settingsBinding = SettingsBinding.inflate(getLayoutInflater());
        setContentView(settingsBinding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(settingsBinding.settings, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the Shared Preferences where we save off app settings to use next time
        if (Globals.sp == null) Globals.sp = this.getSharedPreferences(getString(R.string.preference_setting_file_key), Context.MODE_PRIVATE);
        if (Globals.spe == null) Globals.spe = Globals.sp.edit();

        adapter = new SettingsPagerAdapter(this);
        settingsBinding.viewPager.setAdapter(adapter);

        // Number of pages kept in memory to the left and right of current page
        settingsBinding.viewPager.setOffscreenPageLimit(1);

        new TabLayoutMediator(settingsBinding.tabLayout, settingsBinding.viewPager,
                (tab, position) -> {} // No text needed for dot indicators
        ).attach();

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
        SettingsPage1 fragmentPage1 = adapter.getFragmentPage1();
        SettingsPage2 fragmentPage2 = adapter.getFragmentPage2();

        // Page1 & Page2 Settings
        if (fragmentPage1 != null && fragmentPage1.binding != null) {
            int CompetitionId = Globals.CompetitionList.getCompetitionId(fragmentPage1.binding.spinnerCompetition.getSelectedItem().toString());

            // If we changed the CompetitionId, set Global flag to reload some of the data
            if (CompetitionId > 0) {
                if (CompetitionId != fragmentPage1.savedCompetitionId)
                    intent.putExtra(Constants.Settings.RELOAD_DATA_KEY, 1);
                Globals.spe.putInt(Constants.Prefs.COMPETITION_ID, CompetitionId);
            }

            int DeviceId = Globals.DeviceList.getDeviceId(fragmentPage1.binding.spinnerDevice.getSelectedItem().toString());
            if (DeviceId > 0) {
                Globals.spe.putInt(Constants.Prefs.DEVICE_ID, DeviceId);
            }

            String ScoutingTeam = String.valueOf(fragmentPage1.binding.editScoutingTeam.getText());
            if (!ScoutingTeam.isEmpty()) {
                Globals.spe.putString(Constants.Prefs.SCOUTING_TEAM, ScoutingTeam);
            }

            int NumMatches = Integer.parseInt(fragmentPage1.binding.editNumMatches.getText().toString());
            if (NumMatches < 1) NumMatches = 1;
            Globals.spe.putInt(Constants.Prefs.NUM_MATCHES, NumMatches);
        }

        // UI Settings
        if (fragmentPage2 != null && fragmentPage2.binding != null) {
            int ColorId = Globals.ColorList.getColorId(fragmentPage2.binding.spinnerColor.getSelectedItem().toString());
            if (ColorId > 0) {
                Globals.spe.putInt(Constants.Prefs.COLOR_CONTEXT_MENU, ColorId);
            }

            Globals.CurrentPrefTeamPos = fragmentPage2.binding.spinnerPrefTeamPos.getSelectedItemPosition();
            Globals.spe.putInt(Constants.Prefs.PREF_TEAM_POS, Globals.CurrentPrefTeamPos);

            int CurrentQRSize = Integer.parseInt(fragmentPage2.binding.editQRSize.getText().toString());
            Globals.spe.putInt(Constants.Prefs.QR_SIZE, CurrentQRSize);

            Globals.CurrentFieldOrientationPos = fragmentPage2.binding.spinnerOrientation.getSelectedItemPosition();
            Globals.spe.putInt(Constants.Prefs.PREF_ORIENTATION, Globals.CurrentFieldOrientationPos);
        }

        Globals.spe.apply();
        setResult(RESULT_OK, intent);
        finish();
    }
}