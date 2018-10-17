package com.lobemusonda.schoolbustracker;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChangeRouteFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "ChangeRouteFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;

    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mCurrentLocation;
    private GoogleMap mMap;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseChildren;

    private ProgressBar mProgressBar;
    private Spinner mSpinnerChildren, mSpinnerPickUp, mSpinnerDropOff;
//    private ArrayAdapter<String> mSpinnerArrayAdapter;
    private ChildSpinAdapter mSpinAdapter;
    private ArrayList<Child> mChildren;
//    private ArrayList<String> mChildNames;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_route, container, false);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseChildren = FirebaseDatabase.getInstance().getReference("children").child(mAuth.getCurrentUser().getUid());
        mChildren = new ArrayList<>();
//        mChildNames = new ArrayList<>();

        // Inflate the layout for this fragment
        mProgressBar = view.findViewById(R.id.progressBar);
        mSpinnerChildren = view.findViewById(R.id.spinnerChildren);
        mSpinnerPickUp = view.findViewById(R.id.spinnerPickUp);
        mSpinnerDropOff = view.findViewById(R.id.spinnerDropOff);

        getLocationPermission();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mProgressBar.setVisibility(View.VISIBLE);
        mDatabaseChildren.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChildren.clear();
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    mChildren.add(child);
                }
//                for (Child child : mChildren) {
//                    mChildNames.add(child.getFirstName()+" "+child.getLastName());
//                }
//                mSpinnerArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, mChildNames);
                mSpinAdapter = new ChildSpinAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, mChildren);
                mSpinnerChildren.setAdapter(mSpinAdapter);
                mProgressBar.setVisibility(View.GONE);
                mSpinnerChildren.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        Child selectedChild = mChildren.get(i);

                        Toast.makeText(getContext(), selectedChild.getFirstName()+" is selected", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (mLocationPermissionGranted) {
            //mMap = googleMap;
            getDeviceLocation();
        }
        findBusStations();
    }

    public void findBusStations() {
        Log.d(TAG, "findBusStations: called");
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location="+"-15.3740549"+","+"28.2911655");
        stringBuilder.append("&radius=" + 1000);
        stringBuilder.append("&type="+"bus_station");
        stringBuilder.append("&key="+getResources().getString(R.string.API_key));

        String url = stringBuilder.toString();

        Object dataTransfer[] = new Object[3];
        //dataTransfer[0] = mMap;
        dataTransfer[0] = url;
        dataTransfer[1] = mSpinnerPickUp;
        dataTransfer[2] = mSpinnerDropOff;

        GetNearByPlaces getNearByPlaces = new GetNearByPlaces(getContext());
        getNearByPlaces.execute(dataTransfer);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: called");
        if (mLocationPermissionGranted) {
            mMap = googleMap;
            getDeviceLocation();
        }
        findBusStations();
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            mCurrentLocation = (Location) task.getResult();
                        } else {
                            Toast.makeText(getContext(), "Unable to find current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }

    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (ContextCompat.checkSelfPermission(this.getContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(
                        getActivity(),
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE
                );
            }
        } else {
            ActivityCompat.requestPermissions(
                    getActivity(),
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

                }
            }
        }
    }
}