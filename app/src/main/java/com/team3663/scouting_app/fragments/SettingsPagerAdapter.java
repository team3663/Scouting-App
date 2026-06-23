package com.team3663.scouting_app.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SettingsPagerAdapter extends FragmentStateAdapter {
    private SettingsPage1 fragmentPage1;
    private SettingsPage2 fragmentPage2;

    public SettingsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) { 
        Fragment frag = new Fragment();
        switch (position){
            case 0:
                fragmentPage1 = new SettingsPage1();
                frag = fragmentPage1;
                break;
            case 1: 
                fragmentPage2 = new SettingsPage2();
                frag = fragmentPage2;
                break;
        }
        
        return frag;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public SettingsPage1 getFragmentPage1() {
        return fragmentPage1;
    }

    public SettingsPage2 getFragmentPage2() {
        return fragmentPage2;
    }
}