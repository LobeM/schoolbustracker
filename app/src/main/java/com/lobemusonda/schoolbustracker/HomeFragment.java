package com.lobemusonda.schoolbustracker;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by lobemusonda on 8/14/18.
 */

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseChildren;

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private ChildrenAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Child> mChild;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseChildren = FirebaseDatabase.getInstance().getReference("children").child(mAuth.getCurrentUser().getUid());
        mChild = new ArrayList<>();

        FloatingActionButton fabAddChild = view.findViewById(R.id.fabAddChild);
        fabAddChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddChildActivity.class);
                startActivity(intent);
            }
        });

        mProgressBar = view.findViewById(R.id.progressBar);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getChildren();
    }

    public void getChildren() {
        mProgressBar.setVisibility(View.VISIBLE);
        mDatabaseChildren.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChild.clear();
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    mChild.add(child);
                }
                mAdapter = new ChildrenAdapter(mChild);

                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);
                mProgressBar.setVisibility(View.GONE);

                if (isServicesOk()) {
                    mAdapter.setOnItemClickListener(new ChildrenAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            Intent intent = new Intent(getContext(), MapActivity.class);
                            intent.putExtra(MapActivity.EXTRA_ID, mChild.get(position).getChildId());
                            intent.putExtra(MapActivity.EXTRA_DRIVER, mChild.get(position).getDriverID());
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean isServicesOk() {
        Log.d(TAG, "isServicesOk: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOk: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOk: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(getContext(), "You cant make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
