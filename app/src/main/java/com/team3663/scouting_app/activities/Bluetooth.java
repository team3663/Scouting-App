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

import com.team3663.scouting_app.config.Globals;
import com.team3663.scouting_app.databinding.BluetoothBinding;

import java.security.Permission;
import java.security.Permissions;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static UUID MY_UUID;
    private static final String enable = Manifest.permission.BLUETOOTH_CONNECT;
    private BluetoothBinding bluetoothBinding;


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
        InitNext();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothBinding.btnOn.setOnClickListener(view -> {
            if (bluetoothAdapter == null) {
                // Device doesn't support Bluetooth
                Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
            } else {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                        if (bluetoothAdapter.isDiscovering()) {
                            bluetoothAdapter.cancelDiscovery();
                            Toast.makeText(this, " adapter cancelled", Toast.LENGTH_LONG).show();
                        }
                        else{bluetoothAdapter.startDiscovery();
                            Toast.makeText(this, "Begin discovery", Toast.LENGTH_LONG).show();
                            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);

                        }
                    }
                }
            }


        });
    }
//

    private void InitNext() {
        bluetoothBinding.butNext.setOnClickListener(view -> {
            // Reset pre-Match settings for next time
            Globals.isStartingNote = true;
            Globals.isPractice = false;

            Intent GoToPreMatch = new Intent(Bluetooth.this, PreMatch.class);
            startActivity(GoToPreMatch);

            finish();
        });
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



