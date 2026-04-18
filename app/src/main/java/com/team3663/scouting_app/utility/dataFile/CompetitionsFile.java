package com.team3663.scouting_app.utility.dataFile;

import android.content.Context;
import com.team3663.scouting_app.R;

import java.util.ArrayList;
import java.util.Collections;

public class CompetitionsFile extends _DataFile {
    private final ArrayList<CompetitionRow> competition_list;

    public CompetitionsFile(Context in_context) {
        super(in_context, in_context.getString(R.string.file_competitions), in_context.getString(R.string.applaunch_loading_competitions), in_context.getString(R.string.applaunch_file_error_competitions));

        competition_list = new ArrayList<>();
    }

    @Override
    protected void processLine(String[] in_line, String in_orig_line) {
        addCompetitionRow(in_line[0], in_line[4], in_line[6]);
    }

    @Override
    public void clearList() {
        competition_list.clear();
    }


    // Member Function: Add a row of competition info into the list giving all of the data individually
    public void addCompetitionRow(String in_Id, String in_description, String in_attended) {
        competition_list.add(new CompetitionRow(in_Id, in_description, in_attended));
    }

    // Member Function: return the size of the list
    public int size() {
        return competition_list.size();
    }

    // Member Function: return a list of Competition ID (used for Settings page)
    public ArrayList<String> getCompetitionList() {
        ArrayList<String> ret = new ArrayList<>();

        // If we have nothing to process, return nothing!
        if (competition_list.isEmpty()) return ret;

        // Make an array list of Competition Ids
        for (CompetitionRow cr : competition_list) {
            ret.add(cr.description);
        }

        Collections.sort(ret);
        return ret;
    }

    // Member Function: get whether this competition was attended in person
    public boolean isAttended(int in_id) {
        boolean ret = false;

        for (CompetitionRow cr : competition_list) {
            if (in_id == cr.id) {
                ret = cr.attended;
                break;
            }
        }

        return ret;
    }

    // Member Function: get the id that matches the description
    public int getCompetitionId(String in_description) {
        int ret = 0;

        for (CompetitionRow cr : competition_list) {
            if (in_description.equals(cr.description)) {
                ret = cr.id;
                break;
            }
        }

        return ret;
    }

    // Member Function: get the description that matches the id
    public String getCompetitionDescription(int in_id) {
        String ret = "";

        for (CompetitionRow cr : competition_list) {
            if (in_id == cr.id) {
                ret = cr.description;
                break;
            }
        }

        return ret;
    }

    // =============================================================================================
    // Class:       CompetitionRow
    // Description: Defines a structure/class to hold the information for each Competition.
    // =============================================================================================
    protected static class CompetitionRow {
        private final int id;
        private final String description;
        private final boolean attended;

        // Constructor with individual data
        public CompetitionRow(String in_id, String in_description, String in_attended) {
            id = Integer.parseInt(in_id);
            description = in_description;
            if (in_attended.isEmpty()) attended = false;
            else attended = Boolean.parseBoolean(in_attended);
        }
    }
}