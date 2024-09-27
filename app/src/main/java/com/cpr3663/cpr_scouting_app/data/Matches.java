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
        if (in_match_id > 0 && in_match_id < match_list.size())
            return match_list.get(in_match_id);

        return null;
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
        private String red1 = "";
        private String red2 = "";
        private String red3 = "";
        private String blue1 = "";
        private String blue2 = "";
        private String blue3 = "";

        // Constructor with a csv string
        public MatchRow(String in_csvRow) {
            if (!in_csvRow.equals(Constants.NO_MATCH)) {
                String[] data = in_csvRow.split(",");

                // Validate we have enough values otherwise this was a bad row and we'll get an out-of-bounds exception
                if (data.length == 8) {
                    red1 = data[2].trim();
                    red2 = data[3].trim();
                    red3 = data[4].trim();
                    blue1 = data[5].trim();
                    blue2 = data[6].trim();
                    blue3 = data[7].trim();
                }
            }
        }

        // Constructor with individual data
        public MatchRow(String in_red1, String in_red2, String in_red3, String in_blue1, String in_blue2, String in_blue3) {
            red1 = in_red1.trim();
            red2 = in_red2.trim();
            red3 = in_red3.trim();
            blue1 = in_blue1.trim();
            blue2 = in_blue2.trim();
            blue3 = in_blue3.trim();
        }

        // Member Function: Return a list of team numbers for this match
        public ArrayList<String> getListOfTeams() {
            ArrayList<String> ret = new ArrayList<>();

            ret.add(red1);
            ret.add(red2);
            ret.add(red3);
            ret.add(blue1);
            ret.add(blue2);
            ret.add(blue3);

            return ret;
        }

        // Member Function: Return the team that is in a certain position
        public String getTeamInPosition(String in_position) {
            String ret = "";

            switch (in_position.toUpperCase()) {
                case "BLUE 1":
                case "BLUE1":
                    ret = blue1;
                    break;
                case "BLUE 2":
                case "BLUE2":
                    ret = blue2;
                    break;
                case "BLUE 3":
                case "BLUE3":
                    ret = blue3;
                    break;
                case "RED 1":
                case "RED1":
                    ret = red1;
                    break;
                case "RED 2":
                case "RED2":
                    ret = red2;
                    break;
                case "RED 3":
                case "RED3":
                    ret = red3;
                    break;
            }

            return ret;
        }
    }
}
