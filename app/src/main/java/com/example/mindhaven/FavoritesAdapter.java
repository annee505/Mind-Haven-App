package com.example.mindhaven;

import android.content.Context;
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

/**
 * Adapter for displaying favorites in a RecyclerView
 */
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    private List<Recommendation> favorites;
    private Context context;
    private OnFavoriteClickListener favoriteClickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Recommendation recommendation, int position);
    }

    public FavoritesAdapter(Context context, List<Recommendation> favorites, OnFavoriteClickListener listener) {
        this.context = context;
        this.favorites = favorites;
        this.favoriteClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recommendation favorite = favorites.get(position);

        holder.titleTextView.setText(favorite.getTitle());
        holder.descriptionTextView.setText(favorite.getDescription());
        holder.moodTextView.setText("Mood: " + favorite.getMood());

        // Set date if available
        if (favorite.getDateAdded() != null) {
            holder.dateTextView.setText("Added: " + dateFormat.format(favorite.getDateAdded()));
            holder.dateTextView.setVisibility(View.VISIBLE);
        } else {
            holder.dateTextView.setVisibility(View.GONE);
        }

        // Set icon based on type
        if ("book".equals(favorite.getType())) {
            holder.typeIcon.setImageResource(R.drawable.ic_book);
        } else if ("music".equals(favorite.getType())) {
            holder.typeIcon.setImageResource(R.drawable.ic_music);
        } else if ("movie".equals(favorite.getType())) {
            holder.typeIcon.setImageResource(R.drawable.ic_movie);
        }

        // Set up remove button
        holder.removeButton.setOnClickListener(v -> {
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(favorite, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    /**
     * ViewHolder for favorite items
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView moodTextView;
        TextView dateTextView;
        ImageView typeIcon;
        ImageButton removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_favorite_title);
            descriptionTextView = itemView.findViewById(R.id.text_favorite_description);
            moodTextView = itemView.findViewById(R.id.text_favorite_mood);
            dateTextView = itemView.findViewById(R.id.text_favorite_date);
            typeIcon = itemView.findViewById(R.id.image_type_icon);
            removeButton = itemView.findViewById(R.id.button_remove);
        }
    }
}
