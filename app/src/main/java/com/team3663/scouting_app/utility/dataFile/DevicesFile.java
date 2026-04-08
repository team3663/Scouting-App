package com.team3663.scouting_app.utility.dataFile;

import android.content.Context;

import com.team3663.scouting_app.R;

import java.util.ArrayList;
import java.util.Collections;

public class DevicesFile extends _DataFile {
    private final ArrayList<DeviceRow> device_list;

    public DevicesFile(Context in_context) {
        super(in_context, in_context.getString(R.string.file_devices), in_context.getString(R.string.applaunch_loading_devices), in_context.getString(R.string.applaunch_file_error_devices));

        device_list = new ArrayList<>();
    }

    @Override
    protected void processLine(String[] in_line, String in_orig_line) {
        addDeviceRow(in_line[0], in_line[1], in_line[5]);
    }

    @Override
    public void clearList() {
        device_list.clear();
    }

    // Member Function: Add a row of device info into the list giving the data in a csv format
    public void addDeviceRow(String in_device_number, String in_team_number, String in_description) {
        device_list.add(new DeviceRow(in_device_number, in_team_number, in_description));
    }

    // Member Function: return the size of the list
    public int size() {
        return device_list.size();
    }

    // Member Function: return a list of Device descriptions
    public ArrayList<String> getDeviceList() {
        ArrayList<String> ret = new ArrayList<>();

        // If we have nothing to process, return nothing!
        if (device_list.isEmpty()) return ret;

        // Make an array list of Device Ids
        for (DeviceRow dr : device_list) {
            ret.add(dr.description);
        }

        // Sort the list
        Collections.sort(ret);
        return ret;
    }

    // Member Function: get the team number of this device by the description
    public String getTeamNumberByDescription(String in_description) {
        String ret = "";

        for (DeviceRow dr : device_list) {
            if (in_description.equals(dr.description)) {
                ret = dr.team_number;
                break;
            }
        }

        return ret;
    }

    // Member Function: get the team number of this device by the description
    public String getTeamNumberByDeviceId(int in_Id) {
        String ret = "";

        for (DeviceRow dr : device_list) {
            if (in_Id == dr.id) {
                ret = dr.team_number;
                break;
            }
        }

        return ret;
    }

    // Member Function: get the id that matches the description
    public int getDeviceId(String in_description) {
        int ret = 0;

        for (DeviceRow dr : device_list) {
            if (in_description.equals(dr.description)) {
                ret = dr.id;
                break;
            }
        }

        return ret;
    }

    // Member Function: get the description that matches the id
    public String getDeviceDescription(int in_id) {
        String ret = "";

        for (DeviceRow dr : device_list) {
            if (in_id == dr.id) {
                ret = dr.description;
                break;
            }
        }

        return ret;
    }

    // =============================================================================================
    // Class:       DeviceRow
    // Description: Defines a structure/class to hold the information for each Device.
    // =============================================================================================
    protected static class DeviceRow {
        private final int id;
        private final String team_number;
        private final String description;

        // Constructor with individual data
        public DeviceRow(String in_device_number, String in_team_number, String in_description) {
            id = Integer.parseInt(in_device_number);
            team_number = in_team_number;
            description = in_description;
        }
    }
}