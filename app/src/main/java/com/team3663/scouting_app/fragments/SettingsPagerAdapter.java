package com.team3663.scouting_app.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SettingsPagerAdapter extends FragmentStateAdapter {
    public SettingsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) { 
        Fragment frag = new Fragment();
        switch (position){
            case 0:
                frag = new SettingsPage1();
                break;
            case 1: 
                frag = new SettingsPage2();
                break;
            case 2:
                frag = new SettingsPage3();
                break;
            case 3:
                frag = new SettingsPage4();
                break;
        }
        
        return frag;
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}