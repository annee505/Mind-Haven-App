package com.example.mindhaven;

import android.content.Context;
import android.util.Log;
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
 * with improved favorite functionality and animations
 */
public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {

    private static final String TAG = "RecommendationAdapter";

    private List<Recommendation> recommendations;
    private Context context;
    private OnFavoriteClickListener favoriteClickListener;
    private FirestoreService firestoreService;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Recommendation recommendation, boolean isFavorite);
    }

    public RecommendationAdapter(Context context, List<Recommendation> recommendations, OnFavoriteClickListener listener) {
        this.context = context;
        this.recommendations = recommendations;
        this.favoriteClickListener = listener;
        this.firestoreService = new FirestoreService();
        Log.d(TAG, "Adapter created with " + (recommendations != null ? recommendations.size() : 0) + " items");
    }

    public void shuffleRecommendations() {
        if (recommendations != null && !recommendations.isEmpty()) {
            Collections.shuffle(recommendations, new Random(System.currentTimeMillis()));
            notifyDataSetChanged();
            Log.d(TAG, "Recommendations shuffled");
        } else {
            Log.d(TAG, "No recommendations to shuffle");
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation, parent, false);
        Log.d(TAG, "Created view holder");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "Binding view at position " + position);

        Recommendation recommendation = recommendations.get(position);
        holder.titleTextView.setText(recommendation.getTitle());
        holder.descriptionTextView.setText(recommendation.getDescription());

        // Add mood label
        if (holder.moodTextView != null) {
            holder.moodTextView.setText("Based on: " + recommendation.getMood());
        }

        // Check with Firestore if this is a favorite
        firestoreService.checkIfFavorite(recommendation, new FirestoreService.FirestoreCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isFavorite) {
                recommendation.setFavorite(isFavorite);
                holder.favoriteButton.setImageResource(
                        isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border
                );
                Log.d(TAG, recommendation.getTitle() + " is " + (isFavorite ? "a favorite" : "not a favorite"));
            }

            @Override
            public void onError(Exception e) {
                // Default to not favorite
                holder.favoriteButton.setImageResource(R.drawable.ic_favorite_border);
                Log.e(TAG, "Error checking if favorite: " + e.getMessage());
            }
        });

        holder.favoriteButton.setOnClickListener(v -> {
            boolean newFavoriteState = !recommendation.isFavorite();
            recommendation.setFavorite(newFavoriteState);
            Log.d(TAG, "Favorite button clicked, new state: " + newFavoriteState);

            // Animate button
            holder.favoriteButton.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        holder.favoriteButton.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start();

                        holder.favoriteButton.setImageResource(
                                newFavoriteState ? R.drawable.ic_favorite : R.drawable.ic_favorite_border
                        );
                    }).start();

            // Update Firestore
            if (newFavoriteState) {
                firestoreService.saveFavorite(recommendation, new FirestoreService.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(TAG, "Recommendation saved to favorites: " + recommendation.getTitle());
                        // Save this recommendation to user history
                        firestoreService.saveToHistory(recommendation);
                    }

                    @Override
                    public void onError(Exception e) {
                        // If error, revert state
                        recommendation.setFavorite(false);
                        holder.favoriteButton.setImageResource(R.drawable.ic_favorite_border);
                        Log.e(TAG, "Error saving favorite: " + e.getMessage());
                    }
                });
            } else {
                firestoreService.removeFavorite(recommendation.getId(), new FirestoreService.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(TAG, "Recommendation removed from favorites: " + recommendation.getTitle());
                    }

                    @Override
                    public void onError(Exception e) {
                        // If error, revert state
                        recommendation.setFavorite(true);
                        holder.favoriteButton.setImageResource(R.drawable.ic_favorite);
                        Log.e(TAG, "Error removing favorite: " + e.getMessage());
                    }
                });
            }

            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(recommendation, newFavoriteState);
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = recommendations != null ? recommendations.size() : 0;
        Log.d(TAG, "Item count: " + count);
        return count;
    }

    /**
     * Update recommendations list with new data
     */
    public void updateRecommendations(List<Recommendation> newRecommendations) {
        Log.d(TAG, "Updating recommendations with " +
                (newRecommendations != null ? newRecommendations.size() : 0) + " items");

        if (newRecommendations == null) {
            return;
        }
        this.recommendations = newRecommendations;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for recommendation items
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView moodTextView;
        ImageButton favoriteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_recommendation_title);
            descriptionTextView = itemView.findViewById(R.id.text_recommendation_description);
            moodTextView = itemView.findViewById(R.id.text_recommendation_mood);
            favoriteButton = itemView.findViewById(R.id.button_favorite);
        }
    }
}