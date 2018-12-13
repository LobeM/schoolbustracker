package com.lobemusonda.schoolbustracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChangeRouteFragment extends Fragment implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "ChangeRouteFragment";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseChildren, mDatabaseLocation;
    private GoogleApiClient mClient;

    private ProgressBar mProgressBar;
    private Spinner mSpinnerChildren, mSpinnerPickUp, mSpinnerDropOff;
    private Button mButtonSave;
    private ChildSpinAdapter mSpinAdapter;
    private ArrayList<Child> mChildren;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_route, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseChildren = FirebaseDatabase.getInstance().getReference("children").child(mAuth.getCurrentUser().getUid());
        mDatabaseLocation = FirebaseDatabase.getInstance().getReference("locations");

        mChildren = new ArrayList<>();

        // Inflate the layout for this fragment
        mProgressBar = view.findViewById(R.id.progressBar);
        mSpinnerChildren = view.findViewById(R.id.spinnerChildren);
        mSpinnerPickUp = view.findViewById(R.id.spinnerPickUp);
        mSpinnerDropOff = view.findViewById(R.id.spinnerDropOff);
        mButtonSave = view.findViewById(R.id.buttonSave);

        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLocation();
            }
        });

        buildGoogleApiClient();
        getChildren();

        return view;
    }

    private void getChildren() {
        mProgressBar.setVisibility(View.VISIBLE);
        mDatabaseChildren.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChildren.clear();
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    mChildren.add(child);
                }
                mSpinAdapter = new ChildSpinAdapter(getContext(), R.layout.support_simple_spinner_dropdown_item, mChildren);
                mSpinnerChildren.setAdapter(mSpinAdapter);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void findBusStations(LatLng currentLocation) {
        Log.d(TAG, "findBusStations: called");
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location="+currentLocation.latitude+","+currentLocation.longitude);
        stringBuilder.append("&radius=" + 5000);
        stringBuilder.append("&type="+"bus_station");
        stringBuilder.append("&key="+getResources().getString(R.string.API_key));

        String url = stringBuilder.toString();

        Object dataTransfer[] = new Object[4];
        dataTransfer[0] = url;
        dataTransfer[1] = mSpinnerPickUp;
        dataTransfer[2] = mSpinnerDropOff;
        dataTransfer[3] = mProgressBar;
        mProgressBar.setVisibility(View.VISIBLE);

        GetNearByPlaces getNearByPlaces = new GetNearByPlaces(getContext());
        getNearByPlaces.execute(dataTransfer);
    }

    public void saveLocation() {
        Child selectedChild = (Child) mSpinnerChildren.getSelectedItem();
        final BusStation pickUp = (BusStation) mSpinnerPickUp.getSelectedItem();
        final BusStation dropOff = (BusStation) mSpinnerDropOff.getSelectedItem();

        final String driverID = selectedChild.getDriverID();
        final String childID = selectedChild.getChildId();

        if (pickUp == null) {
            Toast.makeText(getContext(), "Select bus station", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dropOff == null) {
            Toast.makeText(getContext(), "Select bus station", Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        ChildLocation childLocation = new ChildLocation(mAuth.getCurrentUser().getUid(), pickUp, dropOff, selectedChild.getStatus());
        mDatabaseLocation.child(driverID).child(childID).setValue(childLocation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mProgressBar.setVisibility(View.GONE);
            }
        });

    }

    protected synchronized void buildGoogleApiClient() {
        mClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: called");

        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        findBusStations(currentLocation);

        if (mClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mClient, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: called");
        LocationRequest locationRequest = new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}