package com.lobemusonda.schoolbustracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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

public class MessagesFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseMessages, mDatabaseChildren;

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private MessageAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Message> mMessages;
    private ArrayList<String> mDriverIDs;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseMessages = FirebaseDatabase.getInstance().getReference("messages");
        mDatabaseChildren = FirebaseDatabase.getInstance().getReference("children").child(mAuth.getCurrentUser().getUid());
        mMessages = new ArrayList<>();
        mDriverIDs = new ArrayList<>();

//        mProgressBar = view.findViewById(R.id.progressBar);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDriverIDs();
    }

    private void getDriverIDs() {
//        mProgressBar.setVisibility(View.VISIBLE);
        mDatabaseChildren.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDriverIDs.clear();
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    mDriverIDs.add(child.getDriverID());
                }

                getMessages();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getMessages() {
        for (String driverID: mDriverIDs) {
            mDatabaseMessages.child(driverID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mMessages.clear();
                    for (DataSnapshot messageSnap : dataSnapshot.getChildren()) {
                        Message message = messageSnap.getValue(Message.class);
                        mMessages.add(message);
                    }
                    mAdapter = new MessageAdapter(mMessages);

                    mRecyclerView.setLayoutManager(mLayoutManager);
                    mRecyclerView.setAdapter(mAdapter);
//                    mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
