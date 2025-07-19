package com.kundevahal.lostnfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    TextInputEditText Firstname, Middlename, Lastname, contactNo, editTextEmail, editTextPassword;
    Button ButtonSignin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;

    FirebaseDatabase db;
    DatabaseReference reference;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.pass);
        ButtonSignin = findViewById(R.id.btn_signin);
        progressBar = findViewById(R.id.progressbar);
        textView = findViewById(R.id.LoginNow);
        Firstname = findViewById(R.id.First_name);
        Middlename = findViewById(R.id.middle_name);
        Lastname = findViewById(R.id.last_name);
        contactNo = findViewById(R.id.Mobile_Number);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ButtonSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password, fname, Mname, lname, contact;
                email = String.valueOf(editTextEmail.getText().toString().replaceAll("\\s+",""));
                password = String.valueOf(editTextPassword.getText().toString());
                fname = String.valueOf(Firstname.getText().toString().replaceAll("\\s+",""));
                Mname = String.valueOf(Middlename.getText().toString().replaceAll("\\s+",""));
                lname = String.valueOf(Lastname.getText().toString().replaceAll("\\s+",""));
                contact = String.valueOf(contactNo.getText().toString());

                boolean isValid = true;

                if (TextUtils.isEmpty(email)) {
                    editTextEmail.setError("Enter Email");
                    isValid = false;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(SignupActivity.this," Password cannot be empty",Toast.LENGTH_SHORT).show();
                    isValid = false;
                }else if (password.length() < 8) {
                    editTextPassword.setError("Password must be at least 8 characters");
                    isValid = false;
                } else if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
                    editTextPassword.setError("Password must contain at least one special character");
                    isValid = false;
                } else if (!password.matches(".*[A-Za-z].*")) {
                    editTextPassword.setError("Password must contain at least one letter");
                    isValid = false;
                } else if (!password.matches(".*\\d.*")) {
                    editTextPassword.setError("Password must contain at least one digit");
                    isValid = false;
                }

                if (TextUtils.isEmpty(fname) || !fname.matches("[a-zA-Z ]+")) {
                    Firstname.setError("Enter a valid First Name");
                    isValid = false;
                }

                if (TextUtils.isEmpty(Mname) || !Mname.matches("[a-zA-Z ]+")) {
                    Middlename.setError("Enter a valid Father Name");
                    isValid = false;
                }

                if (TextUtils.isEmpty(lname) || !lname.matches("[a-zA-Z ]+")) {
                    Lastname.setError("Enter a valid Last Name");
                    isValid = false;
                }

                if (TextUtils.isEmpty(contact) || !contact.matches("^[789][0-9]{9}$")) {
                    contactNo.setError("Enter a valid Contact (10 digits)");
                    isValid = false;
                }

                if (!isValid) {
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                DatabaseReference userref = FirebaseDatabase.getInstance().getReference("Users");
                                                String UserID = userref.push().getKey();
                                                Users users = new Users(fname, Mname, lname, contact, email);
                                                userref.child(UserID).setValue(users);

                                                editTextEmail.setText("");
                                                editTextPassword.setText("");
                                                Firstname.setText("");
                                                Middlename.setText("");
                                                Lastname.setText("");
                                                contactNo.setText("");

                                                editTextEmail.clearFocus();
                                                editTextPassword.clearFocus();
                                                Firstname.clearFocus();
                                                Middlename.clearFocus();
                                                Lastname.clearFocus();
                                                contactNo.clearFocus();

                                                editTextEmail.setError(null);
                                                editTextPassword.setError(null);
                                                Firstname.setError(null);
                                                Middlename.setError(null);
                                                Lastname.setError(null);
                                                contactNo.setError(null);

                                                Toast.makeText(SignupActivity.this, "User Registered Successfully, please verify your Email",
                                                        Toast.LENGTH_SHORT).show();
                                                mAuth.signOut(); // Sign out the user to prevent auto-login
                                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(SignupActivity.this, "Failed To send Verification Email",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                    // Sign in success, update UI with the signed-in user's information
                                    //FirebaseUser user = mAuth.getCurrentUser();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(SignupActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });
    }
}
