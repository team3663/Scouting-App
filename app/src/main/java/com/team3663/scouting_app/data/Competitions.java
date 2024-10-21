package com.team3663.scouting_app.data;

import java.util.ArrayList;

// =============================================================================================
// Class:       Competitions
// Description: Defines a structure/class to hold the information for all Competitions
// =============================================================================================
public class Competitions {
    private final ArrayList<CompetitionRow> competition_list;

    // Constructor
    public Competitions() {
        competition_list = new ArrayList<CompetitionRow>();
    }

    // Member Function: Add a row of competition info into the list giving all of the data individually
    public void addCompetitionRow(String in_Id, String in_description) {
        competition_list.add(new CompetitionRow(in_Id, in_description));
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

    // Member Function: Empties out the list
    public void clear() {
        competition_list.clear();
    }

    // =============================================================================================
    // Class:       CompetitionRow
    // Description: Defines a structure/class to hold the information for each Competition.
    // =============================================================================================
    private static class CompetitionRow {
        private final int id;
        private final String description;

        // Constructor with individual data
        public CompetitionRow(String in_id, String in_description) {
            id = Integer.parseInt(in_id);
            description = in_description;
        }
    }
}

