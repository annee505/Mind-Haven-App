package com.example.mindhaven;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Activity for book recommendations
 */
public class BooksActivity extends BaseRecommendationActivity {

    private static final String TAG = "BooksActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set activity title
        setTitle("Book Recommendations");

        // Select a default mood to show initial recommendations
        if (selectedMoods.isEmpty()) {
            selectedMoods.add("happy"); // Default mood
            Log.d(TAG, "Setting default mood: happy");
            loadRecommendationsForMoods(selectedMoods);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_books;
    }

    @Override
    protected String getRecommendationType() {
        return "book";
    }

    @Override
    protected void loadRecommendationsForMoods(List<String> moods) {
        showLoading(true);

        // Add debug logs
        Log.d(TAG, "Loading book recommendations for moods: " + moods);

        recommendationService.getBookRecommendations(moods, new RecommendationService.RecommendationCallback() {
            @Override
            public void onSuccess(List<Recommendation> result) {
                Log.d(TAG, "Received " + result.size() + " book recommendations");

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

                // Track user selections in Firestore for improved recommendations in the future
                if (currentUser != null && !result.isEmpty()) {
                    firestoreService.saveToHistory(result.get(0));
                    Log.d(TAG, "First recommendation saved to history");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading book recommendations: " + errorMessage);
                Toast.makeText(BooksActivity.this,
                        "Error: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        });
    }
}