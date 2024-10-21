package com.team3663.scouting_app.data;

import java.util.ArrayList;

// =============================================================================================
// Class:       ClimbPositions
// Description: Defines a structure/class to hold the information for all Climb Position
// =============================================================================================
public class ClimbPositions {
    private final ArrayList<ClimbPositionRow> climbPosition_list;

    // Constructor
    public ClimbPositions() {
        climbPosition_list = new ArrayList<>();
    }

    // Member Function: Add a row of Climb Position info into the list giving the data individually
    public void addClimbPositionRow(String in_id, String in_description) {
        climbPosition_list.add(new ClimbPositionRow(in_id, in_description));
    }

    // Member Function: return the size of the list
    public int size() {
        return climbPosition_list.size();
    }

    // Member Function: Get back the Id for a given Climb Position entry (needed for logging)
    public int getClimbPositionId(String in_description) {
        int ret = 0;

        // Loop through the Climb Position list to find a matching description and return the id
        for (ClimbPositionRow cpr : climbPosition_list) {
            if (cpr.description.equals(in_description)) {
                ret = cpr.id;
                break;
            }
        }
        return ret;
    }

    // Member Function: Return a string list of all records
    public ArrayList<String> getDescriptionList() {
        ArrayList<String> descriptions = new ArrayList<>();

        for (int i = 0; i < climbPosition_list.size(); i++) {
            descriptions.add(climbPosition_list.get(i).description);
        }
        return descriptions;
    }

    // Member Function: Empties out the list
    public void clear() {
        climbPosition_list.clear();
    }

    // =============================================================================================
    // Class:       ClimbPositionRow
    // Description: Defines a structure/class to hold the information for each Climb Position
    // =============================================================================================
    private static class ClimbPositionRow {
        private final int id;
        private final String description;

        // Constructor with individual data
        public ClimbPositionRow(String in_id, String in_description) {
            id = Integer.parseInt(in_id);
            description = in_description;
        }
    }
}

