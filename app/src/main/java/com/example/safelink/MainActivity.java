package com.example.safelink;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private AnimatorSet pulseAnimatorSet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Intent serviceIntent = new Intent(this, FirebaseService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }


        startPulseAnimation();


        LinearLayout sosCard = findViewById(R.id.sos_btn);
        sosCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TriggeredActivity.class);
            startActivity(intent);
        });

        // --- NEARBY ALERTS CLICK LISTENER ---
        LinearLayout nearbyAlertsCard = findViewById(R.id.nearby_help);
        nearbyAlertsCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity2_nearby_help.class);
            startActivity(intent);
        });



        // --- LOCATION SHARE BUTTON CLICK LISTENER ---
        MaterialCardView cardLocation = findViewById(R.id.card_location);
        cardLocation.setOnClickListener(v -> {
            // TODO: Add your location sharing logic here
        });
    }


    private void startPulseAnimation() {
        View pulseRing = findViewById(R.id.pulse_ring);

        // Enable hardware acceleration for smoother animation
        pulseRing.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Scale X animation (horizontal expansion)
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(pulseRing, "scaleX", 1f, 1.2f, 1f);
        scaleX.setDuration(2000); // 2 seconds per pulse
        scaleX.setRepeatCount(ValueAnimator.INFINITE);

        // Scale Y animation (vertical expansion)
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(pulseRing, "scaleY", 1f, 1.2f, 1f);
        scaleY.setDuration(2000);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);

        // Alpha animation (fade out effect)
        ObjectAnimator alpha = ObjectAnimator.ofFloat(pulseRing, "alpha", 0.6f, 0f);
        alpha.setDuration(2000);
        alpha.setRepeatCount(ValueAnimator.INFINITE);

        // Combine all animations
        pulseAnimatorSet = new AnimatorSet();
        pulseAnimatorSet.playTogether(scaleX, scaleY, alpha);
        pulseAnimatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimatorSet.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop animation when app goes to background to save battery
        if (pulseAnimatorSet != null && pulseAnimatorSet.isRunning()) {
            pulseAnimatorSet.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restart animation when app comes back to foreground
        if (pulseAnimatorSet != null && !pulseAnimatorSet.isRunning()) {
            pulseAnimatorSet.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up animation resources
        if (pulseAnimatorSet != null) {
            pulseAnimatorSet.cancel();
            pulseAnimatorSet = null;
        }
    }
}