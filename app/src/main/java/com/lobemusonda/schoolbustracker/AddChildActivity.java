package com.lobemusonda.schoolbustracker;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
    private DatabaseReference mDatabaseUsers, mDatabaseChildren;
    private FirebaseDatabase mDatabase;
    private Spinner mSpinnerSchools;
    private EditText mEditTextFirstName, mEditTextLastName;
    private Button mButtonAddChild;
    private ProgressBar mProgressBar;
    private List<String> mDriverIdList, mSchoolList;
    private String mDriverId;
    private String mBusNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("users");
        mDatabaseChildren = FirebaseDatabase.getInstance().getReference("children").child(mAuth.getCurrentUser().getUid());

        mButtonAddChild = findViewById(R.id.buttonAddChild);
        mSpinnerSchools = findViewById(R.id.spinnerSchools);
        mEditTextFirstName = findViewById(R.id.editTextFirstName);
        mEditTextLastName = findViewById(R.id.editTextLastName);
        mProgressBar = findViewById(R.id.progressBar);

        mDriverIdList = new ArrayList<>();
        mSchoolList = new ArrayList<>();

        //getDriver();
        getSchools();

        mButtonAddChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveChild();
            }
        });
    }

    private void getSchools() {
        mProgressBar.setVisibility(View.VISIBLE);
        mDatabase.getReference("schools").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot schoolSnapshot: dataSnapshot.getChildren()) {
                    mDriverIdList.add(schoolSnapshot.getKey());
                    mSchoolList.add(schoolSnapshot.getValue(String.class));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item, mSchoolList);
                mSpinnerSchools.setAdapter(adapter);
                getDriverIds();
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getDriverIds() {
        mSpinnerSchools.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mDriverId = mDriverIdList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void saveChild() {
        String firstName = mEditTextFirstName.getText().toString().trim();
        String lastName = mEditTextLastName.getText().toString().trim();
//        String busNo = mTextViewBusNo.getText().toString();

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
        if (mDriverId.isEmpty()) {
            Toast.makeText(this, R.string.no_bus_av, Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        String id = mDatabaseChildren.push().getKey();
        Child child = new Child(id, firstName, lastName, mDriverId);
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
}
