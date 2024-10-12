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
//              clear()
//                  clears out any stored data/events that were logged
//              LookupEvent(int EventId)
//                  check whether an event has previously been logged
//              isLastEventAnOrphan()
//                  see if the last FOP logged event has any possible "next events" (orphaned)
//              UndoLastEvent()
//                  UNDO the last FOP event recorded
// =============================================================================================
public class Logger {
    private int seq_number; // Track the current sequence number for events
    private int seq_number_prev_common = 0; // Track previous sequence number for all common events
    private int seq_number_prev_defended = 0; // Track previous sequence number for just defended toggle
    private int seq_number_prev_defense = 0; // Track previous sequence number for just defense toggle
    private final ArrayList<Pair<String, String>> match_log_data = new ArrayList<>();
    private final Context appContext;
    private final ArrayList<LoggerEventRow> match_log_events = new ArrayList<>();

    // Constructor: create the new files
    public Logger(Context in_context) {
        appContext = in_context;

        // Ensure the things are reset
        seq_number = 0;
        this.clear();

        // If this is a practice, just exit
        if (Globals.isPractice) return;

        // Add an empty logging row so that Seq# is the same as the index
        match_log_events.add(new LoggerEventRow(0, "", "", "", ""));
    }

    // Member Function: Clear out any saved data from the logger.
    public void clear() {
        match_log_events.clear();
        match_log_data.clear();
    }

    // Member Function: Close out the logger.  Write out all of the non-time based match data and close the files.
    public void close() {
        String path = appContext.getString(R.string.logger_path);
        boolean rc = true;

        // If this is a practice, there's nothing to do
        if (Globals.isPractice) return;

        // Ensure the path (if it's not blank) has a trailing delimiter
        if (!path.isEmpty()) {
            if (!path.endsWith("/")) path = path + "/";
        }

        // Define the filenames/files to be used for this logger
        String filename_data = path + Globals.CurrentCompetitionId + "_" + Globals.CurrentMatchNumber + "_" + Globals.CurrentDeviceId + "_d.csv";
        String filename_event = path + Globals.CurrentCompetitionId + "_" + Globals.CurrentMatchNumber + "_" + Globals.CurrentDeviceId + "_e.csv";

        File file_data = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename_data);
        File file_event = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename_event);

        // Ensure the directory structure exists first - only need to check with one file
        if (!Objects.requireNonNull(file_data.getParentFile()).exists()) {
            rc = Objects.requireNonNull(file_data.getParentFile()).mkdirs();
            if (!rc) Toast.makeText(appContext, "Failed to create directory: " + file_data.getParentFile().getName(), Toast.LENGTH_LONG).show();
        }

        // Delete files to ensure we're not creating more than Constants.KEEP_NUMBER_OF_MATCHES
        // Only look at _d.csv files, ensure it's a file, and store off CreationTime attribute
        File[] files = file_data.getParentFile().listFiles();
        ArrayList<Long> last_created = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith("d.csv")) {
                    BasicFileAttributes attrs;
                    try {
                        attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    last_created.add(attrs.creationTime().toMillis());
                }
            }
        }

        // If there's too many files, go through and delete ANY that are older than then Nth - 1
        if ((files != null) && (last_created.size() >= Globals.NumberMatchFilesKept)) {
            // Sort the list and find the Nth - 1 oldest file (because we're about to create the Nth)
            Collections.sort(last_created);
            Collections.reverse(last_created);

            long created_check = last_created.get(Globals.NumberMatchFilesKept - 1);

            for (File file : files) {
                if (file.isFile()) {
                    BasicFileAttributes attrs;
                    try {
                        attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (attrs.creationTime().toMillis() < created_check) rc = file.delete();
                    if (!rc) Toast.makeText(appContext, "Failed to delete file: " + file.getName(), Toast.LENGTH_LONG).show();
                }
            }
        }

        // Write out the log files
        WriteOutDataFile(file_data);
        WriteOutEventFile(file_event);
        this.clear();
    }

    // Member Function: Find the correct data in the Key/Value Pair variable
    private String FindValueInPair(String in_Key) {
        String ret = ",";

        // loop through the pairs and stop if you find a key match.  Append the value if found.
        for(Pair<String, String> p : match_log_data) {
            if (p.first.equals(in_Key)) {
                ret += p.second;
                break;
            }
        }

        return ret;
    }

    // Member Function: Write out the "D" file
    private void WriteOutDataFile(File in_File_Data) {
        boolean rc;

        // If the output file doesn't exist, output a stream to it and copy contents over
        if (in_File_Data.exists())
            Toast.makeText(appContext, "File already exists: " + in_File_Data.getName(), Toast.LENGTH_LONG).show();
        else {
            try {
                rc = in_File_Data.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!rc) Toast.makeText(appContext, "Failed to create file: " + in_File_Data.getName(), Toast.LENGTH_LONG).show();
        }

        if (!in_File_Data.canWrite()) Toast.makeText(appContext, "File not writeable: " + in_File_Data.getName(), Toast.LENGTH_LONG).show();

        FileOutputStream fos_data;

        try {
            fos_data = new FileOutputStream(in_File_Data,false);
        } catch (Exception e) {
            Toast.makeText(appContext, "Failed to create output stream: " + in_File_Data.getName() + " (ERROR: " + e.getMessage() + ")", Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }

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
        csv_header += "," + Constants.LOGKEY_START_TIME;

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
        csv_line += FindValueInPair(Constants.LOGKEY_START_TIME);

        try {
            // Write out the data
            fos_data.write(csv_header.getBytes(StandardCharsets.UTF_8));
            fos_data.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            fos_data.write(csv_line.getBytes(StandardCharsets.UTF_8));
            fos_data.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));

            fos_data.flush();
            fos_data.close();
            System.gc();
        } catch (IOException e) {
            Toast.makeText(appContext, "Failed to close out data log file. (ERROR: " + e.getMessage() + ")", Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }
    }

    // Member Function: Write out the "E" file
    private void WriteOutEventFile(File in_File_Event) {
        boolean rc;

        if (in_File_Event.exists())
            Toast.makeText(appContext, "File already exists: " + in_File_Event.getName(), Toast.LENGTH_LONG).show();
        else {
            try {
                rc = in_File_Event.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!rc) Toast.makeText(appContext, "Failed to create file: " + in_File_Event.getName(), Toast.LENGTH_LONG).show();
        }

        if (!in_File_Event.canWrite()) Toast.makeText(appContext, "File not writeable: " + in_File_Event.getName(), Toast.LENGTH_LONG).show();

        FileOutputStream fos_event;
        try {
            fos_event = new FileOutputStream(in_File_Event, false);

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
        } catch (Exception e) {
            Toast.makeText(appContext, "Failed to create output stream: " + in_File_Event.getName() + " (ERROR: " + e.getMessage() + ")", Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }

        // Form the output line that goes in the csv file.
        for (int i = 1; i < match_log_events.size(); ++i) {
            LoggerEventRow ler = match_log_events.get(i);
            String csv_line = Globals.CurrentCompetitionId + ":" + Globals.CurrentMatchNumber + ":" + Globals.CurrentDeviceId;
            csv_line += "," + i + "," + ler.EventId + "," + ler.LogTime + "," + ler.X + "," + ler.Y + "," + ler.PrevSeq;
            try {
                fos_event.write(csv_line.getBytes(StandardCharsets.UTF_8));
                fos_event.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                Toast.makeText(appContext, "Failed to write out event data: " + csv_line + " (ERROR: " + e.getMessage() + ")", Toast.LENGTH_LONG).show();
                throw new RuntimeException(e);
            }
        }

        try {
            fos_event.flush();
            fos_event.close();
        } catch (IOException e) {
            Toast.makeText(appContext, "Failed to close out event log file. (ERROR: " + e.getMessage() + ")", Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }
    }

    // Member Function: Log a time-based event
    public void LogEvent(int in_EventId, float in_X, float in_Y, boolean in_NewSequence, double in_time) {
        // If this is a practice, there's nothing to do
        if (Globals.isPractice) return;

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

        // If this is NOT a new sequence, we need to write out the previous event id that goes with this one
        String prev = "";
        if (!in_NewSequence) prev = String.valueOf(seq_number_prev);
        
        // Determine string values for x, y. Truncate them.
        String string_x = String.valueOf((int) in_X);
        String string_y = String.valueOf((int) in_Y);

        // Determine elapsed time and round to 1 decimal places
        // Get min of elapsed time and match length in order to essentially cap the time that will be recorded
        String string_time = String.valueOf(Math.min(Math.round((in_time - Match.startTime) / 100.0) / 10.0, Match.TIMER_AUTO_LENGTH + Match.TIMER_TELEOP_LENGTH));

        if (string_time.endsWith(".0")) string_time = string_time.substring(0, string_time.length() - 2);

        match_log_events.add(new LoggerEventRow(in_EventId, string_time, string_x, string_y, prev));
    }

    // Member Function: Log a time-based event (with no time passed in)
    public void LogEvent(int in_EventId, float in_X, float in_Y, boolean in_NewSequence){
        // If this is a practice, there's nothing to do
        if (Globals.isPractice) return;

        LogEvent(in_EventId, in_X, in_Y, in_NewSequence, System.currentTimeMillis());
    }

    // Member Function: Log a non-time based event - just store this for later.
    public void LogData(String in_Key, String in_Value) {
        // If this is a practice, there's nothing to do
        if (Globals.isPractice) return;

        match_log_data.add(new Pair<>(in_Key, in_Value.trim()));
    }

    // Member Function: Determine if an Event (for Id) was logged already
    public boolean LookupEvent(int in_EventId) {
        for (LoggerEventRow ler : match_log_events) {
            if (ler.EventId == in_EventId) return true;
        }

        return false;
    }

    // Member Function: Check if the last logged event is an orphan
    public boolean isLastEventAnOrphan() {
        boolean foundLast = false;
        LoggerEventRow ler = null;
        // Check for a no-op
        if (match_log_events.isEmpty()) return false;

        // See if the last FOP logged event has "next events" that can happen (ie: an orphan)
        for (int i = match_log_events.size() - 1; i >=0 && !foundLast; --i) {
            ler = match_log_events.get(i);

            switch (ler.EventId) {
                case Constants.EVENT_ID_DEFENDED_START:
                case Constants.EVENT_ID_DEFENDED_END:
                case Constants.EVENT_ID_DEFENSE_START:
                case Constants.EVENT_ID_DEFENSE_END:
                case Constants.EVENT_ID_NOT_MOVING_START:
                case Constants.EVENT_ID_NOT_MOVING_END:
                    break;
                default:
                    foundLast = true;
            }
        }

        // If we didn't find a last row worth looking at, just return now
        if (!foundLast) return false;

        // Return value based on whether there are any Next Events (orphan) or not.
        return !Globals.EventList.getNextEvents(ler.EventId).isEmpty();
    }

    // Member Function: Undo the last logged Event and return the previous EventId
    public int UndoLastEvent() {
        int lastIndex = -1;
        LoggerEventRow ler;

        // Check for a no-op
        if (match_log_events.isEmpty()) return -1;

        // Find the last FOP logged event
        for (int i = match_log_events.size() - 1; i >=0 && lastIndex < 0; --i) {
            if (Globals.EventList.isEventInFOP(match_log_events.get(i).EventId)) lastIndex = i;
        }

        // If we didn't find the last logged event (that's bad) show a Toast and return
        if (lastIndex < 0) {
            Toast.makeText(appContext, R.string.match_bad_undo, Toast.LENGTH_SHORT).show();
            return -1;
        }

        // In order to UNDO this event, we need to find what the new last event is and return it
        // after we remove the one we need to undo.
        match_log_events.remove(lastIndex);

        // For any events AFTER this removed event, we need to decrement the "PrevSeq" since they all
        // shifted up one slot IF it pointed to something AFTER where we removed
        for (int i = lastIndex; i < match_log_events.size(); ++i){
            ler = match_log_events.get(i);
            if (!ler.PrevSeq.isEmpty() && (Integer.parseInt(ler.PrevSeq) > lastIndex)) {
                ler.PrevSeq = String.valueOf(Integer.parseInt(ler.PrevSeq) - 1);
            }
        }

        // Find the (new) last FOP logged event
        for (int i = match_log_events.size() - 1; i >=0; --i) {
            if (Globals.EventList.isEventInFOP(match_log_events.get(i).EventId)) return match_log_events.get(i).EventId;
        }

        return -1;
    }

    // =============================================================================================
    // Class:       LoggerEventRow
    // Description: Contains all data for a single log event
    // Methods:
    // =============================================================================================
    protected static class LoggerEventRow {
        int EventId;
        String LogTime;
        String X;
        String Y;
        String PrevSeq;

        // Constructor: create a new LogEventRow
        public LoggerEventRow(int in_EventId, String in_Time, String in_X, String in_Y, String in_PrevSeq) {
            EventId = in_EventId;
            LogTime = in_Time;
            X = in_X;
            Y = in_Y;
            PrevSeq = in_PrevSeq;
        }
    }
}