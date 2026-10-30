package com.team3663.scouting_app.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.databinding.QrFragmentBinding;

public class QRFragment extends Fragment {
    private QrFragmentBinding binding;
    private static final String ARG_DATA = "qr_data";

    public static QRFragment newInstance(String data) {
        QRFragment fragment = new QRFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATA, data);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = QrFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            String data = getArguments().getString(ARG_DATA);
            generateQRImage(data);
        }
    }

    private void generateQRImage(String qrData) {
        BarcodeEncoder be = new BarcodeEncoder();
        try {
            Bitmap bm = be.encodeBitmap(qrData, BarcodeFormat.QR_CODE, Constants.QRCode.QR_LENGTH, Constants.QRCode.QR_LENGTH);
            binding.imageQRData.setImageBitmap(bm);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to generate QR Code!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
