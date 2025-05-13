package com.example.mindhaven;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Activity for movie recommendations
 */
public class MoviesActivity extends BaseRecommendationActivity {

    private static final String TAG = "MoviesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set activity title
        setTitle("Movie Recommendations");

        // Select a default mood to show initial recommendations
        if (selectedMoods.isEmpty()) {
            selectedMoods.add("happy"); // Default mood
            Log.d(TAG, "Setting default mood: happy");
            loadRecommendationsForMoods(selectedMoods);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_movies;
    }

    @Override
    protected String getRecommendationType() {
        return "movie";
    }

    @Override
    protected void loadRecommendationsForMoods(List<String> moods) {
        showLoading(true);

        // Add debug logs
        Log.d(TAG, "Loading movie recommendations for moods: " + moods);

        recommendationService.getMovieRecommendations(moods, new RecommendationService.RecommendationCallback() {
            @Override
            public void onSuccess(List<Recommendation> result) {
                Log.d(TAG, "Received " + result.size() + " movie recommendations");

                recommendations.clear();
                recommendations.addAll(result);

                // Make sure adapter is not null
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Adapter notified of data change");
                } else {
                    Log.e(TAG, "Adapter is null!");
                }

                showLoading(false);

                // Track user selections in Firestore for improved recommendations
                if (currentUser != null && !result.isEmpty()) {
                    firestoreService.saveToHistory(result.get(0));
                    Log.d(TAG, "First recommendation saved to history");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading movie recommendations: " + errorMessage);
                Toast.makeText(MoviesActivity.this,
                        "Error: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        });
    }
}