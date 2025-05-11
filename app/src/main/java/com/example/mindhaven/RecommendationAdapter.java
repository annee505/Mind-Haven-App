package com.example.mindhaven;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Adapter for displaying recommendations in a RecyclerView
 */
public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private List<Recommendation> recommendations;
    private Context context;
    private OnFavoriteClickListener favoriteClickListener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Recommendation recommendation, boolean isFavorite);
    }

    public RecommendationAdapter(Context context, List<Recommendation> recommendations, OnFavoriteClickListener listener) {
        this.context = context;
        this.recommendations = recommendations;
        this.favoriteClickListener = listener;
    }

    public void shuffleRecommendations() {
        Collections.shuffle(recommendations, new Random(System.currentTimeMillis()));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recommendation recommendation = recommendations.get(position);
        holder.titleTextView.setText(recommendation.getTitle());
        holder.descriptionTextView.setText(recommendation.getDescription());

        holder.favoriteButton.setImageResource(
                recommendation.isFavorite() ?
                        R.drawable.ic_favorite :
                        R.drawable.ic_favorite_border
        );

        holder.favoriteButton.setOnClickListener(v -> {
            boolean newFavoriteState = !recommendation.isFavorite();
            recommendation.setFavorite(newFavoriteState);
            holder.favoriteButton.setImageResource(
                    newFavoriteState ?
                            R.drawable.ic_favorite :
                            R.drawable.ic_favorite_border
            );
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(recommendation, newFavoriteState);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recommendations.size();
    }

    /**
     * Update recommendations list with new data
     */
    public void updateRecommendations(List<Recommendation> newRecommendations) {
        this.recommendations = newRecommendations;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for recommendation items
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        ImageButton favoriteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_recommendation_title);
            descriptionTextView = itemView.findViewById(R.id.text_recommendation_description);
            favoriteButton = itemView.findViewById(R.id.button_favorite);
        }
    }
} 