package com.lobemusonda.schoolbustracker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by lobemusonda on 8/15/18.
 */

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildrenViewHolder>{
    private ArrayList<ChildItem> mChildItems;
    private OnItemClickListener mListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }



    public static class ChildrenViewHolder extends RecyclerView.ViewHolder {
        public TextView mChildName;
        public TextView mBusNo;

        public ChildrenViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            mChildName = itemView.findViewById(R.id.card_name);
            mBusNo = itemView.findViewById(R.id.card_bus_no);

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

    public ChildrenAdapter(ArrayList<ChildItem> childItems) {
        mChildItems = childItems;
    }

    @Override
    public ChildrenViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_item, parent, false);
        ChildrenViewHolder cvh = new ChildrenViewHolder(v, mListener);
        return cvh;
    }

    @Override
    public void onBindViewHolder(ChildrenViewHolder holder, int position) {
        ChildItem currentItem = mChildItems.get(position);

        holder.mChildName.setText(currentItem.getmChildName());
        holder.mBusNo.setText(currentItem.getmBusNo());
    }

    @Override
    public int getItemCount() {
        return mChildItems.size();
    }
}
