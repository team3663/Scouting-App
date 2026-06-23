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
import com.team3663.scouting_app.databinding.FragmentSettingsPage2Binding;

public class SettingsPage2 extends Fragment {
    public FragmentSettingsPage2Binding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsPage2Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initColors();
        initFieldOrientation();
        initPrefTeamPos();
        initQRSize();
        initShadowMode();
    }

    // =============================================================================================
    // Function:    initColors
    // Description: Initialize the Color Palette field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initColors() {
        // Adds Color information to spinner
        ArrayAdapter<String> adp_Color = new ArrayAdapter<>(requireContext(),
                R.layout.cpr_spinner, Globals.ColorList.getDescriptionList());
        adp_Color.setDropDownViewResource(R.layout.cpr_spinner_item);
        binding.spinnerColor.setAdapter(adp_Color);

        // Set the selection (if there is one) to the saved one
        int savedColorId = Globals.sp.getInt(Constants.Prefs.COLOR_CONTEXT_MENU, -1);
        if ((savedColorId > -1) && (adp_Color.getCount() > 0))
            binding.spinnerColor.setSelection(adp_Color.getPosition(Globals.ColorList.getColorDescription(savedColorId)), true);

        // Define the actions when an item is selected.  Set text color and set description text
        binding.spinnerColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(requireContext().getColor(R.color.cpr_bkgnd));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
        ArrayAdapter<String> adp_PrefOrientation = new ArrayAdapter<>(requireContext(),
                R.layout.cpr_spinner, Constants.Settings.PREF_FIELD_ORIENTATION);
        adp_PrefOrientation.setDropDownViewResource(R.layout.cpr_spinner_item);
        binding.spinnerOrientation.setAdapter(adp_PrefOrientation);

        // Set the selection (if there is one) to the saved one
        int savedPrefOrientation = Globals.sp.getInt(Constants.Prefs.PREF_ORIENTATION, 0);
        binding.spinnerOrientation.setSelection(savedPrefOrientation, true);
    }

    // =============================================================================================
    // Function:    initPrefTeamPos
    // Description: Initialize the Preferred Team Position field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initPrefTeamPos() {
        // Adds PreferredTeamPosition information to spinner
        ArrayAdapter<String> adp_PrefTeamPos = new ArrayAdapter<>(requireContext(),
                R.layout.cpr_spinner, Constants.Settings.PREF_TEAM_POS);
        adp_PrefTeamPos.setDropDownViewResource(R.layout.cpr_spinner_item);
        binding.spinnerPrefTeamPos.setAdapter(adp_PrefTeamPos);

        // Set the selection (if there is one) to the saved one
        int savedPrefTeamPos = Globals.sp.getInt(Constants.Prefs.PREF_TEAM_POS, 0);
        binding.spinnerPrefTeamPos.setSelection(savedPrefTeamPos, true);
    }

    // =============================================================================================
    // Function:    initQRSize
    // Description: Initialize the QR Size field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initQRSize() {
        // MUST CONVERT TO STRING or it crashes with out warning
        binding.editQRSize.setText(String.valueOf(Globals.sp.getInt(Constants.Prefs.QR_SIZE, Constants.QRCode.QR_SIZE_DEFAULT)));
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
        String paddedText = binding.checkboxShadowMode.getText() + Globals.CheckBoxTextPadding;

        binding.checkboxShadowMode.setText(paddedText);
        binding.checkboxShadowMode.setChecked(Globals.isShadowMode);
        binding.checkboxShadowMode.setOnCheckedChangeListener((buttonView, isChecked) -> Globals.isShadowMode = isChecked);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}