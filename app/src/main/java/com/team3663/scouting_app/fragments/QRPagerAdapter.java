package com.team3663.scouting_app.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class QRPagerAdapter extends FragmentStateAdapter {
    private final ArrayList<String> qrDataPages;

    public QRPagerAdapter(@NonNull FragmentActivity fragmentActivity, ArrayList<String> qrDataList) {
        super(fragmentActivity);
        this.qrDataPages = qrDataList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return QRFragment.newInstance(qrDataPages.get(position));
    }

    @Override
    public int getItemCount() {
        return qrDataPages.size();
    }
}
