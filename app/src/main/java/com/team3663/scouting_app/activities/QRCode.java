package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.QrCodeBinding;
import com.team3663.scouting_app.fragments.QRPagerAdapter;

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
    private QR_FileString qrFileString;

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
    }

    // =============================================================================================
    // Function:    InitQRData
    // Description: Initialize the ViewPager and adapter for QR image pages
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    private void InitQRData() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.preference_setting_file_key), Context.MODE_PRIVATE);
        int currentQRSize = sp.getInt(Constants.Prefs.QR_SIZE, Constants.QRCode.QR_SIZE_DEFAULT);
        if (currentQRSize <= 0) currentQRSize = Constants.QRCode.QR_SIZE_DEFAULT;

        qrFileString = new QR_FileString(Globals.CurrentCompetitionId + "_" + Globals.TransmitMatchNum + "_" + Globals.CurrentDeviceId + "_" + Globals.TransmitMatchType + ".csv" + Constants.Logger.FILE_LINE_SEPARATOR +
                getFileAsString() + Constants.Logger.FILE_LINE_SEPARATOR +
                Constants.QRCode.EOF + Constants.Logger.FILE_LINE_SEPARATOR, currentQRSize);

        QRPagerAdapter adapter = new QRPagerAdapter(this, qrFileString.getPages());
        qrCodeBinding.viewPagerQR.setAdapter(adapter);

        new TabLayoutMediator(qrCodeBinding.tabLayoutQR, qrCodeBinding.viewPagerQR,
                (tab, position) -> {} // No text needed for dot indicators
        ).attach();

        qrCodeBinding.textImagePage.setText(String.format("Image 1 of %d", qrFileString.getNumPages()));

        qrCodeBinding.viewPagerQR.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                qrCodeBinding.textImagePage.setText(String.format("Image %d of %d", position + 1, qrFileString.getNumPages()));
            }
        });
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
            Globals.numStartingGamePiece = Constants.PreMatch.STARTING_GAME_PIECES;
            Globals.isPractice = false;

            // Reset post-Match values for next time
            Globals.CurrentAccuracy = Constants.PostMatch.ACCURACY_NOT_SELECTED;
            Globals.CurrentClimbLevel = Constants.PostMatch.CLIMB_LEVEL_NOT_SELECTED;
            Globals.CurrentClimbPosition = Constants.PostMatch.CLIMB_POSITION_NOT_SELECTED;
            Globals.stealFuelValue = Constants.PostMatch.STEAL_FUEL_NOT_SELECTED;
            Globals.affectedByDefenseValue = Constants.PostMatch.AFFECTED_BY_DEFENSE_NOT_SELECTED;

            // Increases the team number so that it autofills for the next match correctly
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
        qrCodeBinding.textFileStatsFileSize.setText(String.format("%d bytes", qrFileString.getSize()));
    }

    // =============================================================================================
    // Function:    getFileAsString
    // Description: Reads the match CSV data file and returns its content as a String
    // Parameters:  none
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
    // Class:       QR_FileString
    // Description: Defines a structure/class to hold the information for the file data we'll use
    //              to generate the QR codes
    // =============================================================================================
    private static class QR_FileString {
        private final ArrayList<String> file_page = new ArrayList<>();
        private final int size;

        // Constructor
        public QR_FileString(String in_data, int qrSize) {
            size = in_data.length();
            int begin = 0;
            int end;

            int effectiveQrSize = qrSize > 0 ? qrSize : Constants.QRCode.QR_SIZE_DEFAULT;

            while (begin < size) {
                if (size - begin <= effectiveQrSize) {
                    end = size;
                } else {
                    end = begin + (effectiveQrSize * Constants.QRCode.QR_PREFERRED_SIZE_PERCENTAGE / 100);
                }

                file_page.add(in_data.substring(begin, end));
                begin = end;
            }
        }

        public ArrayList<String> getPages() {
            return file_page;
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
