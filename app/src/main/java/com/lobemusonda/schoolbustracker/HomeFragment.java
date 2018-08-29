package com.lobemusonda.schoolbustracker;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;

/**
 * Created by lobemusonda on 8/14/18.
 */

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private RecyclerView mRecyclerView;
    private ChildrenAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<ChildItem> mChildItems;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mChildItems = new ArrayList<>();
        mChildItems.add(new ChildItem("Keziah Chakaba", "ABH 3907", -15.3971622, 28.3057016));
        mChildItems.add(new ChildItem("Elizabeth Mulindwa", "ABH 3907", -15.388111, 28.325354));
        mChildItems.add(new ChildItem("Sibeso Mukelebai", "BAB 2015", -15.4207532, 28.2870607));
        mChildItems.add(new ChildItem("Nyanzigi Ramadani", "ABA 6969", -15.384593, 28.3153254));

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new ChildrenAdapter(mChildItems);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        if (isServicesOk()) {
            mAdapter.setOnItemClickListener(new ChildrenAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    //mChildItems.get(position).setmBusNo("Clicked");
                    //mAdapter.notifyItemChanged(position);
                    Intent intent = new Intent(getContext(), MapActivity.class);
                    intent.putExtra(MapActivity.EXTRA_POSITION, position);
                    startActivity(intent);
                }
            });
        }

        return view;
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
