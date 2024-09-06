package com.cpr3663.cpr_scouting_app.data;

import java.util.ArrayList;

// =============================================================================================
// Class:       ClimbPositions
// Description: Defines a structure/class to hold the information for all Climb Position
// Methods:     addClimbPositionRow()
//                  add a row of Climb Position info
//              getClimbPositionRow()
//                  return a ClimbPositionRow item for the given Climb Position id
// =============================================================================================
public class ClimbPositions {
    private final ArrayList<ClimbPositionRow> climbPosition_list;

    // Constructor
    public ClimbPositions() {
        climbPosition_list = new ArrayList<ClimbPositionRow>();
    }

    // Member Function: Add a row of Climb Position info into the list giving the data individually
    public void addClimbPositionRow(String in_id, String in_description) {
        climbPosition_list.add(new ClimbPositionRow(in_id, in_description));
    }

    // Member Function: return the size of the list
    public int size() {
        return climbPosition_list.size();
    }

    // Member Function: Get back a row of data for a given Climb Position entry
    public ClimbPositionRow getClimbPositionRow(int in_index) {
        return climbPosition_list.get(in_index);
    }

    // Member Function: Get back the Id for a given Climb Position entry (needed for logging)
    public int getClimbPositionId(String in_description) {
        int ret = 0;

        // Loop through the Climb Position list to find a matching description and return the id
        for (ClimbPositionRow cpr : climbPosition_list) {
            if (cpr.getDescription().equals(in_description)) {
                ret = cpr.id;
                break;
            }
        }
        return ret;
    }

    // =============================================================================================
    // Class:       ClimbPositionRow
    // Description: Defines a structure/class to hold the information for each Climb Position
    // Methods:     getId()
    //                  returns the (int) Climb Position number for this row to use for logging
    //              getDescription()
    //                  returns the (String) description for this row
    // =============================================================================================
    public static class ClimbPositionRow {
        // Class Members
        private final int id;
        private final String description;

        // Constructor with individual data
        public ClimbPositionRow(String in_id, String in_description) {
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

