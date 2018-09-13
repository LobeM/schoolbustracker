package com.lobemusonda.schoolbustracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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
    public static final String EXTRA_POSITION = "child_position";
    private static final String TAG = "MapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseChildren;
    private DatabaseReference mDatabaseDrivers;

    private Button mButtonTrack;
    
    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ArrayList<Child> mChildList;
    private ArrayList<Driver> mDriverList;
    private Driver mCurrentDriver;
    private int mChildPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseChildren = FirebaseDatabase.getInstance().getReference("children").child(mAuth.getCurrentUser().getUid());
        mDatabaseDrivers = FirebaseDatabase.getInstance().getReference("users");

        Intent intent = getIntent();
        mChildPosition = intent.getIntExtra(EXTRA_POSITION, 0);
        
        mButtonTrack = findViewById(R.id.buttonTrackDriver);

        mChildList = new ArrayList<>();
        mDriverList = new ArrayList<>();

        getChild();

        mButtonTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentDriver.getStatus().equals("online")) {
                    MarkerOptions options = new MarkerOptions()
                            .position(new LatLng(mCurrentDriver.getLatitude(), mCurrentDriver.getLongitude()));
                    mMap.addMarker(options);
                    getDriverLocation();
                } else {
                    Toast.makeText(getApplicationContext(), "Driver is offline", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private void getDriver(final Child currentChild) {
        Log.d(TAG, "getDriver: called");
        mDriverList.clear();
        mDatabaseDrivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: snapshot =" + driverSnapshot.getValue());
                    String value = driverSnapshot.child("type").getValue(String.class);
                    if (value.equals("driver")) {
                        Log.d(TAG, "onDataChange: Driver found");
                        mDriverList.add(driverSnapshot.getValue(Driver.class));
                    }
                }
                for (Driver driver : mDriverList) {
                    Log.d(TAG, "onDataChange: bus no "+driver.getBusNo());
                    if (driver.getBusNo().equals(currentChild.getBusNo())) {
                        mCurrentDriver = driver;
                        Log.d(TAG, "onDataChange: current driver saved");
                        getLocationPermission();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getChild() {
        Log.d(TAG, "getChild: called");
        mChildList.clear();
        mDatabaseChildren.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChildList.clear();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    mChildList.add(child);
                }
                final Child currentChild = mChildList.get(mChildPosition);
                Log.d(TAG, "onDataChange: child = " + currentChild.getFirstName());
                getDriver(currentChild);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: Map is ready");
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            //getDriverLocation();

            if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }

    }

    private void getDriverLocation() {
        Log.d(TAG, "getDriverLocation: called");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            moveCamera(new LatLng(mCurrentDriver.getLatitude(), mCurrentDriver.getLongitude()),
                                    DEFAULT_ZOOM);
                        } else {
                            Toast.makeText(MapActivity.this, "Unable to find childs location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

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
