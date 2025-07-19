package com.kundevahal.lostnfound;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private Context context;
    private ArrayList<Item> arrayList;
    private FirebaseAuth auth;

    public ItemAdapter(Context context, ArrayList<Item> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.listed_item, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = arrayList.get(position);
        holder.itemname.setText(item.getName());
        holder.location.setText(item.getLocation());
        holder.datetime.setText(item.getDateTime());

        // Load image using Glide
        Glide.with(context)
                .load(item.getItemImageUrl())
                .placeholder(R.drawable.baseline_image_24) // Optional placeholder image
                .into(holder.itemImage);

        String currentUserEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;

        // Check if the current user is the owner of the item
        boolean isCurrentUserOwner = currentUserEmail != null && currentUserEmail.equals(item.getUserEmail());

        if (isCurrentUserOwner) {
            // Enable delete functionality for the owner
            holder.itemView.setOnClickListener(v -> showDeleteConfirmationDialog(item));
        } else if (currentUserEmail != null) {
            // Check if the current user is an authenticator
            checkIfAuthenticator(currentUserEmail, holder, item);
        } else {
            // Disable click action for other users
            holder.itemView.setOnClickListener(null);
        }
    }

    // Method to check if the current user is an authenticator
    private void checkIfAuthenticator(String email, ItemViewHolder holder, Item item) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        databaseRef.child("Authenticator").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // The user is an authenticator
                            holder.itemView.setOnClickListener(v -> {
                                // Hide views in the main activity
                                if (context instanceof AuthenticatorMainActivity) {
                                    ((AuthenticatorMainActivity) context).hideViews();
                                }

                                // Replace the fragment in the current activity
                                if (context instanceof FragmentActivity) {
                                    FragmentActivity activity = (FragmentActivity) context;
                                    activity.getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragment_container, VerifyClaimFragment.newInstance(item))
                                            .addToBackStack(null) // Optional: Add this transaction to the back stack
                                            .commit();
                                }
                            });
                        } else {
                            // The user is not an authenticator, disable click action
                            holder.itemView.setOnClickListener(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle possible errors.
                    }
                });
    }

    private void showDeleteConfirmationDialog(final Item item) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(item);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteItem(Item item) {
        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("lost_items").child(item.getItemId());
        itemRef.removeValue();
        arrayList.remove(item);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemname, location, datetime;
        ImageView itemImage;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemname = itemView.findViewById(R.id.tvitemname);
            location = itemView.findViewById(R.id.tvlocation);
            datetime = itemView.findViewById(R.id.tvdatetime);
            itemImage = itemView.findViewById(R.id.item_image);
        }
    }
}
