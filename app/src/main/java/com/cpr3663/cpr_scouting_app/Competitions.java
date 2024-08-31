package com.cpr3663.cpr_scouting_app;

import java.util.ArrayList;

// =============================================================================================
// Class:       Competitions
// Description: Defines a structure/class to hold the information for all Competitions
// Methods:     addCompetitionInfoRow()
//                  add a row of competition info
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

    // Member Function: Get back a row of data for a given competition
    public CompetitionRow getCompetitionRow(int in_index) {
        return competition_list.get(in_index);
    }

    // =============================================================================================
    // Class:       CompetitionRow
    // Description: Defines a structure/class to hold the information for each Device.
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

