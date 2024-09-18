package com.cpr3663.cpr_scouting_app.data;

import java.util.ArrayList;

// =============================================================================================
// Class:       TrapResults
// Description: Defines a structure/class to hold the information for all Trap Result
// Methods:     addTrapResultRow()
//                  add a row of Trap Result info
//              size()
//                  return the number of start positions we have
//              getTrapResultRow()
//                  return a TrapResultRow item for the given Trap Result id
//              getDescriptionList()
//                  return a String Array of all of the descriptions of the Trap Results
//              clear()
//                  empties out the list
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

    // Member Function: Get back a row of data for a given Trap Result entry
    public TrapResultRow getTrapResultRow(int in_index) {
        return trapResult_list.get(in_index);
    }

    // Member Function: Get back the Id for a given Trap Result entry (needed for logging)
    public int getTrapResultId(String in_description) {
        int ret = 0;

        // Loop through the Trap Result list to find a matching description and return the id
        for (TrapResultRow trr : trapResult_list) {
            if (trr.getDescription().equals(in_description)) {
                ret = trr.id;
                break;
            }
        }
        return ret;
    }

    // Member Function: Return a string list of all records
    // TODO See if we can make it return a ArrayList<String> for consistency
    public String[] getDescriptionList() {
        String[] descriptions = new String[trapResult_list.size()];

        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = trapResult_list.get(i).getDescription();
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
    // Methods:     getId()
    //                  returns the (int) Trap Result number for this row to use for logging
    //              getDescription()
    //                  returns the (String) description for this row
    // =============================================================================================
    public static class TrapResultRow {
        // Class Members
        private final int id;
        private final String description;

        // Constructor with individual data
        public TrapResultRow(String in_id, String in_description) {
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

