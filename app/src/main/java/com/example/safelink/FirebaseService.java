package com.example.safelink; // <--- CHECK THIS MATCHES YOUR PACKAGE

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseService extends Service {

    private DatabaseReference myRef;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 1. Create the Notification to keep the service running
        startForeground(1, createNotification());

        // 2. Start Listening to Firebase
        startListening();

        return START_STICKY; // Restart if Android kills it
    }

    private void startListening() {
        Log.d("FirebaseService", "Connecting to Database...");

        // Connect to the exact path where ESP8266 writes "ON"
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("sos_trigger");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Read the value (it might be null first time)
                String value = snapshot.getValue(String.class);
                Log.d("FirebaseService", "Database Value: " + value);

                if ("ON".equals(value)) {
                    launchSOS();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseService", "Database Error: " + error.getMessage());
            }
        });
    }

    private void launchSOS() {
        Log.e("FirebaseService", "SOS TRIGGERED! Launching App...");

        // Open the TriggeredActivity
        Intent intent = new Intent(this, TriggeredActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Crucial for starting from a Service
        startActivity(intent);
    }

    private Notification createNotification() {
        String channelId = "SOS_Channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Safety Service", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Safety System Active")
                .setContentText("Connected to Cloud...")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}