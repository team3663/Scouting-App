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
public class Debug {
    private static int Indent = 0;
    private static DocumentFile file_df = null;
    private final Context appContext;
    public static ArrayList<String> Params;
    public Debug(Context in_context) {
        appContext = in_context;
        DocumentFile df_logfiledir = Globals.output_df.getParentFile();
        String filename = "applog.txt";

        assert df_logfiledir != null;
        if (df_logfiledir.findFile(filename) != null) Objects.requireNonNull(df_logfiledir.findFile(filename)).delete();

        file_df = df_logfiledir.createFile("text/csv", filename);

        assert file_df != null;
        if (!file_df.canWrite()) Toast.makeText(appContext, "File not writeable: " + file_df.getName(), Toast.LENGTH_LONG).show();
    }

    public void In(String in_FunctionName) throws IOException {
        String padding = " ";
        Indent += 2;
        in_FunctionName = in_FunctionName + ": ";
        OutputStream fos_logdata;

        try {
            fos_logdata = appContext.getContentResolver().openOutputStream(file_df.getUri());
        } catch (Exception e) {
            Toast.makeText(appContext, "Failed to create output stream: " + file_df.getName() + " (ERROR: " + e.getMessage() + ")", Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }

        try {
            // Write out the data
            assert fos_logdata != null;
            fos_logdata.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS ")).getBytes(StandardCharsets.UTF_8));
            fos_logdata.write(padding.repeat(Indent).getBytes(StandardCharsets.UTF_8));
            fos_logdata.write(in_FunctionName.getBytes(StandardCharsets.UTF_8));
            for (String param : Params) {
                param += ", ";
                fos_logdata.write(param.getBytes(StandardCharsets.UTF_8));
            }
            fos_logdata.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));

            fos_logdata.flush();
            fos_logdata.close();
            System.gc();
        } catch (IOException e) {
            Toast.makeText(appContext, "Failed to close out data log file. (ERROR: " + e.getMessage() + ")", Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }

        Params.clear();
    }

    public void Out() {
        Indent -= 2;
    }
}
