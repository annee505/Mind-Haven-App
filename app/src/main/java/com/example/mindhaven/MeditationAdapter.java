package com.example.mindhaven;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindhaven.models.MeditationAudio;

import java.util.List;

public class MeditationAdapter extends RecyclerView.Adapter<MeditationAdapter.ViewHolder> {
    private List<MeditationAudio> meditationAudios;
    private OnMeditationItemClickListener listener;
    private OnFavoriteClickListener favoriteListener;

    public interface OnMeditationItemClickListener {
        void onMeditationItemClick(MeditationAudio audio);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(MeditationAudio audio);
    }

    public MeditationAdapter(List<MeditationAudio> meditationAudios, OnMeditationItemClickListener listener) {
        this.meditationAudios = meditationAudios;
        this.listener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meditation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MeditationAudio audio = meditationAudios.get(position);
        holder.bind(audio);
    }

    @Override
    public int getItemCount() {
        return meditationAudios.size();
    }

    public void updateAudios(List<MeditationAudio> newAudios) {
        this.meditationAudios = newAudios;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageCategory;
        private final TextView textCategory;
        private final ImageView imageFavorite;
        private final TextView textTitle;
        private final TextView textDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCategory = itemView.findViewById(R.id.imageCategory);
            textCategory = itemView.findViewById(R.id.textCategory);
            imageFavorite = itemView.findViewById(R.id.imageFavorite);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDuration = itemView.findViewById(R.id.textDuration);
        }

        public void bind(final MeditationAudio audio) {
            textTitle.setText(audio.getTitle());
            textDuration.setText(audio.getDuration());
            textCategory.setText(audio.getCategory());

            // Set background color based on category
            String category = audio.getCategory();
            int backgroundColor = getCategoryColor(category);
            imageCategory.setBackgroundColor(backgroundColor);

            // Set favorite icon
            imageFavorite.setImageResource(
                    audio.isFavorite() ? R.drawable.ic_favorite : R.drawable.ic_favorite_border
            );

            // Set up click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMeditationItemClick(audio);
                }
            });

            imageFavorite.setOnClickListener(v -> {
                if (favoriteListener != null) {
                    favoriteListener.onFavoriteClick(audio);

                    // Update UI immediately (will be confirmed when DB update completes)
                    boolean newState = !audio.isFavorite();
                    audio.setFavorite(newState);
                    imageFavorite.setImageResource(
                            newState ? R.drawable.ic_favorite : R.drawable.ic_favorite_border
                    );
                }
            });
        }

        private int getCategoryColor(String category) {
            if (category == null) return Color.parseColor("#9575cd"); // Default purple

            switch (category.toLowerCase()) {
                case "sleep":
                    return Color.parseColor("#6b75db"); // Blue-purple
                case "stress":
                    return Color.parseColor("#66bb6a"); // Green
                case "anxiety":
                    return Color.parseColor("#ffa726"); // Orange
                case "focus":
                    return Color.parseColor("#42a5f5"); // Blue
                default:
                    return Color.parseColor("#9575cd"); // Default purple
            }
        }
    }
}