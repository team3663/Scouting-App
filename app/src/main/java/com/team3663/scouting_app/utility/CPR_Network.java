package com.team3663.scouting_app.utility;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// =============================================================================================
// Class:       CPR_Network
// Description: Custom class to handle network related tasks.
// =============================================================================================
public class CPR_Network {
    // =============================================================================================
    // Class Globals
    // =============================================================================================
    private final Context appContext;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private static final String GOOGLE_TAG = "DriveUploadHelper";

    // =============================================================================================
    // Why a reachability check ended the way it did.
    // =============================================================================================
    public enum Result {
        REACHABLE,              // socket connected successfully
        NO_NETWORK,             // device has no active internet-capable network
        HOST_UNREACHABLE,       // network exists, but host:port could not be reached
        TRANSMISSION_SUCCESS,   // data sent successfully
        TRANSMISSION_FAILURE,   // data send failed
        NO_DATA,                // no data to send
        SQL_EXCEPTION           // SQL error occurred
    }

    public interface Callback {
        void onResult(@NonNull Result result);
    }

    // Constructor: create the new Network object
    public CPR_Network(@NonNull Context in_context) {
        // Use the application context to avoid leaking an Activity.
        this.appContext = in_context.getApplicationContext();
        this.executor = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // =============================================================================================
    // Function:    check (step 1)
    // Description: Asynchronously checks reachability. The callback is invoked on the main thread.
    // Parameters:  in_host      hostname or IP of the remote machine
    //              in_port      port the remote service listens on (e.g. 8080, 443, 22)
    //              in_timeoutMs socket connect timeout in milliseconds
    //              in_callback  invoked on the main thread with the result
    // Output:      void
    // =============================================================================================
    public void check(@NonNull String in_host, int in_port, int in_timeoutMs, @NonNull Callback in_callback) {
        executor.execute(() -> {
            Result result = checkBlocking(in_host, in_port, in_timeoutMs);
            mainHandler.post(() -> in_callback.onResult(result));
        });
    }

    // =============================================================================================
    // Function:    checkBlocking
    // Description: Synchronous check. Must NOT be called on the main thread (blocks on socket I/O).
    //              Exposed for use inside your own background threads / coroutines.
    // Parameters:  in_host      hostname or IP of the remote machine
    //              in_port      port the remote service listens on (e.g. 8080, 443, 22)
    //              in_timeoutMs socket connect timeout in milliseconds
    // Output:      void
    // =============================================================================================
    @NonNull
    public Result checkBlocking(@NonNull String in_host, int in_port, int in_timeoutMs) {
        if (!hasActiveInternet()) {
            return Result.NO_NETWORK;
        }
        return isHostReachable(in_host, in_port, in_timeoutMs)
                ? Result.REACHABLE
                : Result.HOST_UNREACHABLE;
    }

    // =============================================================================================
    // Function:    hasActiveInternet
    // Description: is there an active, internet-capable network?
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    public boolean hasActiveInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        Network network = cm.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    // =============================================================================================
    // Function:    isHostReachable (step 2)
    // Description: can we open a TCP connection to host:port?
    // Parameters:  in_host      hostname or IP of the remote machine
    //              in_port      port the remote service listens on (e.g. 8080, 443, 22)
    //              in_timeoutMs socket connect timeout in milliseconds
    // Output:      void
    // =============================================================================================
    private boolean isHostReachable(@NonNull String in_host, int in_port, int in_timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(in_host, in_port), in_timeoutMs);
            return true;
        } catch (IOException e) {
            // unreachable, connection refused, or timed out
            return false;
        }
    }

    // =============================================================================================
    // Function:    shutdown
    // Description: Call when you're done (e.g. in onDestroy) to release the thread pool.
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    public void shutdown() {
        executor.shutdownNow();
    }

    // =============================================================================================
    // Function:    pickWIFI
    // Description: Choose new Wi-Fi to connect to
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    public void pickWIFI() {
        try {
            // Try quick Wi-Fi panel
            Intent GoToSystemWIFI = new Intent(Settings.Panel.ACTION_WIFI);
            GoToSystemWIFI.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            appContext.startActivity(GoToSystemWIFI);
        } catch (Exception e) {
            // Fallback to full Wi-Fi settings
            Intent GoToSystemWIFI = new Intent(Settings.ACTION_WIFI_SETTINGS);
            GoToSystemWIFI.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            appContext.startActivity(GoToSystemWIFI);
        }
    }

    // =============================================================================================
    // Function:    sendFileToSQLServer
    // Description: Asynchronously sends a scouting file to the SQL Server. Callback is on main thread.
    // Parameters:  in_callback  invoked on the main thread with the result
    // Output:      void
    // =============================================================================================
    public void sendFileToSQLServer(@NonNull Callback in_callback) {
        executor.execute(() -> {
            Result result = sendFileToSQLServerBlocking();
            mainHandler.post(() -> in_callback.onResult(result));
        });
    }

    // =============================================================================================
    // Function:    sendFileToSQLServerBlocking
    // Description: Synchronous send. Must NOT be called on the main thread.
    // Parameters:  void
    // Output:      Result
    // =============================================================================================
    @NonNull
    public Result sendFileToSQLServerBlocking() {
        if (!hasActiveInternet()) {
            return Result.NO_NETWORK;
        }

        String sql_server = Globals.sp.getString(Constants.Prefs.SQL_SERVER, "");
        String sql_database = Globals.sp.getString(Constants.Prefs.SQL_DATABASE, "");
        String sql_user = Globals.sp.getString(Constants.Prefs.SQL_USER, "");
        String sql_password = Globals.sp.getString(Constants.Prefs.SQL_PASSWORD, "");

        // Before proceeding, make sure we have settings and a valid connection to the SQL Server
        if (sql_server.isEmpty() || sql_database.isEmpty() || sql_user.isEmpty() || sql_password.isEmpty()) {
            return Result.TRANSMISSION_FAILURE;
        }

        if (!isHostReachable(sql_server, 1433, 3000)) {
            return Result.HOST_UNREACHABLE;
        }

        // String url = "jdbc:sqlserver://" + sql_server + ";database=" + sql_database + ";encrypt=true;trustServerCertificate=true;useBulkCopyForBatchInsert=true;bulkCopyForBatchInsertFireTriggers=true";
        String url = "jdbc:jtds:sqlserver://" + sql_server + ":1433/" + sql_database + ";loginTimeout=10;socketTimeout=30";
        String sql = "INSERT INTO Load.Scouting_File(Line) VALUES(?)";
        HashMap<Integer, String> line_values = getFileAsStringHashMap();

        // Before proceeding, make sure we have data to send
        if (line_values.isEmpty()) {
            return Result.NO_DATA;
        }

        // On Android the driver must be registered explicitly; auto-discovery is unreliable
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            return Result.SQL_EXCEPTION;
        }

        try (Connection conn = DriverManager.getConnection(url, sql_user, sql_password)) {
            conn.setAutoCommit(false);
            // Insert the data, line by line
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                for (String line : line_values.values()) {
                    ps.setString(1, line);
                    ps.addBatch();
                }

                ps.executeBatch();
                conn.commit();
                return Result.TRANSMISSION_SUCCESS;
            } catch (SQLException e) {
                // Rollback the transaction on error
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // ignore
                }
                return Result.SQL_EXCEPTION;
            }
        } catch (SQLException e) {
            return Result.SQL_EXCEPTION;
        }
    }

    // =============================================================================================
    // Function:    sendFileToSQLServerBlocking
    // Description: Synchronous send. Must NOT be called on the main thread.
    // Parameters:  void
    // Output:      Result
    // =============================================================================================
    @NonNull
    public Result sendFileToSQLServerBlockingJDBC() {
        if (!hasActiveInternet()) {
            return Result.NO_NETWORK;
        }

        String sql_server = Globals.sp.getString(Constants.Prefs.SQL_SERVER, "");
        String sql_database = Globals.sp.getString(Constants.Prefs.SQL_DATABASE, "");
        String sql_user = Globals.sp.getString(Constants.Prefs.SQL_USER, "");
        String sql_password = Globals.sp.getString(Constants.Prefs.SQL_PASSWORD, "");

        // Before proceeding, make sure we have settings and a valid connection to the SQL Server
        if (sql_server.isEmpty() || sql_database.isEmpty() || sql_user.isEmpty() || sql_password.isEmpty()) {
            return Result.TRANSMISSION_FAILURE;
        }

        if (!isHostReachable(sql_server, 1433, 3000)) {
            return Result.HOST_UNREACHABLE;
        }

        String url = "jdbc:sqlserver://" + sql_server + ";database=" + sql_database + ";encrypt=true;trustServerCertificate=true;useBulkCopyForBatchInsert=true;bulkCopyForBatchInsertFireTriggers=true";
        String sql = "INSERT INTO Load.Scouting_File(Line) VALUES(?)";
        HashMap<Integer, String> line_values = new HashMap<>();
        line_values = getFileAsStringHashMap();

        // Before proceeding, make sure we have data to send
        if (line_values.isEmpty()) {
            return Result.NO_DATA;
        }

        try (Connection conn = DriverManager.getConnection(url, sql_user, sql_password)) {
            conn.setAutoCommit(false);
            // Insert the data, line by line
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                for (String line : line_values.values()) {
                    ps.setString(1, line);
                    ps.addBatch();
                }

                ps.executeBatch();
                conn.commit();
                return Result.TRANSMISSION_SUCCESS;
            } catch (SQLException e) {
                // Rollback the transaction on error
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // ignore
                }
                return Result.SQL_EXCEPTION;
            }
        } catch (SQLException e) {
            return Result.SQL_EXCEPTION;
        }
    }

    // =============================================================================================
    // Function:    getFileAsStringHashMap
    // Description: Initialize the Next Match button
    // Parameters:  void
    // Output:      String representing the entire contents of the file
    // =============================================================================================
    public HashMap<Integer, String> getFileAsStringHashMap() {
        String filename = Globals.CurrentCompetitionId + "_" + Globals.TransmitMatchNum + "_" + Globals.CurrentDeviceId + "_" + Globals.TransmitMatchType + ".csv";
        HashMap<Integer, String> file_as_hashmap = new HashMap<>();
        String line;
        int size = 0;

        try {
            // Open up the correct input stream
            InputStream is;
            DocumentFile df = Globals.output_df.findFile(filename);
            assert df != null;
            is = appContext.getContentResolver().openInputStream(df.getUri());

            // Read in the data
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                size++;
                file_as_hashmap.put(size, line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // If we made it this far, add the "F" record at the beginning
        file_as_hashmap.put(0, "F," + Globals.CurrentCompetitionId + "," + Globals.TransmitMatchNum + "," + Globals.CurrentDeviceId + "," + Globals.TransmitMatchType);

        return file_as_hashmap;
    }

    // =============================================================================================
    // Function:    uploadToGoogle
    // Description: Asynchronously copy the file to a Google Drive
    // Parameters:  in_host      hostname or IP of the remote machine
    //              in_port      port the remote service listens on (e.g. 8080, 443, 22)
    //              in_timeoutMs socket connect timeout in milliseconds
    //              in_callback  invoked on the main thread with the result
    // Output:      void
    // =============================================================================================
    public void uploadToGoogle(@NonNull Context in_context) {
        final String filename = Globals.CurrentCompetitionId + "_" + Globals.TransmitMatchNum + "_" + Globals.CurrentDeviceId + "_" + Globals.TransmitMatchType + ".csv";
        DocumentFile df = Globals.output_df.findFile(filename);

        // validate the file exists
        if (df==null || !df.exists() || !df.isFile()) {
            Toast.makeText(in_context, "Google Upload Failed: File not found", Toast.LENGTH_LONG).show();
            return;
        }

        final long localSize = df.length();
        final Uri sourceUri = df.getUri();

        executor.execute(() -> {
            try {
                // validate connectivity
                if (!hasActiveInternet()) {
                    Toast.makeText(in_context, "Google Upload Failed: No Internet Connection", Toast.LENGTH_LONG).show();
                    return;
                }

                String mimeType = in_context.getContentResolver().getType(sourceUri);
                if (mimeType == null) {
                    Toast.makeText(in_context, "Google Upload Failed: File Type Not Found", Toast.LENGTH_LONG).show();
                    return;
                }

                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName(filename);
                fileMetadata.setParents(Collections.singletonList(Globals.sp.getString(Constants.Prefs.GOOGLE_DRIVE, "")));

                InputStream inputStream = in_context.getContentResolver().openInputStream(sourceUri);
                if (inputStream == null) {
                    Toast.makeText(in_context, "Google Upload Failed: Unable to open file", Toast.LENGTH_LONG).show();
                    return;
                }

                InputStreamContent fileContent = new InputStreamContent(mimeType, inputStream);
                if (localSize > 0) {
                    fileContent.setLength(localSize);
                }

                com.google.api.services.drive.model.File uploadedFile = driveService.files()
                        .create(fileMetadata, fileContent)
                        .setFields("id, name, size, trashed")
                        .execute();

                if (uploadedFile == null) {
                    Toast.makeText(in_context, "Google Upload Failed: Error transferring file", Toast.LENGTH_LONG).show();
                    return;
                }

                // Validate upload
                com.google.api.services.drive.model.File remoteFile = driveService.files()
                        .get(uploadedFile.getId())
                        .setFields("id, size, trashed")
                        .execute();

                if (remoteFile == null || remoteFile.getId() == null || Boolean.TRUE.equals(remoteFile.getTrashed())) {
                    Toast.makeText(in_context, "Google Upload Failed: Unable to find remote file", Toast.LENGTH_LONG).show();
                    return;
                }

                if (remoteFile.getSize() == null || remoteFile.getSize() != localSize) {
                    Toast.makeText(in_context, "Google Upload Failed: File size mismatch", Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(in_context, "Google Upload Successful", Toast.LENGTH_LONG).show();
            }
            catch (Exception e) {
                Toast.makeText(in_context, "Google Upload Failed: Exception occurred", Toast.LENGTH_LONG).show();
            }
        });
    }
}
