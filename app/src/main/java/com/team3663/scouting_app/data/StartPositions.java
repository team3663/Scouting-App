package com.team3663.scouting_app.data;

import java.util.ArrayList;

// =============================================================================================
// Class:       StartPositions
// Description: Defines a structure/class to hold the information for all Start Position
// =============================================================================================
public class StartPositions {
    private final ArrayList<StartPositionRow> startPosition_list;

    // Constructor
    public StartPositions() {
        startPosition_list = new ArrayList<StartPositionRow>();
    }

    // Member Function: Add a row of Start Position info into the list giving the data individually
    public void addStartPositionRow(String in_id, String in_description) {
        startPosition_list.add(new StartPositionRow(in_id, in_description));
    }

    // Member Function: return the size of the list
    public int size() {
        return startPosition_list.size();
    }

    // Member Function: Get back the Id for a given Start Position entry (needed for logging)
    public int getStartPositionId(String in_description) {
        int ret = 0;

        // Loop through the Start Position list to find a matching description and return the id
        for (StartPositionRow spr : startPosition_list) {
            if (spr.description.equals(in_description)) {
                ret = spr.id;
                break;
            }
        }
        return ret;
    }

    // Member Function: get the description that matches the id
    public String getStartPositionDescription(int in_id) {
        String ret = "";

        for (StartPositionRow spr : startPosition_list) {
            if (in_id == spr.id) {
                ret = spr.description;
                break;
            }
        }

        return ret;
    }

    // Member Function: Return a string list of all records
    public ArrayList<String> getDescriptionList() {
        ArrayList<String> descriptions = new ArrayList<>();

        for (int i = 0; i < startPosition_list.size(); i++) {
            descriptions.add(startPosition_list.get(i).description);
        }
        return descriptions;
    }

    // Member Function: Empties out the list
    public void clear() {
        startPosition_list.clear();
    }

    // =============================================================================================
    // Class:       StartPositionRow
    // Description: Defines a structure/class to hold the information for each Start Position
    // =============================================================================================
    private static class StartPositionRow {
        private final int id;
        private final String description;

        // Constructor with individual data
        public StartPositionRow(String in_id, String in_description) {
            id = Integer.parseInt(in_id);
            description = in_description;
        }
    }
}

