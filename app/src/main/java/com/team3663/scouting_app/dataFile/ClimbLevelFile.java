package com.team3663.scouting_app.dataFile;

import android.content.Context;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;

import java.util.ArrayList;

public class ClimbLevelFile extends _DataFile {
    private final ArrayList<ClimbLevelRow> ClimbLevel_list;

    public ClimbLevelFile(Context in_context) {
        super(in_context, in_context.getString(R.string.file_climb_levels), in_context.getString(R.string.applaunch_loading_climb_levels));

        ClimbLevel_list = new ArrayList<>();
        addClimbLevelRow(String.valueOf(Constants.PostMatch.CLIMB_LEVEL_NOT_SELECTED), "<Select One>");
    }

    @Override
    protected void processLine(String[] in_line, String in_orig_line) {
        addClimbLevelRow(in_line[0], in_line[1]);
    }

    @Override
    public void clearList() {
        ClimbLevel_list.clear();
        addClimbLevelRow(String.valueOf(Constants.PostMatch.CLIMB_LEVEL_NOT_SELECTED), "<Select One>");
    }

    // Member Function: Add a row of Climb Level
    // info into the list giving the data individually
    public void addClimbLevelRow(String in_id, String in_description) {
        ClimbLevel_list.add(new ClimbLevelRow(in_id, in_description));
    }

    // Member Function: return the size of the list
    public int size() {
        return ClimbLevel_list.size();
    }

    // Member Function: Get back the ID for a given Climb Level
    // entry (needed for logging)
    public String getClimbLevelDescription(int in_id) {
        String ret = "";

        // Loop through the Climb Level
        // list to find a matching id and return the description
        for (ClimbLevelRow cr : ClimbLevel_list) {
            if (cr.id == in_id) {
                ret = cr.description;
                break;
            }
        }

        return ret;
    }

    // Member Function: Get back the ID for a given Climb Level
    // entry (needed for logging)
    public int getClimbLevelId(String in_description) {
        int ret = 0;

        // Loop through the Climb Level
        // list to find a matching description and return the id
        for (ClimbLevelRow cr : ClimbLevel_list) {
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

        for (int i = 0; i < ClimbLevel_list.size(); i++) {
            ret.add(ClimbLevel_list.get(i).description);
        }

        return ret;
    }

    // =============================================================================================
    // Class:       ClimbLevelRow
    // Description: Defines a structure/class to hold the information for each Climb Level
    // =============================================================================================
    protected static class ClimbLevelRow {
        private final int id;
        private final String description;

        // Constructor with individual data
        public ClimbLevelRow(String in_id, String in_description) {
            id = Integer.parseInt(in_id);
            description = in_description;
        }
    }
}