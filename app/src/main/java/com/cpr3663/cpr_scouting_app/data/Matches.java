package com.cpr3663.cpr_scouting_app.data;

import com.cpr3663.cpr_scouting_app.Constants;

import java.util.ArrayList;

// =============================================================================================
// Class:       Matches
// Description: Defines a structure/class to hold the information for all Matches
// Methods:     getMatchInfoRow()
//                  return a MatchInfoRow item for the given match id
//              clear()
//                  empties out the list
// =============================================================================================
public class Matches {
    private final ArrayList<MatchRow> match_list;

    // Constructor
    public Matches() {
        match_list = new ArrayList<MatchRow>();
    }

    // Member Function: Add a row of match info into the list giving all of the data individually
    public void addMatchRow(String in_red1, String in_red2, String in_red3, String in_blue1, String in_blue2, String in_blue3) {
        match_list.add(new MatchRow(in_red1, in_red2, in_red3, in_blue1, in_blue2, in_blue3));
    }

    // Member Function: Add a row of match info into the list giving the data in a csv format
    public void addMatchRow(String in_csvRow) {
        match_list.add(new MatchRow(in_csvRow));
    }

    // Member Function: Get back a row of data for a given match
    public MatchRow getMatchInfoRow(int in_match_id) {
        return match_list.get(in_match_id);
    }

    // Member Function: Return the number of matches (so it doesn't include the index 0)
    public int getNumberOfMatches() {
        return match_list.size() - 1;
    }

    // Member Function: Return the size of the list of Matches
    public int size() {
        return match_list.size();
    }

    // Member Function: Empties out the list
    public void clear() {
        match_list.clear();
    }

    // =============================================================================================
    // Class:       MatchInfoRow
    // Description: Defines a structure/class to hold the information for each Match
    // Methods:     getListOfTeams()
    //                  return an array of team numbers (6 of them) that are in this match
    // =============================================================================================
    public static class MatchRow {
        // Class Members
        private int red1 = 0;
        private int red2 = 0;
        private int red3 = 0;
        private int blue1 = 0;
        private int blue2 = 0;
        private int blue3 = 0;

        // Constructor with a csv string
        public MatchRow(String in_csvRow) {
            if (!in_csvRow.equals(Constants.NO_MATCH)) {
                String[] data = in_csvRow.split(",");

                // Validate we have enough values otherwise this was a bad row and we'll get an out-of-bounds exception
                if (data.length == 8) {
                    red1 = Integer.parseInt(data[2]);
                    red2 = Integer.parseInt(data[3]);
                    red3 = Integer.parseInt(data[4]);
                    blue1 = Integer.parseInt(data[5]);
                    blue2 = Integer.parseInt(data[6]);
                    blue3 = Integer.parseInt(data[7]);
                }
            }
        }

        // Constructor with individual data
        public MatchRow(String in_red1, String in_red2, String in_red3, String in_blue1, String in_blue2, String in_blue3) {
            red1 = Integer.parseInt(in_red1);
            red2 = Integer.parseInt(in_red2);
            red3 = Integer.parseInt(in_red3);
            blue1 = Integer.parseInt(in_blue1);
            blue2 = Integer.parseInt(in_blue2);
            blue3 = Integer.parseInt(in_blue3);
        }

        // Member Function: Return a list of team numbers for this match
        public int[] getListOfTeams() {
            return new int[] {red1, red2, red3, blue1, blue2, blue3};
        }
    }
}
