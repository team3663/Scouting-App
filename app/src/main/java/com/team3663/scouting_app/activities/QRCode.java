package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.QrCodeBinding;

public class QRCode extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private QrCodeBinding qrCodeBinding;
    private final int QR_SIZE = 390;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle in_savedInstanceState) {
        super.onCreate(in_savedInstanceState);
        EdgeToEdge.enable(this);
        qrCodeBinding = QrCodeBinding.inflate(getLayoutInflater());
        View page_root_view = qrCodeBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(qrCodeBinding.qrCode, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize activity components

        InitQRData();
        InitQREvent();
        InitNext();
     }

    private void InitQREvent() {
        BarcodeEncoder be = new BarcodeEncoder();

        try {
            Bitmap bm = be.encodeBitmap("this is a test of a qr code generated data", BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            qrCodeBinding.imageQREvent.setImageBitmap(bm);
        } catch (Exception e) {
            Toast.makeText(QRCode.this, "Failed to generate QR Code!", Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }
    }

    private void InitQRData() {
        BarcodeEncoder be = new BarcodeEncoder();

        try {
            Bitmap bm = be.encodeBitmap("this is a test of a qr code generated data", BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
            qrCodeBinding.imageQRData.setImageBitmap(bm);
        } catch (Exception e) {
            Toast.makeText(QRCode.this, "Failed to generate QR Code!", Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }
    }

    // =============================================================================================
    // Function:    initNext
    // Description: Initialize the Next Match button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitNext() {
        qrCodeBinding.butNext.setOnClickListener(view -> {
            // Reset pre-Match settings for next time
            Globals.isStartingNote = true;
            Globals.isPractice = false;

            Intent GoToPreMatch = new Intent(QRCode.this, PreMatch.class);
            startActivity(GoToPreMatch);

            finish();
        });
    }
}
