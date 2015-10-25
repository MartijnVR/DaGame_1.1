package com.vanrooy.martijn.dagame11;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private Circle circleLoc;
    private Circle circleTarget;
    private MyLocations locations = new MyLocations();
    private int r = 5;
    private static final String TAG = "Message";
    private LatLng CURRENT_TARGET;
    private LatLng TARGET_MAIN = new LatLng(50.864164, 4.678891);
    private LatLng TARGET_SEC = new LatLng(50.864021, 4.678460);
    private Marker markerTarget;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    public Marker mymarker;
    boolean gps_connected = false;
    boolean network_connected = false;
    boolean connections_working = false;
    public float zoomlevel = 18;
    public boolean zoomed = false;
    public LatLng loc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Creating.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.i(TAG,"Creating part 1 success");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        Log.i(TAG,"APIclient created");
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)        // 5 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds

        final Button zoombutton = (Button)findViewById(R.id.zoombutton);


        zoombutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i(TAG,"clicked!");
                Log.i(TAG,String.valueOf(loc));
                if (loc!=null){
                    Log.i(TAG,"moving camera");
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomlevel));
            }}
        });

        Log.i(TAG, "Oncreate success");
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        changeTarget(TARGET_MAIN, TARGET_SEC);


    }

    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        if (connections_working){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            while (location == null){
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            }
            Log.i(TAG, "Handle New Location.");
            handleNewLocation(location);


        } else if (!network_connected) {
            Log.i(TAG, "No network.");
            show_alertdialog_network();
        } else {
            Log.i(TAG, "No GPS.");
            show_alertdialog_gps();



        }}

    public void show_alertdialog_network() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No network!");
        builder.setMessage("Please turn on wifi or network data.");
        builder.setPositiveButton("To network data", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings$DataUsageSummaryActivity"));;
                startActivity(intent);
            }
        });
        builder.setNegativeButton("To wifi", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNeutralButton("Nahh", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MapsActivity.this, "No game for you!", Toast.LENGTH_SHORT).show();
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        builder.show();
    }

    public void show_alertdialog_gps(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No gps!");
        builder.setMessage("Please turn on location services.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Nahh", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MapsActivity.this, "No game for you!", Toast.LENGTH_SHORT).show();
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        builder.show();
    }


    private void handleNewLocation(Location location) {
        Log.d(TAG, "handling New Location");
        this.loc = new LatLng(location.getLatitude(), location.getLongitude());
        Log.i(TAG, String.valueOf(loc));
        if (!zoomed){
            Log.i(TAG,"zooming.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomlevel));
            zoomed = true;
        }
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

        addPoints(locations.getMyLocation(locations.getMySize() - 1), locations.getTargetLocation(locations.getTargetSize() - 1));

        if (mymarker != null) {
            mymarker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .position(loc)
                .title("I am here!");
        mymarker = mMap.addMarker(options);
        Log.i(TAG, "Marker placed.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onconnectionfailed");
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location Changed.");
        handleNewLocation(location);



    }

    @Override
    protected void onPause() {
        Log.i(TAG, "Paused.");
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (LocationListener) this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Onresume");
        zoomed = false;
        super.onResume();
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) network_connected = true;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) gps_connected = true;
        if (network_connected == true && gps_connected == true) connections_working = true;
        Log.i(TAG,"Connecting apiclient");
        mGoogleApiClient.connect();

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
        TextView points_score = (TextView) findViewById(R.id.points_score);

        if (CalculationByDistance(location, target) <= r*2) {
            String points_str = (String) points_score.getText();
            int points_int = Integer.parseInt(points_str);
            points_int += 10;
            points_str = Integer.toString(points_int);
            points_score.setText(points_str);

            changeTarget(TARGET_MAIN,TARGET_SEC);
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

    public void zoombutton(View view) {
        Log.i(TAG, "clicked!");
//        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//        Log.i(TAG, String.valueOf(location));
        if (loc != null) {
            Log.i(TAG, "moving camera");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomlevel));
        }
    }
}
