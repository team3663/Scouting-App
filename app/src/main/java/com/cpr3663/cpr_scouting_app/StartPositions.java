package com.cpr3663.cpr_scouting_app;

import java.lang.reflect.Array;
import java.util.ArrayList;

// =============================================================================================
// Class:       StartPositions
// Description: Defines a structure/class to hold the information for all Start Position
// Methods:     addStartPositionRow()
//                  add a row of Start Position info
//              getStartPositionRow()
//                  return a StartPositionRow item for the given Start Position id
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

    // Member Function: Get back a row of data for a given Start Position entry
    public StartPositionRow getStartPositionRow(int in_index) {
        return startPosition_list.get(in_index);
    }

    // Member Function: Get back the Id for a given Start Position entry (needed for logging)
    public int getStartPositionId(String in_description) {
        int ret = 0;

        // Loop through the Start Position list to find a matching description and return the id
        for (StartPositionRow spr : startPosition_list) {
            if (spr.getDescription().equals(in_description)) {
                ret = spr.id;
                break;
            }
        }
        return ret;
    }

    public String[] getDescriptionList() {
        String[] descriptions = new String[startPosition_list.size()];

        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = startPosition_list.get(i).getDescription();
        }
        return descriptions;
    }

    // =============================================================================================
    // Class:       StartPositionRow
    // Description: Defines a structure/class to hold the information for each Start Position
    // Methods:     getId()
    //                  returns the (int) Start Position number for this row to use for logging
    //              getDescription()
    //                  returns the (String) description for this row
    // =============================================================================================
    public static class StartPositionRow {
        // Class Members
        private final int id;
        private final String description;

        // Constructor with individual data
        public StartPositionRow(String in_id, String in_description) {
            id = Integer.parseInt(in_id);
            description = in_description;
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

