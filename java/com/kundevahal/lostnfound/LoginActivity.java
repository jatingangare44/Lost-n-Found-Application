package com.kundevahal.lostnfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword;
    Button ButtonLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView, AuthtextView, forgotPassTextView;
    ImageButton imageButton;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            checkUserTypeAndRedirect(currentUser.getEmail());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.pass);
        ButtonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressbar);
        textView = findViewById(R.id.SigninNow);
        forgotPassTextView = findViewById(R.id.forgotpass);
        imageButton = findViewById(R.id.authsignin);

        // Check for internet connectivity
        if (!isNetworkAvailable()) {
            Toast.makeText(LoginActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Intent = new Intent(getApplicationContext(), AuthenticatorActivity.class);
                startActivity(Intent);
                finish();
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(Intent);
                finish();
            }
        });


        ButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = editTextEmail.getText().toString();
                password = editTextPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(LoginActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null && user.isEmailVerified()) {
                                        checkUserTypeAndRedirect(email);
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Please verify your email address.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "Invalid Email or Password !", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        forgotPassTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an alert dialog to ask for the user's email
                final EditText resetEmail = new EditText(v.getContext());
                resetEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                resetEmail.setHint("Enter your email");

                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Enter your email to receive the reset link.");
                passwordResetDialog.setView(resetEmail);

                passwordResetDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the email entered by the user
                        String uemail = resetEmail.getText().toString().trim();
                        if (TextUtils.isEmpty(uemail)) {
                            Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                        } else {

                            // Directly send reset link without checking if the email exists
                            mAuth.sendPasswordResetEmail(uemail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Exception e = task.getException();
                                        if (e != null) {
                                            Log.e("ResetEmail", "Failed to send reset email: " + e.getMessage());
                                        }
                                        Toast.makeText(LoginActivity.this, "Error! Reset link is not sent", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });

                passwordResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Close the dialog
                        dialog.dismiss();
                    }
                });

                passwordResetDialog.create().show();
            }
        });
    }

    private void checkUserTypeAndRedirect(String email) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        databaseRef.child("Authenticator").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    startActivity(new Intent(LoginActivity.this, AuthenticatorMainActivity.class));
                } else {
                    startActivity(new Intent(LoginActivity.this, UserMainActivity.class));
                }
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
