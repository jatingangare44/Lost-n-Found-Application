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

public class AcceptedRequestFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAcceptedItemsAdapter acceptedItemsAdapter;
    private List<AcceptedItem> acceptedItems = new ArrayList<>();
    private List<AcceptedItem> originalList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private String currentUserEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accepted_request, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewAcceptedItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        acceptedItemsAdapter = new UserAcceptedItemsAdapter(acceptedItems, getContext());
        recyclerView.setAdapter(acceptedItemsAdapter);

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth
        loadItems(); // Load accepted items for the logged-in user

        return view;
    }

    private void loadItems() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin(); // Redirect to login if no user is logged in
            return;
        }

        currentUserEmail = currentUser.getEmail(); // Get the email of the current user

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("accepted_items");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                acceptedItems.clear();
                originalList.clear(); // Assuming you want to keep an original list of items

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    AcceptedItem item = dataSnapshot.getValue(AcceptedItem.class);
                    if (item != null && currentUserEmail != null && currentUserEmail.equals(item.getUserEmail())) {
                        // Only add items that belong to the logged-in user
                        acceptedItems.add(item);
                        originalList.add(item); // Maintain the original list if needed
                    }
                }
                acceptedItemsAdapter.notifyDataSetChanged(); // Notify adapter about data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase Error", error.getMessage());
                Toast.makeText(getContext(), "Failed to load accepted items.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToLogin() {
        // Your login redirection logic here
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }
}
