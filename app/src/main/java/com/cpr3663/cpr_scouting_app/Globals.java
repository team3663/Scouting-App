package com.cpr3663.cpr_scouting_app;

import java.util.ArrayList;

public class Globals {
    public static ArrayList<String> TeamList = new ArrayList<String>();
    public static Competitions CompetitionList = new Competitions();
    public static Matches MatchList = new Matches();
    public static Devices DeviceList = new Devices();
    public static DNPs DNPList = new DNPs();
    public static Events EventList = new Events();
    public static Comments CommentList = new Comments();

    public static int CurrentCompetitionId;
    public static int CurrentMatchNumber;
    public static int CurrentTeamNumber;
    public static int CurrentDeviceId;
}
