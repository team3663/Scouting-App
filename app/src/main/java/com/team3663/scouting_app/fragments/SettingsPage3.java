package com.team3663.scouting_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.team3663.scouting_app.databinding.FragmentSettingsPage3Binding;

public class SettingsPage3 extends Fragment {
    public FragmentSettingsPage3Binding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsPage3Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initColors();
    }

    // =============================================================================================
    // Function:    initColors
    // Description: Initialize the Color Palette field
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initColors() {
    }
}