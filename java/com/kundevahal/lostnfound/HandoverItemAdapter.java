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

import java.util.ArrayList;

public class HandoverItemAdapter extends RecyclerView.Adapter<HandoverItemAdapter.HandoverViewHolder> {

    private ArrayList<HandoverItem> handoverItems;
    private Context context;

    public HandoverItemAdapter(ArrayList<HandoverItem> handoverItems, Context context) {
        this.handoverItems = handoverItems;
        this.context = context;
    }

    @NonNull
    @Override
    public HandoverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.handover_item_layout, parent, false);
        return new HandoverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HandoverViewHolder holder, int position) {
        HandoverItem item = handoverItems.get(position);
        holder.itemName.setText(item.getItemName());
        holder.userName.setText(item.getUserName());
        holder.userPhone.setText(item.getUserPhone());

        // Load photo using Glide
        Glide.with(context).load(item.getPhotoUrl()).into(holder.itemPhoto);
    }

    @Override
    public int getItemCount() {
        return handoverItems.size();
    }

    public static class HandoverViewHolder extends RecyclerView.ViewHolder {
        ImageView itemPhoto;
        TextView itemName, userName, userPhone;

        public HandoverViewHolder(@NonNull View itemView) {
            super(itemView);
            itemPhoto = itemView.findViewById(R.id.imageViewItemPhoto);
            itemName = itemView.findViewById(R.id.textViewItemName);
            userName = itemView.findViewById(R.id.textViewUserName);
            userPhone = itemView.findViewById(R.id.textViewUserPhone);
        }
    }
}

