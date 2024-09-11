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

    // Member Function: Get back a row of data for a given id
    public CompetitionRow getCompetitionRow(int id) {
        CompetitionRow competition = null;
        for (CompetitionRow competitionRow : competition_list) {
            if (id == competitionRow.getId()) {
                competition = competitionRow;
                break;
            }
        }
        return competition;
    }

    // Member Function: return a list of Competition ID (used for Settings page)
    public ArrayList<String> getCompetitionIdList() {
        ArrayList<Integer> ret_int = new ArrayList<>();
        ArrayList<String> ret = new ArrayList<>();

        // If we have nothing to process, return nothing!
        if (competition_list.isEmpty()) return ret;

        // Make an array list of Competition Ids
        for (CompetitionRow cr : competition_list) {
            ret_int.add(cr.getId());
        }

        // Sort (numerically) the list and convert into the String ArrayList
        Collections.sort(ret_int);
        for (Integer i : ret_int) ret.add(i.toString());

        return ret;
    }

    public String getCompetitionDescriptionById(int in_Id) {
        String ret = "";

        for (CompetitionRow cr : competition_list) {
            if (in_Id == cr.getId()) {
                ret = cr.getDescription();
                break;
            }
        }

        return ret;
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

