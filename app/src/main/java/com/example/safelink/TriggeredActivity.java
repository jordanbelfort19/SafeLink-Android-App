package com.example.safelink;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class TriggeredActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triggered);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng myLocation = new LatLng(12.9716, 77.5946); // User
        LatLng policeLocation = new LatLng(12.9750, 77.5990); // Police

        // 1. Add User Marker (Standard Red Pin)
        mMap.addMarker(new MarkerOptions()
                .position(myLocation)
                .title("My Location"));

        // 2. Add Police Car Marker (USING THE CUSTOM XML ICON)
        mMap.addMarker(new MarkerOptions()
                .position(policeLocation)
                .title("Police Arriving")
                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_police_car))); // <--- USING HELPER HERE

        // 3. Draw Route Line
        List<LatLng> path = new ArrayList<>();
        path.add(myLocation);
        path.add(policeLocation);

        mMap.addPolyline(new PolylineOptions()
                .addAll(path)
                .width(10)
                .color(Color.BLUE)
                .geodesic(true));

        // 4. Move Camera to fit
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(myLocation);
        builder.include(policeLocation);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
    }

    // --- HELPER FUNCTION TO CONVERT XML TO BITMAP FOR MAPS ---
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) return null;

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}