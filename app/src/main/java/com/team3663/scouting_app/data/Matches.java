package com.team3663.scouting_app.data;

import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;

import java.util.ArrayList;

// =============================================================================================
// Class:       Matches
// Description: Defines a structure/class to hold the information for all Matches.  This means
//              we have an array of arrays.  First array defines which match type we're holding
//              (quals, semis, finals).  The second (inner) array has the list of matches for
//              that type.
// =============================================================================================
public class Matches {
    private final ArrayList<MatchesForType> match_list;

    // Constructor
    public Matches(){
        // We need to add a whole match list for each type of matches
        match_list = new ArrayList<>();
        for (int i = 0; i < Constants.PreMatch.NUMBER_OF_MATCH_TYPES; ++i) {
            match_list.add(new MatchesForType());
        }
    }

    // Member Function: Add a row of match info into the list giving the data in a csv format
    public void addMatchRowForNoMatch() {
        match_list.get(Globals.CurrentMatchType - 1).addMatchRowForNoMatch();
    }

    // Member Function: Add a row of match info into the list giving the individual data
    public void addMatchRow(String in_match_type_short_form, String in_MatchNum, String in_red1, String in_red2, String in_red3, String in_blue1, String in_blue2, String in_blue3) {
        int match_type = Globals.MatchTypeList.getMatchTypeId(in_match_type_short_form);

        for (int i = Globals.MatchList.size(); i < Integer.parseInt(in_MatchNum); i++) {
            match_list.get(match_type - 1).addMatchRowForNoMatch();
        }

        if (Integer.parseInt(in_MatchNum) < match_list.get(match_type - 1).size())
            match_list.get(match_type - 1).setMatchRow(in_MatchNum, in_red1, in_red2, in_red3, in_blue1, in_blue2, in_blue3);
        else
            match_list.get(match_type - 1).addMatchRow(in_red1, in_red2, in_red3, in_blue1, in_blue2, in_blue3);
    }

    // Member Function: do we have valid match data for this one
    public boolean isCurrentMatchValid() {
        return match_list.get(Globals.CurrentMatchType - 1).isCurrentMatchValid();
    }

    // Member Function: Return a list of team numbers for this match (must be set in Globals.CurrentMatchNumber)
    public ArrayList<String> getListOfTeams() {
        return match_list.get(Globals.CurrentMatchType - 1).getListOfTeams();
    }

    // Member Function: Return the team that is in a certain position
    public String getTeamInPosition(String in_position) {
        return match_list.get(Globals.CurrentMatchType - 1).getTeamInPosition(in_position);
    }

    // Member Function: Return the size of the list of Matches
    public int size() {
        return match_list.get(Globals.CurrentMatchType - 1).size();
    }

    // Member Function: Empties out the list
    public void clear() {
        for (int i = 0; i < match_list.size(); ++i) {
            match_list.get(i).clear();
        }
    }

        // =============================================================================================
    // Class:       MatchesForType
    // Description: Defines a structure/class to hold the information for all Matches
    // =============================================================================================
    public static class MatchesForType {
        private final ArrayList<MatchInfoRow> match_list_for_type;

        // Constructor
        public MatchesForType() {
            match_list_for_type = new ArrayList<>();
        }

        // Member Function: Add a row of match info into the list giving the data in a csv format
        public void addMatchRowForNoMatch() {
            match_list_for_type.add(new MatchInfoRow());
        }

        // Member Function: Add a row of match info into the list giving the data individually
        public void addMatchRow(String in_red1, String in_red2, String in_red3, String in_blue1, String in_blue2, String in_blue3) {
            match_list_for_type.add(new MatchInfoRow(in_red1, in_red2, in_red3, in_blue1, in_blue2, in_blue3));
        }

        // Member Function: Set a row of match info into the list giving the data individually
        public void setMatchRow(String in_MatchNum, String in_red1, String in_red2, String in_red3, String in_blue1, String in_blue2, String in_blue3) {
            match_list_for_type.set(Integer.parseInt(in_MatchNum), new MatchInfoRow(in_red1, in_red2, in_red3, in_blue1, in_blue2, in_blue3));
        }

        // Member Function: do we have valid match data for this one
        public boolean isCurrentMatchValid() {
            return (Globals.CurrentMatchNumber > 0) && (Globals.CurrentMatchNumber < match_list_for_type.size());
        }

        // Member Function: Return a list of team numbers for this match (must be set in Globals.CurrentMatchNumber)
        public ArrayList<String> getListOfTeams() {
            ArrayList<String> ret = new ArrayList<>();

            if (this.isCurrentMatchValid()) {
                int currMatch = Globals.CurrentMatchNumber;
                ret.add(match_list_for_type.get(currMatch).red1);
                ret.add(match_list_for_type.get(currMatch).red2);
                ret.add(match_list_for_type.get(currMatch).red3);
                ret.add(match_list_for_type.get(currMatch).blue1);
                ret.add(match_list_for_type.get(currMatch).blue2);
                ret.add(match_list_for_type.get(currMatch).blue3);
            }

            return ret;
        }

        // Member Function: Return the team that is in a certain position
        public String getTeamInPosition(String in_position) {
            String ret = "";

            if (!this.isCurrentMatchValid()) return ret;
            int currMatch = Globals.CurrentMatchNumber;

            switch (in_position.toUpperCase()) {
                case "BLUE 1":
                case "BLUE1":
                    ret = match_list_for_type.get(currMatch).blue1;
                    break;
                case "BLUE 2":
                case "BLUE2":
                    ret = match_list_for_type.get(currMatch).blue2;
                    break;
                case "BLUE 3":
                case "BLUE3":
                    ret = match_list_for_type.get(currMatch).blue3;
                    break;
                case "RED 1":
                case "RED1":
                    ret = match_list_for_type.get(currMatch).red1;
                    break;
                case "RED 2":
                case "RED2":
                    ret = match_list_for_type.get(currMatch).red2;
                    break;
                case "RED 3":
                case "RED3":
                    ret = match_list_for_type.get(currMatch).red3;
                    break;
            }

            return ret;
        }

        // Member Function: Return the size of the list of Matches
        public int size() {
            return match_list_for_type.size();
        }

        // Member Function: Empties out the list
        public void clear() {
            match_list_for_type.clear();
        }

        // =============================================================================================
        // Class:       MatchInfoRow
        // Description: Defines a structure/class to hold the information for each Match
        // =============================================================================================
        private static class MatchInfoRow {
            private String red1 = "";
            private String red2 = "";
            private String red3 = "";
            private String blue1 = "";
            private String blue2 = "";
            private String blue3 = "";

            // Constructor with no match information
            public MatchInfoRow() {
            }

            // Constructor with values
            public MatchInfoRow(String in_red1, String in_red2, String in_red3, String in_blue1, String in_blue2, String in_blue3) {
                red1 = in_red1;
                red2 = in_red2;
                red3 = in_red3;
                blue1 = in_blue1;
                blue2 = in_blue2;
                blue3 = in_blue3;
            }
        }
    }
}
