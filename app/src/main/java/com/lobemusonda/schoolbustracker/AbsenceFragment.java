package com.lobemusonda.schoolbustracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AbsenceFragment extends Fragment {
    private static final String TAG = "AbsenceFragment";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private ProgressBar mProgressBar;
    private Spinner mSpinner;
    private Button mAddBtn, mRemoveBtn;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    private ChildSpinAdapter mSpinAdapter;
    private ChildrenAdapter mAdapter;
    private ArrayList<Child> mChildren;
    private ArrayList<Child> mAbsentList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_absence, container, false);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        mChildren = new ArrayList<>();
        mAbsentList = new ArrayList<>();

//         Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: inflated");
        mProgressBar = view.findViewById(R.id.progressBar);
        mSpinner = view.findViewById(R.id.spinner_children);
        mAddBtn = view.findViewById(R.id.btn_add_absent);
        mRemoveBtn = view.findViewById(R.id.btn_remove_absent);

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerAbsent();
//                updateChild("absent");
            }
        });

        mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAbsence();
//                updateChild("dropped");
            }
        });

        getChildren();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getAbsentList();
    }

    private void removeAbsence() {
        Child selectedChild = (Child) mSpinner.getSelectedItem();
        if (selectedChild.getStatus().equals("inBus")){
            Toast.makeText(getContext(), "Child in bus!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedChild.getStatus().equals("dropped")){
            Toast.makeText(getContext(), "Child not added!", Toast.LENGTH_SHORT).show();
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        selectedChild.setStatus("absent");
        final String childID = selectedChild.getChildId();
        mDatabase.getReference("absent").child(mAuth.getCurrentUser().getUid())
                .child(childID)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                updateChild(childID, "dropped");
            }
        });
    }

    private void registerAbsent() {
        Child selectedChild = (Child) mSpinner.getSelectedItem();
        if (selectedChild.getStatus().equals("inBus")){
            Toast.makeText(getContext(), "Child in bus!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedChild.getStatus().equals("absent")){
            Toast.makeText(getContext(), "Already added!", Toast.LENGTH_SHORT).show();
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);
        selectedChild.setStatus("absent");
        final String childID = selectedChild.getChildId();

        mDatabase.getReference("absent").child(mAuth.getCurrentUser().getUid())
                .child(childID)
                .setValue(selectedChild)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                updateChild(childID, "absent");
            }
        });
    }

    private void updateChild(String childID, String status) {
        mDatabase.getReference("children").child(mAuth.getCurrentUser().getUid())
                .child(childID).child("status").setValue(status)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void getAbsentList() {
        mProgressBar.setVisibility(View.VISIBLE);
        mDatabase.getReference("absent").child(mAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAbsentList.clear();
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()){
                    Child child = childSnapshot.getValue(Child.class);
                    mAbsentList.add(child);
                }
                mAdapter = new ChildrenAdapter(mAbsentList);

                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(mAdapter);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getChildren() {
        mProgressBar.setVisibility(View.VISIBLE);
        mDatabase.getReference("children").child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChildren.clear();
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    mChildren.add(child);
                }
                mSpinAdapter = new ChildSpinAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, mChildren);
                mSpinner.setAdapter(mSpinAdapter);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
