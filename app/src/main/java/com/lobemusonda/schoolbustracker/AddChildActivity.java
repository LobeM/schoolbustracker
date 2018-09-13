package com.lobemusonda.schoolbustracker;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddChildActivity extends AppCompatActivity {
    private static final String TAG = "AddChildActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference, mDatabaseChildren;
    private TextView mTextViewBusNo, mTextViewLabel;
    private EditText mEditTextFirstName, mEditTextLastName;
    private Button mButtonAddChild;
    private ProgressBar mProgressBar;
    private List<String> mBusList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
        mDatabaseChildren = FirebaseDatabase.getInstance().getReference("children").child(mAuth.getCurrentUser().getUid());

        mButtonAddChild = findViewById(R.id.buttonAddChild);
        mTextViewBusNo = findViewById(R.id.textViewBusNo);
        mTextViewLabel = findViewById(R.id.textViewBusNoLabel);
        mEditTextFirstName = findViewById(R.id.editTextFirstName);
        mEditTextLastName = findViewById(R.id.editTextLastName);
        mProgressBar = findViewById(R.id.progressBar);

        mBusList = new ArrayList<>();

        mButtonAddChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChild();
            }
        });
    }

    private void saveChild() {
        String firstName = mEditTextFirstName.getText().toString().trim();
        String lastName = mEditTextLastName.getText().toString().trim();
        String busNo = mTextViewBusNo.getText().toString();

        if (firstName.isEmpty()) {
            mEditTextFirstName.setError("First name is required");
            mEditTextFirstName.requestFocus();
            return;
        }
        if (lastName.isEmpty()) {
            mEditTextLastName.setError("First name is required");
            mEditTextLastName.requestFocus();
            return;
        }
        if (busNo.equals(R.string.no_bus_av)) {
            Toast.makeText(this, R.string.no_bus_av, Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        String id = mDatabaseChildren.push().getKey();
        Child child = new Child(id, firstName, lastName, busNo);
        mDatabaseChildren.child(id).setValue(child).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mProgressBar.setVisibility(View.GONE);
                onBackPressed();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                    if (driverSnapshot.child("type").getValue().equals("driver")) {
                        mBusList.add(driverSnapshot.child("busNo").getValue(String.class));
                    }
                }
                if (!mBusList.isEmpty()) {
                    mTextViewBusNo.setText(mBusList.get(0));
                } else {
                    mTextViewLabel.setVisibility(View.GONE);
                    mTextViewBusNo.setText(R.string.no_bus_av);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddChildActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
