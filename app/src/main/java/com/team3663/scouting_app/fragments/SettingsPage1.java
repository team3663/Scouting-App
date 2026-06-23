package com.team3663.scouting_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.FragmentSettingsPage1Binding;

public class SettingsPage1 extends Fragment {
    public FragmentSettingsPage1Binding binding;
    public int savedCompetitionId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsPage1Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initCompetition();
        initDevice();
        initScoutingTeam();
        initNumMatches();
    }

    // =============================================================================================
    // Function:    initCompetition
    // Description: Initialize the competition id field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initCompetition() {
        // Adds Competition information to spinner
        ArrayAdapter<String> adp_Competition = new ArrayAdapter<>(requireContext(),
                R.layout.cpr_spinner, Globals.CompetitionList.getCompetitionList());
        adp_Competition.setDropDownViewResource(R.layout.cpr_spinner_item);
        binding.spinnerCompetition.setAdapter(adp_Competition);

        // Set the selection (if there is one) to the saved one
        savedCompetitionId = Globals.sp.getInt(Constants.Prefs.COMPETITION_ID, -1);
        if ((savedCompetitionId > -1) && (adp_Competition.getCount() > 0)) {
            int pos = adp_Competition.getPosition(Globals.CompetitionList.getCompetitionDescription(savedCompetitionId));
            if (pos > -1) binding.spinnerCompetition.setSelection(pos, true);
        }

        // Define the actions when an item is selected.  Set text color and set description text
        binding.spinnerCompetition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(requireContext().getColor(R.color.cpr_bkgnd));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
        ArrayAdapter<String> adp_Device = new ArrayAdapter<>(requireContext(),
                R.layout.cpr_spinner, Globals.DeviceList.getDeviceList());
        adp_Device.setDropDownViewResource(R.layout.cpr_spinner_item);
        binding.spinnerDevice.setAdapter(adp_Device);

        // Set the selection (if there is one) to the saved one
        int savedDeviceId = Globals.sp.getInt(Constants.Prefs.DEVICE_ID, -1);
        if ((savedDeviceId > -1) && (adp_Device.getCount() > 0)) {
            binding.spinnerDevice.setSelection(adp_Device.getPosition(Globals.DeviceList.getDeviceDescription(savedDeviceId)), true);
            binding.editScoutingTeam.setText(String.valueOf(Globals.DeviceList.getTeamNumberByDeviceId(savedDeviceId)));
            binding.textScoutingTeamName.setText(Globals.TeamList.getTeam(Globals.DeviceList.getTeamNumberByDeviceId(savedDeviceId)));
        }

        // Define the actions when an item is selected.  Set text color and set description text
        binding.spinnerDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                requireActivity().runOnUiThread(() -> {
                    String team_num = Globals.DeviceList.getTeamNumberByDescription(binding.spinnerDevice.getSelectedItem().toString());
                    binding.editScoutingTeam.setText(team_num);
                    binding.textScoutingTeamName.setText(Globals.TeamList.getTeam(team_num));
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
        binding.editScoutingTeam.setText(Globals.sp.getString(Constants.Prefs.SCOUTING_TEAM, ""));

        // Define a text box for the name of the Team to appear in when you enter the Number
        String ScoutingTeamNumStr = String.valueOf(binding.editScoutingTeam.getText());
        if (!ScoutingTeamNumStr.isEmpty()) {
            binding.textScoutingTeamName.setText(Globals.TeamList.getTeam(ScoutingTeamNumStr));
        }

        binding.editScoutingTeam.setOnFocusChangeListener((view, focus) -> {
            if (!focus) {
                String ScoutingTeamNumStr1 = String.valueOf(binding.editScoutingTeam.getText());
                if (!ScoutingTeamNumStr1.isEmpty()) {
                    binding.textScoutingTeamName.setText(Globals.TeamList.getTeam(ScoutingTeamNumStr1));
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
        binding.editNumMatches.setText(String.valueOf(Globals.sp.getInt(Constants.Prefs.NUM_MATCHES, 50)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}