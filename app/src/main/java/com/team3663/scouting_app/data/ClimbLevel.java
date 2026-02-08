package com.team3663.scouting_app.data;

import java.util.ArrayList;
import java.util.Objects;

// =============================================================================================
// Class:       ClimbLevel
// Description: Defines structure/class to hold the information for all Climb Level values
// =============================================================================================
public class ClimbLevel {
    private final ArrayList<ClimbLevelRow> climbLevel_list;

    public ClimbLevel() {
        climbLevel_list = new ArrayList<>();

        // Add climb level values into the list
        addClimbLevelRow("-1", "<Select One>");
        addClimbLevelRow("L3", "L3");
        addClimbLevelRow("L2", "L2");
        addClimbLevelRow("L1", "L1");
    }

    // Add a row
    public void addClimbLevelRow(String in_value, String in_description) {
        climbLevel_list.add(new ClimbLevelRow(in_value, in_description));
    }

    // Return the size
    public int size() {
        return climbLevel_list.size();
    }

    public ArrayList<String> getDescriptionList() {
        ArrayList<String> descriptions = new ArrayList<>();
        for (ClimbLevel.ClimbLevelRow clr : climbLevel_list) {
            descriptions.add(clr.description);
        }
        return descriptions;
    }

    public String getClimbLevelValue(String in_description) {
        for (ClimbLevel.ClimbLevelRow clr : climbLevel_list) {
            if (Objects.equals(clr.description, in_description)) {
                return clr.value;
            }
        }
        return "";
    }

    public String getClimbLevelDescription(String in_value) {
        for (ClimbLevel.ClimbLevelRow clr : climbLevel_list) {
            if (Objects.equals(clr.value, in_value)) {
                return clr.description;
            }
        }
        return "";
    }

    // Member Function: Empties out the list
    public void clear() {climbLevel_list.clear();}

    // =============================================================================================
    // Class:       ClimbPositionRow
    // Description: Defines a structure/class to hold the information for each climb position
    // =============================================================================================
    private static class ClimbLevelRow {
        private final String value;
        private final String description;

        public ClimbLevelRow(String in_value, String in_description) {
            value = in_value;
            description = in_description;
        }
    }
}
