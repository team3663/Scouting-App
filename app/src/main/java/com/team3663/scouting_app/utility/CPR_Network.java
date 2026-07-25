package com.team3663.scouting_app.utility;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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

    // =============================================================================================
    // Why a reachability check ended the way it did.
    // =============================================================================================
    public enum Result {
        REACHABLE,          // socket connected successfully
        NO_NETWORK,         // device has no active internet-capable network
        HOST_UNREACHABLE    // network exists, but host:port could not be reached
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
    // Description: Choose new wifi to connect to
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
}
