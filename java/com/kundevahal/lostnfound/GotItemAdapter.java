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

public class GotItemAdapter extends RecyclerView.Adapter<GotItemAdapter.ViewHolder> {

    private List<HandoverItem> itemList;
    private Context context;

    public GotItemAdapter(List<HandoverItem> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_handover_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HandoverItem item = itemList.get(position);
        holder.itemName.setText(item.getItemName());
        holder.location.setText(item.getLocation());
        holder.description.setText(item.getDescription());

        // Load the image using Glide or any image-loading library
        Glide.with(context)
                .load(item.getPhotoUrl())
                .into(holder.itemImage);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName, location, description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            location = itemView.findViewById(R.id.location);
            description = itemView.findViewById(R.id.description);
        }
    }
}
