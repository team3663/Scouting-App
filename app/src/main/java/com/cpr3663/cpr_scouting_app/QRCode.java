package com.cpr3663.cpr_scouting_app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cpr3663.cpr_scouting_app.QRCode;
import com.cpr3663.cpr_scouting_app.databinding.QrCodeBinding;

public class QRCode extends AppCompatActivity {
    // =============================================================================================
    // Constants
    // =============================================================================================


    // =============================================================================================
    // Global variables
    // =============================================================================================
    private QrCodeBinding qrCodeBinding;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        qrCodeBinding = QrCodeBinding.inflate(getLayoutInflater());
        View page_root_view = qrCodeBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(qrCodeBinding.qrCode, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        
    }
}