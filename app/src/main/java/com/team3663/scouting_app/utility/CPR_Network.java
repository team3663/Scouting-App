package com.team3663.scouting_app.utility;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.team3663.scouting_app.R;
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
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// =============================================================================================
// Class:       CPR_Network
// Description: Custom class to handle network related tasks.
// =============================================================================================
public class CPR_Network {
    public static boolean filesDownloaded = false;

    // =============================================================================================
    // Class Globals
    // =============================================================================================
    private final Context appContext;
    private final ExecutorService executor;
    private final Handler mainHandler;
    // drive.file is a non-sensitive scope (no OAuth verification needed). It grants access only to
    // files this app creates, which is all we do here: create a file in the shared folder (any
    // signed-in account can write to it via the folder's "anyone with the link can edit" grant).
    // Note: this scope does NOT allow listing/reading other files in that folder.
    public static final String GOOGLE_DRIVE_SCOPE = DriveScopes.DRIVE;
    private static final Scope DRIVE_SCOPE = new Scope(GOOGLE_DRIVE_SCOPE);
    private static final String LOG_TAG = "CPR_Network";
    // completeSignIn() returns this when the Drive service was built successfully.
    public static final int SIGN_IN_OK = 0;
    // completeSignIn() returns this when sign-in produced no usable account (no ApiException status).
    public static final int SIGN_IN_NO_ACCOUNT = -1;
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
        LOCAL_DIR_DNE,          // no local directory exists
        SQL_EXCEPTION           // SQL error occurred
    }

    public interface Callback {
        void onResult(@NonNull Result result);
    }

    // =============================================================================================
    // Describes a newer app version (APK) found in the Google Drive download folder.
    // version is a {major, minor, patch} triple parsed from the file name.
    // =============================================================================================
    public static class AppUpdateInfo {
        public final String fileId;
        public final String fileName;
        public final int[] version;

        public AppUpdateInfo(@NonNull String in_fileId, @NonNull String in_fileName, @NonNull int[] in_version) {
            this.fileId = in_fileId;
            this.fileName = in_fileName;
            this.version = in_version;
        }
    }

    // Result of an asynchronous app-update check (callback is invoked on the main thread).
    public interface UpdateCheckCallback {
        void onUpdateAvailable(@NonNull AppUpdateInfo in_info);
        void onNoUpdate();
    }

    // Result of an asynchronous APK download (callback is invoked on the main thread).
    // in_apkUri is the MediaStore content Uri of the downloaded APK, or null on failure.
    public interface ApkDownloadCallback {
        void onDownloaded(Uri in_apkUri);
    }

    // Constructor: create the new Network object
    public CPR_Network(@NonNull Context in_context) {
        // Use the application context to avoid leaking an Activity.
        this.appContext = in_context.getApplicationContext();
        this.executor = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());

        // Try to build the Drive service silently from an already-authorized account, so callers
        // that only need Drive access (and never an interactive sign-in) are ready immediately.
        ensureDriveService();
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
    // Function:    ensureVPN
    // Description: Checks if the dependent VPN client is installed and working.
    // Parameters:  void
    // Output:      void
    // =============================================================================================
    public void ensureVPN() {
        // Check if VPN client is installed
        PackageManager pm = appContext.getPackageManager();
        try {
            pm.getPackageInfo(Constants.AppLaunch.VPN_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(appContext, R.string.applaunch_vpn_not_installed, Toast.LENGTH_LONG).show();
            return;
        }

        // Check if VPN client is running by checking the VPN tunnel is actually up
        ConnectivityManager cm = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Toast.makeText(appContext, R.string.applaunch_vpn_not_checked, Toast.LENGTH_LONG).show();
            return;
        }

        // Check every network, not just the active one
        boolean vpn_working = false;
        for (Network network : cm.getAllNetworks()) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            if (caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                vpn_working = true;
                break;
            }
        }

        // Launch if VPN is not working AND we have internet (otherwise it's moot)
        if (!vpn_working && Globals.network.hasActiveInternet()) {
            Intent launchIntent = pm.getLaunchIntentForPackage(Constants.AppLaunch.VPN_PACKAGE_NAME);
            if (launchIntent != null) {
                appContext.startActivity(launchIntent);
            } else {
                Toast.makeText(appContext, R.string.applaunch_vpn_not_launched, Toast.LENGTH_LONG).show();
            }
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
    // Function:    ensureDriveService
    // Description: Make the Drive service usable WITHOUT any interactive sign-in.  Returns true if
    //              the service is already built, or if it can be built silently from an account that
    //              previously granted the Drive scope.  Returns false if an interactive sign-in is
    //              required (the caller should then launch getSignInIntent()).
    // Parameters:  void
    // Output:      boolean - true if the Drive service is ready to use
    // =============================================================================================
    public boolean ensureDriveService() {
        if (driveService != null) return true;

        GoogleSignInAccount last = GoogleSignIn.getLastSignedInAccount(appContext);
        if (GoogleSignIn.hasPermissions(last, DRIVE_SCOPE) && last.getAccount() != null) {
            initDriveService(last.getAccount());
            return true;
        }
        return false;
    }

    // =============================================================================================
    // Function:    getSignInIntent
    // Description: Build the Google sign-in / Drive-consent Intent.  The caller launches this through
    //              its own ActivityResultLauncher (the launcher must be registered on an Activity /
    //              Fragment, which is why it can't live here), then passes the result to
    //              completeSignIn().
    // Parameters:  void
    // Output:      Intent to launch
    // =============================================================================================
    @NonNull
    public Intent getSignInIntent() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(DRIVE_SCOPE)
                .build();
        return GoogleSignIn.getClient(appContext, gso).getSignInIntent();
    }

    // =============================================================================================
    // Function:    completeSignIn
    // Description: Process the result of the sign-in Intent launched from getSignInIntent() and build
    //              the Drive service.  The account picker can "succeed" while granting the Drive
    //              scope fails/was declined, so getResult() may still throw; when it does, we fall
    //              back to any account that actually ended up with the scope before giving up.
    // Parameters:  in_data  the Intent returned to the caller's ActivityResultLauncher
    // Output:      SIGN_IN_OK on success, SIGN_IN_NO_ACCOUNT if no usable account, otherwise the
    //              ApiException status code (e.g. 10 = DEVELOPER_ERROR, 12501 = cancelled)
    // =============================================================================================
    public int completeSignIn(Intent in_data) {
        try {
            GoogleSignInAccount account = GoogleSignIn
                    .getSignedInAccountFromIntent(in_data)
                    .getResult(ApiException.class);
            if (account != null && account.getAccount() != null) {
                initDriveService(account.getAccount());
                return SIGN_IN_OK;
            }
        } catch (ApiException e) {
            // Identity selection and scope consent are separate steps; if the scope was actually
            // granted, use the account anyway.  Otherwise report the real status code.
            if (ensureDriveService()) return SIGN_IN_OK;
            Log.w(LOG_TAG, "Google sign-in failed, status=" + e.getStatusCode() + " ("
                    + GoogleSignInStatusCodes.getStatusCodeString(e.getStatusCode()) + ")", e);
            return e.getStatusCode();
        }
        return SIGN_IN_NO_ACCOUNT;
    }

    // =============================================================================================
    // Function:    showToast
    // Description: Post a Toast to the main thread (safe to call from a background thread).
    // Parameters:  in_context   context used to build the Toast
    //              in_message   text to display
    //              in_duration  Toast.LENGTH_SHORT or Toast.LENGTH_LONG
    // Output:      void
    // =============================================================================================
    private void showToast(@NonNull Context in_context, @NonNull String in_message) {
        mainHandler.post(() -> Toast.makeText(in_context, in_message, Toast.LENGTH_LONG).show());
    }

    // =============================================================================================
    // Function:    uploadToGoogle
    // Description: Asynchronously copy the file to the shared Google Drive folder. initDriveService()
    //              must have been called first. Feedback is shown via Toast on the main thread.
    // Parameters:  in_callback invoked on the main thread with the result
    // Output:      void
    // =============================================================================================
    public void uploadToGoogle(@NonNull Callback in_callback) {
        // We must have a Drive service (built from a signed-in account) before we can upload
        if (driveService == null) {
            showToast(appContext, "Google Upload Failed: Not signed in to Google");
            in_callback.onResult(Result.TRANSMISSION_FAILURE);
            return;
        }

        final String filename = Globals.CurrentCompetitionId + "_" + Globals.TransmitMatchNum + "_" + Globals.CurrentDeviceId + "_" + Globals.TransmitMatchType + ".csv";
        DocumentFile df = Globals.output_df.findFile(filename);

        // validate the file exists
        if (df==null || !df.exists() || !df.isFile()) {
            showToast(appContext, "Google Upload Failed: File not found");
            in_callback.onResult(Result.NO_DATA);
            return;
        }

        final long localSize = df.length();
        final Uri sourceUri = df.getUri();

        executor.execute(() -> {
            try {
                // validate connectivity
                if (!hasActiveInternet()) {
                    showToast(appContext, "Google Upload Failed: No Internet Connection");
                    mainHandler.post(() -> in_callback.onResult(Result.NO_NETWORK));
                    return;
                }

                String mimeType = appContext.getContentResolver().getType(sourceUri);
                if (mimeType == null) {
                    showToast(appContext, "Google Upload Failed: File Type Not Found");
                    mainHandler.post(() -> in_callback.onResult(Result.TRANSMISSION_FAILURE));
                    return;
                }

                File fileMetadata = new File();
                fileMetadata.setName(filename);

                String folderId = Globals.sp.getString(Constants.Prefs.GOOGLE_DRIVE_UPLOAD, Constants.Settings.DEFAULT_GOOGLE_UPLOAD);
                if (folderId.isEmpty()) {
                    showToast(appContext, "Google Upload Failed: No folder ID configured");
                    mainHandler.post(() -> in_callback.onResult(Result.TRANSMISSION_FAILURE));
                    return;
                }
                fileMetadata.setParents(Collections.singletonList(folderId));

                InputStream inputStream = appContext.getContentResolver().openInputStream(sourceUri);
                if (inputStream == null) {
                    showToast(appContext, "Google Upload Failed: Unable to open file");
                    mainHandler.post(() -> in_callback.onResult(Result.TRANSMISSION_FAILURE));
                    return;
                }

                InputStreamContent fileContent = new InputStreamContent(mimeType, inputStream);
                if (localSize > 0) {
                    fileContent.setLength(localSize);
                }

                // setSupportsAllDrives(true) is required when the parent folder lives in a Shared
                // Drive (or was shared to us from another account).
                File uploadedFile = driveService.files()
                        .create(fileMetadata, fileContent)
                        .setSupportsAllDrives(true)
                        .setFields("id, name, size, trashed")
                        .execute();

                if (uploadedFile == null) {
                    showToast(appContext, "Google Upload Failed: Error transferring file");
                    mainHandler.post(() -> in_callback.onResult(Result.TRANSMISSION_FAILURE));
                    return;
                }

                // Validate upload
                File remoteFile = driveService.files()
                        .get(uploadedFile.getId())
                        .setSupportsAllDrives(true)
                        .setFields("id, size, trashed")
                        .execute();

                if (remoteFile == null || remoteFile.getId() == null || Boolean.TRUE.equals(remoteFile.getTrashed())) {
                    showToast(appContext, "Google Upload Failed: Unable to find remote file");
                    mainHandler.post(() -> in_callback.onResult(Result.TRANSMISSION_FAILURE));
                    return;
                }

                if (remoteFile.getSize() == null || remoteFile.getSize() != localSize) {
                    showToast(appContext, "Google Upload Failed: File size mismatch");
                    mainHandler.post(() -> in_callback.onResult(Result.TRANSMISSION_FAILURE));
                    return;
                }

                showToast(appContext, "Google Upload Successful");
                mainHandler.post(() -> in_callback.onResult(Result.TRANSMISSION_SUCCESS));
            }
            catch (Exception e) {
                showToast(appContext, "Google Upload Failed: Exception occurred");
                mainHandler.post(() -> in_callback.onResult(Result.TRANSMISSION_FAILURE));
            }
        });
    }

    // =============================================================================================
    // Function:    downloadFromGoogle
    // Description: Asynchronously copy the files from the shared Google Drive folder. initDriveService()
    //              must have been called first. Feedback is shown via Toast on the main thread.
    //              For each remote file:
    //                  - if no local copy exists, download it
    //                  - if a local copy exists and the MD5 checksum matches, skip it
    //                  - if a local copy exists but the MD5 checksum differs, re-download (overwrite)
    // Parameters:  in_callback invoked on the main thread with the result
    // Output:      void
    // =============================================================================================
    public void downloadFromGoogle(@NonNull Callback in_callback) {
        // validate connectivity
        if (!hasActiveInternet()) {
            showToast(appContext, "Google Download Failed: No Internet Connection");
            mainHandler.post(() -> in_callback.onResult(Result.NO_NETWORK));
            return;
        }

        // We must have a Drive service (built from a signed-in account) before we can upload
        if (driveService == null) {
            showToast(appContext, "Google Download Failed: Not signed in to Google");
            in_callback.onResult(Result.TRANSMISSION_FAILURE);
            return;
        }

        // Ensure the folder exists
        if (!Globals.input_df.exists()) {
            showToast(appContext, "Google Download Failed: Local directory not found");
            in_callback.onResult(Result.LOCAL_DIR_DNE);
            return;
        }

        executor.execute(() -> {
            try {
                int files_downloaded = 0;
                for (File remote : listGoogleFiles()) {
                    // silently ignore any google native docs (they don't have a checksum)
                    if (remote.getMd5Checksum() == null) continue;

                    String name = remote.getName();
                    DocumentFile local = Globals.input_df.findFile(name);

                    // if the local file exists but isn't a file, abort
                    if (local != null && !local.isFile()) {
                        showToast(appContext, "Google Download Failed: File not found");
                        mainHandler.post(() -> in_callback.onResult(Result.NO_DATA));
                        return;
                    }

                    // if the local file doesn't exist, download it.
                    if (local == null || !local.exists()) {
                        if (downloadOneFileFromGoogle(remote.getId(), name)) files_downloaded++;
                        else {
                            showToast(appContext, "Google Download Failed: Error transmitting file " + name);
                            mainHandler.post(() -> in_callback.onResult(Result.TRANSMISSION_SUCCESS));
                        }
                        continue;
                    }

                    String remoteMD5 = remote.getMd5Checksum();
                    String localMD5 = getFileMD5(local);

                    // if the checksums differ, download it again
                    if (!remoteMD5.equalsIgnoreCase(localMD5)) {
                        if (downloadOneFileFromGoogle(remote.getId(), name)) files_downloaded++;
                        else {
                            showToast(appContext, "Google Download Failed: Error transmitting file " + local.getName());
                            mainHandler.post(() -> in_callback.onResult(Result.TRANSMISSION_SUCCESS));
                        }
                    }
                }

                if (files_downloaded > 0) {
                    showToast(appContext, "Google Download Successful: " + files_downloaded + " files");
                    CPR_Network.filesDownloaded = true;
                }
                else showToast(appContext, "Google Download: No new files found");

                mainHandler.post(() -> in_callback.onResult(Result.TRANSMISSION_SUCCESS));
            }
            catch (Exception e) {
                if (!Objects.equals(e.getMessage(), "No files found")) showToast(appContext, "Google Download Failed: Exception occurred");
                mainHandler.post(() -> in_callback.onResult(Result.TRANSMISSION_FAILURE));
            }
        });
    }

    // =============================================================================================
    // Function:    downloadOneFileFromGoogle
    // Description: download a single file from Google
    // Parameters:  in_remoteFileId     file ID from Google Drive
    //              in_df               local documentFile
    // Output:      List of Files
    // =============================================================================================
    public boolean downloadOneFileFromGoogle(String in_remoteFileId, String in_localFileName) {
        DocumentFile tmp = Globals.input_df.findFile(in_localFileName + ".part");
        DocumentFile dest = Globals.input_df.findFile(in_localFileName);

        // if the tmp file exists, delete it
        if (tmp != null && tmp.exists()) tmp.delete();

        // create the file
        tmp = Globals.input_df.createFile("text/csv", in_localFileName + ".part");

        if (tmp == null) return false;

        try (OutputStream out = appContext.getContentResolver().openOutputStream(tmp.getUri(), "wt")) {
            if (out == null) return false;

            driveService.files().get(in_remoteFileId).executeMediaAndDownloadTo(out);
            out.flush();
        } catch (IOException e) {
            tmp.delete();
            return false;
        }

        // if we fail to delete the local file, abort
        if (dest != null && dest.exists() && !dest.delete()) {
            tmp.delete();
            return false;
        }

        // if we fail to rename the tmp file, abort
        if (!tmp.renameTo(in_localFileName)) {
            tmp.delete();
            return false;
        }

        return true;
    }

    // =============================================================================================
    // Function:    listGoogleFiles
    // Description: Lists all non-trashed files directly inside a GoogleDrive folder
    // Parameters:  void
    // Output:      List of Files
    // =============================================================================================
    public List<File> listGoogleFiles() throws IOException {
        List<File> files = new ArrayList<>();
        String pageToken = null;

        String folderId = Globals.sp.getString(Constants.Prefs.GOOGLE_DRIVE_DOWNLOAD, Constants.Settings.DEFAULT_GOOGLE_DOWNLOAD);
        if (folderId.isEmpty()) {
            throw new IOException("Google folder ID is not configured");
        }

        String query = "'" + folderId + "' in parents and trashed = false "
                + "and mimeType != 'application/vnd.google-apps.folder'";
        do {
            FileList page = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, md5Checksum, mimeType, size)")
                    .setSupportsAllDrives(true)
                    .setIncludeItemsFromAllDrives(true)
                    .setPageSize(1000)
                    .setPageToken(pageToken)
                    .execute();

            if (page.getFiles() != null) {
                files.addAll(page.getFiles());
            }

            pageToken = page.getNextPageToken();
        } while (pageToken != null);

        if (files.isEmpty()) {
            showToast(appContext, "Google Download Failed: No files found");
            throw new IOException("No files found");
        }

        return files;
    }

    // =============================================================================================
    // Function:    getFileMD5
    // Description: Calculates the MD5 hash of a file. This matches the md5Checksum provided by
    //              Google Drive metadata.
    // Parameters:  in_df  DocumentFile to hash
    // Output:      String representing the hex MD5 hash, or null if it fails
    // =============================================================================================
    public String getFileMD5(DocumentFile in_df) {
        // validate the file exists
        if (in_df==null || !in_df.exists() || !in_df.isFile()) {
            showToast(appContext, "Local Checksum (MD5) Failed: File not found");
            return "";
        }

        try (InputStream is = appContext.getContentResolver().openInputStream(in_df.getUri())) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            if (is == null) return null;
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : md5sum) {
                hexString.append(Character.forDigit((b >> 4) & 0xF, 16));
                hexString.append(Character.forDigit(b & 0xF, 16));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            showToast(appContext, "Local Checksum (MD5) Failed: MD5 algorithm unavailable");
            return null;
        }
        catch (IOException e) {
            showToast(appContext, "Local Checksum (MD5) Failed: Unable to read file");
            return null;
        }
    }

    // =============================================================================================
    // Function:    checkForAppUpdate
    // Description: Asynchronously look in the Google Drive download folder for an APK newer than the
    //              currently installed version.  initDriveService() must have been called first.
    //              The callback is invoked on the main thread with either the newest newer APK found
    //              or a "no update" result (which also covers no internet / not signed in / errors).
    // Parameters:  in_currentVersion  installed version as {major, minor, patch}
    //              in_callback        invoked on the main thread with the result
    // Output:      void
    // =============================================================================================
    public void checkForAppUpdate(@NonNull int[] in_currentVersion, @NonNull UpdateCheckCallback in_callback) {
        executor.execute(() -> {
            AppUpdateInfo info = findNewerApkBlocking(in_currentVersion);
            mainHandler.post(() -> {
                if (info != null) in_callback.onUpdateAvailable(info);
                else in_callback.onNoUpdate();
            });
        });
    }

    // =============================================================================================
    // Function:    findNewerApkBlocking
    // Description: Synchronous scan of the Google Drive download folder for the newest APK whose
    //              version is strictly greater than the installed version.  Must NOT be called on
    //              the main thread.  Returns null when there is no newer APK, no internet, no Drive
    //              service, or an error occurs.
    // Parameters:  in_currentVersion  installed version as {major, minor, patch}
    // Output:      AppUpdateInfo (or null)
    // =============================================================================================
    private AppUpdateInfo findNewerApkBlocking(@NonNull int[] in_currentVersion) {
        if (!hasActiveInternet() || driveService == null) return null;

        // Matches "<prefix>-<major>.<minor>.<patch>.apk" (case-insensitive on the .apk extension)
        Pattern apk_pattern = Pattern.compile(
                Pattern.quote(Constants.AppLaunch.APK_NAME_PREFIX) + "-(\\d+)\\.(\\d+)\\.(\\d+)\\.apk",
                Pattern.CASE_INSENSITIVE);

        AppUpdateInfo best = null;
        try {
            for (File remote : listDriveFilesInDownloadFolder()) {
                String name = remote.getName();
                if (name == null) continue;

                Matcher m = apk_pattern.matcher(name);
                if (!m.matches()) continue;

                int[] version = {
                        Integer.parseInt(Objects.requireNonNull(m.group(1))),
                        Integer.parseInt(Objects.requireNonNull(m.group(2))),
                        Integer.parseInt(Objects.requireNonNull(m.group(3)))
                };

                // Only keep APKs strictly newer than what's installed, and keep the newest of those
                if (compareVersion(version, in_currentVersion) <= 0) continue;
                if (best == null || compareVersion(version, best.version) > 0) {
                    best = new AppUpdateInfo(remote.getId(), name, version);
                }
            }
        } catch (IOException e) {
            return null;
        }

        return best;
    }

    // =============================================================================================
    // Function:    listDriveFilesInDownloadFolder
    // Description: Lists all non-trashed, non-folder files directly inside the configured Google
    //              Drive download folder.  Unlike listGoogleFiles(), this does NOT toast or throw
    //              when the folder is empty - it just returns an empty list.
    // Parameters:  void
    // Output:      List of Files
    // =============================================================================================
    private List<File> listDriveFilesInDownloadFolder() throws IOException {
        List<File> files = new ArrayList<>();

        String folderId = Globals.sp.getString(Constants.Prefs.GOOGLE_DRIVE_DOWNLOAD, Constants.Settings.DEFAULT_GOOGLE_DOWNLOAD);
        if (folderId.isEmpty()) return files;

        String query = "'" + folderId + "' in parents and trashed = false "
                + "and mimeType != 'application/vnd.google-apps.folder'";
        String pageToken = null;
        do {
            FileList page = driveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setSupportsAllDrives(true)
                    .setIncludeItemsFromAllDrives(true)
                    .setPageSize(1000)
                    .setPageToken(pageToken)
                    .execute();

            if (page.getFiles() != null) {
                files.addAll(page.getFiles());
            }

            pageToken = page.getNextPageToken();
        } while (pageToken != null);

        return files;
    }

    // =============================================================================================
    // Function:    compareVersion
    // Description: Compares two {major, minor, patch} version triples.
    // Parameters:  in_a  first version
    //              in_b  second version
    // Output:      negative if a < b, zero if equal, positive if a > b
    // =============================================================================================
    private int compareVersion(@NonNull int[] in_a, @NonNull int[] in_b) {
        for (int i = 0; i < 3; i++) {
            if (in_a[i] != in_b[i]) return Integer.compare(in_a[i], in_b[i]);
        }
        return 0;
    }

    // =============================================================================================
    // Function:    downloadApkToDownloads
    // Description: Asynchronously download an APK from Google Drive into the device's public
    //              Downloads folder (via MediaStore).  initDriveService() must have been called
    //              first.  The callback is invoked on the main thread with the content Uri of the
    //              downloaded APK, or null on failure.
    // Parameters:  in_info      the APK to download
    //              in_callback  invoked on the main thread with the result
    // Output:      void
    // =============================================================================================
    public void downloadApkToDownloads(@NonNull AppUpdateInfo in_info, @NonNull ApkDownloadCallback in_callback) {
        executor.execute(() -> {
            Uri uri = downloadApkToDownloadsBlocking(in_info);
            mainHandler.post(() -> in_callback.onDownloaded(uri));
        });
    }

    // =============================================================================================
    // Function:    downloadApkToDownloadsBlocking
    // Description: Synchronous download of an APK from Google Drive into the public Downloads folder.
    //              Must NOT be called on the main thread.  Writes atomically using MediaStore's
    //              IS_PENDING flag and returns the content Uri on success (null on failure).
    // Parameters:  in_info  the APK to download
    // Output:      Uri (or null)
    // =============================================================================================
    private Uri downloadApkToDownloadsBlocking(@NonNull AppUpdateInfo in_info) {
        if (!hasActiveInternet() || driveService == null) return null;

        ContentResolver resolver = appContext.getContentResolver();
        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        // Best-effort removal of any copy we previously downloaded, so MediaStore doesn't pile up
        // "name (1).apk" duplicates.  We can only delete items this app owns; ignore failures.
        deleteExistingDownload(resolver, collection, in_info.fileName);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, in_info.fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, Constants.AppLaunch.APK_MIME_TYPE);
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        Uri itemUri = resolver.insert(collection, values);
        if (itemUri == null) return null;

        try (OutputStream out = resolver.openOutputStream(itemUri, "wt")) {
            if (out == null) {
                resolver.delete(itemUri, null, null);
                return null;
            }
            driveService.files().get(in_info.fileId).executeMediaAndDownloadTo(out);
            out.flush();
        } catch (Exception e) {
            resolver.delete(itemUri, null, null);
            return null;
        }

        // Publish the finished file so other apps (the package installer) can read it
        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        resolver.update(itemUri, values, null, null);

        return itemUri;
    }

    // =============================================================================================
    // Function:    deleteExistingDownload
    // Description: Best-effort delete of a previously downloaded file with the same display name in
    //              the Downloads collection.  Only items this app owns can be deleted; failures are
    //              ignored.
    // Parameters:  in_resolver    the content resolver
    //              in_collection  the Downloads collection Uri
    //              in_fileName    the display name to remove
    // Output:      void
    // =============================================================================================
    private void deleteExistingDownload(@NonNull ContentResolver in_resolver, @NonNull Uri in_collection, @NonNull String in_fileName) {
        String selection = MediaStore.Downloads.DISPLAY_NAME + " = ? AND "
                + MediaStore.Downloads.RELATIVE_PATH + " LIKE ?";
        String[] args = {in_fileName, Environment.DIRECTORY_DOWNLOADS + "%"};
        try {
            in_resolver.delete(in_collection, selection, args);
        } catch (Exception e) {
            // ignore - not fatal, MediaStore will simply create a uniquely named copy
        }
    }
}
