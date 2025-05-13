package com.example.mindhaven;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Base activity for all recommendation activities (Books, Music, Movies)
 */
public abstract class BaseRecommendationActivity extends AppCompatActivity
        implements RecommendationAdapter.OnFavoriteClickListener {

    private static final String TAG = "BaseRecommendation";

    protected RecyclerView recyclerRecommendations;
    protected RecommendationAdapter adapter;
    protected ProgressBar loadingIndicator;
    protected ChipGroup moodChipGroup;
    protected Button shuffleButton;

    protected List<Recommendation> recommendations = new ArrayList<>();
    protected List<String> selectedMoods = new ArrayList<>();
    protected RecommendationService recommendationService;
    protected FirestoreService firestoreService;
    protected FirebaseUser currentUser;

    // Abstract methods to be implemented by subclasses
    protected abstract int getLayoutResourceId();
    protected abstract String getRecommendationType();
    protected abstract void loadRecommendationsForMoods(List<String> moods);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestoreService = new FirestoreService();
        recommendationService = new RecommendationService(this);

        Log.d(TAG, "Activity created: " + getClass().getSimpleName());

        // Initialize views
        recyclerRecommendations = findViewById(R.id.recycler_recommendations);
        loadingIndicator = findViewById(R.id.loading_indicator);
        moodChipGroup = findViewById(R.id.mood_chip_group);
        shuffleButton = findViewById(R.id.button_shuffle);

        // Log view initialization
        Log.d(TAG, "RecyclerView is " + (recyclerRecommendations != null ? "found" : "null"));
        Log.d(TAG, "LoadingIndicator is " + (loadingIndicator != null ? "found" : "null"));
        Log.d(TAG, "ChipGroup is " + (moodChipGroup != null ? "found" : "null"));
        Log.d(TAG, "ShuffleButton is " + (shuffleButton != null ? "found" : "null"));

        // Set up RecyclerView if it exists
        if (recyclerRecommendations != null) {
            recyclerRecommendations.setLayoutManager(new LinearLayoutManager(this));
            adapter = new RecommendationAdapter(this, recommendations, this);
            recyclerRecommendations.setAdapter(adapter);
            Log.d(TAG, "RecyclerView setup complete");
        } else {
            Log.e(TAG, "RecyclerView not found in layout");
        }

        // Set up shuffle button if it exists
        if (shuffleButton != null) {
            shuffleButton.setOnClickListener(v -> {
                if (adapter != null) {
                    adapter.shuffleRecommendations();
                    Toast.makeText(this, "Recommendations shuffled", Toast.LENGTH_SHORT).show();
                }
            });
            Log.d(TAG, "Shuffle button setup complete");
        } else {
            Log.e(TAG, "Shuffle button not found in layout");
        }

        // Setup mood chips
        setupMoodChips();
    }

    /**
     * Set up mood selection chips
     */
    protected void setupMoodChips() {
        // Make sure the chipGroup exists before trying to use it
        if (moodChipGroup == null) {
            Log.e(TAG, "ChipGroup is null, cannot setup mood chips");
            return;
        }

        String[] moods = {"Happy", "Sad", "Excited", "Anxious", "Angry", "Bored",
                "Nostalgic", "Hopeful", "Stressed", "Curious"};

        Log.d(TAG, "Setting up " + moods.length + " mood chips");

        for (String mood : moods) {
            Chip chip = new Chip(this);
            chip.setText(mood);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);

            // Set chip styling
            try {
                chip.setChipBackgroundColorResource(R.color.chip_background_selector);
            } catch (Exception e) {
                Log.e(TAG, "Error setting chip background: " + e.getMessage());
                // If the selector doesn't exist, use default styling
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedMoods.add(mood.toLowerCase());
                    Log.d(TAG, "Mood selected: " + mood.toLowerCase());
                } else {
                    selectedMoods.remove(mood.toLowerCase());
                    Log.d(TAG, "Mood deselected: " + mood.toLowerCase());
                }

                if (!selectedMoods.isEmpty()) {
                    Log.d(TAG, "Loading recommendations for moods: " + selectedMoods);
                    loadRecommendationsForMoods(selectedMoods);
                } else {
                    Log.d(TAG, "No moods selected, clearing recommendations");
                    // Clear recommendations if no mood selected
                    recommendations.clear();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            });

            moodChipGroup.addView(chip);
        }

        Log.d(TAG, "Mood chips setup complete");
    }

    /**
     * Show or hide loading indicator
     */
    protected void showLoading(boolean isLoading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            Log.d(TAG, "Loading indicator " + (isLoading ? "shown" : "hidden"));
        } else {
            Log.e(TAG, "Loading indicator is null");
        }

        if (recyclerRecommendations != null) {
            recyclerRecommendations.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            Log.d(TAG, "RecyclerView " + (isLoading ? "hidden" : "shown"));
        } else {
            Log.e(TAG, "RecyclerView is null");
        }
    }

    /**
     * Handle favorite button click
     */
    @Override
    public void onFavoriteClick(Recommendation recommendation, boolean isFavorite) {
        Log.d(TAG, "Favorite clicked: " + recommendation.getTitle() + ", isFavorite: " + isFavorite);

        if (isFavorite) {
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        }
    }
}