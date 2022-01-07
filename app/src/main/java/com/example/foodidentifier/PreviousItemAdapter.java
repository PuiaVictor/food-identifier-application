package com.example.foodidentifier;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PreviousItemAdapter extends RecyclerView.Adapter<PreviousItemAdapter.PreviousItemHolder> {

    Context context;

    List<PreviousItem> previousItemsList = new ArrayList<>();

    public PreviousItemAdapter(Context context, List<PreviousItem> previousItemsList){
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
        PreviousItem previousItem = previousItemsList.get(position);
        holder.itemName.setText(previousItem.getPreviousItemName());
        holder.confidence.setText(previousItem.getPreviousItemConfidence());
    }

    @Override
    public int getItemCount() {
        return previousItemsList.size();
    }

    public static class PreviousItemHolder extends RecyclerView.ViewHolder{

        TextView itemName, confidence;

        public PreviousItemHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.previousItemName);
            confidence = itemView.findViewById(R.id.previousItemConfidence);
        }
    }
}
