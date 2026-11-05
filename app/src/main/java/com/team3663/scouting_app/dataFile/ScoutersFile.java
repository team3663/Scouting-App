package com.team3663.scouting_app.dataFile;

import android.content.Context;

import com.team3663.scouting_app.R;

import java.util.ArrayList;

public class ScoutersFile extends _DataFile {
    private final ArrayList<ScouterRow> scouter_list;

    public ScoutersFile(Context in_context) {
        super(in_context, in_context.getString(R.string.file_scouters), in_context.getString(R.string.applaunch_loading_scouters));

        scouter_list = new ArrayList<>();
    }

    @Override
    protected void processLine(String[] in_line, String in_orig_line) {
        addScouterRow(in_line[0], in_line[1]);
    }

    @Override
    public void clearList() {
        scouter_list.clear();
    }

    // Member Function: Add a row of Scouter info into the list giving the data individually
    public void addScouterRow(String in_id, String in_Scouter_Name) {
        scouter_list.add(new ScouterRow(in_id, in_Scouter_Name));
    }

    // Member Function: Add a row of Scouter info into the list giving the data individually
    public void addScouterRow(int in_id, String in_Scouter_Name) {
        scouter_list.add(new ScouterRow(in_id, in_Scouter_Name));
    }

    // Member Function: return the size of the list
    public int size() {
        return scouter_list.size();
    }

    // Member Function: return the size of the list
    public boolean isScouterInList(String in_Scouter_Name) {
        for (ScouterRow sr : scouter_list) {
            if (sr.name.equals(in_Scouter_Name.toUpperCase())) return true;
        }
        return false;
    }

    // Member Function: Return a string list of all records
    public ArrayList<String> getScouterList() {
        ArrayList<String> ret = new ArrayList<>();

        for (int i = 0; i < scouter_list.size(); i++) {
            ret.add(scouter_list.get(i).name);
        }

        return ret;
    }

    // =============================================================================================
    // Class:       ScouterRow
    // Description: Defines a structure/class to hold the information for each Scouter
    // =============================================================================================
    protected static class ScouterRow {
        private final int id;
        private final String name;

        // Constructor with individual data
        public ScouterRow(String in_id, String in_Scouter_Name) {
            id = Integer.parseInt(in_id);
            name = in_Scouter_Name.toUpperCase();
        }

        // Constructor with individual data
        public ScouterRow(int in_id, String in_Scouter_Name) {
            id = in_id;
            name = in_Scouter_Name.toUpperCase();
        }
    }
}