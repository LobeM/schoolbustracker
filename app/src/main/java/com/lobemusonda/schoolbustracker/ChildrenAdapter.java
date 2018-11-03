package com.lobemusonda.schoolbustracker;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by lobemusonda on 8/15/18.
 */

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildrenViewHolder>{
    private ArrayList<Child> mChild;
    private OnItemClickListener mListener;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }



    public static class ChildrenViewHolder extends RecyclerView.ViewHolder {
        private TextView mChildName, mBusNo, mInBus, mDropped, mAbsent;
        private FrameLayout mColorLabel;

        public ChildrenViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            mChildName = itemView.findViewById(R.id.card_name);
            mBusNo = itemView.findViewById(R.id.card_bus_no);
            mInBus = itemView.findViewById(R.id.txtInBus);
            mDropped = itemView.findViewById(R.id.txtDropped);
            mAbsent = itemView.findViewById(R.id.txtAbsent);
            mColorLabel = itemView.findViewById(R.id.cardColorLabel);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null)  {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public ChildrenAdapter(ArrayList<Child> childItems) {
        mChild = childItems;
    }

    @Override
    public ChildrenViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_item, parent, false);
        ChildrenViewHolder cvh = new ChildrenViewHolder(v, mListener);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        return cvh;
    }

    @Override
    public void onBindViewHolder(ChildrenViewHolder holder, int position) {
        Child currentItem = mChild.get(position);

        holder.mChildName.setText(currentItem.getFirstName() + " "+ currentItem.getLastName());
        getBusNo(currentItem.getDriverID(), holder);
        getStatus(currentItem.getStatus(), holder);
    }

    private void getStatus(String status, ChildrenViewHolder holder) {
        switch (status) {
            case "inBus":
                holder.mInBus.setTypeface(Typeface.DEFAULT_BOLD);
                holder.mColorLabel.setBackgroundColor(Color.GREEN);
                break;
            case "dropped":
                holder.mDropped.setTypeface(Typeface.DEFAULT_BOLD);
                holder.mColorLabel.setBackgroundColor(Color.BLUE);
                break;
            case "absent":
                holder.mAbsent.setTypeface(Typeface.DEFAULT_BOLD);
                holder.mColorLabel.setBackgroundColor(Color.RED);
                break;
        }
    }

    private void getBusNo(String driverID, final ChildrenViewHolder holder) {
        mDatabaseUsers.child(driverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String busNo = dataSnapshot.child("busNo").getValue(String.class);
                holder.mBusNo.setText(busNo);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return mChild.size();
    }
}
