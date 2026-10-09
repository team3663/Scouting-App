package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.*;
import com.team3663.scouting_app.databinding.SettingsBinding;
import com.team3663.scouting_app.fragments.*;
import com.google.android.material.tabs.TabLayoutMediator;
import com.team3663.scouting_app.utility.CPR_Network;

public class Settings extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    SettingsBinding settingsBinding;
    SettingsPagerAdapter adapter;
    public static SharedPreferences sp;
    private static SharedPreferences.Editor spe;

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
        if (sp == null) sp = this.getSharedPreferences(getString(R.string.preference_setting_file_key), Context.MODE_PRIVATE);
        if (spe == null) spe = sp.edit();

        // Set the files downloaded to false when we first get into settings.
        CPR_Network.filesDownloaded = false;

        adapter = new SettingsPagerAdapter(this);
        settingsBinding.viewPager.setAdapter(adapter);

        // Number of pages kept in memory to the left and right of current page
        settingsBinding.viewPager.setOffscreenPageLimit(3);

        new TabLayoutMediator(settingsBinding.tabLayout, settingsBinding.viewPager,
                (tab, position) -> {} // No text needed for dot indicators
        ).attach();

        // Define a Cancel Button
        settingsBinding.butCancel.setOnClickListener(view -> CancelSettings());

        // Define a Save Button
        settingsBinding.butSave.setOnClickListener(view -> SaveSettings());
    }

    // =============================================================================================
    // Function:    CancelSettings
    // Description: Cancel any changes made (ignore them) but set the intent to reload the data
    //              if new files were downloaded.
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void CancelSettings() {
        Intent intent = new Intent();

        if (CPR_Network.filesDownloaded) intent.putExtra(Constants.Settings.RELOAD_DATA_KEY, 1);

        setResult(RESULT_OK, intent);
        finish();
    }

    // =============================================================================================
    // Function:    SaveSettings
    // Description: Save off the settings before closing this activity
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void SaveSettings() {
        Intent intent = new Intent();
        // These fragment tags are created by the fragment manager and so are safe tags to use.
        // Look them up rather than cache them.
        // The cached fields were only ever assigned inside createFragment().  After a rotation or process-death restore,
        // FragmentStateAdaptor re-attaches the saved fragments without calling createFragment() so our cached fields
        // would stay null and SaveSettings would silently skip those pages.  findFragmentByTag() asks for the actual
        // current instance, so it works everytime.
        SettingsPage1 fragmentPage1 = (SettingsPage1) getSupportFragmentManager().findFragmentByTag("f0");
        SettingsPage2 fragmentPage2 = (SettingsPage2) getSupportFragmentManager().findFragmentByTag("f1");
        SettingsPage3 fragmentPage3 = (SettingsPage3) getSupportFragmentManager().findFragmentByTag("f2");
        SettingsPage4 fragmentPage4 = (SettingsPage4) getSupportFragmentManager().findFragmentByTag("f3");

        // Page1 Settings
        if (fragmentPage1 != null && fragmentPage1.binding != null) {
            int CompetitionId = 0;

            if (fragmentPage1.binding.spinnerCompetition.getSelectedItem() != null)
                CompetitionId = Globals.CompetitionList.getCompetitionId(fragmentPage1.binding.spinnerCompetition.getSelectedItem().toString());

            // If we changed the CompetitionId, set Global flag to reload some of the data
            if (CompetitionId > 0) {
                if (CompetitionId != fragmentPage1.savedCompetitionId)
                    intent.putExtra(Constants.Settings.RELOAD_DATA_KEY, 1);
                spe.putInt(Constants.Prefs.COMPETITION_ID, CompetitionId);
            }

            int DeviceId = 0;

            if (fragmentPage1.binding.spinnerDevice.getSelectedItem() != null)
                DeviceId = Globals.DeviceList.getDeviceId(fragmentPage1.binding.spinnerDevice.getSelectedItem().toString());

            if (DeviceId > 0) {
                spe.putInt(Constants.Prefs.DEVICE_ID, DeviceId);
            }

            String ScoutingTeam = String.valueOf(fragmentPage1.binding.editScoutingTeam.getText());
            if (!ScoutingTeam.isEmpty()) {
                spe.putString(Constants.Prefs.SCOUTING_TEAM, ScoutingTeam);
            }

            Globals.CurrentPrefTeamPos = fragmentPage1.binding.spinnerPrefTeamPos.getSelectedItemPosition();
            spe.putInt(Constants.Prefs.PREF_TEAM_POS, Globals.CurrentPrefTeamPos);

            Globals.CurrentFieldOrientationPos = fragmentPage1.binding.spinnerOrientation.getSelectedItemPosition();
            spe.putInt(Constants.Prefs.PREF_ORIENTATION, Globals.CurrentFieldOrientationPos);
        }

        // Page2 Settings
        if (fragmentPage2 != null && fragmentPage2.binding != null) {
            int ColorId = 0;

            if (fragmentPage2.binding.spinnerColor.getSelectedItem() != null)
                ColorId = Globals.ColorList.getColorId(fragmentPage2.binding.spinnerColor.getSelectedItem().toString());

            if (ColorId > 0) {
                spe.putInt(Constants.Prefs.COLOR_CONTEXT_MENU, ColorId);
            }

            int CurrentQRSize = Integer.parseInt(fragmentPage2.binding.editQRSize.getText().toString());
            spe.putInt(Constants.Prefs.QR_SIZE, CurrentQRSize);

            int NumMatches = Integer.parseInt(fragmentPage2.binding.editNumMatches.getText().toString());
            if (NumMatches < 1) NumMatches = 1;
            spe.putInt(Constants.Prefs.NUM_MATCHES, NumMatches);
        }

        // Page3 Settings
        if (fragmentPage3 != null && fragmentPage3.binding != null) {
            Editable userField;

            userField = fragmentPage3.binding.editServer.getText();
            spe.putString(Constants.Prefs.SQL_SERVER, (userField != null) ? userField.toString() : "");

            userField = fragmentPage3.binding.editDatabase.getText();
            spe.putString(Constants.Prefs.SQL_DATABASE, (userField != null) ? userField.toString() : "");

            userField = fragmentPage3.binding.editUser.getText();
            spe.putString(Constants.Prefs.SQL_USER, (userField != null) ? userField.toString() : "");

            userField = fragmentPage3.binding.editPassword.getText();
            spe.putString(Constants.Prefs.SQL_PASSWORD, (userField != null) ? userField.toString() : "");
        }

        // Page4 Settings
        if (fragmentPage4 != null && fragmentPage4.binding != null) {
            Editable userField;

            userField = fragmentPage4.binding.editGoogleUpload.getText();
            spe.putString(Constants.Prefs.GOOGLE_DRIVE_UPLOAD, (userField != null) ? userField.toString() : "");

            userField = fragmentPage4.binding.editGoogleDownload.getText();
            spe.putString(Constants.Prefs.GOOGLE_DRIVE_DOWNLOAD, (userField != null) ? userField.toString() : "");

            if (CPR_Network.filesDownloaded) intent.putExtra(Constants.Settings.RELOAD_DATA_KEY, 1);
        }

        spe.apply();
        setResult(RESULT_OK, intent);
        finish();
    }
}