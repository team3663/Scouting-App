package com.team3663.scouting_app.data;

import java.util.ArrayList;
import java.util.Objects;

// =============================================================================================
// Class:       Accuracy
// Description: Defines a structure/class to hold the information for all Accuracy values
// =============================================================================================

public class Accuracy {

    private final ArrayList<AccuracyRow> Accuracy_list;

    public Accuracy() {
        Accuracy_list = new ArrayList<>();

        // Add accuracy values into the list
        addAccuracyRow("-1","<Select One>");
        addAccuracyRow("12", "0 - 25%");
        addAccuracyRow("37", "25 - 50%");
        addAccuracyRow("62", "50 - 75%");
        addAccuracyRow("82", "75 - 90%");
        addAccuracyRow("95", "95%");
        addAccuracyRow("100", "100%");
    }

    // Add a row
    public void addAccuracyRow(String in_value, String in_description) {
        Accuracy_list.add(new AccuracyRow(in_value, in_description));
    }

    // Return the size
    public int size() {
        return Accuracy_list.size();
    }

    public ArrayList<String> getDescriptionList() {
        ArrayList<String> descriptions = new ArrayList<>();
        for (AccuracyRow ar : Accuracy_list) {
            descriptions.add(ar.description);
        }
        return descriptions;
    }

    public String getAccuracyValue(String in_description) {
        for (AccuracyRow ar : Accuracy_list) {
            if (Objects.equals(ar.description, in_description)) {
                return ar.value;
            }
        }
        return "";
    }

    public String getAccuracyDescription(String in_value) {
        for (AccuracyRow ar : Accuracy_list) {
            if (Objects.equals(ar.value, in_value)) {
                return ar.description;
            }
        }
        return "";
    }

    // Member Function: Empties out the list
    public void clear() {
        Accuracy_list.clear();
    }

    // =============================================================================================
    // Class:       AccuracyRow
    // Description: Defines a structure/class to hold the information for each match type
    // =============================================================================================
    private static class AccuracyRow {
        private final String value;
        private final String description;

        public AccuracyRow(String in_value, String in_description) {
            value = in_value;
            description = in_description;
        }
    }
}