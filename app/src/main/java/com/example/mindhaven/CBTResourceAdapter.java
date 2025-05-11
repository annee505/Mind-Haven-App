package com.example.mindhaven;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CBTResourceAdapter extends RecyclerView.Adapter<CBTResourceAdapter.ViewHolder> {
    private List<CBTWorksheet> worksheets;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(CBTWorksheet worksheet);
    }

    public CBTResourceAdapter(List<CBTWorksheet> worksheets, OnItemClickListener listener) {
        this.worksheets = worksheets;
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cbt_resource, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CBTWorksheet worksheet = worksheets.get(position);
        holder.titleText.setText(worksheet.getTitle());
        holder.descriptionText.setText(worksheet.getDescription());

        // Set up the click listener
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(worksheet);
            }
        });
    }

    @Override
    public int getItemCount() {
        return worksheets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView descriptionText;

        ViewHolder(View view) {
            super(view);
            titleText = view.findViewById(R.id.textTitle);
            descriptionText = view.findViewById(R.id.textDescription);
        }
    }
}