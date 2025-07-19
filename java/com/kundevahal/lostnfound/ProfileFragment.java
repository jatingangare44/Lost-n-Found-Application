package com.kundevahal.lostnfound;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment implements OnMapReadyCallback {

    private TextView userEmail, firstName, middleName, lastName, mobileNumber;
    private Button logoutButton, editProfile, deleteButton,redirectmap;
    private FirebaseAuth mAuth;
    private ImageButton backButton;
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        user = mAuth.getCurrentUser();

        // Initialize UI elements
        userEmail = view.findViewById(R.id.user_email);
        firstName = view.findViewById(R.id.first_name);
        middleName = view.findViewById(R.id.middle_name);
        lastName = view.findViewById(R.id.last_name);
        mobileNumber = view.findViewById(R.id.mobile_number);
        logoutButton = view.findViewById(R.id.btn_logout);
        backButton = view.findViewById(R.id.back_button);
        editProfile = view.findViewById(R.id.btn_edit_profile);
        deleteButton = view.findViewById(R.id.btn_delete_account);
        redirectmap = view.findViewById(R.id.btn_redirect_map);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.gmap);
        supportMapFragment.getMapAsync(this);

        // Set user email
        if (user != null) {
            String email = user.getEmail();
            userEmail.setText(email);

            // Fetch additional user details from the database using email as key
            Query userQuery = databaseReference.orderByChild("email").equalTo(email);
            userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String firstNameStr = userSnapshot.child("firstname").getValue(String.class);
                            String middleNameStr = userSnapshot.child("middlename").getValue(String.class);
                            String lastNameStr = userSnapshot.child("lastname").getValue(String.class);
                            String mobileNumberStr = userSnapshot.child("contact").getValue(String.class);

                            firstName.setText(firstNameStr != null ? firstNameStr : "N/A");
                            middleName.setText(middleNameStr != null ? middleNameStr : "N/A");
                            lastName.setText(lastNameStr != null ? lastNameStr : "N/A");
                            mobileNumber.setText(mobileNumberStr != null ? mobileNumberStr : "N/A");
                        }
                    } else {
                        Log.e(TAG, "No user data found for email: " + email);
                        Toast.makeText(getContext(), "No user data found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: ", error.toException());
                    Toast.makeText(getContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    promptUserForPassword(email);
                }
            });
        }

        redirectmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Specify the URL you want to open
                String url = "https://maps.app.goo.gl/VzpfUizzkDMt98kj8";

                // Create an intent to open the URL
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));

                // Start the activity to open the URL in a browser
                startActivity(intent);

            }
        });

        // Set onClickListener for the logout button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment editProfileFragment = new EditUserProfileFragment();
                FragmentManager fragmentManager = getParentFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_layout, editProfileFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        // Set onClickListener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        return view;
    }

    private void promptUserForPassword(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Reauthenticate");
        builder.setMessage("Please enter your password to delete your account");

        // Use EditText to allow user to input password
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();
                if (!password.isEmpty()) {
                    reauthenticateAndDeleteUserAccount(email, password);
                } else {
                    Toast.makeText(getContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void reauthenticateAndDeleteUserAccount(String email, String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Reauthentication successful.");
                deleteUserAccount(email);
            } else {
                Log.e(TAG, "Reauthentication failed: ", task.getException());
                Toast.makeText(getContext(), "Re-authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserAccount(String email) {
        Query userQuery = FirebaseDatabase.getInstance().getReference().child("Users")
                .orderByChild("email").equalTo(email);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "User data found. Proceeding to delete user data.");
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        userSnapshot.getRef().removeValue().addOnCompleteListener(removeTask -> {
                            if (removeTask.isSuccessful()) {
                                Log.d(TAG, "User data deleted successfully. Proceeding to delete Firebase Authentication account.");
                                user.delete().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User account deleted from Firebase Authentication.");
                                        Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                                        // Redirect to LoginActivity
                                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                                        startActivity(intent);
                                        requireActivity().finish();
                                    } else {
                                        Log.e(TAG, "Failed to delete Firebase Authentication account: ", task.getException());
                                        Toast.makeText(getContext(), "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Log.e(TAG, "Failed to delete user data: ", removeTask.getException());
                                Toast.makeText(getContext(), "Failed to delete account data", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "No user data found for email: " + email);
                    Toast.makeText(getContext(), "No user data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Database error: ", error.toException());
                Toast.makeText(getContext(), "Failed to delete account data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng location= new LatLng(18.969205990358127, 73.06330746871606);
        googleMap.addMarker(new MarkerOptions().position(location).title("Grampanchayat Kundevahal"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,18));

    }
}
