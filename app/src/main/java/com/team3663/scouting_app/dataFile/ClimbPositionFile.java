package com.team3663.scouting_app.dataFile;

import android.content.Context;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;

import java.util.ArrayList;

public class ClimbPositionFile extends _DataFile {
    private final ArrayList<ClimbPositionRow> climbPosition_list;

    public ClimbPositionFile(Context in_context) {
        super(in_context, in_context.getString(R.string.file_climb_positions), in_context.getString(R.string.applaunch_loading_climb_positions));

        climbPosition_list = new ArrayList<>();
        addClimbPositionRow(String.valueOf(Constants.PostMatch.CLIMB_POSITION_NOT_SELECTED), "<Select One>");
    }

    @Override
    protected void processLine(String[] in_line, String in_orig_line) {
        addClimbPositionRow(in_line[0], in_line[1]);
    }

    @Override
    public void clearList() {
        climbPosition_list.clear();
        addClimbPositionRow(String.valueOf(Constants.PostMatch.CLIMB_POSITION_NOT_SELECTED), "<Select One>");
    }

    // Member Function: Add a row of Climb Position info into the list giving the data individually
    public void addClimbPositionRow(String in_id, String in_description) {
        climbPosition_list.add(new ClimbPositionRow(in_id, in_description));
    }

    // Member Function: return the size of the list
    public int size() {
        return climbPosition_list.size();
    }

    // Member Function: Get back the ID for a given Climb Position entry (needed for logging)
    public String getClimbPositionDescription(int in_id) {
        String ret = "";

        // Loop through the Climb Position list to find a matching id and return the description
        for (ClimbPositionRow cr : climbPosition_list) {
            if (cr.id == in_id) {
                ret = cr.description;
                break;
            }
        }

        return ret;
    }

    // Member Function: Get back the ID for a given Climb Position entry (needed for logging)
    public int getClimbPositionId(String in_description) {
        int ret = 0;

        // Loop through the Climb Position list to find a matching description and return the id
        for (ClimbPositionRow cr : climbPosition_list) {
            if (cr.description.equals(in_description)) {
                ret = cr.id;
                break;
            }
        }
        return ret;
    }

    // Member Function: Return a string list of all records
    public ArrayList<String> getDescriptionList() {
        ArrayList<String> ret = new ArrayList<>();

        for (int i = 0; i < climbPosition_list.size(); i++) {
            ret.add(climbPosition_list.get(i).description);
        }

        return ret;
    }

    // =============================================================================================
    // Class:       ClimbPositionRow
    // Description: Defines a structure/class to hold the information for each Climb Position
    // =============================================================================================
    protected static class ClimbPositionRow {
        private final int id;
        private final String description;

        // Constructor with individual data
        public ClimbPositionRow(String in_id, String in_description) {
            id = Integer.parseInt(in_id);
            description = in_description;
        }
    }
}