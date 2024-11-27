package com.team3663.scouting_app.utility;

import android.content.Context;
import android.util.Pair;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.utility.achievements.Achievements;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

// =============================================================================================
// Class:       Logger
// Description: Sets up how all of the scouting data will be logged to disk.
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
        // If this is a practice, there's nothing to do
        if (Globals.isPractice) return;

        // Define the filenames/files to be used for this logger
        String filename_data = Globals.CurrentCompetitionId + "_" + Globals.CurrentMatchNumber + "_" + Globals.CurrentDeviceId + "_d.csv";
        String filename_event = Globals.CurrentCompetitionId + "_" + Globals.CurrentMatchNumber + "_" + Globals.CurrentDeviceId + "_e.csv";

        // Delete files to ensure we're not creating more than Constants.KEEP_NUMBER_OF_MATCHES
        // Only look at _d.csv files, ensure it's a file, and store off CreationTime attribute
        // Only look at files associated with the current competition
        DocumentFile[] list_of_files = Globals.output_df.listFiles();
        ArrayList<Integer> filename_match_list = new ArrayList<>();
        for (DocumentFile df : list_of_files) {
            String[] file_parts = df.getName().split("_");

            if ((Integer.parseInt(file_parts[0]) == Globals.CurrentCompetitionId) && (file_parts[3].equals("d.csv")))
                filename_match_list.add(Integer.parseInt(file_parts[1]));
        }

        // If there's too many files, go through and delete ANY that are older than then Nth - 1
        // What we'll do is collect attributes and names (_d files only) into two sync'd lists.
        // We'll take a copy of the last_created_list and sort it to easily find the create date of the Nth - 1 one.
        // Then we'll iterate through our original sync'd two lists and see if one should be deleted based on the
        // create date, and if so, use the matching name from the other list to do so.
        if (filename_match_list.size() >= Globals.NumberMatchFilesKept) {
            // Sort the list and find the Nth - 1 oldest file (because we're about to create the Nth)
            Collections.sort(filename_match_list);
            Collections.reverse(filename_match_list);

            DocumentFile file_df;

            for (int i = Globals.NumberMatchFilesKept - 1; i < filename_match_list.size(); ++i) {
                file_df = Globals.output_df.findFile(Globals.CurrentCompetitionId + "_" + filename_match_list.get(i) + "_" + Globals.CurrentDeviceId + "_d.csv");
                if (file_df != null) file_df.delete();
                file_df = Globals.output_df.findFile(Globals.CurrentCompetitionId + "_" + filename_match_list.get(i) + "_" + Globals.CurrentDeviceId + "_e.csv");
                if (file_df != null) file_df.delete();
            }
        }

        // Check if the current file exists before creating it.  DocumentFile will create a "... (1).csv" file
        // if it previously existed, instead of overwriting it.
        if (Globals.output_df.findFile(filename_data) != null) Globals.output_df.findFile(filename_data).delete();
        if (Globals.output_df.findFile(filename_event) != null) Globals.output_df.findFile(filename_event).delete();

        DocumentFile data_df = Globals.output_df.createFile("text/csv", filename_data);
        DocumentFile event_df = Globals.output_df.createFile("text/csv", filename_event);

        // Write out the log files
        assert data_df != null;
        assert event_df != null;
        WriteOutDataFile(data_df);
        WriteOutEventFile(event_df);
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
    private void WriteOutDataFile(DocumentFile in_data_df) {
        if (!in_data_df.canWrite()) Toast.makeText(appContext, "File not writeable: " + in_data_df.getName(), Toast.LENGTH_LONG).show();

        OutputStream fos_data;

        try {
            fos_data = appContext.getContentResolver().openOutputStream(in_data_df.getUri());
        } catch (Exception e) {
            Toast.makeText(appContext, "Failed to create output stream: " + in_data_df.getName() + " (ERROR: " + e.getMessage() + ")", Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }

        // Start the csv line with the event key
        String csv_header = Constants.Logger.LOGKEY_DATA_KEY;
        String csv_line = Globals.CurrentCompetitionId + ":" + Globals.CurrentMatchNumber + ":" + Globals.CurrentDeviceId;

        // Append to the csv line the values in the correct order
        csv_header += "," + Constants.Logger.LOGKEY_MATCH_TYPE;
        csv_header += "," + Constants.Logger.LOGKEY_TEAM_TO_SCOUT;
        csv_header += "," + Constants.Logger.LOGKEY_TEAM_SCOUTING;
        csv_header += "," + Constants.Logger.LOGKEY_SCOUTER;
        csv_header += "," + Constants.Logger.LOGKEY_DID_PLAY;
        csv_header += "," + Constants.Logger.LOGKEY_START_POSITION;
        csv_header += "," + Constants.Logger.LOGKEY_DID_LEAVE_START;
        csv_header += "," + Constants.Logger.LOGKEY_CLIMB_POSITION;
        csv_header += "," + Constants.Logger.LOGKEY_TRAP;
        csv_header += "," + Constants.Logger.LOGKEY_COMMENTS;
        csv_header += "," + Constants.Logger.LOGKEY_ACHIEVEMENT;
        csv_header += "," + Constants.Logger.LOGKEY_START_TIME_OFFSET;
        csv_header += "," + Constants.Logger.LOGKEY_START_TIME;

        csv_line += FindValueInPair(Constants.Logger.LOGKEY_MATCH_TYPE);
        csv_line += FindValueInPair(Constants.Logger.LOGKEY_TEAM_TO_SCOUT);
        csv_line += FindValueInPair(Constants.Logger.LOGKEY_TEAM_SCOUTING);
        csv_line += FindValueInPair(Constants.Logger.LOGKEY_SCOUTER);
        csv_line += FindValueInPair(Constants.Logger.LOGKEY_DID_PLAY);
        csv_line += FindValueInPair(Constants.Logger.LOGKEY_START_POSITION);
        csv_line += FindValueInPair(Constants.Logger.LOGKEY_DID_LEAVE_START);
        csv_line += FindValueInPair(Constants.Logger.LOGKEY_CLIMB_POSITION);
        csv_line += FindValueInPair(Constants.Logger.LOGKEY_TRAP);
        csv_line += FindValueInPair(Constants.Logger.LOGKEY_COMMENTS);
        csv_line += FindValueInPair(Constants.Logger.LOGKEY_ACHIEVEMENT);
        csv_line += FindValueInPair(Constants.Logger.LOGKEY_START_TIME_OFFSET);
        csv_line += FindValueInPair(Constants.Logger.LOGKEY_START_TIME);

        try {
            // Write out the data
            assert fos_data != null;
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
    private void WriteOutEventFile(DocumentFile in_event_df) {
        if (!in_event_df.canWrite()) Toast.makeText(appContext, "File not writeable: " + in_event_df.getName(), Toast.LENGTH_LONG).show();

        OutputStream fos_event;
        try {
            fos_event = appContext.getContentResolver().openOutputStream(in_event_df.getUri());

            // Write out the header for for the file_event csv file
            String csv_header = Constants.Logger.LOGKEY_EVENT_KEY;
            csv_header += "," + Constants.Logger.LOGKEY_EVENT_SEQ;
            csv_header += "," + Constants.Logger.LOGKEY_EVENT_ID;
            csv_header += "," + Constants.Logger.LOGKEY_EVENT_TIME;
            csv_header += "," + Constants.Logger.LOGKEY_EVENT_X;
            csv_header += "," + Constants.Logger.LOGKEY_EVENT_Y;
            csv_header += "," + Constants.Logger.LOGKEY_EVENT_PREVIOUS_SEQ;

            assert fos_event != null;
            fos_event.write(csv_header.getBytes(StandardCharsets.UTF_8));
            fos_event.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Toast.makeText(appContext, "Failed to create output stream: " + in_event_df.getName() + " (ERROR: " + e.getMessage() + ")", Toast.LENGTH_LONG).show();
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

        Achievements.data_NumEvents++;
        int seq_number_prev = 0;

        // We need to special case the toggle switches.  We must preserve their own "previous" eventID but still
        // keep the sequence numbers going.
        switch (in_EventId) {
            case Constants.Events.ID_DEFENDED_START:
                seq_number_prev_defended = ++seq_number;
                break;
            case Constants.Events.ID_DEFENDED_END:
                seq_number_prev = seq_number_prev_defended;
                seq_number++;
                break;
            case Constants.Events.ID_DEFENSE_START:
                seq_number_prev_defense = ++seq_number;
                break;
            case Constants.Events.ID_DEFENSE_END:
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
        String string_time = String.valueOf(Math.min(Math.round((in_time - Globals.startTime) / 100.0) / 10.0, Constants.Match.TIMER_AUTO_LENGTH + Constants.Match.TIMER_TELEOP_LENGTH));

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
                case Constants.Events.ID_DEFENDED_START:
                case Constants.Events.ID_DEFENDED_END:
                case Constants.Events.ID_DEFENSE_START:
                case Constants.Events.ID_DEFENSE_END:
                case Constants.Events.ID_NOT_MOVING_START:
                case Constants.Events.ID_NOT_MOVING_END:
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
        // Special case if we've backed up to the starting note.
        for (int i = match_log_events.size() - 1; i >=0; --i) {
            if ((Globals.EventList.isEventInFOP(match_log_events.get(i).EventId)) ||
                    (match_log_events.get(i).EventId == Constants.Events.ID_AUTO_STARTNOTE))
                return match_log_events.get(i).EventId;
        }

        return -1;
    }

    // =============================================================================================
    // Class:       LoggerEventRow
    // Description: Contains all data for a single log event
    // =============================================================================================
    private static class LoggerEventRow {
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