package com.team3663.scouting_app.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.FragmentSettingsPage3Binding;

public class SettingsPage3 extends Fragment {
    public FragmentSettingsPage3Binding binding;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsPage3Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initNetwork();
        initChooseWifi();
        initRefresh();
        initFields();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Important: Unregister to avoid memory leaks
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    // =============================================================================================
    // Function:    initFields
    // Description: Initialize the Fields on the fragment
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initFields() {
        binding.editServer.setText(Globals.sp.getString(Constants.Prefs.SQL_SERVER, "mssql01.cpr3663.io"));
        binding.editDatabase.setText(Globals.sp.getString(Constants.Prefs.SQL_DATABASE, "CPR_Scouting_2025"));
        binding.editUser.setText(Globals.sp.getString(Constants.Prefs.SQL_USER, "CPR_Tablet"));
        binding.editPassword.setText(Globals.sp.getString(Constants.Prefs.SQL_PASSWORD, "Aa98o1nTlHb4sg2u0YB2eNHxVfU4n17z"));
    }

    // =============================================================================================
    // Function:    initRefresh
    // Description: Initialize the Refresh button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initRefresh() {
        binding.butRefresh.setOnClickListener(view -> {
            if (Globals.network.hasActiveInternet()) {
                binding.imageInternet.setVisibility(View.VISIBLE);
            } else {
                binding.imageInternet.setVisibility(View.INVISIBLE);
            }
        });
    }

    // =============================================================================================
    // Function:    initChooseWifi
    // Description: Initialize the ChooseWifi button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initChooseWifi() {
        binding.butChoose.setOnClickListener(view -> Globals.network.pickWIFI());
    }

    // =============================================================================================
    // Function:    updateWifiIcon
    // Description: Update the Wi-Fi signal icon with the correct signal strength level
    // Parameters:  in_level    signal strength level
    // Output:      void
    // =============================================================================================
    private void updateWifiIcon(int in_level) {
        // You would typically swap icons here based on level
        // 0: No signal, 1-4: Signal bars
        switch (in_level) {
            case 4: binding.imageWifiSignal.setImageResource(R.drawable.wifi_bar_4); break;
            case 3: binding.imageWifiSignal.setImageResource(R.drawable.wifi_bar_3); break;
            case 2: binding.imageWifiSignal.setImageResource(R.drawable.wifi_bar_2); break;
            case 1: binding.imageWifiSignal.setImageResource(R.drawable.wifi_bar_1); break;
            default: binding.imageWifiSignal.setImageResource(R.drawable.wifi_bar_0); break;
        }

        // check if we have access to the internet
        if (Globals.network.hasActiveInternet()) {
            binding.imageInternet.setVisibility(View.VISIBLE);
        } else {
            binding.imageInternet.setVisibility(View.INVISIBLE);
        }
    }

    // =============================================================================================
    // Function:    initNetwork
    // Description: Initialize the network related fields and process
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initNetwork() {
        // setup Wi-Fi monitoring
        connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(@NonNull Network in_network, @NonNull NetworkCapabilities in_capabilities) {
                int rssi = 0;

                // On API 31+, WifiInfo is part of the capabilities (requires location permission)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    WifiInfo wifiInfo = (WifiInfo) in_capabilities.getTransportInfo();
                    if (wifiInfo != null) rssi = wifiInfo.getRssi();
                } else {
                    // Fallback for API 30
                    WifiManager wm = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    rssi = wm.getConnectionInfo().getRssi();
                }

                // Convert RSSI to a signal level (0 to 4)
                int level = WifiManager.calculateSignalLevel(rssi, 5);

                // Update UI on the main thread
                requireActivity().runOnUiThread(() -> updateWifiIcon(level));
            }

            @Override
            public void onLost(@NonNull Network in_network) {
                // Signal lost or Wi-Fi turned off
                requireActivity().runOnUiThread(() -> updateWifiIcon(-1));
            }
        };

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }
}