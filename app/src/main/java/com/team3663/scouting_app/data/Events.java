package com.team3663.scouting_app.data;

import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;

import java.util.ArrayList;

// =============================================================================================
// Class:       Events
// Description: Defines a structure/class to hold the information for all Event Info
// =============================================================================================
public class Events {
    private final ArrayList<EventRow> event_list;

    // Constructor
    public Events() {
        event_list = new ArrayList<>();
        Globals.MaxEventGroups = 0;
    }

    // Member Function: Add a row of event info into the list giving the data individually
    public void addEventRow(String in_id, String in_group_id, String in_description, String in_phase, String in_seq_start, String in_FOP, String in_next_set, String in_color_index) {
        event_list.add(new EventRow(Integer.parseInt(in_id), Integer.parseInt(in_group_id), in_description, in_phase, Boolean.parseBoolean(in_seq_start), Boolean.parseBoolean(in_FOP), in_next_set, in_color_index));
    }

    // Member Function: Check if an event has a specific color to use
    public boolean hasEventColor(int in_EventId) {
        for (EventRow er : event_list) {
            if ((er.id == in_EventId) && (!er.color.isEmpty()))
                return true;
        }

        return false;
    }

    // Member Function: Return the color code for this event
    public int getEventColor(int in_EventId) {
        for (EventRow er : event_list) {
            if (er.id == in_EventId)
                return Integer.parseInt(er.color) - 1;
        }

        return 0;
    }

    // Member Function: Return a list of Events (description) for a give phase of the match (only ones that start a sequence)
    public ArrayList<String> getEventsForPhase(String in_phase, int in_group_id) {
        ArrayList<String> rc = new ArrayList<>();

        // Return a list depending on the match_phase being asked for and the group id
        for (EventRow er : event_list) {
            if ((er.group_id == in_group_id) && (er.match_phase.equals(in_phase)) && (er.is_FOP_Event) && (er.is_seq_start))
                rc.add((er.description));
        }

        return rc;
    }

    // Member Function: Return a list of Events (description) that can follow a given EventId (next Event in the sequence)
    public ArrayList<String> getNextEvents(int in_EventId) {
        if (in_EventId == -1) return null;

        // Find the event in the list, and return it's list of valid next events
        for (EventRow er : event_list) {
            if (er.id == in_EventId) return er.next_events_desc;
        }

        return null;
    }

    // Member Function: Return the Id for a given Event (needed for logging)
    public int getEventId(String in_EventDescription) {
        int ret = Constants.Events.ID_NO_EVENT;

        // Look through the event rows to find a match
        for (EventRow er : event_list) {
            if (er.description.equals(in_EventDescription) && er.match_phase.equals(Globals.CurrentMatchPhase)) {
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

    // Member Function: Return the description for this Event
    public int getEventGroup(int in_EventId) {
        for (EventRow er : event_list) {
            if (er.id == in_EventId) return er.group_id;
        }

        return -1;
    }

    // =============================================================================================
    // Class:       EventRow
    // Description: Defines a structure/class to hold the information for each Event
    // =============================================================================================
    private class EventRow {
        final int id;
        final int group_id;
        final String description;
        final String match_phase;
        final boolean is_FOP_Event;
        final boolean is_seq_start;
        final String next_event_set;
        final ArrayList<String> next_events_desc;
        final String color;

        // Constructor
        public EventRow(int in_id, int in_group_id, String in_description, String in_phase, Boolean in_seq_start, Boolean in_FOP, String in_next_event_set, String in_color_index) {
            id = in_id;
            group_id = in_group_id;
            description = in_description;
            match_phase = in_phase;
            is_FOP_Event = in_FOP;
            is_seq_start = in_seq_start;
            next_event_set = in_next_event_set;
            next_events_desc = new ArrayList<>();
            color = in_color_index;

            Globals.MaxEventGroups = Math.max(Globals.MaxEventGroups, in_group_id);
        }
    }
}
