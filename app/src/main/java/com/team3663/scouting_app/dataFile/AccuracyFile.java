package com.team3663.scouting_app.dataFile;

import android.content.Context;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;

import java.util.ArrayList;

public class AccuracyFile extends _DataFile {
    private final ArrayList<AccuracyRow> Accuracy_list;

    public AccuracyFile(Context in_context) {
        super(in_context, in_context.getString(R.string.file_accuracy), in_context.getString(R.string.applaunch_loading_accuracy));

        Accuracy_list = new ArrayList<>();
        addAccuracyRow(String.valueOf(Constants.PostMatch.ACCURACY_NOT_SELECTED), "<Select One>");
    }

    @Override
    protected void processLine(String[] in_line, String in_orig_line) {
        addAccuracyRow(in_line[0], in_line[1]);
    }

    @Override
    public void clearList() {
        Accuracy_list.clear();
        addAccuracyRow(String.valueOf(Constants.PostMatch.ACCURACY_NOT_SELECTED), "<Select One>");
    }

    // Member Function: Add a row of Accuracy
    // info into the list giving the data individually
    public void addAccuracyRow(String in_id, String in_description) {
        Accuracy_list.add(new AccuracyRow(in_id, in_description));
    }

    // Member Function: return the size of the list
    public int size() {
        return Accuracy_list.size();
    }

    // Member Function: Get back the ID for a given Accuracy
    // entry (needed for logging)
    public String getAccuracyDescription(int in_id) {
        String ret = "";

        // Loop through the Accuracy
        // list to find a matching id and return the description
        for (AccuracyRow cr : Accuracy_list) {
            if (cr.id == in_id) {
                ret = cr.description;
                break;
            }
        }

        return ret;
    }

    // Member Function: Get back the ID for a given Accuracy
    // entry (needed for logging)
    public int getAccuracyId(String in_description) {
        int ret = 0;

        // Loop through the Accuracy
        // list to find a matching description and return the id
        for (AccuracyRow cr : Accuracy_list) {
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

        for (int i = 0; i < Accuracy_list.size(); i++) {
            ret.add(Accuracy_list.get(i).description);
        }

        return ret;
    }

    // =============================================================================================
    // Class:       AccuracyRow
    // Description: Defines a structure/class to hold the information for each Accuracy
    // =============================================================================================
    protected static class AccuracyRow {
        private final int id;
        private final String description;

        // Constructor with individual data
        public AccuracyRow(String in_id, String in_description) {
            id = Integer.parseInt(in_id);
            description = in_description;
        }
    }
}