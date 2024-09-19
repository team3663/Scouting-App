package com.cpr3663.cpr_scouting_app.data;

import java.util.ArrayList;
import java.util.Collections;

// =============================================================================================
// Class:       Devices
// Description: Defines a structure/class to hold the information for all Devices
// Methods:     addDeviceInfoRow()
//                  add a row of device info
//              size()
//                  return the number of devices we have
//              getDeviceInfoRow()
//                  return a DeviceInfoRow item for the given device id
//              clear()
//                  empties out the list
// =============================================================================================
public class Devices {
    private final ArrayList<DeviceRow> device_list;

    // Constructor
    public Devices() {
        device_list = new ArrayList<DeviceRow>();
    }

    // Member Function: Add a row of device info into the list giving the data in a csv format
    public void addDeviceRow(String in_device_number, String in_team_number, String in_description) {
        device_list.add(new DeviceRow(in_device_number, in_team_number, in_description));
    }

    // Member Function: return the size of the list
    public int size() {
        return device_list.size();
    }

    // Member Function: Get back a row of data for a given id
    public DeviceRow getDeviceRow(int deviceNumber) {
        DeviceRow device = null;
        for (DeviceRow deviceRow : device_list) {
            if (deviceNumber == deviceRow.getId()) {
                device = deviceRow;
                break;
            }
        }
        return device;
    }

    // Member Function: return a list of Device descriptions
    public ArrayList<String> getDeviceList() {
        ArrayList<String> ret = new ArrayList<>();

        // If we have nothing to process, return nothing!
        if (device_list.isEmpty()) return ret;

        // Make an array list of Device Ids
        for (DeviceRow dr : device_list) {
            ret.add(dr.getDescription());
        }

        // Sort the list
        Collections.sort(ret);
        return ret;
    }

    // Member Function:
    public int getTeamNumberByDescription(String in_description) {
        int ret = 0;

        for (DeviceRow dr : device_list) {
            if (in_description.equals(dr.getDescription())) {
                ret = dr.getTeamNumber();
                break;
            }
        }

        return ret;
    }

    // Member Function: get the id that matches the description
    public int getDeviceId(String in_description) {
        int ret = 0;

        for (DeviceRow dr : device_list) {
            if (in_description.equals(dr.getDescription())) {
                ret = dr.getId();
                break;
            }
        }

        return ret;
    }

    // Member Function: get the description that matches the id
    public String getDeviceDescription(int in_id) {
        String ret = "";

        for (DeviceRow dr : device_list) {
            if (in_id == dr.getId()) {
                ret = dr.getDescription();
                break;
            }
        }

        return ret;
    }

    // Member Function: Empties out the list
    public void clear() {
        device_list.clear();
    }

    // =============================================================================================
    // Class:       DeviceRow
    // Description: Defines a structure/class to hold the information for each Device.
    // Methods:     getDeviceNumber()
    //                  returns the (int) device number for this row.
    //              getTeamNumber()
    //                  returns the (int) device number for this row.
    //              getDescription()
    //                  returns the (String) description for this row.
    // =============================================================================================
    public static class DeviceRow {
        // Class Members
        private final int id;
        private final int team_number;
        private final String description;

        // Constructor with individual data
        public DeviceRow(String in_device_number, String in_team_number, String in_description) {
            id = Integer.parseInt(in_device_number);
            team_number = Integer.parseInt(in_team_number);
            description = in_description;
        }

        // Getter
        public int getId() {
            return id;
        }

        // Getter
        public int getTeamNumber() {
            return team_number;
        }

        // Getter
        public String getDescription() {
            return description;
        }
    }
}
