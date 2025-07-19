package com.kundevahal.lostnfound;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GotItemFragment extends Fragment {

    private RecyclerView recyclerView;
    private GotItemAdapter gotItemAdapter;
    private List<HandoverItem> gotItems = new ArrayList<>();
    private FirebaseAuth mAuth;
    private String currentUserEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_got_item, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewGotItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        gotItemAdapter = new GotItemAdapter(gotItems, getContext());
        recyclerView.setAdapter(gotItemAdapter);
        mAuth = FirebaseAuth.getInstance();

        loadItems(); // Load items when the view is created

        return view;
    }

    private void loadItems() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin(); // Redirect to login if no user is logged in
            return;
        }

        currentUserEmail = currentUser.getEmail(); // Get the email of the current user

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("handover_records");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gotItems.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HandoverItem item = dataSnapshot.getValue(HandoverItem.class);
                    if (item != null && currentUserEmail != null && currentUserEmail.equals(item.getEmail())) {
                        // Only add items that belong to the logged-in user
                        gotItems.add(item);
                    }
                }
                gotItemAdapter.notifyDataSetChanged(); // Notify adapter about data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase Error", error.getMessage());
                Toast.makeText(getContext(), "Failed to load handed-over items.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the back stack
        startActivity(intent);
        getActivity().finish(); // Optionally finish the current activity
    }
}
