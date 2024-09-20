package com.cpr3663.cpr_scouting_app.data;

import java.util.ArrayList;

// =============================================================================================
// Class:       Comments
// Description: Defines a structure/class to hold the information for all Comments
// Methods:     addCommentRow()
//                  add a row of comment info
//              size()
//                  return the number of comments we have
//              getCommentRow()
//                  return a CommentInfoRow item for the given comment id
//              getCommentList()
//                  return an ArrayList<> of Comments
//              getCommentId()
//                  return the comment Id for a given Comment (for logging)
//              getDescriptionList()
//                  return a String Array of all of the descriptions of the Comments
//              clear()
//                  empties out the list
// =============================================================================================
public class Comments {
    private final ArrayList<CommentRow> comment_list;

    // Constructor
    public Comments() {
        comment_list = new ArrayList<CommentRow>();
    }

    // Member Function: Add a row of Comment info into the list giving the data individually
    public void addCommentRow(String in_id, String in_order, String in_description) {
        comment_list.add(new CommentRow(in_id, in_order, in_description));
    }

    // Member Function: return the size of the list
    public int size() {
        return comment_list.size();
    }

    // Member Function: Get back a row of data for a given id
    public CommentRow getCommentRow(int in_index) {
        return comment_list.get(in_index);
    }

    // Member Function: Return an ordered list of Comments
    public String[] getCommentList() {
        // If there are no comments, return a null
        if (comment_list.isEmpty()) return null;

        // Since we'll load only Active/Valid comments, we can assume all have an order.
        // Insert into the String array at the ordered location.
        String[] ret = new String[comment_list.size()];

        for (CommentRow cr : comment_list) {
            ret[cr.order - 1] = cr.description;
        }

        return ret;
    }

    // Member Function: Get back the Id for a given DNP entry (needed for logging)
    public int getCommentId(String in_description) {
        int ret = 0;

        // Loop through the DNP list to find a matching description and return the id
        for (CommentRow cr : comment_list) {
            if (cr.getDescription().equals(in_description)) {
                ret = cr.id;
                break;
            }
        }

        return ret;
    }

    // Member Function: Return a string list of all records
    // TODO See if we can make it return a ArrayList<String> for consistency
    public String[] getDescriptionList() {
        String[] descriptions = new String[comment_list.size()];

        for (int i = 0; i < descriptions.length; i++) {
            descriptions[i] = comment_list.get(i).getDescription();
        }
        return descriptions;
    }

    // Member Function: Empties out the list
    public void clear() {
        comment_list.clear();
    }

    // =============================================================================================
    // Class:       CommentRow
    // Description: Defines a structure/class to hold the information for each Comment
    // Methods:     getId()
    //                  returns the (int) comment number for this row to use for logging
    //              getOrder()
    //                  returns the (int) order for this comment
    //              getDescription()
    //                  returns the (String) description for this row
    // =============================================================================================
    public static class CommentRow {
        // Class Members
        private final int id;
        private final int order;
        private final String description;

        // Constructor with individual data
        public CommentRow(String in_id, String in_order, String in_description) {
            id = Integer.parseInt(in_id);
            order = Integer.parseInt(in_order);
            description = in_description;
        }

        // Getter
        public int getId() {
            return id;
        }

        // Getter
        public int getOrder() {
            return order;
        }

        // Getter
        public String getDescription() {
            return description;
        }
    }
}
