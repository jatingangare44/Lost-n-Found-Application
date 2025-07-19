package com.kundevahal.lostnfound;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class EditUserProfileFragment extends Fragment {

    private EditText editFirstName, editMiddleName, editLastName, editMobileNumber;
    private Button saveChangesButton, cancelButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_user_profile, container, false);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI elements
        editFirstName = view.findViewById(R.id.edit_first_name);
        editMiddleName = view.findViewById(R.id.edit_middle_name);
        editLastName = view.findViewById(R.id.edit_last_name);
        editMobileNumber = view.findViewById(R.id.edit_mobile_number);
        saveChangesButton = view.findViewById(R.id.btn_save_changes);
        cancelButton = view.findViewById(R.id.btn_cancel);

        // Get current user email
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();
            loadUserData();
        }

        // Set onClickListener for the Save Changes button
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
            }
        });

        // Set onClickListener for the Cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelEdit();
            }
        });

        return view;
    }

    private void loadUserData() {
        if (email != null) {
            Query userQuery = databaseReference.orderByChild("email").equalTo(email);
            userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String firstName = userSnapshot.child("firstname").getValue(String.class);
                            String middleName = userSnapshot.child("middlename").getValue(String.class);
                            String lastName = userSnapshot.child("lastname").getValue(String.class);
                            String mobileNumber = userSnapshot.child("contact").getValue(String.class);

                            if (firstName != null) editFirstName.setText(firstName);
                            if (middleName != null) editMiddleName.setText(middleName);
                            if (lastName != null) editLastName.setText(lastName);
                            if (mobileNumber != null) editMobileNumber.setText(mobileNumber);
                        }
                    } else {
                        Toast.makeText(getActivity(), "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveChanges() {
        String firstName = editFirstName.getText().toString().trim();
        String middleName = editMiddleName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String mobileNumber = editMobileNumber.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(firstName) || !firstName.matches("[a-zA-Z ]+")) {
            editFirstName.setError("Enter a valid First Name");
            isValid = false;
        }

        if (TextUtils.isEmpty(middleName) || !middleName.matches("[a-zA-Z ]+")) {
            editMiddleName.setError("Enter a valid Middle Name");
            isValid = false;
        }

        if (TextUtils.isEmpty(lastName) || !lastName.matches("[a-zA-Z ]+")) {
            editLastName.setError("Enter a valid Last Name");
            isValid = false;
        }

        if (TextUtils.isEmpty(mobileNumber) || mobileNumber.length() != 10 || !mobileNumber.matches("[0-9]+")) {
            editMobileNumber.setError("Enter a valid Mobile Number (10 digits)");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        if (email != null) {
            Query userQuery = databaseReference.orderByChild("email").equalTo(email);
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
                                                getFragmentManager().popBackStack();
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

    private void cancelEdit() {
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
        }
    }
}
