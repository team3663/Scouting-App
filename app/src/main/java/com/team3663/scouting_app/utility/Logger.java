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
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

// =============================================================================================
// Class:       Logger
// Description: Sets up how all of the scouting data will be logged to disk.
// =============================================================================================
public class Logger {
    private final ArrayList<Pair<String, String>> match_log_data = new ArrayList<>();
    private final Context appContext;
    private final ArrayList<LoggerEventRow> match_log_events = new ArrayList<>();
    // Keep track of the previous sequence number (per event group) so we know how to link a subsequent
    // event for that group.  Helps tremendously with UNDO actions.  This is updated / used by the logger.
    private static final int[] previous_seq = new int[Globals.MaxEventGroups + 1];

    // Public Global Variables
    // Keep track of the currently selected event (per event group) to be used to build the context menus
    // A value of -1 means there is no current sequence started and all "starting events" should be used.
    // (Adding 1 so that we can be "1" based and not have to keep "-1" a lot of places)
    public static int[] current_event = new int[Globals.MaxEventGroups + 1];

    // Constructor: create the new logger
    public Logger(Context in_context) {
        appContext = in_context;

        // Default all arrays to -1
        Arrays.fill(current_event, -1);
        Arrays.fill(previous_seq, -1);

        // Ensure the things are reset
        this.clear();

        // If this is a practice, just exit
        if (Globals.isPractice) return;

        // Add an empty logging row so that Seq# is the same as the index
        match_log_events.add(new LoggerEventRow(-1, 0, 0, 0, ""));
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

        // Define the filename/file to be used for this logger
        String filename = Globals.CurrentCompetitionId + "_" + Globals.CurrentMatchNumber + "_" + Globals.CurrentDeviceId + "_" + Globals.MatchTypeList.getMatchTypeShortForm(Globals.CurrentMatchType) + ".csv";

        // If the FileList is empty, assume we haven't checked for files and do so now.
        if (Globals.FileList.isEmpty()) SearchForFiles();

        // Delete files to ensure we're not creating more than Constants.KEEP_NUMBER_OF_MATCHES
        // If there's too many files, sort the files by LastModified date and delete the oldest ones.
        if (Globals.FileList.size() >= Globals.NumberMatchFilesKept) {
            // Sort the list descending and delete from the list those that are above the limit number of files
            ArrayList<Long> last_modified = new ArrayList<>(Globals.FileList.values());
            Collections.sort(last_modified);
            Collections.reverse(last_modified);
            long limit = last_modified.get(Globals.NumberMatchFilesKept - 1);
            DocumentFile file_df;

            for (String delete_file_name : Globals.FileList.keySet()) {
                if (Globals.FileList.get(delete_file_name) <= limit) {
                    file_df = Globals.output_df.findFile(delete_file_name);
                    if (file_df != null) file_df.delete();
                    Globals.FileList.remove(delete_file_name);
                }
            }
        }

        // Check if the current file exists before creating it (delete if it does).
        if (Globals.output_df.findFile(filename) != null) Objects.requireNonNull(Globals.output_df.findFile(filename)).delete();

        DocumentFile match_df = Globals.output_df.createFile("text/csv", filename);

        // Write out the log files and add it to the list
        assert match_df != null;
        WriteOutDataFile(match_df);
        WriteOutEventFile(match_df);
        Globals.FileList.put(match_df.getName(), match_df.lastModified());
        this.clear();
    }

    // Member Function: Search the output directory for any existing files.
    static public void SearchForFiles() {
        DocumentFile[] list_of_files = Globals.output_df.listFiles();

        for (DocumentFile df : list_of_files) {
            String[] file_parts = Objects.requireNonNull(df.getName()).split("_");

            if ((Integer.parseInt(file_parts[0]) == Globals.CurrentCompetitionId) && (file_parts[3].endsWith(".csv"))) {
                Globals.FileList.put(df.getName(), df.lastModified());
            }
        }
    }

    // Member Function: Find the correct data in the Key/Value Pair variable
    private String FindValueInPair(String in_Key) {
        String ret = "";

        // loop through the pairs and stop if you find a key match.  Append the value if found.
        for(Pair<String, String> p : match_log_data) {
            if (p.first.equals(in_Key)) {
                ret = p.second;
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

        // Start the line (header as well) with the Record Type (1 for the Data line)
        StringBuilder csv_line = new StringBuilder();
        csv_line.append(",1");
        for (String header : Constants.Logger.LOGKEY_DATA_FILE_HEADER) {
            csv_line.append(",").append(FindValueInPair(header));
        }

        // Trim leading ","
        if (csv_line.length() > 0) csv_line.delete(0, 1);

        try {
            // Write out the data
            assert fos_data != null;
            fos_data.write(csv_line.toString().getBytes(StandardCharsets.UTF_8));
            fos_data.write(Constants.Logger.FILE_LINE_SEPARATOR.getBytes(StandardCharsets.UTF_8));

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
            assert fos_event != null;
        } catch (Exception e) {
            Toast.makeText(appContext, "Failed to create output stream: " + in_event_df.getName() + " (ERROR: " + e.getMessage() + ")", Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }

        // Form the output line that goes in the csv file.
        for (int i = 1; i < match_log_events.size(); ++i) {
            LoggerEventRow ler = match_log_events.get(i);

            // Normalize the X, Y coordinates to be a %age of the image size
            // Multiply the %age by 10,000 to get a whole number representing 2 digits of precision (ie: 0.3956 turns into 3956)
            // We do this to save 2 characters in the .csv file that we need to transmit.
            int normalized_x = 0;
            int normalized_y = 0;
            if (Constants.Match.IMAGE_WIDTH > 0) normalized_x = (int)(10_000.0 * ler.X / Constants.Match.IMAGE_WIDTH);
            if (Constants.Match.IMAGE_HEIGHT > 0) normalized_y = (int)(10_000.0 * ler.Y / Constants.Match.IMAGE_HEIGHT);

            StringBuilder csv_line = new StringBuilder();
            csv_line.append(",").append(i)
                    .append(",").append(ler.EventId)
                    .append(",").append(ler.LogTime)
                    .append(",").append(normalized_x)
                    .append(",").append(normalized_y)
                    .append(",").append(ler.PrevSeq);
            try {
                fos_event.write(csv_line.toString().getBytes(StandardCharsets.UTF_8));
                fos_event.write(Constants.Logger.FILE_LINE_SEPARATOR.getBytes(StandardCharsets.UTF_8));
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
    public void LogEvent(int in_EventId, float in_X, float in_Y, double in_time) {
        // If this is a practice, there's nothing to do
        if (Globals.isPractice) return;

        // Update Achievement data
        String ach_desc = Globals.EventList.getEventDescription(in_EventId);
        Achievements.data_NumEvents++;

        if (Constants.Achievements.EVENT_IDS_PICKUP_ALGAE.contains(in_EventId)) Achievements.data_match_AlgaePickup++; // 2025
        if (Constants.Achievements.EVENT_IDS_SCORE_ALGAE_IN_NET.contains(in_EventId)) Achievements.data_match_AlgaeInNet++; // 2025
        if (Constants.Achievements.EVENT_IDS_SCORE_ALGAE_IN_PROCESSOR.contains(in_EventId)) Achievements.data_match_AlgaeInProcessor++; // 2025
        if (Constants.Achievements.EVENT_IDS_PICKUP_CORAL_GROUND.contains(in_EventId)) Achievements.data_match_CoralPickupGround++; // 2025
        if (Constants.Achievements.EVENT_IDS_PICKUP_CORAL_STATION.contains(in_EventId)) Achievements.data_match_CoralPickupStation++; // 2025
        if (Constants.Achievements.EVENT_IDS_DROP_CORAL.contains(in_EventId)) Achievements.data_match_CoralDropped++; // 2025
        if (Constants.Achievements.EVENT_IDS_PLACE_CORAL.contains(in_EventId)) Achievements.data_match_CoralLevel[Integer.parseInt(ach_desc.substring(ach_desc.length() - 1))]++; // 2025
        if ((Constants.Achievements.EVENT_IDS_SCORING.contains(in_EventId)) && Globals.isDefended) Achievements.data_ScoreWhileDefended++; // 2025
        if (Constants.Achievements.EVENT_ID_CLIMB_SUCCESS == in_EventId) Achievements.data_match_ClimbSuccess++;

        // Determine the EventGroup Id this event belongs to
        int GroupId = Globals.EventList.getEventGroup(in_EventId);

        // If this is NOT a new sequence, we need to write out the previous event id that goes with this one
        String prev = "";

        if (!Globals.EventList.isEventStartOfSeq(in_EventId))
            prev = String.valueOf(previous_seq[GroupId]);

        // Determine string values for x, y. Truncate them.
        int log_x = (int) in_X;
        int log_y = (int) in_Y;

        // Determine elapsed time and round to a tenth of a second with no decimal (so 10.3 seconds is 103 tenths)
        // Get min of elapsed time and match length in order to essentially cap the time that will be recorded based on Match Phase
        int log_time;
        switch (Globals.CurrentMatchPhase) {
            case Constants.Phases.AUTO:
                log_time = (int) (Math.min(Math.round((in_time - Globals.StartTime) / 100.0), Constants.Match.TIMER_AUTO_LENGTH * 10));
                break;
            case Constants.Phases.TELEOP:
                log_time = (int) (Math.min(Math.round((in_time - Globals.StartTime) / 100.0), (Constants.Match.TIMER_AUTO_LENGTH + Constants.Match.TIMER_TELEOP_LENGTH) * 10));
                break;
            default:
                log_time = 0;
        }

        match_log_events.add(new LoggerEventRow(in_EventId, log_time, log_x, log_y, prev));

        // Save off this event as the "current" event for its group.
        current_event[GroupId] = in_EventId;

        // Save this off as the previous event (to link the next one to it).
        // Safe to do this everytime since if we start a new sequence, we override (above) to blank.
        previous_seq[GroupId] = match_log_events.size() - 1;
    }

    // Member Function: Log a time-based event (with no time passed in)
    public void LogEvent(int in_EventId){
        // If this is a practice, there's nothing to do
        if (Globals.isPractice) return;

        LogEvent(in_EventId, 0, 0, System.currentTimeMillis());
    }

    // Member Function: Log a non-time based event - just store this for later.
    public void LogData(String in_Key, String in_Value) {
        // If this is a practice, there's nothing to do
        if (Globals.isPractice) return;

        match_log_data.add(new Pair<>(in_Key, in_Value.trim()));
    }

    // Member Function: Check if the last logged event is an orphan
    public boolean isLastEventAnOrphan() {
        boolean foundLast;
        boolean rc = false;
        LoggerEventRow ler;
        // Check for a no-op
        if (match_log_events.isEmpty()) return false;

        for (int group_id = 1; group_id <= Globals.MaxEventGroups; ++group_id) {
            foundLast = false;

            // See if the last FOP logged event for this group has "next events" that can happen (ie: an orphan)
            for (int i = match_log_events.size() - 1; i >= 0 && !foundLast; --i) {
                ler = match_log_events.get(i);

                // Only look at rows that match the group id we're interested in
                if (Globals.EventList.getEventGroup(ler.EventId) == group_id) {
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

                // If we found a last row worth looking at, check if it's an orphan
                // Use an "OR" condition to find if ANY group had an orphan event.
                if (foundLast)
                    rc = rc || !Globals.EventList.getNextEvents(ler.EventId).isEmpty();
            }
        }

        return rc;
    }

    // Member Function: Undo the last logged Event and return the previous EventId
    public int UndoLastEvent() {
        int lastIndex = -1;
        int lastEventId = -1;
        int lastEventGroupId;
        int rc;
        LoggerEventRow ler;

        // Check for a no-op
        if (match_log_events.isEmpty()) return -1;

        // Find the last FOP logged event
        for (int i = match_log_events.size() - 1; i >=0; --i) {
            lastEventId = match_log_events.get(i).EventId;
            if (Globals.EventList.isEventInFOP(lastEventId)) {
                lastIndex = i;
                break;
            }
        }

        // If we didn't find the last logged event (that's bad) show a Toast and return
        if (lastIndex < 0) {
            Toast.makeText(appContext, R.string.match_bad_undo, Toast.LENGTH_SHORT).show();
            return -1;
        }

        // Undo any achievements from this event being undone.
        String ach_desc = Globals.EventList.getEventDescription(lastEventId);
        Achievements.data_NumEvents--;
        if (Constants.Achievements.EVENT_IDS_PICKUP_ALGAE.contains(lastEventId)) Achievements.data_match_AlgaePickup--; // 2025
        if (Constants.Achievements.EVENT_IDS_SCORE_ALGAE_IN_NET.contains(lastEventId)) Achievements.data_match_AlgaeInNet--; // 2025
        if (Constants.Achievements.EVENT_IDS_SCORE_ALGAE_IN_PROCESSOR.contains(lastEventId)) Achievements.data_match_AlgaeInProcessor--; // 2025
        if (Constants.Achievements.EVENT_IDS_PICKUP_CORAL_GROUND.contains(lastEventId)) Achievements.data_match_CoralPickupGround--; // 2025
        if (Constants.Achievements.EVENT_IDS_PICKUP_CORAL_STATION.contains(lastEventId)) Achievements.data_match_CoralPickupStation--; // 2025
        if (Constants.Achievements.EVENT_IDS_DROP_CORAL.contains(lastEventId)) Achievements.data_match_CoralDropped--; // 2025
        if (Constants.Achievements.EVENT_IDS_PLACE_CORAL.contains(lastEventId)) Achievements.data_match_CoralLevel[Integer.parseInt(ach_desc.substring(ach_desc.length() - 1))]--; // 2025
        if (Constants.Achievements.EVENT_ID_CLIMB_SUCCESS == lastEventId) Achievements.data_match_ClimbSuccess--;
        // To determine if they were not moving, we need to find the next previous item for the "defended" group.  If that item
        // has some "next events", then it's toggled on and we need to decrement the achievement counter.
        // Because the scouter can undo a lot of events, we can't rely on the current_event[] to evaluate since this may
        // be undoing an event well before the current defended event happened.
        if (Constants.Achievements.EVENT_IDS_SCORING.contains(lastEventId))
            for (int i = lastIndex - 1; i > 0; i--) {
                if (Globals.EventList.getEventGroup(match_log_events.get(i).EventId) == Constants.Achievements.DEFENDED_EVENT_GROUP) {
                    if (!Globals.EventList.getNextEvents(match_log_events.get(i).EventId).isEmpty()) Achievements.data_ScoreWhileDefended--;
                    break;
                }
            }

        // Check the match_event_log to see if there's a previous sequence.  If so, use that to set
        // the current event.
        lastEventGroupId = Globals.EventList.getEventGroup(lastEventId);

        if (match_log_events.get(lastIndex).PrevSeq.isEmpty()) {
            current_event[lastEventGroupId] = -1;
            previous_seq[lastEventGroupId] = -1;
        }
        else {
            previous_seq[lastEventGroupId] = Integer.parseInt(match_log_events.get(lastIndex).PrevSeq);
            current_event[lastEventGroupId] = match_log_events.get(previous_seq[lastEventGroupId]).EventId;
        }

        // We can now remove the event.
        match_log_events.remove(lastIndex);

        // For any events AFTER this removed event, we need to decrement the "PrevSeq" since they all
        // shifted up one slot IF it pointed to something AFTER where we removed
        for (int i = lastIndex; i < match_log_events.size(); ++i) {
            ler = match_log_events.get(i);
            if (!ler.PrevSeq.isEmpty() && (Integer.parseInt(ler.PrevSeq) > lastIndex)) {
                ler.PrevSeq = String.valueOf(Integer.parseInt(ler.PrevSeq) - 1);
            }
        }

        // For any previous_seq event groups that have a value > lastIndex, we also need to decrement them.
        // Only toggles should be affected here.
        for (int i = 1; i <= Globals.MaxEventGroups; ++i) {
            if (previous_seq[i] > lastIndex)
                previous_seq[i]--;
        }

        // Set the default return code to be -1 (there's no more previous events)
        rc = -1;

        // Find the (new) last FOP logged event regardless of "group" and save it as the return value.
        // Special case if we've backed up to the starting game piece.
        for (int i = match_log_events.size() - 1; i >=0; --i) {
            if ((Globals.EventList.isEventInFOP(match_log_events.get(i).EventId)) ||
                    (match_log_events.get(i).EventId == Constants.Events.ID_AUTO_START_GAME_PIECE)) {
                rc = match_log_events.get(i).EventId;
                break;
            }
        }

        return rc;
    }

    // =============================================================================================
    // Class:       LoggerEventRow
    // Description: Contains all data for a single log event
    // =============================================================================================
    private static class LoggerEventRow {
        int EventId;
        int LogTime;
        int X;
        int Y;
        String PrevSeq;

        // Constructor: create a new LogEventRow
        public LoggerEventRow(int in_EventId, int in_Time, int in_X, int in_Y, String in_PrevSeq) {
            EventId = in_EventId;
            X = in_X;
            Y = in_Y;
            PrevSeq = in_PrevSeq;

            // Ensure we are always generating a positive (or zero) increment to the logged time to prevent negative sequence cycle times
            // This can mostly happen only between AUTO and TELEOP in the 3 second gap, but good to have here to ensure the integrity of the data
            int prev_time = 0;
            if ((Globals.EventLogger != null) && (!Globals.EventLogger.match_log_events.isEmpty())) prev_time = Globals.EventLogger.match_log_events.get(Globals.EventLogger.match_log_events.size() - 1).LogTime;
            LogTime = Math.max(in_Time, prev_time);
        }
    }
}