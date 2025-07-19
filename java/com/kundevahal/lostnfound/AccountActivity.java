package com.kundevahal.lostnfound;

import static androidx.core.app.PendingIntentCompat.getActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";

    private TextView firstname, textfname, middlename, textmname, lastname, textlname, mobilenumber, textmnname, location, textlocation,profiletxt;
    private Button buttonEditProfile, buttonDeleteAccount;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);


        firstname = findViewById(R.id.firstname);
        textfname = findViewById(R.id.text_firstname);
        middlename = findViewById(R.id.middlename);
        textmname = findViewById(R.id.text_middlename);
        lastname = findViewById(R.id.lastname);
        textlname = findViewById(R.id.text_lastname);
        mobilenumber = findViewById(R.id.mobilenumber);
        textmnname = findViewById(R.id.text_mobilenumber);
        location = findViewById(R.id.location);
        textlocation = findViewById(R.id.text_location);
        buttonEditProfile = findViewById(R.id.button_edit_profile);
        buttonDeleteAccount = findViewById(R.id.button_delete_account);
        imageButton = findViewById(R.id.backarrowbutton);
        profiletxt = findViewById(R.id.profileText);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user != null) {
            String email = user.getEmail();
            if (email != null) {
                Query userQuery = FirebaseDatabase.getInstance().getReference().child("Authenticator")
                        .orderByChild("email").equalTo(email);

                userQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                Authenticator authenticator = userSnapshot.getValue(Authenticator.class);
                                if (authenticator != null) {
                                    firstname.setText(authenticator.getFirstname() != null ? authenticator.getFirstname() : "N/A");
                                    middlename.setText(authenticator.getMiddlename() != null ? authenticator.getMiddlename() : "N/A");
                                    lastname.setText(authenticator.getLastname() != null ? authenticator.getLastname() : "N/A");
                                    mobilenumber.setText(authenticator.getContact() != null ? authenticator.getContact() : "N/A");
                                    location.setText(authenticator.getLocation() != null ? authenticator.getLocation() : "N/A");
                                } else {
                                    Log.d(TAG, "Authenticator object is null");
                                }
                            }
                        } else {
                            Log.d(TAG, "Snapshot does not exist");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(AccountActivity.this, "Failed to load user info: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(AccountActivity.this, AuthenticatorMainActivity.class));
                        finish();
                    }
                });

                buttonEditProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firstname.setVisibility(View.GONE);
                        middlename.setVisibility(View.GONE);
                        lastname.setVisibility(View.GONE);
                        mobilenumber.setVisibility(View.GONE);
                        location.setVisibility(View.GONE);
                        buttonEditProfile.setVisibility(View.GONE);
                        buttonDeleteAccount.setVisibility(View.GONE);
                        textfname.setVisibility(View.GONE);
                        textmname.setVisibility(View.GONE);
                        textlname.setVisibility(View.GONE);
                        textmnname.setVisibility(View.GONE);
                        textlocation.setVisibility(View.GONE);
                        profiletxt.setText("Edit Profile");


                        EditAccountFragment editAccountFragment = new EditAccountFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, editAccountFragment)
                                .addToBackStack(null)
                                .commit();

                    }
                });

                buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        promptUserForPassword(email);
                    }
                });
            } else {
                // Handle case where email is null
                Toast.makeText(this, "User email is null", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AccountActivity.this, LoginActivity.class));
                finish();
            }
        } else {
            // Handle case where user is not authenticated
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AccountActivity.this, LoginActivity.class));
            finish();
        }

    }

    private void promptUserForPassword(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reauthenticate");
        builder.setMessage("Please enter your password to delete your account");

        // Use EditText to allow user to input password
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();
                if (!password.isEmpty()) {
                    reauthenticateAndDeleteUserAccount(email, password);
                } else {
                    Toast.makeText(AccountActivity.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
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
                deleteUserAccount(email);
            } else {
                Toast.makeText(AccountActivity.this, "Re-authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUserAccount(String email) {
        Query userQuery = FirebaseDatabase.getInstance().getReference().child("Authenticator")
                .orderByChild("email").equalTo(email);

        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    userSnapshot.getRef().removeValue().addOnCompleteListener(removeTask -> {
                        if (removeTask.isSuccessful()) {
                            // Proceed to delete the user account
                            user.delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(AccountActivity.this, "Account deleted", Toast.LENGTH_SHORT).show();
                                    // Redirect to LoginActivity
                                    Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(AccountActivity.this, "Failed to delete account: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(AccountActivity.this, "Failed to delete account data", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AccountActivity.this, "Failed to delete account data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AccountActivity.this, AuthenticatorMainActivity.class));
        finish();
    }
}