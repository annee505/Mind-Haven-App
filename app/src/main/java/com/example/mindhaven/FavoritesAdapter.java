package com.example.mindhaven;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    private static final String TAG = "FavoritesAdapter";
    private List<Recommendation> favorites;
    private Context context;
    private OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Recommendation recommendation, int position);
    }

    public FavoritesAdapter(Context context, List<Recommendation> favorites, OnFavoriteClickListener listener) {
        this.context = context;
        this.favorites = favorites;
        this.listener = listener;
        Log.d(TAG, "Adapter created with " + (favorites != null ? favorites.size() : 0) + " items");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            Recommendation favorite = favorites.get(position);
            Log.d(TAG, "Binding position " + position + ": " + favorite.getTitle());

            // Set the title
            holder.titleTextView.setText(favorite.getTitle());

            // Set the description (with null check)
            if (favorite.getDescription() != null && !favorite.getDescription().isEmpty()) {
                holder.descriptionTextView.setText(favorite.getDescription());
                holder.descriptionTextView.setVisibility(View.VISIBLE);
            } else {
                holder.descriptionTextView.setVisibility(View.GONE);
            }

            // Set the type icon based on the recommendation type
            if ("book".equals(favorite.getType())) {
                holder.typeImageView.setImageResource(R.drawable.ic_book);
            } else if ("music".equals(favorite.getType())) {
                holder.typeImageView.setImageResource(R.drawable.ic_music);
            } else if ("movie".equals(favorite.getType())) {
                holder.typeImageView.setImageResource(R.drawable.ic_movie);
            } else {
                // Default icon
                holder.typeImageView.setImageResource(R.drawable.ic_favorite);
            }

            // Set up click listener for the remove button
            holder.removeButton.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                    listener.onFavoriteClick(favorite, adapterPosition);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error binding view holder at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        int count = favorites != null ? favorites.size() : 0;
        Log.d(TAG, "getItemCount() = " + count);
        return count;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        ImageView typeImageView;
        ImageButton removeButton;

        ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_favorite_title);
            descriptionTextView = itemView.findViewById(R.id.text_favorite_description);
            typeImageView = itemView.findViewById(R.id.image_favorite_type);
            removeButton = itemView.findViewById(R.id.button_remove_favorite);
        }
    }
}