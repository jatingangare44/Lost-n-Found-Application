package com.kundevahal.lostnfound;

import static android.app.PendingIntent.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.kundevahal.lostnfound.databinding.ActivityUserMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserMainActivity extends AppCompatActivity {

    ActivityUserMainBinding binding;
    FirebaseUser user;
    FirebaseAuth auth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new HomeFragment());


        databaseReference = FirebaseDatabase.getInstance().getReference("accepted_items");

//        FirebaseMessaging.getInstance().subscribeToTopic("notification");
        listenForNewAcceptedItems();


        // Set up the BottomNavigationView
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.user_home) {
                replaceFragment(new HomeFragment());
            }else if (itemId == R.id.user_got_item) {
                replaceFragment(new GotItemFragment());
            } else if (itemId == R.id.user_accepted_request) {
                replaceFragment(new AcceptedRequestFragment());
            }else {
                return false;
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
    private void listenForNewAcceptedItems() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        final String currentUserEmail = user.getEmail(); // Get the email of the current user
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                // A new lost item has been added
                String itemName = snapshot.child("name").getValue(String.class);
                //String location = snapshot.child("location").getValue(String.class);
                String dateTime = snapshot.child("dateTime").getValue(String.class);

                // Send a notification with the lost item details
                String message = itemName + " on " + dateTime;
                NotificationHelper notificationHelper = new NotificationHelper();
                notificationHelper.sendNotificationToUsers(UserMainActivity.this,currentUserEmail, "Item Accepted ", message);
                //notificationHelper.listenForNewItemAdded(getActivity());
//                "Your item has been successfully added."
//                NotificationHelper.sendNotification(AddItemFragment.this, "Lost Item Submitted", message);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Handle item updates, if needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

}
