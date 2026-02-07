package com.team3663.scouting_app.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.team3663.scouting_app.R;
import com.team3663.scouting_app.config.Constants;
import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.BluetoothBinding;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.security.Permission;
import java.security.Permissions;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final UUID OBEX_UUID = UUID.fromString("00001105-0000-1000-8000-00805f9b34fb");
    private static final int PERMISSION_REQUEST_CODE = 1;

    private BluetoothAdapter bluetoothAdapter;
    private ListView deviceListView;
    private Button scanButton;
    private Button selectFileButton;
    private TextView statusTextView;
    private ProgressBar progressBar;

    private ArrayList<BluetoothDevice> deviceList = new ArrayList<>();
    private ArrayAdapter<String> deviceAdapter;
    private Uri selectedFileUri;
    private BluetoothBinding bluetoothBinding;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle in_savedInstanceState) {
        super.onCreate(in_savedInstanceState);
        EdgeToEdge.enable(this);
        bluetoothBinding = BluetoothBinding.inflate(getLayoutInflater());
        View page_root_view = bluetoothBinding.getRoot();
        setContentView(page_root_view);
        ViewCompat.setOnApplyWindowInsetsListener(bluetoothBinding.bluetooth, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        deviceListView = findViewById(R.id.deviceListView);
        scanButton = findViewById(R.id.scanButton);
        selectFileButton = findViewById(R.id.selectFileButton);
        statusTextView = findViewById(R.id.statusTextView);
        progressBar = findViewById(R.id.progressBar);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Initialize device adapter
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(deviceAdapter);

        // File picker launcher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedFileUri = result.getData().getData();
                        statusTextView.setText("File selected: " + getFileName(selectedFileUri));
                    }
                }
        );
l
        // Check permissions
        checkPermissions();

        // Scan for paired devices
        scanButton.setOnClickListener(v -> scanForDevices());

        // Select file button
        selectFileButton.setOnClickListener(v -> selectFile());

        // Device click listener
        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            if (selectedFileUri == null) {
                Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show();
                return;
            }
            BluetoothDevice device = deviceList.get(position);
            sendFile(device);
        });
    }
//

private void checkPermissions() {
    String[] permissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES
    };

    ArrayList<String> permissionsNeeded = new ArrayList<>();
    for (String permission : permissions) {
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(permission);
        }
    }

    if (!permissionsNeeded.isEmpty()) {
        ActivityCompat.requestPermissions(this,
                permissionsNeeded.toArray(new String[0]),
                PERMISSION_REQUEST_CODE);
    }
}

private void scanForDevices() {
    if (bluetoothAdapter == null) {
        Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        return;
    }

    if (!bluetoothAdapter.isEnabled()) {
        Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
        return;
    }

    deviceList.clear();
    deviceAdapter.clear();

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show();
        return;
    }

    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

    if (pairedDevices.size() > 0) {
        for (BluetoothDevice device : pairedDevices) {
            deviceList.add(device);
            deviceAdapter.add(device.getName() + "\n" + device.getAddress());
        }
        statusTextView.setText("Found " + pairedDevices.size() + " paired device(s)");
    } else {
        statusTextView.setText("No paired devices found");
    }
}

private void selectFile() {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("*/*");
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    filePickerLauncher.launch(intent);
}

private String getFileName(Uri uri) {
    String result = null;
    if (uri.getScheme().equals("content")) {
        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    result = cursor.getString(nameIndex);
                }
            }
        }
    }
    if (result == null) {
        result = uri.getPath();
        int cut = result.lastIndexOf('/');
        if (cut != -1) {
            result = result.substring(cut + 1);
        }
    }
    return result;
}

private void sendFile(BluetoothDevice device) {
    new Thread(() -> {
        BluetoothSocket socket = null;
        try {
            runOnUiThread(() -> {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                statusTextView.setText("Connecting to " + device.getName() + "...");
                progressBar.setProgress(0);
            });

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                runOnUiThread(() -> Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show());
                return;
            }

            socket = device.createRfcommSocketToServiceRecord(OBEX_UUID);
            socket.connect();

            runOnUiThread(() -> statusTextView.setText("Connected! Sending file..."));

            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = getContentResolver().openInputStream(selectedFileUri);

            byte[] buffer = new byte[1024];
            int bytes;
            long totalBytes = 0;
            long fileSize = inputStream.available();

            while ((bytes = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytes);
                totalBytes += bytes;

                final int progress = (int) ((totalBytes * 100) / fileSize);
                runOnUiThread(() -> progressBar.setProgress(progress));
            }

            outputStream.flush();
            inputStream.close();

            runOnUiThread(() -> {
                statusTextView.setText("File sent successfully!");
                Toast.makeText(this, "Transfer complete!", Toast.LENGTH_LONG).show();
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                statusTextView.setText("Transfer failed: " + e.getMessage());
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }).start();
}

}
//            final BroadcastReceiver receiver = new BroadcastReceiver() {
//                public void onReceive(Context context, Intent intent) {
//                    String action = intent.getAction();
//                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                        // Add the device to a list or connect to it
//                        Globals.deviceAddress= device.getAddress();
//                    }
//                }
//
//            };
//
//// Register the BroadcastReceiver
//            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//            registerReceiver(receiver, filter);
//
//
//            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(Globals.deviceAddress);
//            BluetoothSocket socket = null;
//            try {
//                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
//                socket.connect();
//            } catch (IOException e) {
//                e.printStackTrace();
//                // Handle the error
//            }
//
//            OutputStream outputStream = null;
//            try {
//                outputStream = socket.getOutputStream();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            try (FileInputStream fileInputStream = new FileInputStream(getFile(Globals.transmitMatchNum, "d"))) {
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while (true) {
//                    try {
//                        if (!((bytesRead = fileInputStream.read(buffer)) != -1)) break;
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                    try {
//                        outputStream.write(buffer, 0, bytesRead);
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//                try {
//                    fileInputStream.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            } catch (FileNotFoundException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            try {
//                outputStream.close();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//    private static File getFile(int in_Match_ID, String in_Extension) throws FileNotFoundException {
//        String filename = Globals.CurrentCompetitionId + "_" + in_Match_ID + "_" + Globals.CurrentDeviceId + "_" + in_Extension + ".csv";
//        File file = new File(filename);
////        FileInputStream fis = new FileInputStream(file);
//        return file;



