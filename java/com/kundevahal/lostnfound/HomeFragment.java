package com.kundevahal.lostnfound;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    DatabaseReference databaseReference,lostItemsRef;
    ArrayList<Item> list;
    ArrayList<Item> originalList;
    ItemAdapter itemAdapter;
    private FloatingActionButton fab;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        fab = view.findViewById(R.id.fab);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        recyclerView = view.findViewById(R.id.all_item_list);
        databaseReference = FirebaseDatabase.getInstance().getReference("lost_items");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        list = new ArrayList<>();
        originalList = new ArrayList<>();
        itemAdapter = new ItemAdapter(getContext(), list);
        recyclerView.setAdapter(itemAdapter);

        // Initialize Firebase Database reference
//        lostItemsRef = FirebaseDatabase.getInstance().getReference("lost_items");

        // Check if there is a logged-in user
        if (currentUser == null) {
            // No user is logged in, redirect to LoginActivity
            redirectToLogin();
        }

        // Set up search functionality
        SearchView searchView = view.findViewById(R.id.user_search_view);
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

        // Set up account circle button click listener
        ImageButton accountCircleButton = view.findViewById(R.id.account_circle_button);
        accountCircleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to ProfileFragment
                Fragment profileFragment = new ProfileFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, profileFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(100);
                    }
                }
                openAddItemFragment();
            }
        });

        ImageButton notificationButton = view.findViewById(R.id.notification_button);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), UserNotificationActivity.class));
            }
        });

        return view;
    }

    private void loadItems() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }
        final String currentUserEmail = currentUser.getEmail(); // Get the email of the current user

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                originalList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Item item = dataSnapshot.getValue(Item.class);
                    if (item != null && currentUserEmail != null && currentUserEmail.equals(item.getUserEmail())) {
                        // Only add items that belong to the logged-in user
                        list.add(item);
                        originalList.add(item);
                    }
                }
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase Error", error.getMessage());
                Toast.makeText(getContext(), "Failed to load items.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filter(String query) {
        ArrayList<Item> filteredList = new ArrayList<>();

        for (Item item : originalList) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(getContext(), "No item found", Toast.LENGTH_SHORT).show();
        }


        list.clear();
        list.addAll(filteredList);
        itemAdapter.notifyDataSetChanged();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void openAddItemFragment() {
        AddItemFragment addItemFragment = new AddItemFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, addItemFragment); // Ensure this ID is correct
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
