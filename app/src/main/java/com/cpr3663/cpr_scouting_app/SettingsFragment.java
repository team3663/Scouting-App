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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentSettingsBinding = fragmentSettingsBinding.inflate(getLayoutInflater());

        width = 500; height = 500;

        // TODO Comment and fix this
        TextView text_Header = fragmentSettingsBinding.textHeader;
        text_Header.setText("Settings");
        text_Header.setTextSize(25F);
        text_Header.setTextColor(Color.BLACK);
        text_Header.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        text_Header.setX(0F);
        text_Header.setY(0F);
//        ViewGroup.LayoutParams text_Time_LP = new ViewGroup.LayoutParams(width, 200);
//        text_Header.setLayoutParams(text_Time_LP);
        text_Header.setBackgroundColor(Color.TRANSPARENT);

        // TODO Comment and fix this
        ToggleButton but_Done = fragmentSettingsBinding.butDone;
        but_Done.setTextOff("Done");
        but_Done.setTextOn("Done");
        but_Done.setTextSize(20F);
        but_Done.setTextColor(Color.WHITE);
        but_Done.setTextAlignment(Layout.Alignment.ALIGN_CENTER.ordinal() + 2);
        ViewGroup.LayoutParams but_Done_LP = view.getLayoutParams();
        but_Done_LP = new ViewGroup.LayoutParams(300, 100);
        but_Done.setLayoutParams(but_Done_LP);
        but_Done.setX(width - but_Done.getWidth());
        but_Done.setY(height - but_Done.getHeight());
        but_Done.setBackgroundColor(Color.BLACK);

        but_Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }

}