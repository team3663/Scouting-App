package com.team3663.scouting_app.fragments;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.FragmentSettingsPage4Binding;
import com.team3663.scouting_app.utility.CPR_Network;

import java.util.Objects;

public class SettingsPage4 extends Fragment {
    public FragmentSettingsPage4Binding binding;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ActivityResultLauncher<Intent> googleSignInLauncher;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsPage4Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initNetwork();
        initChooseWifi();
        initGoogleSignIn();
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
    // Function:    handleGoogleDownloadResult
    // Description: Handle the result of a Google Drive download
    // Parameters:  result  the result of the download
    // Output:      void
    // =============================================================================================
    private void handleGoogleDownloadResult(CPR_Network.Result result) {
        if (result == CPR_Network.Result.TRANSMISSION_SUCCESS) {
            binding.imageGoogleResult.setImageResource(R.drawable.checkmark);
        } else {
            binding.imageGoogleResult.setImageResource(R.drawable.x);
        }

        binding.butDownload.setEnabled(true);
        binding.butDownload.setClickable(true);
        binding.butDownload.setBackgroundColor(requireContext().getColor(R.color.white));
    }

    // =============================================================================================
    // Function:    initGoogleSignIn
    // Description: Register the launcher that receives the result of the Google sign-in flow.
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initGoogleSignIn() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        GoogleSignInAccount account = GoogleSignIn
                                .getSignedInAccountFromIntent(result.getData())
                                .getResult(ApiException.class);
                        onGoogleSignedIn(account);
                    } catch (ApiException e) {
                        Toast.makeText(requireContext().getApplicationContext(), "Google sign-in failed", Toast.LENGTH_SHORT).show();
                        binding.imageGoogleResult.setImageResource(R.drawable.x);
                    }
                });
    }

    // =============================================================================================
    // Function:    initFields
    // Description: Initialize the Fields on the fragment
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void initFields() {
        String pref_Upload = Globals.sp.getString(Constants.Prefs.GOOGLE_DRIVE_UPLOAD, "");
        String pref_Download = Globals.sp.getString(Constants.Prefs.GOOGLE_DRIVE_DOWNLOAD, "");

        // Set up fields.  If the Google folders are either empty or happen to match the default, turn on the default checkbox,
        // otherwise allow them to be edited.
        if (pref_Upload.isEmpty() || pref_Upload.equals(Constants.Settings.DEFAULT_GOOGLE_UPLOAD)) {
            binding.editGoogleUpload.setText(Constants.Settings.DEFAULT_GOOGLE_UPLOAD);
            binding.butDefaultUpload.setChecked(true);
            binding.editGoogleUpload.setEnabled(false);
        } else {
            binding.editGoogleUpload.setText(pref_Upload);
            binding.butDefaultUpload.setChecked(false);
            binding.editGoogleUpload.setEnabled(true);
        }

        if (pref_Download.isEmpty() || pref_Download.equals(Constants.Settings.DEFAULT_GOOGLE_DOWNLOAD)) {
            binding.editGoogleDownload.setText(Constants.Settings.DEFAULT_GOOGLE_DOWNLOAD);
            binding.butDefaultDownload.setChecked(true);
            binding.editGoogleDownload.setEnabled(false);
        } else {
            binding.editGoogleDownload.setText(pref_Upload);
            binding.butDefaultDownload.setChecked(false);
            binding.editGoogleDownload.setEnabled(true);
        }

        // If we default the folder, set the checkbox to checked and disable the edit field and set the default folder name
        binding.butDefaultUpload.setOnClickListener(view -> {
            if (binding.butDefaultUpload.isChecked()) {
                binding.editGoogleUpload.setText(Constants.Settings.DEFAULT_GOOGLE_UPLOAD);
                binding.editGoogleUpload.setEnabled(false);
            } else {
                binding.editGoogleUpload.setEnabled(true);
            }
        });

        // If we default the folder, set the checkbox to checked and disable the edit field and set the default folder name
        binding.butDefaultDownload.setOnClickListener(view -> {
            if (binding.butDefaultDownload.isChecked()) {
                binding.editGoogleDownload.setText(Constants.Settings.DEFAULT_GOOGLE_DOWNLOAD);
                binding.editGoogleDownload.setEnabled(false);
            } else {
                binding.editGoogleDownload.setEnabled(true);
            }
        });

        // Listen for a button click
        binding.butDownload.setOnClickListener(view -> {
            binding.butDownload.setEnabled(false);
            binding.butDownload.setClickable(false);
            binding.butDownload.setBackgroundColor(requireContext().getColor(R.color.light_grey));

            // If the Drive service is already built this session, upload straight away
            if (Globals.network.isDriveServiceReady()) {
                Globals.network.downloadFromGoogle(this::handleGoogleDownloadResult);
                return;
            }

            // Reuse an existing sign-in if it already granted the Drive scope
            Scope driveScope = new Scope(CPR_Network.GOOGLE_DRIVE_SCOPE);
            GoogleSignInAccount last = GoogleSignIn.getLastSignedInAccount(requireContext().getApplicationContext());
            if (GoogleSignIn.hasPermissions(last, driveScope)) {
                onGoogleSignedIn(last);
                return;
            }

            // Otherwise start the interactive sign-in / consent flow
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(driveScope)
                    .build();
            googleSignInLauncher.launch(GoogleSignIn.getClient(requireContext().getApplicationContext(), gso).getSignInIntent());

        });
    }

    // =============================================================================================
    // Function:    onGoogleSignedIn
    // Description: Build the Drive service from the signed-in account and start the upload.
    // Parameters:  in_account  the account returned from Google sign-in
    // Output:      void
    // =============================================================================================
    private void onGoogleSignedIn(GoogleSignInAccount in_account) {
        if (in_account == null || in_account.getAccount() == null) {
            Toast.makeText(requireContext().getApplicationContext(), "Google sign-in failed", Toast.LENGTH_SHORT).show();
            binding.imageGoogleResult.setImageResource(R.drawable.x);
            return;
        }

        Globals.network.initDriveService(in_account.getAccount());
        Globals.network.downloadFromGoogle(this::handleGoogleDownloadResult);
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