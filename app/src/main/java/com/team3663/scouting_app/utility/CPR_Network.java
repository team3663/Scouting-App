package com.team3663.scouting_app.utility;

import android.accounts.Account;
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

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

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
    // drive.file is a non-sensitive scope (no OAuth verification needed). It grants access only to
    // files this app creates, which is all we do here: create a file in the shared folder (any
    // signed-in account can write to it via the folder's "anyone with the link can edit" grant).
    // Note: this scope does NOT allow listing/reading other files in that folder.
    public static final String GOOGLE_DRIVE_SCOPE = DriveScopes.DRIVE_FILE;
    private Drive driveService;

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

        String url = "jdbc:sqlserver://" + sql_server + ";database=" + sql_database + ";encrypt=true;trustServerCertificate=true;useBulkCopyForBatchInsert=true;bulkCopyForBatchInsertFireTriggers=true";

        String sql = "INSERT INTO Load.Scouting_File(Line) VALUES(?)";
        HashMap<Integer, String> line_values = getFileAsStringHashMap();

        // Before proceeding, make sure we have data to send
        if (line_values.isEmpty()) {
            return Result.NO_DATA;
        }

        // On Android the driver must be registered explicitly; auto-discovery is unreliable
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
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
    // Function:    getFileAsStringHashMap
    // Description: Reads in the scouting file (defined by Globals) and convert it to a string hashmap
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
    // Function:    initDriveService
    // Description: Build the Google Drive service from a signed-in Google account. Must be called
    //              (with a Drive-scoped account) before uploadToGoogle(). Uploads run as this
    //              user, so any Drive folder shared with them (including one they don't own) is
    //              reachable by folder id.
    // Parameters:  in_account  the signed-in Google account with GOOGLE_DRIVE_SCOPE granted
    // Output:      void
    // =============================================================================================
    public void initDriveService(@NonNull Account in_account) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                appContext, Collections.singletonList(GOOGLE_DRIVE_SCOPE));
        credential.setSelectedAccount(in_account);

        driveService = new Drive.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("CPR Scouting App")
                .build();
    }

    // =============================================================================================
    // Function:    isDriveServiceReady
    // Description: Has a Drive service been built from a signed-in account yet?
    // Parameters:  void
    // Output:      boolean
    // =============================================================================================
    public boolean isDriveServiceReady() {
        return driveService != null;
    }

    // =============================================================================================
    // Function:    showToast
    // Description: Post a Toast to the main thread (safe to call from a background thread).
    // Parameters:  in_context   context used to build the Toast
    //              in_message   text to display
    //              in_duration  Toast.LENGTH_SHORT or Toast.LENGTH_LONG
    // Output:      void
    // =============================================================================================
    private void showToast(@NonNull Context in_context, @NonNull String in_message, int in_duration) {
        mainHandler.post(() -> Toast.makeText(in_context, in_message, in_duration).show());
    }

    // =============================================================================================
    // Function:    uploadToGoogle
    // Description: Asynchronously copy the file to the shared Google Drive folder. initDriveService()
    //              must have been called first. Feedback is shown via Toast on the main thread.
    // Parameters:  in_context  context used for content resolution and Toast feedback
    // Output:      void
    // =============================================================================================
    public boolean uploadToGoogle(@NonNull Context in_context) {
        // We must have a Drive service (built from a signed-in account) before we can upload
        if (driveService == null) {
            showToast(in_context, "Google Upload Failed: Not signed in to Google", Toast.LENGTH_LONG);
            return false;
        }

        final String filename = Globals.CurrentCompetitionId + "_" + Globals.TransmitMatchNum + "_" + Globals.CurrentDeviceId + "_" + Globals.TransmitMatchType + ".csv";
        DocumentFile df = Globals.output_df.findFile(filename);

        // validate the file exists
        if (df==null || !df.exists() || !df.isFile()) {
            showToast(in_context, "Google Upload Failed: File not found", Toast.LENGTH_LONG);
            return false;
        }

        final long localSize = df.length();
        final Uri sourceUri = df.getUri();

        executor.execute(() -> {
            try {
                // validate connectivity
                if (!hasActiveInternet()) {
                    showToast(in_context, "Google Upload Failed: No Internet Connection", Toast.LENGTH_LONG);
                    return;
                }

                String mimeType = in_context.getContentResolver().getType(sourceUri);
                if (mimeType == null) {
                    showToast(in_context, "Google Upload Failed: File Type Not Found", Toast.LENGTH_LONG);
                    return;
                }

                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName(filename);
                fileMetadata.setParents(Collections.singletonList(Globals.sp.getString(Constants.Prefs.GOOGLE_DRIVE_UPLOAD, "")));

                InputStream inputStream = in_context.getContentResolver().openInputStream(sourceUri);
                if (inputStream == null) {
                    showToast(in_context, "Google Upload Failed: Unable to open file", Toast.LENGTH_LONG);
                    return;
                }

                InputStreamContent fileContent = new InputStreamContent(mimeType, inputStream);
                if (localSize > 0) {
                    fileContent.setLength(localSize);
                }

                // setSupportsAllDrives(true) is required when the parent folder lives in a Shared
                // Drive (or was shared to us from another account).
                com.google.api.services.drive.model.File uploadedFile = driveService.files()
                        .create(fileMetadata, fileContent)
                        .setSupportsAllDrives(true)
                        .setFields("id, name, size, trashed")
                        .execute();

                if (uploadedFile == null) {
                    showToast(in_context, "Google Upload Failed: Error transferring file", Toast.LENGTH_LONG);
                    return;
                }

                // Validate upload
                com.google.api.services.drive.model.File remoteFile = driveService.files()
                        .get(uploadedFile.getId())
                        .setSupportsAllDrives(true)
                        .setFields("id, size, trashed")
                        .execute();

                if (remoteFile == null || remoteFile.getId() == null || Boolean.TRUE.equals(remoteFile.getTrashed())) {
                    showToast(in_context, "Google Upload Failed: Unable to find remote file", Toast.LENGTH_LONG);
                    return;
                }

                if (remoteFile.getSize() == null || remoteFile.getSize() != localSize) {
                    showToast(in_context, "Google Upload Failed: File size mismatch", Toast.LENGTH_LONG);
                    return;
                }

                showToast(in_context, "Google Upload Successful", Toast.LENGTH_LONG);
            }
            catch (Exception e) {
                showToast(in_context, "Google Upload Failed: Exception occurred", Toast.LENGTH_LONG);
            }
        });

        return true;
    }
}
