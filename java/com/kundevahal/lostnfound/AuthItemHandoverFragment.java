package com.kundevahal.lostnfound;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AuthItemHandoverFragment extends Fragment {

    private RecyclerView recyclerView;
    private HandoverItemAdapter adapter;
    private ArrayList<HandoverItem> handoverItems;
    private DatabaseReference databaseReference;
    private ImageButton backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auth_item_handover, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewHandoverItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        handoverItems = new ArrayList<>();
        adapter = new HandoverItemAdapter(handoverItems, getContext());
        recyclerView.setAdapter(adapter);

        backButton = view.findViewById(R.id.btnBackArrowHandover);
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        loadHandoverItems();

        return view;
    }

    private void loadHandoverItems() {
        databaseReference = FirebaseDatabase.getInstance().getReference("handover_records");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handoverItems.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    HandoverItem item = dataSnapshot.getValue(HandoverItem.class);
                    if (item != null) {
                        handoverItems.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }
}
