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
import com.team3663.scouting_app.databinding.QrCodeBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class QRCode extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private QrCodeBinding qrCodeBinding;
    static private QR_FileString qrFileString;
    static private int currentImagePage;

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

        Globals.DebugLogger.In("QRCode:onCreate");

        // Initialize activity components
        InitQRData();
        InitBack();
        InitNext();
        InitFileStats();
        InitNextImage();
        InitPrevImage();

        Globals.DebugLogger.Out();
     }

    // =============================================================================================
    // Function:    InitQRData
    // Description: Initialize the QR image for the Data file
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitQRData() {
        Globals.DebugLogger.In("QRCode:InitQRData");

        qrFileString = new QR_FileString(Globals.CurrentCompetitionId + "_" + Globals.TransmitMatchNum + "_" + Globals.CurrentDeviceId + "_" + Globals.MatchTypeList.getMatchTypeShortForm(Globals.TransmitMatchType) + "_d.csv" + "\n" +
                getFileAsString("d") + "\n" +
                Constants.QRCode.EOF + "\n" +
                Globals.CurrentCompetitionId + "_" + Globals.TransmitMatchNum + "_" + Globals.CurrentDeviceId + "_" + Globals.MatchTypeList.getMatchTypeShortForm(Globals.CurrentMatchType) + "_e.csv" + "\n" +
                getFileAsString("e") + "\n" +
                Constants.QRCode.EOF);

        qrCodeBinding.butPrevImage.setEnabled(false);
        qrCodeBinding.butPrevImage.setClickable(false);
        qrCodeBinding.butPrevImage.setVisibility(View.INVISIBLE);
        if (qrFileString.getNumPages() > 1) {
            qrCodeBinding.butNextImage.setEnabled(true);
            qrCodeBinding.butNextImage.setClickable(true);
            qrCodeBinding.butNextImage.setVisibility(View.VISIBLE);
        }
        else {
            qrCodeBinding.butNextImage.setEnabled(false);
            qrCodeBinding.butNextImage.setClickable(false);
            qrCodeBinding.butNextImage.setVisibility(View.INVISIBLE);
        }

        qrCodeBinding.textImagePage.setText(String.format("Image 1 of %s", String.valueOf(qrFileString.getNumPages())));
        currentImagePage = 0;
        generateQRImage();

        Globals.DebugLogger.Out();
    }

    // =============================================================================================
    // Function:    InitBack
    // Description: Initialize the Back button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitBack() {
        Globals.DebugLogger.In("QRCode:InitBack");

        qrCodeBinding.butBack.setOnClickListener(view -> {
            Globals.DebugLogger.In("QRCode:butBack:Click");

            Intent GoToSubmitData = new Intent(QRCode.this, SubmitData.class);
            startActivity(GoToSubmitData);

            qrFileString = null;

            Globals.DebugLogger.Out();
            finish();
        });

        Globals.DebugLogger.Out();
    }

    // =============================================================================================
    // Function:    InitNext
    // Description: Initialize the Next Match button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitNext() {
        Globals.DebugLogger.In("QRCode:InitNext");

        qrCodeBinding.butNext.setOnClickListener(view -> {
            Globals.DebugLogger.In("QRCode:butNext:Click");

            // Reset pre-Match settings for next time
            Globals.isStartingGamePiece = true;
            Globals.isPractice = false;

            // Increases the team number so that it auto fills for the next match correctly
            Globals.CurrentMatchNumber++;

            Intent GoToPreMatch = new Intent(QRCode.this, PreMatch.class);
            startActivity(GoToPreMatch);

            qrFileString = null;

            Globals.DebugLogger.Out();
            finish();
        });

        Globals.DebugLogger.Out();
    }

    // =============================================================================================
    // Function:    InitFileStats
    // Description: Initialize the File Stats fields
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitFileStats() {
        Globals.DebugLogger.In("QRCode:InitFileStats");

        qrCodeBinding.textFileStatsCompetition.setText(Globals.CompetitionList.getCompetitionDescription(Globals.CurrentCompetitionId));
        qrCodeBinding.textFileStatsMatch.setText(String.valueOf(Globals.TransmitMatchNum));
        qrCodeBinding.textFileStatsMatchType.setText(Globals.MatchTypeList.getMatchTypeDescription(Globals.TransmitMatchType));
        qrCodeBinding.textFileStatsFileSize.setText(String.format("%s bytes", String.valueOf(qrFileString.getSize())));

        Globals.DebugLogger.Out();
    }

    // =============================================================================================
    // Function:    InitNextImage
    // Description: Initialize the Next Match button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitNextImage() {
        Globals.DebugLogger.In("QRCode:InitNextImage");

        qrCodeBinding.butNextImage.setOnClickListener(view -> {
            Globals.DebugLogger.In("QRCode:butNextImage:Click");

            currentImagePage++;
            qrCodeBinding.textImagePage.setText(String.format("Image %s of %s", String.valueOf(currentImagePage + 1), String.valueOf(qrFileString.getNumPages())));
            qrCodeBinding.butPrevImage.setEnabled(true);
            qrCodeBinding.butPrevImage.setClickable(true);
            qrCodeBinding.butPrevImage.setVisibility(View.VISIBLE);

            if (currentImagePage < qrFileString.getNumPages() - 1) {
                qrCodeBinding.butNextImage.setEnabled(true);
                qrCodeBinding.butNextImage.setClickable(true);
                qrCodeBinding.butNextImage.setVisibility(View.VISIBLE);
            }
            else {
                qrCodeBinding.butNextImage.setEnabled(false);
                qrCodeBinding.butNextImage.setClickable(false);
                qrCodeBinding.butNextImage.setVisibility(View.INVISIBLE);
            }

            generateQRImage();

            Globals.DebugLogger.Out();
        });

        Globals.DebugLogger.Out();
    }

    // =============================================================================================
    // Function:    InitPrevImage
    // Description: Initialize the Next Match button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitPrevImage() {
        Globals.DebugLogger.In("QRCode:InitPrevImage");

        qrCodeBinding.butPrevImage.setOnClickListener(view -> {
            Globals.DebugLogger.In("QRCode:butPrevImage:Click");

            currentImagePage--;
            qrCodeBinding.textImagePage.setText(String.format("Image %s of %s", String.valueOf(currentImagePage + 1), String.valueOf(qrFileString.getNumPages())));
            qrCodeBinding.butNextImage.setEnabled(true);
            qrCodeBinding.butNextImage.setClickable(true);
            qrCodeBinding.butNextImage.setVisibility(View.VISIBLE);

            if (currentImagePage > 0) {
                qrCodeBinding.butPrevImage.setEnabled(true);
                qrCodeBinding.butPrevImage.setClickable(true);
                qrCodeBinding.butPrevImage.setVisibility(View.VISIBLE);
            }
            else {
                qrCodeBinding.butPrevImage.setEnabled(false);
                qrCodeBinding.butPrevImage.setClickable(false);
                qrCodeBinding.butPrevImage.setVisibility(View.INVISIBLE);
            }

            generateQRImage();

            Globals.DebugLogger.Out();
        });

        Globals.DebugLogger.Out();
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
        Globals.DebugLogger.Params.add("in_Extension=" + in_Extension);
        Globals.DebugLogger.In("QRCode:getFileAsString");

        // Validate we have a proper extension
        if (!(in_Extension.equals("d") || in_Extension.equals("e")))
            return "";

        String filename = Globals.CurrentCompetitionId + "_" + Globals.TransmitMatchNum + "_" + Globals.CurrentDeviceId + "_" + Globals.MatchTypeList.getMatchTypeShortForm(Globals.TransmitMatchType) + "_" + in_Extension + ".csv";
        StringBuilder file_as_string = new StringBuilder();
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
                if (file_as_string.length() > 0)
                    file_as_string.append("\n");

                file_as_string.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Globals.DebugLogger.Out();
        return file_as_string.toString();
    }

    // =============================================================================================
    // Function:    generateQRImage
    // Description: Generate the QR code for the current "page" of data
    // Parameters:  in_QRFileString
    //                  The structure representing what we need to generate the QR code for
    //              in_Page
    //                  The "page" of data we need to generate the QR code for
    // Output:      void
    // =============================================================================================
    public void generateQRImage() {
        Globals.DebugLogger.In("QRCode:generateQRImage");

        BarcodeEncoder be = new BarcodeEncoder();
        String qrData = qrFileString.getPage(currentImagePage);

        try {
            Bitmap bm = be.encodeBitmap(qrData, BarcodeFormat.QR_CODE, Constants.QRCode.QR_LENGTH, Constants.QRCode.QR_LENGTH);
            qrCodeBinding.imageQRData.setImageBitmap(bm);
        } catch (Exception e) {
            Toast.makeText(QRCode.this, "Failed to generate QR Code!", Toast.LENGTH_LONG).show();
        }

        Globals.DebugLogger.Out();
    }

    // =============================================================================================
    // Class:       QR_FileString
    // Description: Defines a structure/class to hold the information for the file data we'll use
    //              to generate the QR codes
    // =============================================================================================
    private static class QR_FileString {
        ArrayList<String> file_page = new ArrayList<>();
        int size = 0;

        // Constructor
        public QR_FileString (String in_data) {
            Globals.DebugLogger.In("QRCode:QR_FileString:constructor");

            size = in_data.length();
            int begin = 0;
            int end;

            while (begin < size) {
                if (size - begin <= Constants.QRCode.PREFERRED_QR_DATA_SIZE) end = size;
                else end = begin + Constants.QRCode.PREFERRED_QR_DATA_SIZE;

                file_page.add(in_data.substring(begin, end));
                begin = end;
            }

            Globals.DebugLogger.Out();
        }

        // Member Function: Return the (partial) String for the data for a particular page
        public String getPage(int in_Page) {
            Globals.DebugLogger.Params.add("in_Page=" + in_Page);
            Globals.DebugLogger.In("QRCode:QR_FileString:getPage");

            if (file_page == null) return "";

            if ((in_Page < 0) || (in_Page >= file_page.size()))
                return "";

            Globals.DebugLogger.Out();
            return file_page.get(in_Page);
        }

        // Member Function: Return the number of pages of data we have
        public int getNumPages() {
            Globals.DebugLogger.In("QRCode:QR_FileString:getNumPages");

            if (file_page == null) return 0;

            Globals.DebugLogger.Out();
            return file_page.size();
        }

        // Member Function: Return the number of pages of data we have
        public int getSize() {return size; }
    }
}
