package com.team3663.scouting_app.utility.dataFile;

import android.content.Context;

import com.team3663.scouting_app.R;

import java.util.ArrayList;
import java.util.Objects;

public class MatchTypesFile extends _DataFile {
    private final ArrayList<MatchTypeRow> matchType_list;

    public MatchTypesFile(Context in_context) {
        super(in_context, in_context.getString(R.string.file_match_types), in_context.getString(R.string.applaunch_loading_match_types), in_context.getString(R.string.applaunch_file_error_match_types));

        matchType_list = new ArrayList<>();
    }

    @Override
    protected void processLine(String[] in_line, String in_orig_line) {
        addMatchTypeRow(in_line[0], in_line[1]);
    }

    @Override
    public void clearList() {
        matchType_list.clear();
    }

    // Member Function: Add a row of match type info into the list giving the data individually
    public void addMatchTypeRow(String in_short_form, String in_description) {
        matchType_list.add(new MatchTypeRow(in_short_form, in_description));
    }

    // Member Function: return the size of the list
    public int size() {
        return matchType_list.size();
    }

    // Member Function: Return a string list of all records
    public ArrayList<String> getDescriptionList() {
        ArrayList<String> descriptions = new ArrayList<>();

        for (int i = 0; i < matchType_list.size(); i++) {
            descriptions.add(matchType_list.get(i).description);
        }
        return descriptions;
    }

    // Member Function: Get back the ShortForm for a given match type Description
    public String getMatchTypeShortForm(String in_description) {
        String ret = "";

        // Loop through the match type list to find a matching id and return the description
        for (MatchTypeRow mtr : matchType_list) {
            if (Objects.equals(mtr.description, in_description)) {
                ret = mtr.short_form;
                break;
            }
        }
        return ret;
    }

    // Member Function: Get back the Description for a given match type entry
    public String getMatchTypeDescription(String in_short_form) {
        String ret = "";

        // Loop through the match type list to find a matching id and return the description
        for (MatchTypeRow mtr : matchType_list) {
            if (Objects.equals(mtr.short_form, in_short_form)) {
                ret = mtr.description;
                break;
            }
        }
        return ret;
    }

    // =============================================================================================
    // Class:       MatchTypeRow
    // Description: Defines a structure/class to hold the information for each match type
    // =============================================================================================
    protected static class MatchTypeRow {
        private final String short_form;
        private final String description;

        // Constructor with individual data
        public MatchTypeRow(String in_short_form, String in_description) {
            short_form = in_short_form;
            description = in_description;
        }
    }
}