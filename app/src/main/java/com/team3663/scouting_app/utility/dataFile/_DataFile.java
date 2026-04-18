package com.team3663.scouting_app.utility.dataFile;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.activities.AppLaunch;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public abstract class _DataFile {
    private static final List<_DataFile> subclasses = new ArrayList<>();
    private static final HashMap<Integer, String> file_list = new HashMap<>();
    private static final HashMap<Integer, String> error_message_list = new HashMap<>();
    private static final HashMap<Integer, String> loading_message_list = new HashMap<>();

    protected final int file_index;
    protected Context context;

    public _DataFile(@NonNull Context in_context, @NonNull String in_filename, @NonNull String in_loading_message, @NonNull String in_error_message) {
        // make sure we aren't being called without the required data
        if (in_filename.trim().isEmpty() || in_loading_message.trim().isEmpty() || in_error_message.trim().isEmpty()) {
            file_index = -1;
            return;
        }

        // save off things that are unique to the subclass and add this subclass instance to the registered list
        context = Objects.requireNonNull(in_context, "Context cannot be null");
        file_index = file_list.size() + 1;
        file_list.put(file_index, Objects.requireNonNull(in_filename, "Filename cannot be null"));
        error_message_list.put(file_index, Objects.requireNonNull(in_error_message, "Error message cannot be null"));
        loading_message_list.put(file_index, Objects.requireNonNull(in_loading_message, "Loading message cannot be null"));
        subclasses.add(this);
    }

    // ABSTRACT FUNCTIONS
    protected abstract void processLine(String[] in_line, String in_orig_line);
    public abstract void clearList();

    // Ability to reset the context to a new activity
    public void setContext (Context in_context) {
        context = in_context;
    }

    // Return how many files are using this class
    public int getNumberOfFiles() {
        return file_list.size();
    }

    // Iterate through subclasses and clear their lists
    public static void clearAllLists() {
        for (_DataFile subclass : subclasses) {
            subclass.clearList();
        }
    }

    // =============================================================================================
    // Function:    CopyPrivateToPublicFile
    // Description: If the public file doesn't exist, read in the private one and copy it to the
    //              public one.
    // Output:      Whether to use the public file or not
    // Parameters:  void
    // =============================================================================================
    protected boolean CopyPrivateToPublicFile() {
        boolean ret = true;
        String filename = file_list.get(file_index);

        if ((filename == null) || filename.isEmpty()) return false;

        try {
            InputStream in = context.getAssets().open(Constants.Data.PRIVATE_BASE_DIR + "/" + filename);
            DocumentFile out_file = Globals.input_df.findFile(filename);

            // If the output file doesn't exist, output a stream to it and copy contents over
            if (out_file == null) {
                out_file = Globals.input_df.createFile("text/csv", filename);
                if (out_file != null) {
                    OutputStream out = context.getContentResolver().openOutputStream(out_file.getUri());

                    if (out != null) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }

                        out.close();
                    }
                }

            }
        } catch (IOException e) {
            // If anything goes wrong, just use the Private file
            ret = false;

            ((Activity)context).runOnUiThread(() -> Toast.makeText(context, error_message_list.get(file_index), Toast.LENGTH_SHORT).show());
        }

        return ret;
    }

    // =============================================================================================
    // Function:    LoadAllDataFiles
    // Description: Call all of the subclasses LoadDataFile methods
    // Parameters:  in_status_view  = (optional) view to display the loading status
    //              in_progress_bar = (optional) progress bar to update
    //              in_percent_view = (optional) view to display the percent complete
    // Output:      void
    // =============================================================================================
    public static void LoadAllDataFiles(TextView in_status_view, ProgressBar in_progress_bar, TextView in_percent_view, ProgressBar in_progress_bar_overall, TextView in_percent_view_overall) {
        for (_DataFile subclass : subclasses) {
            try {
                subclass.LoadDataFile(in_status_view, in_progress_bar, in_percent_view, in_progress_bar_overall, in_percent_view_overall);
                Thread.sleep(Constants.AppLaunch.SPLASH_SCREEN_DELAY);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // =============================================================================================
    // Function:    LoadDataFile
    // Description: Read from the .csv data file and populates the data into the in_List.
    //              If the in_PublicFileName doesn't exist, try to create if from the private one.
    //              If we can't read from the Public file, read from the Private one.
    // Parameters:  in_status_view  = (optional) view to display the loading status
    //              in_progress_bar = (optional) progress bar to update
    //              in_percent_view = (optional) view to display the percent complete
    // Output:      void
    // =============================================================================================
    public void LoadDataFile(TextView in_status_view, ProgressBar in_progress_bar, TextView in_percent_view, ProgressBar in_progress_bar_overall, TextView in_percent_view_overall) {
        boolean usePublic;
        String line;
        long fileSize = 0;
        long bytesRead = 0;
        int percentComplete = 0;
        String filename = file_list.get(file_index);

        if ((filename == null) || filename.isEmpty()) return;

        // Ensure the public file exists, and if not, copy the private one there.
        // Return back if we should use the private or public file.
        usePublic = CopyPrivateToPublicFile();

        // Update the loading status
        if (in_status_view != null) ((Activity)context).runOnUiThread(() -> in_status_view.setText(loading_message_list.get(file_index)));

        try {
            // Open up the correct input stream
            InputStream is;

            // If we can use the Public file, open the file, then the stream.  for the Private file, we can open the stream directly.
            if (usePublic) {
                DocumentFile df = Globals.input_df.findFile(filename);

                assert df != null;
                assert df.getUri().getPath() != null;
                fileSize = new File(Environment.getExternalStorageDirectory().getPath() + "/Documents/" + Constants.Data.PUBLIC_BASE_DIR + "/" + Constants.Data.PUBLIC_INPUT_DIR + "/" + filename).length();
                is = context.getContentResolver().openInputStream(df.getUri());
            } else {
                is = context.getAssets().open(Constants.Data.PRIVATE_BASE_DIR + "/" + filename);
            }

            // Sleep a tiny bit to help the UI not glitch (otherwise this block of code doesn't
            // do what it's trying to do (things don't become visible, etc).
            long finalFileSize = fileSize; // compiler complained if this wasn't done this way.

            ((Activity)context).runOnUiThread(() -> {
                if (in_progress_bar != null) {
                    in_progress_bar.setProgress(0);
                    in_progress_bar.setMax((int) finalFileSize);
                }
                if (in_percent_view != null) in_percent_view.setText(context.getString(R.string.applaunch_percent, 0));
            });

            // Read in the data
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            br.readLine();
            while ((line = br.readLine()) != null) {
                // Split out the csv line and calculate the number of bytes read so far.
                String[] info;
                bytesRead += line.length() + 2;
                if (bytesRead > fileSize) bytesRead = fileSize;

                // Update file progress (if we know the size) and only update the progressBar if the %age increased.
                // Otherwise, the Teams file has so many updates to the progress bar / text view, it can hang the UI.
                if (fileSize > 0) {
                    int newPercent = (int)(100 * bytesRead / fileSize);
                    if (newPercent > percentComplete) {
                        percentComplete = newPercent;

                        long finalBytesRead = bytesRead; // compiler complained if this wasn't done this way.
                        int finalPercentComplete = percentComplete; // compiler complained if this wasn't done this way.

                        ((Activity)context).runOnUiThread(() -> {
                            if (in_progress_bar != null) in_progress_bar.setProgress((int) finalBytesRead);
                            if (in_percent_view != null) in_percent_view.setText(context.getString(R.string.applaunch_percent, finalPercentComplete));
                        });
                    }
                }

                if (line.contains("\"")) {
                    boolean inQuotes = false;
                    StringBuilder sb = new StringBuilder();
                    List<String> tokens = new ArrayList<>();
                    char[] chars = line.toCharArray();

                    for (char c : chars) {
                        if (c == '\"') {
                            inQuotes = !inQuotes;
                            sb.append(c);
                        } else if (c == ',' && !inQuotes) {
                            tokens.add(sb.toString());
                            sb.setLength(0);
                        } else {
                            sb.append(c);
                        }
                    }
                    tokens.add(sb.toString());
                    info = tokens.toArray(new String[0]);
                }
                else info = line.split(",", -1);

                // Before we process this line, make sure it's not empty.  If so, just skip it.
                // An empty line (even with spaces) will have a length of 1.  We always will want at least 2 values.
                if (info.length > 1) processLine(info, line);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // You can't TOAST on a non-UI thread (like in a Timer)
            ((Activity)context).runOnUiThread(() -> Toast.makeText(context, R.string.applaunch_malformed_file, Toast.LENGTH_LONG).show());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // increment the overall progress
        ((Activity)context).runOnUiThread(() -> {
            if (in_progress_bar_overall != null) {
                in_progress_bar_overall.setProgress(in_progress_bar_overall.getProgress() + 1);
                if (in_percent_view_overall != null) in_percent_view_overall.setText(context.getString(R.string.applaunch_percent, 100 * in_progress_bar_overall.getProgress() / in_progress_bar_overall.getMax()));
            }
        });

    }
}
