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
    private static  ArrayList<EventGroup> EventGroup;

    // Constructor
    public Events() {
        event_list = new ArrayList<>();
        EventGroup = new ArrayList<>();

        // EventGroup will use a 1-based group_id, so add a dummy record for index 0.
        EventGroup.add(new EventGroup("", ""));

        Globals.MaxEventGroups = 0;
    }

    // Member Function: Add an EventGroup to the list
    public void addEventGroup(String in_id, String in_name) {
        int group_id = Integer.parseInt(in_id);

        // Ensure we're adding the group id (in_id) as the same index.  This shouldn't happen but in case there's a gap in group numbers.
        // 1. Ensure the array is at least as big as the group_id needs
        // 2. Just set the value at the right index to the name.
        for (int i = EventGroup.size(); i <= group_id; ++i)
            EventGroup.add(new EventGroup("", ""));

        EventGroup.get(group_id).name = in_name;
        Globals.MaxEventGroups = Math.max(Globals.MaxEventGroups, Integer.parseInt(in_id));
    }

    // Member Function: Check if an EventGroup has Events for this Match Phase
    public boolean hasEventsForGroup(int in_group_id, String in_phase) {
        for (EventRow er : event_list) {
            if ((er.match_phase.equals(in_phase)) && (er.group_id == in_group_id))
                return true;
        }

        return false;
    }

    // Member Function: Return the Group Name for a given Group Id
    public String getGroupName(int in_GroupId) {
        return EventGroup.get(in_GroupId).name;
    }

    // Member Function: Add a row of event info into the list giving the data individually
    public void addEventRow(String in_id, String in_group_id, String in_description, String in_phase, String in_seq_start, String in_FOP, String in_next_set, String in_color_index, String in_TransitionEvent) {
        int transition_event;
        if (in_TransitionEvent.isEmpty()) transition_event = Constants.Match.TRANSITION_EVENT_DNE;
        else transition_event = Integer.parseInt(in_TransitionEvent);

        event_list.add(new EventRow(Integer.parseInt(in_id), Integer.parseInt(in_group_id), in_description, in_phase, Boolean.parseBoolean(in_seq_start), Boolean.parseBoolean(in_FOP), in_next_set, in_color_index, transition_event));
    }

    // Member Function: Check if an event group has a specific color to use
    public boolean hasGroupColor(int in_GroupId) {
        return (!EventGroup.get(in_GroupId).color.isEmpty());
    }

    // Member Function: Check if an event has a specific color to use
    public boolean hasEventColor(int in_EventId) {
        for (EventRow er : event_list) {
            if (er.id == in_EventId)
                return !er.color.isEmpty();
        }

        return false;
    }

    // Member Function: Return the color code for this event
    public int getGroupColor(int in_GroupId) {
        return Integer.parseInt(EventGroup.get(in_GroupId).color) - 1;
    }

    // Member Function: Return the color code for this event
    public int getEventColor(int in_EventId) {
        for (EventRow er : event_list) {
            if (er.id == in_EventId)
                if (!er.color.isEmpty())
                    return Integer.parseInt(er.color) - 1;
                else
                    return -1;
        }

        return -1;
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

    // Member Function: Return a list of Events (ids) for a give phase of the match (only ones that start a sequence)
    public ArrayList<Integer> getEventIdsForPhase(String in_phase, int in_group_id) {
        ArrayList<Integer> rc = new ArrayList<>();

        // Return a list depending on the match_phase being asked for and the group id
        for (EventRow er : event_list) {
            if ((er.group_id == in_group_id) && (er.match_phase.equals(in_phase)) && (er.is_FOP_Event) && (er.is_seq_start))
                rc.add((er.id));
        }

        return rc;
    }

    // Member Function: Return a list of Events (description) that can follow a given EventId (next Event in the sequence)
    public ArrayList<String> getNextEvents(int in_EventId) {
        if (in_EventId == -1) return null;

        // If the Match Phase has changed since we started the event, use the transition event instead (if one exists)
        if (!event_list.get(in_EventId).match_phase.equals(Globals.CurrentMatchPhase) && (event_list.get(in_EventId).transition_event > Constants.Match.TRANSITION_EVENT_DNE))
            in_EventId = event_list.get(in_EventId).transition_event;

        // Find the event in the list, and return it's list of valid next events
        for (EventRow er : event_list) {
            if (er.id == in_EventId) return er.next_events_desc;
        }

        return null;
    }

    // Member Function: Return a list of Events (ids) that can follow a given EventId (next Event in the sequence)
    public ArrayList<Integer> getNextEventIds(int in_EventId) {
        if (in_EventId == -1) return null;

        // If the Match Phase has changed since we started the event, use the transition event instead (if one exists)
        if (!event_list.get(in_EventId).match_phase.equals(Globals.CurrentMatchPhase) && (event_list.get(in_EventId).transition_event > Constants.Match.TRANSITION_EVENT_DNE))
            in_EventId = event_list.get(in_EventId).transition_event;

        // Find the event in the list, and return it's list of valid next events
        for (EventRow er : event_list) {
            if (er.id == in_EventId) return er.next_events_ids;
        }

        return null;
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
                            er.next_events_ids.add(ner.id);
                        }
                    }
                }
            }
        }
    }

    // Member Function: Empties out the list
    public void clear() {
        event_list.clear();
        EventGroup.clear();

        // EventGroup will use a 1-based group_id, so add a dummy record for index 0.
        EventGroup.add(new EventGroup("", ""));
    }

    // Member Function: Is this EventId one that happens in the FOP
    public boolean isEventInFOP(int in_EventId) {
        for (EventRow er : event_list) {
            if (er.id == in_EventId) return er.is_FOP_Event;
        }

        // default to NOT in FOP event
        return false;
    }

    // Member Function: Is this EventId one that starts a new sequence of events
    public boolean isEventStartOfSeq(int in_EventId) {
        for (EventRow er : event_list) {
            if (er.id == in_EventId) return er.is_seq_start;
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
        final ArrayList<Integer> next_events_ids;
        final String color;
        final int transition_event;

        // Constructor
        public EventRow(int in_id, int in_group_id, String in_description, String in_phase, Boolean in_seq_start, Boolean in_FOP, String in_next_event_set, String in_color_index, int in_transition_event) {
            id = in_id;
            group_id = in_group_id;
            description = in_description;
            match_phase = in_phase;
            is_FOP_Event = in_FOP;
            is_seq_start = in_seq_start;
            next_event_set = in_next_event_set;
            next_events_desc = new ArrayList<>();
            next_events_ids = new ArrayList<>();
            color = in_color_index;
            transition_event = in_transition_event;
        }
    }

    // =============================================================================================
    // Class:       EventGroup
    // Description: Defines a structure/class to hold the information for each Event Group
    // =============================================================================================
    private class EventGroup {
        String name;
        String color;

        // Constructor
        public EventGroup(String in_name, String in_color_index) {
            name = in_name;
            color = in_color_index;
        }
    }
}
