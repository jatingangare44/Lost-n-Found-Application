package com.kundevahal.lostnfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class AuthenticatorMainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    private RelativeLayout mainLayout;

    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    ItemAdapter itemAdapter;
    ArrayList<Item> list;
    ArrayList<Item> originalList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator_main);

        auth = FirebaseAuth.getInstance();
        MaterialToolbar toolbar = findViewById(R.id.topAppbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        mainLayout = findViewById(R.id.main_layout);
        View headerView = navigationView.getHeaderView(0);
        TextView headerEmail = headerView.findViewById(R.id.email);
        recyclerView = findViewById(R.id.itemlist);
        databaseReference = FirebaseDatabase.getInstance().getReference("lost_items");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        originalList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, list);
        recyclerView.setAdapter(itemAdapter);

        NotificationHelper notificationHelper = new NotificationHelper();
        listenForNewLostItems();
        //notificationHelper.listenForNewItemAdded(this);


        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            headerEmail.setText(user.getEmail());
            loadAllItems();
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);
            if (id == R.id.nav_account) {
                startActivity(new Intent(getApplicationContext(), AccountActivity.class));
                finish();
            } else if (id == R.id.nav_home) {
                // Stay on the current activity
            } else if (id == R.id.nav_acceptreq) {
                goToAcceptedItemFragment();
            } else if (id == R.id.nav_item_handed_over) {
                hideViews();
                AuthItemHandoverFragment authItemHandoverFragment = new AuthItemHandoverFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, authItemHandoverFragment)
                        .addToBackStack(null)
                        .commit();
                showToast("Item Handed Over is clicked");
            } else if (id == R.id.nav_logout) {
                logoutUser();
            }
            return true;
        });

        // Search functionality
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<Item> filteredList = filter(newText);
                if (filteredList.isEmpty()) {
                    Toast.makeText(AuthenticatorMainActivity.this, "No item found", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        ImageButton NotificationButton = findViewById(R.id.notification_button_auth);
        NotificationButton.setOnClickListener(v -> startActivity(new Intent(AuthenticatorMainActivity.this, AuthenticatorNotificationActivity.class)));
    }

    private void loadAllItems() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                originalList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null) {
                        list.add(item);
                        originalList.add(item);
                    }
                }
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private ArrayList<Item> filter(String query) {
        ArrayList<Item> filteredList = new ArrayList<>();
        for (Item item : originalList) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        list.clear();
        list.addAll(filteredList);
        itemAdapter.notifyDataSetChanged();
        return filteredList;
    }

    private void goToAcceptedItemFragment() {
        hideViews();
        AcceptedItemFragment acceptedItemFragment = new AcceptedItemFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, acceptedItemFragment)
                .addToBackStack(null)
                .commit();
    }

    public void hideViews() {
        mainLayout.findViewById(R.id.topAppbar).setVisibility(View.GONE);
        mainLayout.findViewById(R.id.imagegram).setVisibility(View.GONE);
        mainLayout.findViewById(R.id.itemlist).setVisibility(View.GONE);
        mainLayout.findViewById(R.id.search_view).setVisibility(View.GONE);
    }

    private void showViews() {
        mainLayout.findViewById(R.id.topAppbar).setVisibility(View.VISIBLE);
        mainLayout.findViewById(R.id.imagegram).setVisibility(View.VISIBLE);
        mainLayout.findViewById(R.id.itemlist).setVisibility(View.VISIBLE);
        mainLayout.findViewById(R.id.search_view).setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

        // Check if the current fragment is AcceptedItemFragment
        if (currentFragment instanceof VerifyClaimFragment) {
            showViews();
            fragmentManager.popBackStack();
        } else if (currentFragment instanceof AuthItemHandoverFragment) {
            showViews();
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void listenForNewLostItems() {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                // A new lost item has been added
                String itemName = snapshot.child("name").getValue(String.class);
                String location = snapshot.child("location").getValue(String.class);
                String dateTime = snapshot.child("dateTime").getValue(String.class);

                // Send a notification with the lost item details
                String message = "New item reported: " + itemName + " at " + location + " on " + dateTime;
                NotificationHelper notificationHelper = new NotificationHelper();
                notificationHelper.sendNotificationToAuthenticators(AuthenticatorMainActivity.this, "New Item ", message);
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
