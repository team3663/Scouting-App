package com.cpr3663.cpr_scouting_app.data;

import java.util.ArrayList;
import java.util.Collections;

// =============================================================================================
// Class:       Competitions
// Description: Defines a structure/class to hold the information for all Competitions
// Methods:     addCompetitionInfoRow()
//                  add a row of competition info
//              size()
//                  return the number of competitions we have
//              getCompetitionInfoRow()
//                  return a CompetitionInfoRow item for the given competition id
//              clear()
//                  empties out the list
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
            ret.add(cr.getDescription());
        }

        return ret;
    }

    // Member Function: get the id that matches the description
    public int getCompetitionId(String in_description) {
        int ret = 0;

        for (CompetitionRow cr : competition_list) {
            if (in_description.equals(cr.getDescription())) {
                ret = cr.getId();
                break;
            }
        }

        return ret;
    }

    // Member Function: get the description that matches the id
    public String getCompetitionDescription(int in_id) {
        String ret = "";

        for (CompetitionRow cr : competition_list) {
            if (in_id == cr.getId()) {
                ret = cr.getDescription();
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
    // Methods:     getId()
    //                  returns the (int) competition number for this row.
    //              getDescription()
    //                  returns the (String) description for this row.
    // =============================================================================================
    public static class CompetitionRow {
        // Class Members
        private final int id;
        private final String description;

        // Constructor with individual data
        public CompetitionRow(String in_id, String in_description) {
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

