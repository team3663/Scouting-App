package com.team3663.scouting_app.data;

import java.util.ArrayList;
import java.util.Objects;

// =============================================================================================
// Class:       ClimbPosition
// Description: Defines a structure/class to hold the information for all robot Climb Position values
// =============================================================================================

public class ClimbPosition {

    private final ArrayList<ClimbPositionRow> climbPosition_list;

    public ClimbPosition() {
        climbPosition_list = new ArrayList<>();

        // Add climb position values into the list
        addClimbPositionRow("-1","<Select One>");
        addClimbPositionRow("Left Side", "Left Side");
        addClimbPositionRow("Right Side", "Right Side");
        addClimbPositionRow("Front Upright", "Front Upright");
        addClimbPositionRow("Front Middle", "Front Middle");
        addClimbPositionRow("Back", "Back");
    }

    // Add a row
    public void addClimbPositionRow(String in_value, String in_description) {
        climbPosition_list.add(new ClimbPositionRow(in_value, in_description));
    }

    // Return the size
    public int size() {
        return climbPosition_list.size();
    }

    public ArrayList<String> getDescriptionList() {
        ArrayList<String> descriptions = new ArrayList<>();
        for (ClimbPositionRow cpr : climbPosition_list) {
            descriptions.add(cpr.description);
        }
        return descriptions;
    }

    public String getClimbPositionValue(String in_description) {
        for (ClimbPositionRow cpr : climbPosition_list) {
            if (Objects.equals(cpr.description, in_description)) {
                return cpr.value;
            }
        }
        return "";
    }

    public String getClimbPositionDescription(String in_value) {
        for (ClimbPositionRow cpr : climbPosition_list) {
            if (Objects.equals(cpr.value, in_value)) {
                return cpr.description;
            }
        }
        return "";
    }

    // Member Function: Empties out the list
    public void clear() {climbPosition_list.clear();}

    // =============================================================================================
    // Class:       ClimbPositionRow
    // Description: Defines a structure/class to hold the information for each climb position
    // =============================================================================================
    private static class ClimbPositionRow {
        private final String value;
        private final String description;

        public ClimbPositionRow(String in_value, String in_description) {
            value = in_value;
            description = in_description;
        }
    }
}