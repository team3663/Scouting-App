package com.cpr3663.cpr_scouting_app;

import android.content.Context;
import android.os.Environment;
import android.util.Pair;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

// =============================================================================================
// Class:       Logger
// Description: Sets up how all of the scouting data will be logged to disk.
// Methods:     LogEvent()
//                  used for logging time-based events.  Keep track of previous events, with
//                  special handling of Defense and Defended events.
//              LogData()
//                  used for logging a specific non-time-based scouting piece of data
//              close()
//                  finish logging any/all events, and flush/close the log files
// =============================================================================================
public class Logger {
    private static FileOutputStream fos_data;
    private static FileOutputStream fos_event;
    private static int seq_number = 0; // Track the current sequence number for events
    private static int seq_number_prev_common = 0; // Track previous sequence number for all common events
    private static int seq_number_prev_defended = 0; // Track previous sequence number for just defended toggle
    private static int seq_number_prev_defense = 0; // Track previous sequence number for just defense toggle
    private static final ArrayList<Pair<String, String>> match_data = new ArrayList<Pair<String, String>>();

    // Constructor: create the new files
    public Logger(Context in_context) throws IOException {
        String path = in_context.getString(R.string.logger_path);
        boolean rc = true;

        // Ensure the sequence number is reset
        seq_number = 0;

        // Ensure the path (if it's not blank) has a trailing delimiter
        if (!path.isEmpty()) {
            if (!path.endsWith("/")) path = path + "/";
        }

        // Define the filenames/files to be used for this logger
        String filename_data = path + Globals.CurrentCompetitionId + "_" + Globals.CurrentMatchNumber + "_" + Globals.CurrentDeviceId + "_d.csv";
        String filename_event = path + Globals.CurrentCompetitionId + "_" + Globals.CurrentMatchNumber + "_" + Globals.CurrentDeviceId + "_e.csv";

        File file_data = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename_data);
        File file_event = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename_event);

        // Ensure the directory structure exists first - only need to do one
        if (!Objects.requireNonNull(file_data.getParentFile()).exists()) {
            rc = Objects.requireNonNull(file_data.getParentFile()).mkdirs();
            if (!rc) Toast.makeText(in_context.getApplicationContext(), "Failed to create directory: " + file_data.getParentFile().getName(), Toast.LENGTH_SHORT).show();
        }

        // Delete files to ensure we're not creating more than Constants.KEEP_NUMBER_OF_MATCHES
        // Only look at _d.csv files, ensure it's a file, and store off CreationTime attribute
        File[] files = file_data.getParentFile().listFiles();
        ArrayList<Long> last_created = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith("d.csv")) {
                    BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    last_created.add(attrs.creationTime().toMillis());
                }
            }
        }

        // If there's too many files, go through and delete ANY that are older than then Nth - 1
        if (last_created.size() >= Globals.NumberMatchFilesKept) {
            // Sort the list and find the Nth - 1 oldest file (because we're about to create the Nth)
            Collections.sort(last_created);
            Collections.reverse(last_created);

            long created_check = last_created.get(Globals.NumberMatchFilesKept - 1);

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                        if (attrs.creationTime().toMillis() < created_check) rc = file.delete();
                        if (!rc) Toast.makeText(in_context.getApplicationContext(), "Failed to delete file: " + file.getName(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }

        // If the output file doesn't exist, output a stream to it and copy contents over
        if (!file_data.exists()) rc = file_data.createNewFile();
        if (!rc) Toast.makeText(in_context.getApplicationContext(), "Failed to create file: " + file_data.getName(), Toast.LENGTH_SHORT).show();
        if (!file_event.exists()) rc = file_event.createNewFile();
        if (!rc) Toast.makeText(in_context.getApplicationContext(), "Failed to create file: " + file_event.getName(), Toast.LENGTH_SHORT).show();

        try {
            fos_data = new FileOutputStream(file_data);
            fos_event = new FileOutputStream(file_event);

            // Write out the header for for the file_event csv file
            String csv_header = Constants.LOGKEY_EVENT_KEY;
            csv_header += "," + Constants.LOGKEY_EVENT_SEQ;
            csv_header += "," + Constants.LOGKEY_EVENT_ID;
            csv_header += "," + Constants.LOGKEY_EVENT_TIME;
            csv_header += "," + Constants.LOGKEY_EVENT_X;
            csv_header += "," + Constants.LOGKEY_EVENT_Y;
            csv_header += "," + Constants.LOGKEY_EVENT_PREVIOUS_SEQ;

            fos_event.write(csv_header.getBytes(StandardCharsets.UTF_8));
            fos_event.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            fos_event.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Member Function: Close out the logger.  Write out all of the non-time based match data and close the files.
    public void close(){
        try {
            // Start the csv line with the event key
            String csv_header = Constants.LOGKEY_DATA_KEY;
            String csv_line = Globals.CurrentCompetitionId + ":" + Globals.CurrentMatchNumber + ":" + Globals.CurrentDeviceId;

            // Append to the csv line the values in the correct order
            csv_header += "," + Constants.LOGKEY_TEAM_TO_SCOUT;
            csv_header += "," + Constants.LOGKEY_TEAM_SCOUTING;
            csv_header += "," + Constants.LOGKEY_SCOUTER;
            csv_header += "," + Constants.LOGKEY_DID_PLAY;
            csv_header += "," + Constants.LOGKEY_START_POSITION;
            csv_header += "," + Constants.LOGKEY_DID_LEAVE_START;
            csv_header += "," + Constants.LOGKEY_CLIMB_POSITION;
            csv_header += "," + Constants.LOGKEY_TRAP;
            csv_header += "," + Constants.LOGKEY_COMMENTS;
            csv_header += "," + Constants.LOGKEY_START_TIME_OFFSET;

            csv_line += FindValueInPair(Constants.LOGKEY_TEAM_TO_SCOUT);
            csv_line += FindValueInPair(Constants.LOGKEY_TEAM_SCOUTING);
            csv_line += FindValueInPair(Constants.LOGKEY_SCOUTER);
            csv_line += FindValueInPair(Constants.LOGKEY_DID_PLAY);
            csv_line += FindValueInPair(Constants.LOGKEY_START_POSITION);
            csv_line += FindValueInPair(Constants.LOGKEY_DID_LEAVE_START);
            csv_line += FindValueInPair(Constants.LOGKEY_CLIMB_POSITION);
            csv_line += FindValueInPair(Constants.LOGKEY_TRAP);
            csv_line += FindValueInPair(Constants.LOGKEY_COMMENTS);
            csv_line += FindValueInPair(Constants.LOGKEY_START_TIME_OFFSET);

            // Write out the data
            fos_data.write(csv_header.getBytes(StandardCharsets.UTF_8));
            fos_data.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            fos_data.write(csv_line.getBytes(StandardCharsets.UTF_8));
            fos_data.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));

            fos_event.flush();
            fos_event.close();
            fos_data.flush();
            fos_data.close();
            fos_event = null;
            fos_data = null;
            System.gc();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Member Function: Find the correct data in the Key/Value Pair variable
    private String FindValueInPair(String in_Key) {
        String ret = ",";

        // loop through the pairs and stop if you find a key match.  Append the value if found.
        for(Pair<String, String> p : match_data) {
            if (p.first.equals(in_Key)) {
                ret += p.second;
                break;
            }
        }

        return ret;
    }

    // Member Function: Log a time-based event
    public void LogEvent(int in_EventId, float in_X, float in_Y, boolean in_NewSequence, double in_time) {
        int seq_number_prev = 0;

        // We need to special case the toggle switches.  We must preserve their own "previous" eventID but still
        // keep the sequence numbers going.
        switch (in_EventId) {
            case Constants.EVENT_ID_DEFENDED_START:
                seq_number_prev_defended = ++seq_number;
                break;
            case Constants.EVENT_ID_DEFENDED_END:
                seq_number_prev = seq_number_prev_defended;
                seq_number++;
                break;
            case Constants.EVENT_ID_DEFENSE_START:
                seq_number_prev_defense = ++seq_number;
                break;
            case Constants.EVENT_ID_DEFENSE_END:
                seq_number_prev = seq_number_prev_defense;
                seq_number++;
                break;
            default:
                seq_number_prev = seq_number_prev_common;
                seq_number_prev_common = ++seq_number;
        }

        String prev = "";
        String csv_line = Globals.CurrentCompetitionId + ":" + Globals.CurrentMatchNumber + ":" + Globals.CurrentDeviceId;

        // If this is NOT a new sequence, we need to write out the previous event id that goes with this one
        if (!in_NewSequence) prev = String.valueOf(seq_number_prev);
        
        // Determine string values for x, y and time. Round them to 1 decimal places.
        // If they happen to be whole numbers, trim off the ".0"
        String string_x = String.valueOf((float) Math.round(in_X * 100.0) / 100.0);
        String string_y = String.valueOf((float) Math.round(in_Y * 100.0) / 100.0);
        // Determine elapsed time and round to 1 decimal places and match length
        // Get min of elapsed time and match length in order to essentially cap the time that will be recorded
        String string_time = String.valueOf(Math.min(Math.round((in_time - Match.startTime) / 10.0) / 100.0, Match.TIMER_AUTO_LENGTH + Match.TIMER_TELEOP_LENGTH));

        if (string_x.endsWith(".0")) string_x = string_x.substring(0, string_x.length() - 2);
        if (string_y.endsWith(".0")) string_y = string_y.substring(0, string_y.length() - 2);
        if (string_time.endsWith(".0")) string_time = string_time.substring(0, string_time.length() - 2);
        
        // Form the output line that goes in the csv file.
        csv_line += "," + seq_number + "," + in_EventId + "," + string_time + "," + string_x + "," + string_y + "," + prev;
        try {
            fos_event.write(csv_line.getBytes(StandardCharsets.UTF_8));
            fos_event.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Member Function: Log a time-based event (with no time passed in)
    public void LogEvent(int in_EventId, float in_X, float in_Y, boolean in_NewSequence){
        LogEvent(in_EventId, in_X, in_Y, in_NewSequence, System.currentTimeMillis());
    }

    // Member Function: Log a non-time based event - just store this for later.
    public void LogData(String in_Key, String in_Value) {
        match_data.add(new Pair<String, String>(in_Key, in_Value));
    }
}