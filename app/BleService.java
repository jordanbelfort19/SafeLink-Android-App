package com.example.safelink;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import java.util.UUID;

public class BleService extends Service { // <--- THIS IS THE FIX

    private final String TAG = "BleService";

    // UUIDs matching your ESP32 code
    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID CHAR_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    // REPLACE THIS WITH YOUR ESP32 MAC ADDRESS (Find it using a BLE Scanner app)
    private static final String ESP32_ADDRESS = "XX:XX:XX:XX:XX:XX";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start as Foreground Service so Android doesn't kill it
        startForeground(1, createNotification());
        connectToDevice();
        return START_STICKY;
    }

    private void connectToDevice() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not enabled");
            return;
        }

        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(ESP32_ADDRESS);
            Log.d(TAG, "Connecting to ESP32...");
            // AutoConnect = true (waits for device to come into range)
            bluetoothGatt = device.connectGatt(this, true, gattCallback);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid MAC Address: " + ESP32_ADDRESS);
        } catch (SecurityException e) {
            Log.e(TAG, "Permission missing for Bluetooth connect");
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to ESP32. Discovering services...");
                try {
                    gatt.discoverServices();
                } catch (SecurityException e) {
                    Log.e(TAG, "Permission missing for discovery");
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHAR_UUID);
                    if (characteristic != null) {
                        try {
                            gatt.setCharacteristicNotification(characteristic, true);
                        } catch (SecurityException e) {
                            Log.e(TAG, "Permission missing for notification setup");
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String value = characteristic.getStringValue(0);
            Log.d(TAG, "Received Signal: " + value);

            if ("SOS_TRIGGERED".equals(value)) {
                triggerEmergencyMode();
            }
        }
    };

    private void triggerEmergencyMode() {
        Log.e(TAG, "EMERGENCY DETECTED! Launching App...");

        // This launches your existing TriggeredActivity
        Intent intent = new Intent(this, TriggeredActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("MODE", "EMERGENCY");
        startActivity(intent);
    }

    private Notification createNotification() {
        String channelId = "BleChannel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "SOS Service", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Smart SOS Active")
                .setContentText("Listening for panic button...")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}