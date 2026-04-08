package com.team3663.scouting_app.utility.dataFile;

import android.content.Context;

import com.team3663.scouting_app.R;

import java.util.HashMap;

public class TeamsFile extends _DataFile {
    private final HashMap<String, String> team_list;

    public TeamsFile(Context in_context) {
        super(in_context, in_context.getString(R.string.file_teams), in_context.getString(R.string.applaunch_loading_teams), in_context.getString(R.string.applaunch_file_error_teams));

        team_list = new HashMap<>();
    }

    @Override
    protected void processLine(String[] in_line, String in_orig_line) {
        team_list.put(in_line[0], in_line[1]);
    }

    @Override
    public void clearList() {
        team_list.clear();
    }

    public String getTeam(String in_id) {
        return team_list.getOrDefault(in_id, "");
    }
}