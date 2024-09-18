package com.cpr3663.cpr_scouting_app.data;

import android.graphics.Color;

import java.util.ArrayList;

// =============================================================================================
// Class:       Colors
// Description: Defines a structure/class to hold the information for all Colors
// Methods:     addColorRow()
//                  add a row of Color info
//              size()
//                  return the number of Colors we have
//              getColorRow()
//                  return a ColorRow item for the given Color id
//              getColorId()
//                  return the Id for a given description
//              getDescriptionList()
//                  return a String Array of all of the descriptions of the Colors
//              clear()
//                  empties out the list
// =============================================================================================
public class Colors {
    private final ArrayList<ColorRow> color_list;

    // Constructor
    public Colors() {
        color_list = new ArrayList<ColorRow>();
    }

    // Member Function: Add a row of Color info into the list giving the data individually
    public void addColorRow(String in_id, String in_description, String in_ColorScore, String in_ColorMiss) {
        color_list.add(new ColorRow(in_id, in_description, Color.parseColor(in_ColorScore), Color.parseColor(in_ColorMiss)));
    }

    // Member Function: return the size of the list
    public int size() {
        return color_list.size();
    }

    // Member Function: Get back a row of data for a given Color entry
    public ColorRow getColorRow(int in_index) {
        if ((in_index < color_list.size()) && (in_index >= 0)) return color_list.get(in_index);

        return null;
    }

    // Member Function: Get back the Id for a given Color entry (needed for logging)
    public String getColorDescription(int in_id) {
        String ret = "";

        // Loop through the Color list to find a matching id and return the description
        for (ColorRow cr : color_list) {
            if (cr.getId() == in_id) {
                ret = cr.description;
                break;
            }
        }

        return ret;
    }

    // Member Function: Get back the Id for a given Color entry (needed for logging)
    public int getColorId(String in_description) {
        int ret = 0;

        // Loop through the Color list to find a matching description and return the id
        for (ColorRow cr : color_list) {
            if (cr.getDescription().equals(in_description)) {
                ret = cr.id;
                break;
            }
        }
        return ret;
    }

    // Member Function: Return a string list of all records
    public ArrayList<String> getDescriptionList() {
        ArrayList<String> ret = new ArrayList<>();

        for (int i = 0; i < color_list.size(); i++) {
            ret.add(color_list.get(i).description);
        }

        return ret;
    }

    // Member Function: Empties out the list
    public void clear() {
        color_list.clear();
    }

    // =============================================================================================
    // Class:       ColorRow
    // Description: Defines a structure/class to hold the information for each Color
    // Methods:     getId()
    //                  returns the (int) Color number for this row to use for logging
    //              getDescription()
    //                  returns the (String) description for this row
    // =============================================================================================
    public static class ColorRow {
        // Class Members
        private final int id;
        private final String description;
        private final int color_score;
        private final int color_miss;

        // Constructor with individual data
        public ColorRow(String in_id, String in_description, int in_color_score, int in_color_miss) {
            id = Integer.parseInt(in_id);
            description = in_description;
            color_score = in_color_score;
            color_miss = in_color_miss;
        }

        // Getter
        public int getId() {
            return id;
        }

        // Getter
        public String getDescription() {
            return description;
        }

        // Getter
        public int getColorScore() {
            return color_score;
        }

        // Getter
        public int getColorMiss() {
            return color_miss;
        }
    }
}

