package com.team3663.scouting_app.data;

import android.graphics.Color;

import java.util.ArrayList;

// =============================================================================================
// Class:       Colors
// Description: Defines a structure/class to hold the information for all Colors
// Methods:     addColorRow()
// =============================================================================================
public class Colors {
    private final ArrayList<ColorRow> color_list;

    // Constructor
    public Colors() {
        color_list = new ArrayList<>();
    }

    // Member Function: Add a row of Color info into the list giving the data individually
    public void addColorRow(String in_id, String in_description, String in_Color_CSV) {
        color_list.add(new ColorRow(in_id, in_description, in_Color_CSV));
    }

    // Member Function: return the size of the list
    public int size() {
        return color_list.size();
    }

    // Member Function: check if the color index is valid
    public boolean isColorValid(int in_index) {
        return (in_index < color_list.size()) && (in_index >= 0);
    }

    // Member Function: Get back the color for a score
    public int getColor(int in_ColorId, int in_ColorIndex) {
        if ((this.isColorValid(in_ColorId)) && (in_ColorIndex >= 0) && (in_ColorIndex < color_list.get(in_ColorId).color.size()))
            return color_list.get(in_ColorId).color.get(in_ColorIndex);
        else return 0;
    }

    // Member Function: Get back the Id for a given Color entry (needed for logging)
    public String getColorDescription(int in_id) {
        String ret = "";

        // Loop through the Color list to find a matching id and return the description
        for (ColorRow cr : color_list) {
            if (cr.id == in_id) {
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
            if (cr.description.equals(in_description)) {
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
    // =============================================================================================
    private static class ColorRow {
        private final int id;
        private final String description;
        private final ArrayList<Integer> color = new ArrayList<>();

        // Constructor with individual data
        public ColorRow(String in_id, String in_description, String in_colors_csv) {
            id = Integer.parseInt(in_id);
            description = in_description;
            String[] info = in_colors_csv.split(",", 0);

            for (String s : info) {
                color.add(Color.parseColor(s));
            }
        }
    }
}

