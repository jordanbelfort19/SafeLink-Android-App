package com.example.safelink;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2_nearby_help extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private Polyline currentPolyline; // To keep track of the line so we can remove it later
    private Button btnGoForHelp;

    // 1. Center Point (Simulating Your Location - Koramangala)
    private final LatLng myLocation = new LatLng(12.9352, 77.6245);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity2_nearby_help); // Ensure this matches your XML name

        btnGoForHelp = findViewById(R.id.btn_go_for_help);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.volunteer_map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // ENABLE MARKER CLICKS
        mMap.setOnMarkerClickListener(this);

        // 2. Add marker for YOU (Blue)
        Marker myMarker = mMap.addMarker(new MarkerOptions()
                .position(myLocation)
                .title("You (Volunteer)")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        // Tag this marker so we know it's "Me" and don't draw a line to myself
        myMarker.setTag("ME");

        // 3. List of Sample Threat Locations
        List<LatLng> threats = new ArrayList<>();
        threats.add(new LatLng(12.9279, 77.6271));
        threats.add(new LatLng(12.9450, 77.6350));
        threats.add(new LatLng(12.9300, 77.6100));
        threats.add(new LatLng(12.9400, 77.6200));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(myLocation);

        // 4. Loop through list and add Red Markers
        for (int i = 0; i < threats.size(); i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(threats.get(i))
                    .title("Threat #" + (i + 1))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            builder.include(threats.get(i));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 150));
    }

    /**
     * This function runs whenever ANY marker is clicked
     */
    @Override
    public boolean onMarkerClick(Marker marker) {

        // If user clicks on themselves, ignore it
        if ("ME".equals(marker.getTag())) {
            return false;
        }

        // 1. Remove the old line if it exists
        if (currentPolyline != null) {
            currentPolyline.remove();
        }

        // 2. Draw a new line from YOU to the SELECTED MARKER
        PolylineOptions lineOptions = new PolylineOptions()
                .add(myLocation)
                .add(marker.getPosition())
                .width(12)
                .color(Color.BLUE)
                .geodesic(true); // Makes it follow earth's curvature (straight line)

        currentPolyline = mMap.addPolyline(lineOptions);

        // 3. Update the Button Text
        btnGoForHelp.setText("GO TO: " + marker.getTitle());
        Toast.makeText(this, "Route selected for " + marker.getTitle(), Toast.LENGTH_SHORT).show();

        // 4. Return false so the default behavior (centering camera & showing info window) still happens
        return false;
    }
}