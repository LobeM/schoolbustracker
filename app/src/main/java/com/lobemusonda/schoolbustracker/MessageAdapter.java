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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private ArrayList<Message> mMessages;
    private MessageAdapter.OnItemClickListener mListener;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    public void setOnItemClickListener(MessageAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }



    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView mTitle, mName, mMessage, mTime;

        public MessageViewHolder(View itemView, final MessageAdapter.OnItemClickListener listener) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.card_title);
            mName = itemView.findViewById(R.id.card_name);
            mMessage = itemView.findViewById(R.id.card_message);
            mTime = itemView.findViewById(R.id.card_time);

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

    public MessageAdapter(ArrayList<Message> messageItems) {
        mMessages = messageItems;
    }

    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        MessageAdapter.MessageViewHolder cvh = new MessageAdapter.MessageViewHolder(v, mListener);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        return cvh;
    }

    @Override
    public void onBindViewHolder(MessageAdapter.MessageViewHolder holder, int position) {
        Message currentMessage = mMessages.get(position);

        holder.mTitle.setText(currentMessage.getTitle());
        holder.mName.setText(currentMessage.getUsername());
        holder.mMessage.setText(currentMessage.getMessage());
        holder.mTime.setText(currentMessage.getTime());
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }
}
