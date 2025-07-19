package com.kundevahal.lostnfound;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class UserAcceptedItemsAdapter extends RecyclerView.Adapter<UserAcceptedItemsAdapter.ViewHolder> {

    private List<AcceptedItem> acceptedItems;
    private Context context;

    public UserAcceptedItemsAdapter(List<AcceptedItem> acceptedItems, Context context) {
        this.acceptedItems = acceptedItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_accepted_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AcceptedItem item = acceptedItems.get(position);
        holder.textViewItemName.setText(item.getName());
        holder.textViewLocation.setText(item.getLocation());
        holder.textViewDateTime.setText(item.getDateTime());

        // Load the item image using Glide
        Glide.with(context).load(item.getItemImageUrl()).into(holder.imageViewItem);
    }

    @Override
    public int getItemCount() {
        return acceptedItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewItem;
        public TextView textViewItemName;
        public TextView textViewLocation;
        public TextView textViewDateTime;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
        }
    }
}
