package com.kundevahal.lostnfound;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class EditAccountFragment extends Fragment {

    private EditText firstNameEditText, middleNameEditText, lastNameEditText, mobileNumberEditText;
    private Button updateButton, cancelButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_account, container, false);



        firstNameEditText = view.findViewById(R.id.editTextFirstName);
        middleNameEditText = view.findViewById(R.id.editTextMiddleName);
        lastNameEditText = view.findViewById(R.id.editTextLastName);
        mobileNumberEditText = view.findViewById(R.id.editTextMobileNumber);
        updateButton = view.findViewById(R.id.buttonUpdate);
        cancelButton = view.findViewById(R.id.buttonCancel);

        mAuth = FirebaseAuth.getInstance();
        email = mAuth.getCurrentUser().getEmail();

        loadUserData();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserData();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AccountActivity.class));
                getActivity().finish();
            }
        });

        return view;
    }

    private void loadUserData() {
        Query userQuery = FirebaseDatabase.getInstance().getReference("Authenticator")
                .orderByChild("email").equalTo(email);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String firstName = userSnapshot.child("firstname").getValue(String.class);
                        String middleName = userSnapshot.child("middlename").getValue(String.class);
                        String lastName = userSnapshot.child("lastname").getValue(String.class);
                        String mobileNumber = userSnapshot.child("contact").getValue(String.class);

                        firstNameEditText.setText(firstName);
                        middleNameEditText.setText(middleName);
                        lastNameEditText.setText(lastName);
                        mobileNumberEditText.setText(mobileNumber);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserData() {
        String firstName = firstNameEditText.getText().toString().trim();
        String middleName = middleNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String mobileNumber = mobileNumberEditText.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(firstName) || !firstName.matches("[a-zA-Z ]+")) {
            firstNameEditText.setError("Enter a valid First Name");
            isValid = false;
        } else if (firstName.length()>20) {
            Toast.makeText(getActivity(), "Invalid! More than 20 letters", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(middleName) || !middleName.matches("[a-zA-Z ]+")) {
            middleNameEditText.setError("Enter a valid Middle Name");
            isValid = false;
        }
        else if (middleName.length()>20) {
            Toast.makeText(getActivity(), "Invalid! More than 20 letters", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(lastName) || !lastName.matches("[a-zA-Z ]+")) {
            lastNameEditText.setError("Enter a valid Last Name");
            isValid = false;
        }
        else if (lastName.length()>20) {
            Toast.makeText(getActivity(), "Invalid! More than 20 letters", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(mobileNumber) || mobileNumber.length() != 10 || !mobileNumber.matches("[0-9]+")) {
            mobileNumberEditText.setError("Enter a valid Mobile Number (10 digits)");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        Query userQuery = FirebaseDatabase.getInstance().getReference("Authenticator")
                .orderByChild("email").equalTo(email);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        userSnapshot.getRef().child("firstname").setValue(firstName);
                        userSnapshot.getRef().child("middlename").setValue(middleName);
                        userSnapshot.getRef().child("lastname").setValue(lastName);
                        userSnapshot.getRef().child("contact").setValue(mobileNumber)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getActivity(), AccountActivity.class));
                                            getActivity().finish();
                                        } else {
                                            Toast.makeText(getActivity(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to update user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
