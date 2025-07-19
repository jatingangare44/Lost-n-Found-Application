package com.kundevahal.lostnfound;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class AcceptedItemFragment extends Fragment implements AcceptedItemAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private AcceptedItemAdapter adapter;
    private ArrayList<Item> acceptedItems;
    private DatabaseReference databaseReference;
    private ImageButton backButton; // Added ImageButton reference

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accepted_item, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewAcceptedItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        acceptedItems = new ArrayList<>();
        adapter = new AcceptedItemAdapter(acceptedItems, this);
        recyclerView.setAdapter(adapter);  // Attach the adapter immediately

        backButton = view.findViewById(R.id.btnBackArrow); // Initialize the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to AuthenticatorMainActivity using Intent
                Intent intent = new Intent(getActivity(), AuthenticatorMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish(); // Finish the current activity
            }
        });

        loadAcceptedItems();

        // Handle the back press behavior
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate back to AuthenticatorMainActivity using Intent
                Intent intent = new Intent(getActivity(), AuthenticatorMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish(); // Finish the current activity
            }
        });

        return view;
    }

    private void loadAcceptedItems() {
        databaseReference = FirebaseDatabase.getInstance().getReference("accepted_items");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                acceptedItems.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Item item = dataSnapshot.getValue(Item.class);
                    acceptedItems.add(item);
                }
                adapter.notifyDataSetChanged(); // Notify the adapter about data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }

    @Override
    public void onItemClick(Item item) {
        // Navigate to the handover form fragment with item information
        HandoverFormFragment handoverFormFragment = HandoverFormFragment.newInstance(item);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, handoverFormFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void hideRecyclerView() {
        recyclerView.setVisibility(View.GONE); // Hide the RecyclerView
    }
}
