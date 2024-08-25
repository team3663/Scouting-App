package com.cpr3663.cpr_scouting_app;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.fragment.app.Fragment;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cpr3663.cpr_scouting_app.databinding.AppLaunchBinding;
import com.cpr3663.cpr_scouting_app.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    // =============================================================================================
    // Global Variables
    // =============================================================================================
    FragmentSettingsBinding fragmentSettingsBinding;
    private View view;
    private int width;
    private int height;
    private int BackgroundColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentSettingsBinding = fragmentSettingsBinding.inflate(getLayoutInflater());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }

}