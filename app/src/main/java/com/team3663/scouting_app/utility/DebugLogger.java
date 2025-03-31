package com.team3663.scouting_app.utility;

import android.content.Context;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.team3663.scouting_app.config.Globals;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

// =============================================================================================
// Class:       Debug
// Description: Allows debugging information to be written to disk
// =============================================================================================
public class DebugLogger {
    private static int Indent = 0;
    private static DocumentFile file_df = null;
    private final Context appContext;
    public ArrayList<String> Params = new ArrayList<>();
    public DebugLogger(Context in_context) {
        appContext = in_context;
        DocumentFile df_logfiledir = Globals.output_df.getParentFile();
        String filename = "applog.txt";

        assert df_logfiledir != null;
        if (df_logfiledir.findFile(filename) != null) Objects.requireNonNull(df_logfiledir.findFile(filename)).delete();

        file_df = df_logfiledir.createFile("text/plain", filename);

        assert file_df != null;
        if (!file_df.canWrite()) Toast.makeText(appContext, "File not writeable: " + file_df.getName(), Toast.LENGTH_LONG).show();
    }

    public void In(String in_FunctionName) {
        String padding = "|  ";
        String outline;

        if (Params.size() > 0)
            in_FunctionName = in_FunctionName + ": (";

        OutputStream fos_logdata;

        try {
            fos_logdata = appContext.getContentResolver().openOutputStream(file_df.getUri(), "wa");
        } catch (Exception e) {
            Toast.makeText(appContext, "Failed to create output stream: " + file_df.getName() + " (ERROR: " + e.getMessage() + ")", Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }

        try {
            // Write out the data
            assert fos_logdata != null;
            outline = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS   "));
            outline += padding.repeat(Indent);
            outline += in_FunctionName;

            for (String param : Params) {
                if (param.equals(Params.get(0)))
                    outline += param;
                else
                    outline += ", " + param;
            }

            if (!Params.isEmpty())
                outline += ")";

            outline += System.lineSeparator();

            fos_logdata.write(outline.getBytes(StandardCharsets.UTF_8));

            fos_logdata.flush();
            fos_logdata.close();
            System.gc();
        } catch (IOException e) {
            Toast.makeText(appContext, "Failed to close out data log file. (ERROR: " + e.getMessage() + ")", Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }

        Params.clear();
        Indent++;
    }

    public void Out() {
        Indent--;
    }
}
