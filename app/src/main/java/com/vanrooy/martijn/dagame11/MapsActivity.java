package com.vanrooy.martijn.DaGame_1.1

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    static final String STATE_SCORE = "playerScore";
    private GoogleMap mMap;
    private Circle circleLoc;
    private Circle circleTarget;
    private MyLocations locations = new MyLocations();
    private int r = 5;
    private static final String TAG = "Message";
    private LatLng CURRENT_TARGET;
    private LatLng TARGET_MAIN = new LatLng(50.864214, 4.679001);
    private LatLng TARGET_SEC = new LatLng(50.864176, 4.678812);
    private Marker markerTarget;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            int mCurrentScore = savedInstanceState.getInt(STATE_SCORE);
            TextView points_score = (TextView) findViewById(R.id.points_score);
            String points_str = (String)Integer.toString(mCurrentScore);
            points_score.setText(points_str);


        }

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);

        changeTarget(TARGET_MAIN, TARGET_SEC);

    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        TextView points_score = (TextView) findViewById(R.id.points_score);
        String points_str = (String) points_score.getText();
        int mCurrentScore = Integer.parseInt(points_str);
        savedInstanceState.putInt(STATE_SCORE, mCurrentScore);


        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }



    public GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

            locations.addMyLocation(loc);

            if (circleLoc == null){
                circleLoc = mMap.addCircle(new CircleOptions()
                        .center(loc)
                        .radius(r)
                        .strokeColor(Color.BLUE));
            }
            else {
                circleLoc.remove();

                if (mMap != null) {
                    circleLoc = mMap.addCircle(new CircleOptions()
                            .center(loc)
                            .radius(r)
                            .strokeColor(Color.BLUE));
                }
            }

            addPoints(locations.getMyLocation(locations.getMySize() - 1),locations.getTargetLocation(locations.getTargetSize() - 1));
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        mMap.setOnMyLocationChangeListener(null);
        TextView points_score = (TextView) findViewById(R.id.points_score);
        String points_str = (String) points_score.getText();
        int mCurrentScore = Integer.parseInt(points_str);
        editor.putInt(STATE_SCORE, mCurrentScore);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
/*
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
*/
        TextView points_score = (TextView) findViewById(R.id.points_score);
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        if (preferences.getInt(STATE_SCORE,0) != 0) {
            int mCurrentScore = preferences.getInt(STATE_SCORE, 0);
            String points_str = (String) Integer.toString(mCurrentScore);
            points_score.setText(points_str);
        }




    }



    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371000;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Radius * c;
    }

    public void addPoints(LatLng location, LatLng target) {

        if (CalculationByDistance(location, target) <= r*2) {
            TextView points_score = (TextView) findViewById(R.id.points_score);
            String points_str = (String) points_score.getText();
            int points_int = Integer.parseInt(points_str);
            points_int += 10;
            points_str = Integer.toString(points_int);
            points_score.setText(points_str);

            changeTarget(TARGET_MAIN, TARGET_SEC);
        }
    }

    public void changeTarget(LatLng Target1, LatLng Target2){
        if (CURRENT_TARGET != Target1){
            CURRENT_TARGET = Target1;
        }
        else{
            CURRENT_TARGET = Target2;
        }

        if (markerTarget == null && circleTarget == null){
            markerTarget = mMap.addMarker(new MarkerOptions().position(CURRENT_TARGET).title("TARGET"));
            circleTarget = mMap.addCircle(new CircleOptions()
                    .center(CURRENT_TARGET)
                    .radius(5)
                    .strokeColor(Color.RED));
        }
        else{
            assert markerTarget != null;
            markerTarget.remove();
            circleTarget.remove();

            markerTarget = mMap.addMarker(new MarkerOptions().position(CURRENT_TARGET).title("TARGET"));
            circleTarget = mMap.addCircle(new CircleOptions()
                    .center(CURRENT_TARGET)
                    .radius(5)
                    .strokeColor(Color.RED));

        }

        locations.addTargetLocation(CURRENT_TARGET);
    }
}
