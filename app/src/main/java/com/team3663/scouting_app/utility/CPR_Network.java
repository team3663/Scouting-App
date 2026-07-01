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

/**
 * Checks whether a remote host:port is reachable.
 *
 * <p>Two-step check:
 * <ol>
 *   <li>Is there any active network with internet capability? (cheap, no I/O)</li>
 *   <li>Can we open a TCP socket to the given host:port? (blocking, runs off the UI thread)</li>
 * </ol>
 *
 * <p>Required manifest permissions:
 * <pre>
 *   &lt;uses-permission android:name="android.permission.INTERNET" /&gt;
 *   &lt;uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /&gt;
 * </pre>
 */public class CPR_Network {

    /**
     * Why a reachability check ended the way it did.
     */
    public enum Result {
        REACHABLE,          // socket connected successfully
        NO_NETWORK,         // device has no active internet-capable network
        HOST_UNREACHABLE    // network exists, but host:port could not be reached
    }

    /**
     * Delivered on the main thread.
     */
    public interface Callback {
        void onResult(@NonNull Result result);
    }

    private final Context appContext;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public CPR_Network(@NonNull Context context) {
        // Use the application context to avoid leaking an Activity.
        this.appContext = context.getApplicationContext();
        this.executor = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Asynchronously checks reachability. The callback is invoked on the main thread.
     *
     * @param host      hostname or IP of the remote machine
     * @param port      port the remote service listens on (e.g. 8080, 443, 22)
     * @param timeoutMs socket connect timeout in milliseconds
     * @param callback  invoked on the main thread with the result
     */
    public void check(@NonNull String host, int port, int timeoutMs, @NonNull Callback callback) {
        executor.execute(() -> {
            Result result = checkBlocking(host, port, timeoutMs);
            mainHandler.post(() -> callback.onResult(result));
        });
    }

    /**
     * Synchronous check. Must NOT be called on the main thread (blocks on socket I/O).
     * Exposed for use inside your own background threads / coroutines.
     */
    @NonNull
    public Result checkBlocking(@NonNull String host, int port, int timeoutMs) {
        if (!hasActiveInternet()) {
            return Result.NO_NETWORK;
        }
        return isHostReachable(host, port, timeoutMs)
                ? Result.REACHABLE
                : Result.HOST_UNREACHABLE;
    }

    /**
     * Step 1: is there an active, internet-capable network?
     */
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

    /**
     * Step 2: can we open a TCP connection to host:port?
     */
    private boolean isHostReachable(@NonNull String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException e) {
            // unreachable, connection refused, or timed out
            return false;
        }
    }

    /**
     * Call when you're done (e.g. in onDestroy) to release the thread pool.
     */
    public void shutdown() {
        executor.shutdownNow();
    }

    /**
     * Choose new wifi to connect to
     */
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
