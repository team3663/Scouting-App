package com.team3663.scouting_app.utility.dataFile;

import android.content.Context;
<<<<<<< HEAD:app/src/main/java/com/team3663/scouting_app/utility/dataFile/ScoutersFile.java
import android.icu.text.IDNA;
import android.util.Log;
import android.widget.Toast;

import com.team3663.scouting_app.R;
=======
import android.os.Bundle;
import android.widget.AutoCompleteTextView;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.activities.PreMatch;
import com.team3663.scouting_app.utility.dataFile._DataFile;
>>>>>>> cdcb1f9bb088daac2aa2ae2cb8024670ea7452a2:app/src/main/java/input_data/ScoutersFile.java

import java.util.ArrayList;
import java.util.Arrays;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.view.View;
import android.widget.AdapterView;
import java.util.List;

public class ScoutersFile extends _DataFile {
    private static ArrayList<CommentRow> comment_list;

    public ScoutersFile(Context in_context) {
        super(in_context, in_context.getString(R.string.file_Scouters), in_context.getString(R.string.applaunch_loading_scouters), in_context.getString(R.string.applaunch_file_error_Scouters));

        comment_list = new ArrayList<>();

    }

    @Override
    protected void processLine(String[] in_line, String in_orig_line) {
        if (Boolean.parseBoolean(in_line[1]))
            addCommentRow(in_line[0], in_line[2]);
    }

    @Override
    public void clearList() {
        comment_list.clear();
    }

    // Member Function: Add a row of Comment info into the list giving the data individually
    public void addCommentRow(String in_id, String in_description) {
        comment_list.add(new CommentRow(in_id, in_description));
    }

    // Member Function: return the size of the list
    public int size() {
        return comment_list.size();
    }

    // Member Function: Get back the Id for a given DNP entry (needed for logging)
    public int getCommentId(String in_description) {
        int ret = 0;

        // Loop through the DNP list to find a matching description and return the id
        for (CommentRow cr : comment_list) {
            if (cr.description.equals(in_description)) {
                ret = cr.id;
                break;
            }
        }

        return ret;
    }

    // Member Function: Return a string list of all records
    public static ArrayList<String> getDescriptionList() {
        ArrayList<String> descriptions = new ArrayList<>();

        for (int i = 0; i < comment_list.size(); i++) {
            descriptions.add(comment_list.get(i).description);
        }
        return descriptions;
    }

    // =============================================================================================
    // Class:       CommentRow
    // Description: Defines a structure/class to hold the information for each Comment
    // =============================================================================================
    protected static class CommentRow {
        private final int id;
        private final String description;

        // Constructor with individual data
        public CommentRow(String in_id, String in_description) {
            id = Integer.parseInt(in_id);
            description = in_description;
        }
    }
    public static void autoComplete(AutoCompleteTextView autoView){
        setupScouterNameAutocomplete(autoView, getDescriptionList());
    }
    public static void setupScouterNameAutocomplete(AutoCompleteTextView autoCompleteTextView, ArrayList<String> suggestionsList) {
        // 1. Create an ArrayAdapter using a default Android layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                autoCompleteTextView.getContext(),
                android.R.layout.simple_dropdown_item_1line,
                suggestionsList
        );

        // 2. Set the adapter and the minimum character threshold to trigger suggestions
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1); // Displays dropdown after typing 1 character

        // 3. Set a listener to handle when a user clicks a suggestion
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected name from the adapter
                String selectedName = (String) parent.getItemAtPosition(position);

                // Output the selection back into the text view
                autoCompleteTextView.setText(selectedName);

                // Move cursor to the end of the text
                autoCompleteTextView.setSelection(selectedName.length());
            }
        });
    }
}