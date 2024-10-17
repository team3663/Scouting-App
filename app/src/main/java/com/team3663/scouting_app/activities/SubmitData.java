package com.team3663.scouting_app.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.SubmitDataBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class SubmitData extends AppCompatActivity {
    // =============================================================================================
    // Global variables
    // =============================================================================================
    private SubmitDataBinding submitDataBinding;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        submitDataBinding = SubmitDataBinding.inflate(getLayoutInflater());
        View page_root_view = submitDataBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(submitDataBinding.qrCode, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Keep Achievements invisible
        submitDataBinding.imageAchievement.setVisibility(View.INVISIBLE);
        submitDataBinding.textAchievement.setVisibility(View.INVISIBLE);

//        Animation animation = AnimationUtils.loadAnimation(getApplicationContext()
//                , R.anim.blink);
//        submitDataBinding.imageAchievement.startAnimation(animation);
//        submitDataBinding.imageAchievement.clearAnimation();

        // Adds the items from the match log files array to the list
        ArrayAdapter<String> adp_Match = new ArrayAdapter<String>(this,
                R.layout.cpr_spinner, FindMatches());
        adp_Match.setDropDownViewResource(R.layout.cpr_spinner_item);
        submitDataBinding.spinnerMatch.setAdapter(adp_Match);
        // Set the selection (if there are any) to the latest match (largest value in the list)
        if (adp_Match.getCount() > 0) submitDataBinding.spinnerMatch.setSelection(adp_Match.getCount() - 1, true);

        submitDataBinding.spinnerMatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // If we don't have both data files, display a message.  Otherwise clear it.
                if (!checkDataFiles(submitDataBinding.spinnerMatch.getSelectedItem().toString()))
                    submitDataBinding.textMatchMessage.setText(getString(R.string.submit_match_error));
                else
                    submitDataBinding.textMatchMessage.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        submitDataBinding.butQuit.setOnClickListener(view -> new AlertDialog.Builder(view.getContext())
        .setTitle(getString(R.string.submit_alert_quit_title))
        .setMessage(getString(R.string.submit_alert_quit_message))

        // Specifying a listener allows you to take an action before dismissing the dialog.
        // The dialog is automatically dismissed when a dialog button is clicked.
        .setPositiveButton(getString(R.string.submit_alert_quit_positive), (dialog, which) -> {
            SubmitData.this.finishAffinity();
            System.exit(0);
        })

        // A null listener allows the button to dismiss the dialog and take no further action.
        .setNegativeButton(getString(R.string.submit_alert_cancel), null)
        // TODO make the icon work
//                .setIcon(getDrawable(android.R.attr.alertDialogIcon))
        .show());

        submitDataBinding.butNext.setOnClickListener(view -> {
            Intent GoToPreMatch = new Intent(SubmitData.this, PreMatch.class);
            startActivity(GoToPreMatch);

            finish();
        });
    }

    // =============================================================================================
    // Function:    FindMatches
    // Description: Search through the output csv files (logger files) and parse through them to get
    //              a list of matches that we can submit from.
    // Parameters:  void
    // Output:      ArrayList<String> - list of match numbers sorts numerically
    // =============================================================================================
    private ArrayList<String> FindMatches() {
        ArrayList<Integer> ret_int = new ArrayList<>();
        ArrayList<String> ret = new ArrayList<>();

        // Open the output directory and get all filenames that end with "*d.csv"
        String path = getString(R.string.logger_path);

        // Ensure the path (if it's not blank) has a trailing delimiter
        if (!path.isEmpty()) {
            if (!path.endsWith("/")) path = path + "/";
        }

        // Define the filenames/files to be used for this logger
        File parent = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), path + "dummy").getParentFile();

        // Ensure the directory structure exists first - if not, return nothing
        if ((parent == null) || !parent.isDirectory()) return ret;

        // Get the list of files
        File[] file_list = parent.listFiles();

        // If there's no files, return nothing
        if ((file_list == null) || (file_list.length == 0)) return ret;

        // Parse out the match number from the filename.  If this is a "d" file from the right
        // competition (as defined in Settings) then add it to the list.
        for (File file : file_list) {
            if (file.isFile() && file.getName().endsWith("d.csv")) {
                String[] file_parts = file.getName().split("_");
                if (Integer.parseInt(file_parts[0]) == Globals.CurrentCompetitionId)
                    ret_int.add(Integer.parseInt(file_parts[1]));
            }
        }

        // Sort the list (numerically) and then copy into the String version
        Collections.sort(ret_int);
        for (Integer i : ret_int) ret.add(i.toString());
        return ret;
    }

    // =============================================================================================
    // Function:    checkDataFiles
    // Description: Ensures that there is a data file "e" (we already looked for all "d" ones).
    //              We assume that we already called FindMatches() which did a lot of error checking
    //              so we'll skip ones that have been checked already.
    // Parameters:  void
    // Output:      Boolean - return true if "e" file found.  False if no "e" file found.
    // =============================================================================================
    private Boolean checkDataFiles(String in_match) {
        // Open the output directory and get all filenames that end with "*d.csv"
        String path = getString(R.string.logger_path);

        // Ensure the path (if it's not blank) has a trailing delimiter
        if (!path.isEmpty()) {
            if (!path.endsWith("/")) path = path + "/";
        }

        // Define the filename to check
        File e_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), path + Globals.CurrentCompetitionId + "_" + in_match + "_" + Globals.CurrentDeviceId + "_e.csv" );

        // Ensure the directory structure exists first - if not, return nothing
        return e_file.exists() && e_file.isFile();
    }
}