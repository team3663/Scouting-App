package com.team3663.scouting_app.utility.dataFile;

import android.content.Context;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Globals;

import java.util.ArrayList;

public class EventGroupsFile extends _DataFile {
    private final ArrayList<EventGroupRow> event_group_list;

    public EventGroupsFile(Context in_context) {
        super(in_context, in_context.getString(R.string.file_event_groups), in_context.getString(R.string.applaunch_loading_event_groups), in_context.getString(R.string.applaunch_file_error_event_groups));

        event_group_list = new ArrayList<>();
    }

    @Override
    protected void processLine(String[] in_line, String in_orig_line) {
        addEventGroupRow(in_line[0], in_line[1]);
    }

    @Override
    public void clearList() {
        event_group_list.clear();

        // EventGroup will use a 1-based group_id, so add a dummy record for index 0.
        event_group_list.add(new EventGroupRow("", ""));

        Globals.MaxEventGroups = 0;
    }

    // Member Function: Add an EventGroup to the list
    public void addEventGroupRow(String in_id, String in_name) {
        int group_id = Integer.parseInt(in_id);

        // Ensure we're adding the group id (in_id) as the same index.  This shouldn't happen but in case there's a gap in group numbers.
        // 1. Ensure the array is at least as big as the group_id needs
        // 2. Just set the value at the right index to the name.
        for (int i = event_group_list.size(); i <= group_id; ++i)
            event_group_list.add(new EventGroupRow("", ""));

        event_group_list.get(group_id).name = in_name;
        Globals.MaxEventGroups = Math.max(Globals.MaxEventGroups, Integer.parseInt(in_id));
    }

    // Member Function: Return the color code for this event
    public int getGroupColor(int in_GroupId) {
        return Integer.parseInt(event_group_list.get(in_GroupId).color) - 1;
    }

    // Member Function: Return the Group Name for a given Group Id
    public String getGroupName(int in_GroupId) {
        return event_group_list.get(in_GroupId).name;
    }

    // Member Function: Check if an event group has a specific color to use
    public boolean hasGroupColor(int in_GroupId) {
        return (!event_group_list.get(in_GroupId).color.isEmpty());
    }

    // =============================================================================================
    // Class:       EventGroupRow
    // Description: Defines a structure/class to hold the information for each Event Group
    // =============================================================================================
    protected static class EventGroupRow {
        String name;
        String color;

        // Constructor
        public EventGroupRow(String in_name, String in_color_index) {
            name = in_name;
            color = in_color_index;
        }
    }
}