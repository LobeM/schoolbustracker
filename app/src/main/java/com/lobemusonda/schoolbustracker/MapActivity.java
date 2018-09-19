package com.lobemusonda.schoolbustracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String EXTRA_DRIVER = "Driver ID";
    public static final String EXTRA_ID = "Child ID";
    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseChild;
    private DatabaseReference mDatabaseDriver;

    private Button mButtonTrack;
    
    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Marker mDriverLocationMarker;
//    private ArrayList<Driver> mDriverList;
//    private Driver mCurrentDriver;

    private String mChildId;
    private String mDriverID;
    private boolean mDriverStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();

        mChildId = intent.getStringExtra(EXTRA_ID);
        mDriverID = intent.getStringExtra(EXTRA_DRIVER);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseChild = FirebaseDatabase.getInstance().getReference("children").child(mAuth.getCurrentUser().getUid()).child(mChildId);
        mDatabaseDriver = FirebaseDatabase.getInstance().getReference("users").child(mDriverID);

        mButtonTrack = findViewById(R.id.buttonTrackDriver);

//        mDriverList = new ArrayList<>();

        getLocationPermission();
//        getBusNo();
        getDriverDetails(mDatabaseDriver);

//        mButtonTrack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mCurrentDriver.getStatus().equals("online")) {
//                    MarkerOptions options = new MarkerOptions()
//                            .position(new LatLng(mCurrentDriver.getLatitude(), mCurrentDriver.getLongitude()));
//                    mMap.addMarker(options);
//                    getDriverLocation();
//                } else {
//                    Toast.makeText(getApplicationContext(), "Driver is offline", Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });
    }

    private void getDriverDetails(DatabaseReference databaseDriver) {
        databaseDriver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status = dataSnapshot.child("status").getValue(String.class);
                double latitude = dataSnapshot.child("latitude").getValue(double.class);
                double longitude = dataSnapshot.child("longitude").getValue(double.class);
                LatLng latLng = new LatLng(latitude, longitude);
                if (mDriverLocationMarker != null) {
                    mDriverLocationMarker.remove();
                }
                if (status.equals("online")) {
                    mDriverStatus = true;
                    pinDriverLocation(latLng);
                } else {
                    mDriverStatus = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void pinDriverLocation(LatLng latLng) {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Driver");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        Log.d(TAG, "pinDriverLocation: marked");
        mDriverLocationMarker = mMap.addMarker(markerOptions);
    }


//    private void getBusNo() {
//        mDatabaseChild.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String busNo = dataSnapshot.child("busNo").getValue(String.class);
//                getDriver(busNo);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

//    private void getDriver(final String busNo) {
//        Log.d(TAG, "getDriver: called");
//        mDriverList.clear();
//        mDatabaseDrivers.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
//                    String value = driverSnapshot.child("type").getValue(String.class);
//                    if (value.equals("driver")) {
//                        Log.d(TAG, "onDataChange: Driver found");
//                        mDriverList.add(driverSnapshot.getValue(Driver.class));
//                    }
//                }
//                for (Driver driver : mDriverList) {
//                    if (driver.getBusNo().equals(busNo)) {
//                        mCurrentDriver = driver;
//                        Log.d(TAG, "onDataChange: current driver saved");
//                        getLocationPermission();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: Map is ready");
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }

    }

//    private void getDriverLocation() {
//        Log.d(TAG, "getDriverLocation: called");
//        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//
//        try {
//            if (mLocationPermissionGranted) {
//                Task location = mFusedLocationProviderClient.getLastLocation();
//                location.addOnCompleteListener(new OnCompleteListener() {
//                    @Override
//                    public void onComplete(@NonNull Task task) {
//                        if (task.isSuccessful()) {
//                            moveCamera(new LatLng(mCurrentDriver.getLatitude(), mCurrentDriver.getLongitude()),
//                                    DEFAULT_ZOOM);
//                        } else {
//                            Toast.makeText(MapActivity.this, "Unable to find childs location", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//            }
//        } catch (SecurityException e) {
//            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
//        }
//    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "Unable to find current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }

    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to lat: " + latLng.latitude + ", long: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE
                );
            }
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted = true;
                    // initialize our map
                    initMap();

                }
            }
        }
    }
}
