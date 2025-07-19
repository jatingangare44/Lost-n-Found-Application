package com.kundevahal.lostnfound;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class AcceptedItemAdapter extends RecyclerView.Adapter<AcceptedItemAdapter.ViewHolder> {

    private List<Item> acceptedItems;
    private OnItemClickListener listener;

    public AcceptedItemAdapter(List<Item> acceptedItems, OnItemClickListener listener) {
        this.acceptedItems = acceptedItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_accepted, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = acceptedItems.get(position);
        holder.itemNameTextView.setText(item.getName());
        holder.itemLocationTextView.setText(item.getLocation());
        holder.itemDateTextView.setText(item.getDateTime());

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(item.getItemImageUrl())
                .into(holder.itemImageView);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            listener.onItemClick(item);
            listener.hideRecyclerView(); // Hide the RecyclerView on item click
        });
    }

    @Override
    public int getItemCount() {
        return acceptedItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView, itemLocationTextView, itemDateTextView;
        ImageView itemImageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.tvItemName);
            itemLocationTextView = itemView.findViewById(R.id.tvItemLocation);
            itemDateTextView = itemView.findViewById(R.id.tvItemDateTime);
            itemImageView = itemView.findViewById(R.id.imgItemImage);
        }
    }

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(Item item);
        void hideRecyclerView();  // New method to hide the RecyclerView
    }
}
