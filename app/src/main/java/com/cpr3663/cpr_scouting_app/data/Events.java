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
//              buildNextEvents()
//                  pre-build the list of next events (descriptions) for each event.  To be
//                  called AFTER all events are loaded.
//              clear()
//                  empties out the list
//              isEventInFOP(int EventId)
//                  returns a boolean depending on if the EventId is a Field-Of0Play (FOP) event
//              getEventDescription(int EventId)
//                  returns the text description for the event
// =============================================================================================
public class Events {
    // Class Members
    private final ArrayList<EventRow> event_list;
    private final ArrayList<String> auto_events;
    private final ArrayList<String> teleop_events;

    // Constructor
    public Events() {
        event_list = new ArrayList<>();
        auto_events = new ArrayList<>();
        teleop_events = new ArrayList<>();
    }

    // Member Function: Add a row of event info into the list giving the data individually
    public void addEventRow(String in_id, String in_description, String in_phase, String in_seq_start, String in_FOP, String in_next_set) {
        event_list.add(new EventRow(Integer.parseInt(in_id), in_description, in_phase, Boolean.parseBoolean(in_seq_start), Boolean.parseBoolean(in_FOP), in_next_set));
    }

    // Member Function: Return a list of Events (description) for a give phase of the match (only ones that start a sequence)
    public ArrayList<String> getEventsForPhase(String in_phase) {
        // Return the pre-built list depending on the match_phase being asked for
        if (in_phase.equals(Constants.PHASE_AUTO)) return auto_events;
        if (in_phase.equals(Constants.PHASE_TELEOP)) return teleop_events;

        return null;
    }

    // Member Function: Return a list of Events (description) that can follow a given EventId (next Event in the sequence)
    public ArrayList<String> getNextEvents(int in_EventId) {
        // Find the event in the list, and return it's list of valid next events
        for (EventRow er : event_list) {
            if ((er.id == in_EventId)) return er.next_events_desc;
        }

        return null;
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

    // Member Function:
    public void buildNextEvents() {
        String[] next_set_ids;

        // Loop through all events.
        for (EventRow er : event_list) {
            // Get the set of next events, split them and process the information
            next_set_ids = er.next_event_set.split(":");

            if (next_set_ids.length > 0 && !next_set_ids[0].isEmpty()) {
                // Now find all events match the list of next events we can go to
                // Outer loop needs to be the next_set_ids so we build the list so we can have the context
                // menu be "in order".
                for (int j = er.next_events_desc.size(); j < next_set_ids.length; j++) {
                    for (EventRow ner : event_list) {
                        // If the event we're looking at (i) is in the list of valid next event ids (j) add it to the list
                        if (ner.id == Integer.parseInt(next_set_ids[j])) {
                            er.next_events_desc.add((ner.description));
                        }
                    }
                }
            }
        }
    }

    // Member Function: Empties out the list
    public void clear() {
        event_list.clear();
    }


    // Member Function: Is this EventId one that happens in the FOP
    public boolean isEventInFOP(int in_EventId) {
        for (EventRow er : event_list) {
            if (er.id == in_EventId) return er.is_FOP_Event;
        }

        // default to NOT in FOP event
        return false;
    }

    // Member Function: Return the description for this Event
    public String getEventDescription(int in_EventId) {
        for (EventRow er : event_list) {
            if (er.id == in_EventId) return er.description;
        }

        return "";
    }

    // =============================================================================================
    // Class:       EventRow (PRIVATE)
    // Description: Defines a structure/class to hold the information for each Event
    // Methods:     n/a
    // =============================================================================================
    private class EventRow {
        // Class Members
        final int id;
        final String description;
        final String match_phase;
        final boolean is_FOP_Event;
        final boolean is_seq_start;
        final String next_event_set;
        final ArrayList<String> next_events_desc;

        // Constructor
        public EventRow(int in_id, String in_description, String in_phase, Boolean in_seq_start, Boolean in_FOP, String in_next_event_set) {
            id = in_id;
            description = in_description;
            match_phase = in_phase;
            is_FOP_Event = in_FOP;
            is_seq_start = in_seq_start;
            next_event_set = in_next_event_set;
            next_events_desc = new ArrayList<>();

            // Manually build what events are allowed to start a sequence in each phase
            // Only add to the array if the phase is right AND this is for a FOP (field of play) AND this event starts a sequence
            if (is_FOP_Event && is_seq_start) {
                switch (match_phase) {
                    case Constants.PHASE_AUTO:
                        auto_events.add(description);
                        break;
                    case Constants.PHASE_TELEOP:
                        teleop_events.add(description);
                        break;
                }
            }
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
