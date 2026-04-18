package com.team3663.scouting_app.utility.dataFile;

import android.content.Context;

import com.team3663.scouting_app.R;

import java.util.ArrayList;

public class CommentsFile extends _DataFile {
    private final ArrayList<CommentRow> comment_list;

    public CommentsFile(Context in_context) {
        super(in_context, in_context.getString(R.string.file_comments), in_context.getString(R.string.applaunch_loading_comments), in_context.getString(R.string.applaunch_file_error_comments));

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
    public ArrayList<String> getDescriptionList() {
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
}