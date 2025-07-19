package com.kundevahal.lostnfound;

import static java.util.Locale.filter;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ListedItemFragment extends Fragment {

    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    ItemAdapter itemAdapter;
    ArrayList<Item> list;
    ArrayList<Item> originalList; // Track original list for filtering
    FirebaseAuth auth;
    FirebaseUser user;
    private RelativeLayout mainLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listed_item, container, false);

        recyclerView = view.findViewById(R.id.alllisteditem);
        databaseReference = FirebaseDatabase.getInstance().getReference("items");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        originalList = new ArrayList<>(); // Initialize original list
        itemAdapter = new ItemAdapter(getContext(), list);
        recyclerView.setAdapter(itemAdapter);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppbar);
        DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawer_layout);
        NavigationView navigationView = requireActivity().findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0);
        TextView headerEmail = headerView.findViewById(R.id.email);
        mainLayout = view.findViewById(R.id.mainlayout);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        if (user != null) {
            headerEmail.setText(user.getEmail());
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                drawerLayout.closeDrawer(GravityCompat.START);
                if (id == R.id.nav_account) {
                    Intent intent = new Intent(requireContext(), AccountActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_home) {
                    Intent intent = new Intent(requireContext(), AuthenticatorMainActivity.class);
                    startActivity(intent);
                } else if (id == R.id.nav_acceptreq) {
                    showToast("Accept Request is clicked");
                } else if (id == R.id.nav_item_handed_over) {
                    showToast("Item Handed Over is clicked");
                } else if (id == R.id.nav_logout) {
                    logoutUser();
                }

                return true;
            }
        });

        // Set up search functionality
        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        loadItems();
        return view;
    }

    private void loadItems() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                originalList.clear(); // Clear original list before populating
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null) {
                        list.add(item);
                        originalList.add(item); // Add to original list as well
                    }
                }
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    private void filter(String query) {
        ArrayList<Item> filteredList = new ArrayList<>();

        for (Item item : originalList) {
            // Filter logic based on your item attributes (e.g., item name or description)
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }

        // Update RecyclerView with filtered list
        list.clear();
        list.addAll(filteredList);
        itemAdapter.notifyDataSetChanged();
    }

    private void hideViews() {
        mainLayout.findViewById(R.id.topAppbar).setVisibility(View.GONE);
        mainLayout.findViewById(R.id.imagegram).setVisibility(View.GONE);
        mainLayout.findViewById(R.id.itemlist).setVisibility(View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();

        // Redirect to login or home activity
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}