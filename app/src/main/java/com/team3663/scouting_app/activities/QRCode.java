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

        // Initialize activity components
        InitQRData();
        InitBack();
        InitNext();
        InitFileStats();
        InitNextImage();
        InitPrevImage();
     }

    // =============================================================================================
    // Function:    InitQRData
    // Description: Initialize the QR image for the Data file
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitQRData() {
        qrFileString = new QR_FileString(Globals.CurrentCompetitionId + "_" + Globals.TransmitMatchNum + "_" + Globals.CurrentDeviceId + "_" + Globals.TransmitMatchType + ".csv" + Constants.Logger.FILE_LINE_SEPARATOR +
                getFileAsString() + Constants.Logger.FILE_LINE_SEPARATOR +
                Constants.QRCode.EOF + Constants.Logger.FILE_LINE_SEPARATOR);

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

        qrCodeBinding.textImagePage.setText(String.format("Image 1 of %s", qrFileString.getNumPages()));
        currentImagePage = 0;
        generateQRImage();
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

            qrFileString = null;
            finish();
        });
    }

    // =============================================================================================
    // Function:    InitNext
    // Description: Initialize the Next Match button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitNext() {
        qrCodeBinding.butNext.setOnClickListener(view -> {
            // Reset pre-Match settings for next time
            Globals.isStartingGamePiece = true;
            Globals.isPractice = false;

            // Increases the team number so that it auto fills for the next match correctly
            Globals.CurrentMatchNumber++;

            Intent GoToPreMatch = new Intent(QRCode.this, PreMatch.class);
            startActivity(GoToPreMatch);

            qrFileString = null;
            finish();
        });
    }

    // =============================================================================================
    // Function:    InitFileStats
    // Description: Initialize the File Stats fields
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitFileStats() {
        qrCodeBinding.textFileStatsCompetition.setText(Globals.CompetitionList.getCompetitionDescription(Globals.CurrentCompetitionId));
        qrCodeBinding.textFileStatsMatch.setText(String.valueOf(Globals.TransmitMatchNum));
        qrCodeBinding.textFileStatsMatchType.setText(Globals.MatchTypeList.getMatchTypeDescription(Globals.TransmitMatchType));
        qrCodeBinding.textFileStatsFileSize.setText(String.format("%s bytes", qrFileString.getSize()));
    }

    // =============================================================================================
    // Function:    InitNextImage
    // Description: Initialize the Next Image button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitNextImage() {
        qrCodeBinding.butNextImage.setOnClickListener(view -> {
            currentImagePage++;
            qrCodeBinding.textImagePage.setText(String.format("Image %s of %s", currentImagePage + 1, qrFileString.getNumPages()));
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
        });
    }

    // =============================================================================================
    // Function:    InitPrevImage
    // Description: Initialize the Previous Image button
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitPrevImage() {
        qrCodeBinding.butPrevImage.setOnClickListener(view -> {
            currentImagePage--;
            qrCodeBinding.textImagePage.setText(String.format("Image %s of %s", currentImagePage + 1, qrFileString.getNumPages()));
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
    public String getFileAsString() {
        String filename = Globals.CurrentCompetitionId + "_" + Globals.TransmitMatchNum + "_" + Globals.CurrentDeviceId + "_" + Globals.TransmitMatchType + ".csv";
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
                    file_as_string.append(Constants.Logger.FILE_LINE_SEPARATOR);

                file_as_string.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        BarcodeEncoder be = new BarcodeEncoder();
        String qrData = qrFileString.getPage(currentImagePage);

        try {
            Bitmap bm = be.encodeBitmap(qrData, BarcodeFormat.QR_CODE, Constants.QRCode.QR_LENGTH, Constants.QRCode.QR_LENGTH);
            qrCodeBinding.imageQRData.setImageBitmap(bm);
        } catch (Exception e) {
            Toast.makeText(QRCode.this, "Failed to generate QR Code!", Toast.LENGTH_LONG).show();
        }
    }

    // =============================================================================================
    // Class:       QR_FileString
    // Description: Defines a structure/class to hold the information for the file data we'll use
    //              to generate the QR codes
    // =============================================================================================
    private static class QR_FileString {
        ArrayList<String> file_page = new ArrayList<>();
        int size;

        // Constructor
        public QR_FileString (String in_data) {
            size = in_data.length();
            int begin = 0;
            int end;

            while (begin < size) {
                if (size - begin <= Globals.CurrentQRSize) end = size;
                else end = begin + Globals.CurrentQRSize;

                file_page.add(in_data.substring(begin, end));
                begin = end;
            }
        }

        // Member Function: Return the (partial) String for the data for a particular page
        public String getPage(int in_Page) {
            if (file_page == null) return "";

            if ((in_Page < 0) || (in_Page >= file_page.size()))
                return "";

            return file_page.get(in_Page);
        }

        // Member Function: Return the number of pages of data we have
        public int getNumPages() {
            if (file_page == null) return 0;

            return file_page.size();
        }

        // Member Function: Return the number of pages of data we have
        public int getSize() {
            return size;
        }
    }
}
