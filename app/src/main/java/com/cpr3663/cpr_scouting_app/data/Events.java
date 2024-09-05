package com.cpr3663.cpr_scouting_app.data;

import com.cpr3663.cpr_scouting_app.Constants;
import com.cpr3663.cpr_scouting_app.Match;

import java.util.ArrayList;

// =============================================================================================
// Class:       Events
// Description: Defines a structure/class to hold the information for all Event Info
// Methods:     addEventRow()
//                  adds a row to the list of events
//              getEventsForPhase()
//                  returns a ListArray of possible initial events in the given match phase
//              getNextEvents(int EventId)
//                  returns an array of possible Events that can happen after EventId
//              getEventId(String EventDescription)
//                  returns the internal ID for an event
//                  useful for logging events that don't happen on the field of play.
// =============================================================================================
public class Events {
    // Class Members
    private final ArrayList<EventRow> event_list;

    // Class constants
    public static final String EVENT_STARTING_NOTE = "Auto_StartingNote";
    public static final String EVENT_DEFENDED_START = "Defended_Start";
    public static final String EVENT_DEFENDED_END = "Defended_End";
    public static final String EVENT_DEFENSE_START = "PlayDefense_Start";
    public static final String EVENT_DEFENSE_END = "PlayDefense_End";

    // Constructor
    public Events() {
        event_list = new ArrayList<EventRow>();
    }

    // Member Function: Add a row of event info into the list giving the data individually
    public void addEventRow(String in_id, String in_description, String in_phase, String in_seq_start, String in_FOP, String in_next_set) {
        event_list.add(new EventRow(Integer.parseInt(in_id), in_description, in_phase, Boolean.parseBoolean(in_seq_start), Boolean.parseBoolean(in_FOP), in_next_set));
    }

    // Member Function: Return a list of Events (description) for a give phase of the match (only ones that start a sequence)
    public ArrayList<String> getEventsForPhase(String in_phase) {
        ArrayList<String> ret = new ArrayList<String>();

        // Error check the input and only do this if they passed in a valid parameter
        if (in_phase.equals(Constants.PHASE_AUTO) || in_phase.equals(Constants.PHASE_TELEOP)) {
            for (EventRow eventInfoRow : event_list) {
                // Only build the array if the phase is right AND this is for a FOP (field of play) AND this event starts a sequence
                if ((eventInfoRow.match_phase.equals(in_phase)) && (eventInfoRow.is_FOP_Event) && (eventInfoRow.is_seq_start)) {
                    ret.add(eventInfoRow.description);
                }
            }
        }

        return ret;
    }

    // Member Function: Return a list of Events (description) that can follow a given EventId (next Event in the sequence)
    public ArrayList<String> getNextEvents(int in_EventId) {
        ArrayList<String> ret = new ArrayList<String>();
        String next_set = "";
        String[] next_set_ids;

        // Find the event in the list, and get it's list of valid next events
        for (EventRow er : event_list) {
            if ((er.id == in_EventId)) {
                next_set = er.next_event_set;
                break;
            }
        }

        // Split out the next set of event ids.
        next_set_ids = next_set.split(":");

        // Now find all events match the list of next events we can go to
        for (EventRow er : event_list) {
            for (int j = ret.size(); j < next_set_ids.length; j++) {
                // If the event we're looking at (i) is in the list of valid next event ids (j) add it to the list
                if (er.id == Integer.parseInt(next_set_ids[j])) {
                    ret.add(er.description);
                }
            }
        }

        // If we didn't add anything at this point, return null
        // This means there were no valid events that follow the one passed in
        if (ret.size() == 0) {
            return null;
        }

        return ret;
    }

    // Member Function: Return the Id for a given Event (needed for logging)
    public int getEventId(String in_EventDescription) {
        int ret = Constants.NO_EVENT;

        // Look through the event rows to find a match
        for (EventRow er : event_list) {
            if (er.description.equals(in_EventDescription) && er.match_phase.equals(Match.matchPhase)) {
                ret = er.id;
                break;
            }
        }

        return ret;
    }

    // =============================================================================================
    // Class:       EventRow (PRIVATE)
    // Description: Defines a structure/class to hold the information for each Event
    // Methods:     n/a
    // =============================================================================================
    private class EventRow {
        // Class Members
        private final int id;
        private final String description;
        private final String match_phase;
        private final boolean is_FOP_Event;
        private final boolean is_seq_start;
        private final String next_event_set;

        // Constructor
        public EventRow(int in_id, String in_description, String in_phase, Boolean in_seq_start, Boolean in_FOP, String in_next_event_set) {
            id = in_id;
            description = in_description;
            match_phase = in_phase;
            is_FOP_Event = in_FOP;
            is_seq_start = in_seq_start;
            next_event_set = in_next_event_set;
        }

        // Getter
        public int getId() {
            return id;
        }

        // Getter
        public String getDescription() {
            return description;
        }
    }
}
