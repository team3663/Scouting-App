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
import androidx.documentfile.provider.DocumentFile;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.data.MatchTypes;
import com.team3663.scouting_app.databinding.QrCodeBinding;
import com.team3663.scouting_app.utility.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class QRCode extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private QrCodeBinding qrCodeBinding;
    private Logger logger;

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
        InitBack();
        InitNext();
     }

    // =============================================================================================
    // Function:    InitQREvent
    // Description: Initialize the QR image for the Event file
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitQREvent() {
        BarcodeEncoder be = new BarcodeEncoder();
        String qrDataEvent= Globals.CurrentCompetitionId + "_" + Globals.transmitMatchNum + "_" + Globals.CurrentDeviceId + "_" + Globals.MatchTypeList.getMatchTypeShortForm(Globals.CurrentMatchType) + "_e.csv" + "\n" + getFileAsString("e") + "\n" + Constants.QRCode.EOF;

        if (qrDataEvent.length()> Constants.QRCode.MAX_QR_DATA_SIZE){
            Toast.makeText(QRCode.this, " Data file is too big for this method", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Bitmap bm = be.encodeBitmap(qrDataEvent, BarcodeFormat.QR_CODE,Constants.QRCode.QR_LENGTH, Constants.QRCode.QR_LENGTH);
            qrCodeBinding.imageQREvent.setImageBitmap(bm);
        } catch (Exception e) {
            Toast.makeText(QRCode.this, "Failed to generate QR Code!", Toast.LENGTH_LONG).show();
        }
    }

    // =============================================================================================
    // Function:    InitQRData
    // Description: Initialize the QR image for the Data file
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitQRData() {
        BarcodeEncoder be = new BarcodeEncoder();
        String qrData = Globals.CurrentCompetitionId + "_" + Globals.transmitMatchNum + "_" + Globals.CurrentDeviceId + "_" + Globals.MatchTypeList.getMatchTypeShortForm(Globals.transmitMatchType) + "_d.csv" + "\n" + getFileAsString("d") + "\n" + Constants.QRCode.EOF;

        if (qrData.length()> Constants.QRCode.MAX_QR_DATA_SIZE){
            Toast.makeText(QRCode.this, " Data file is too big for this method", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Bitmap bm = be.encodeBitmap(qrData, BarcodeFormat.QR_CODE, Constants.QRCode.QR_LENGTH, Constants.QRCode.QR_LENGTH);
            qrCodeBinding.imageQRData.setImageBitmap(bm);
        } catch (Exception e) {
            Toast.makeText(QRCode.this, "Failed to generate QR Code!", Toast.LENGTH_LONG).show();
        }
    }

    // =============================================================================================
    // Function:    InitBack
    // Description: Initialize the Back button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitBack() {
        qrCodeBinding.butBack.setOnClickListener(view -> {
            Intent GoToSubmitData = new Intent(QRCode.this, SubmitData.class);
            startActivity(GoToSubmitData);

            finish();
        });
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
            Globals.isStartingGamePiece = true;
            Globals.isPractice = false;

            Intent GoToPreMatch = new Intent(QRCode.this, PreMatch.class);
            startActivity(GoToPreMatch);

            finish();
        });
    }

    // =============================================================================================
    // Function:    getFileAsString
    // Description: Initialize the Next Match button
    // Parameters:  in_Match_ID
    //                  Match number that we want to transmit
    //              in_Extension
    //                  type of file ("d" or "e") we want to convert to a string
    // Output:      String representing the entire contents of the file
    // =============================================================================================
    public String getFileAsString(String in_Extension) {
        // Validate we have a proper extension
        if (!(in_Extension.equals("d") || in_Extension.equals("e")))
            return "";

        String filename = Globals.CurrentCompetitionId + "_" + Globals.transmitMatchNum + "_" + Globals.CurrentDeviceId + "_" + Globals.MatchTypeList.getMatchTypeShortForm(Globals.transmitMatchType) + "_" + in_Extension + ".csv";
        String file_as_string = "";
        String line;

        try {
            // Open up the correct input stream
            InputStream is;
            DocumentFile df = Globals.output_df.findFile(filename);
            assert df != null;
            is = this.getContentResolver().openInputStream(df.getUri());

            // Read in the data
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                if (!file_as_string.isEmpty())
                    file_as_string += "\n";

                file_as_string += line;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return file_as_string;
    }
}
