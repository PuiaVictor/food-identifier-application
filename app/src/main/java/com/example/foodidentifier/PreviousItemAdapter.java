package com.example.foodidentifier;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PreviousItemAdapter extends RecyclerView.Adapter<PreviousItemAdapter.PreviousItemHolder> {

    Context context;

    List<DBItem> previousItemsList = new ArrayList<>();

    public PreviousItemAdapter(Context context, List<DBItem> previousItemsList) {
        this.context = context;
        this.previousItemsList = previousItemsList;
    }

    @NonNull
    @Override
    public PreviousItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recyclerview_row, parent, false);
        return new PreviousItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviousItemHolder holder, int position) {
        DBItem previousItem = previousItemsList.get(position);
        holder.itemName.setText(previousItem.getName());
        holder.confidence.setText(previousItem.getConfidence());
        Picasso.with(context)
                .load(previousItem.getImageUrl())
                .fit()
                .centerCrop()
                .into(holder.itemPicture);
    }

    @Override
    public int getItemCount() {
        return previousItemsList.size();
    }

    public static class PreviousItemHolder extends RecyclerView.ViewHolder {

        TextView itemName, confidence;
        ImageView itemPicture;

        public PreviousItemHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.previousItemName);
            confidence = itemView.findViewById(R.id.previousItemConfidence);
            itemPicture = itemView.findViewById(R.id.item_picture);
        }
    }
}
