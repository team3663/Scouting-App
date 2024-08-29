package com.cpr3663.cpr_scouting_app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import android.content.Context;

public class Logger {
    private static File file_data;
    private static File file_event;
    private final String filename_data;
    private final String filename_event;
    private final FileOutputStream fos_data;
    private final FileOutputStream fos_event;
    private int seq_number=0;
    private int seq_number_prev=0;

    public Logger(Context in_context) {
        filename_data = Globals.CurrentCompetitionId + "_" + Globals.CurrentMatchNumber + "_" + Globals.CurrentDeviceId + "_d.csv";
        filename_event = Globals.CurrentCompetitionId + "_" + Globals.CurrentMatchNumber + "_" + Globals.CurrentDeviceId + "_d.csv";

        file_data = new File(in_context.getFilesDir(), filename_data);
        file_event = new File(in_context.getFilesDir(), filename_event);
        try {
            fos_data = new FileOutputStream(file_data);
            fos_event = new FileOutputStream(file_event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void LogEvent(int in_EventId, int in_X, int in_Y, boolean in_NewSequence){
        double time = ((int)Math.round(System.currentTimeMillis() / 10.0)) / 10.0;
        seq_number_prev = seq_number++;
        String prev="";
        String seq = Globals.CurrentCompetitionId + ":" + Globals.CurrentMatchNumber + ":" + Globals.CurrentDeviceId + ":" + seq_number;
        String csv_line;

        if (in_NewSequence) prev = String.valueOf(seq_number_prev);
        csv_line = seq + "," + in_EventId + "," + time + "," + in_X + "," + in_Y + "," + prev;
        try {
            fos_event.write(csv_line.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close(){
        try {
            fos_event.flush();
            fos_event.close();
            fos_data.flush();
            fos_data.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
