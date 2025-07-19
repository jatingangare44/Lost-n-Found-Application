package com.kundevahal.lostnfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    Handler handler;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null && currentUser.isEmailVerified()) {
                    checkUserTypeAndRedirect(currentUser.getEmail());
                } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 3000); // 3 seconds delay
    }

    private void checkUserTypeAndRedirect(String email) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference authsRef = FirebaseDatabase.getInstance().getReference("Authenticator");

        // Check in Users node
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // If email exists in Users node, redirect to UserMainActivity
                    Intent intent = new Intent(getApplicationContext(), UserMainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // If email doesn't exist in Users node, check in Authenticator node
                    authsRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // If email exists in Authenticator node, redirect to AuthenticatorMainActivity
                                Intent intent = new Intent(getApplicationContext(), AuthenticatorMainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // If email doesn't exist in either node
                                Toast.makeText(SplashActivity.this, "User type not recognized.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(SplashActivity.this, "Database error.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Database error: " + error.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SplashActivity.this, "Database error.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
