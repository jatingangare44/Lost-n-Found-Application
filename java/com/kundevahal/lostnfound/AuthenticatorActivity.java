package com.kundevahal.lostnfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

public class AuthenticatorActivity extends AppCompatActivity {

    TextInputEditText Firstname, Middlename, Lastname, contactNo, editTextEmail, editTextPassword, specialcode;
    Button ButtonSignin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    private final String spcode = "Kundevahal@2004";
    String[] locations = {"Grampanchayat Office", "Kuluaai Mandir"};
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> arrayAdapter;

    @Override
    public void onStart() {
        super.onStart();
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_authenticator);

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
        autoCompleteTextView = findViewById(R.id.location);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.list_location, locations);
        autoCompleteTextView.setAdapter(arrayAdapter);
        specialcode = findViewById(R.id.authenticator_edittext);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String location = parent.getItemAtPosition(position).toString();
                Toast.makeText(AuthenticatorActivity.this, "Location " + location, Toast.LENGTH_SHORT).show();
            }
        });

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
                String email, password, fname, Mname, lname, contact, location, Specialcode;
                email = String.valueOf(editTextEmail.getText().toString());
                password = String.valueOf(editTextPassword.getText().toString());
                fname = String.valueOf(Firstname.getText().toString());
                Mname = String.valueOf(Middlename.getText().toString());
                lname = String.valueOf(Lastname.getText().toString());
                contact = String.valueOf(contactNo.getText().toString());
                location = String.valueOf(autoCompleteTextView.getText().toString());
                Specialcode = String.valueOf(specialcode.getText().toString());

                boolean isValid = true;

                if (TextUtils.isEmpty(email)) {
                    editTextEmail.setError("Enter Email");
                    isValid = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editTextEmail.setError("Enter a valid email");
                    isValid = false;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(AuthenticatorActivity.this," Password cannot be empty",Toast.LENGTH_SHORT).show();
                    isValid = false;
                } else if (password.length() < 8) {
                    editTextPassword.setError("Password must be at least 6 characters");
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
                    Middlename.setError("Enter a valid Middle Name");
                    isValid = false;
                }

                if (TextUtils.isEmpty(lname) || !lname.matches("[a-zA-Z ]+")) {
                    Lastname.setError("Enter a valid Last Name");
                    isValid = false;
                }

                if (TextUtils.isEmpty(contact) || !contact.matches("^[1789][0-9]{9}$")) {
                    contactNo.setError("Enter a valid 10-digit contact number");
                    isValid = false;
                }

                if (TextUtils.isEmpty(location)) {
                    autoCompleteTextView.setError("Enter Location");
                    isValid = false;
                }

                if (!Specialcode.equals(spcode)) {
                    Toast.makeText(AuthenticatorActivity.this,"Invalid Special Code",Toast.LENGTH_SHORT).show();
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
                                                DatabaseReference authRef = FirebaseDatabase.getInstance().getReference("Authenticator");
                                                String AuthID = authRef.push().getKey();
                                                Authenticator auth = new Authenticator(fname, Mname, lname, contact, location, email);
                                                authRef.child(AuthID).setValue(auth);

                                                Toast.makeText(AuthenticatorActivity.this, "User Registered Successfully, please verify your Email", Toast.LENGTH_SHORT).show();
                                                mAuth.signOut(); // Sign out the user to prevent auto-login
                                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(AuthenticatorActivity.this, "Failed To send Verification Email", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(AuthenticatorActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
