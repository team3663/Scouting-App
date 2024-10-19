package com.team3663.scouting_app.data;

import java.util.ArrayList;

// =============================================================================================
// Class:       TrapResults
// Description: Defines a structure/class to hold the information for all Trap Result
// =============================================================================================
public class TrapResults {
    private final ArrayList<TrapResultRow> trapResult_list;

    // Constructor
    public TrapResults() {
        trapResult_list = new ArrayList<TrapResultRow>();
    }

    // Member Function: Add a row of Trap Result info into the list giving the data individually
    public void addTrapResultRow(String in_id, String in_description) {
        trapResult_list.add(new TrapResultRow(in_id, in_description));
    }

    // Member Function: return the size of the list
    public int size() {
        return trapResult_list.size();
    }

    // Member Function: Get back the Id for a given Trap Result entry (needed for logging)
    public int getTrapResultId(String in_description) {
        int ret = 0;

        // Loop through the Trap Result list to find a matching description and return the id
        for (TrapResultRow trr : trapResult_list) {
            if (trr.description.equals(in_description)) {
                ret = trr.id;
                break;
            }
        }
        return ret;
    }

    // Member Function: Return a string list of all records
    public ArrayList<String> getDescriptionList() {
        ArrayList<String> descriptions = new ArrayList<>();

        for (int i = 0; i < trapResult_list.size(); i++) {
            descriptions.add(trapResult_list.get(i).description);
        }
        return descriptions;
    }

    // Member Function: Empties out the list
    public void clear() {
        trapResult_list.clear();
    }

    // =============================================================================================
    // Class:       TrapResultRow
    // Description: Defines a structure/class to hold the information for each Trap Result
    // =============================================================================================
    private static class TrapResultRow {
        private final int id;
        private final String description;

        // Constructor with individual data
        public TrapResultRow(String in_id, String in_description) {
            id = Integer.parseInt(in_id);
            description = in_description;
        }
    }
}

