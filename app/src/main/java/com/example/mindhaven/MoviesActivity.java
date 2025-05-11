package com.example.mindhaven;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class MoviesActivity extends AppCompatActivity implements RecommendationAdapter.OnFavoriteClickListener {

    private ChipGroup moodGroup;
    private ChipGroup moodGroupMore;
    private Button getRecommendationsButton;
    private RecyclerView recyclerView;
    private ProgressBar loadingIndicator;
    private RecommendationAdapter adapter;
    private RecommendationService recommendationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        // Initialize UI components
        moodGroup = findViewById(R.id.mood_group);
        moodGroupMore = findViewById(R.id.mood_group_more);
        getRecommendationsButton = findViewById(R.id.button_get_recommendations);
        recyclerView = findViewById(R.id.recycler_recommendations);
        loadingIndicator = findViewById(R.id.loading_indicator);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecommendationAdapter(this, new ArrayList<Recommendation>(), this);
        recyclerView.setAdapter(adapter);

        // Initialize recommendation service
        recommendationService = new RecommendationService(this);

        // Set up click listener for the button
        getRecommendationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMovieRecommendations();
            }
        });

        // Add shuffle button
        Button shuffleButton = findViewById(R.id.button_shuffle);
        shuffleButton.setOnClickListener(v -> adapter.shuffleRecommendations());
    }

    /**
     * Get movie recommendations based on selected moods
     */
    private void getMovieRecommendations() {
        // Get all selected moods from both chip groups
        List<String> selectedMoods = new ArrayList<>();

        // Check chips in the first group
        for (int i = 0; i < moodGroup.getChildCount(); i++) {
            Chip chip = (Chip) moodGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedMoods.add(chip.getText().toString());
            }
        }

        // Check chips in the second group
        for (int i = 0; i < moodGroupMore.getChildCount(); i++) {
            Chip chip = (Chip) moodGroupMore.getChildAt(i);
            if (chip.isChecked()) {
                selectedMoods.add(chip.getText().toString());
            }
        }

        // Validate selection
        if (selectedMoods.isEmpty()) {
            Toast.makeText(this, "Please select at least one mood", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        loadingIndicator.setVisibility(View.VISIBLE);

        // Get recommendations based on multiple moods
        recommendationService.getMovieRecommendations(selectedMoods, new RecommendationService.RecommendationCallback() {
            @Override
            public void onSuccess(List<Recommendation> recommendations) {
                // Hide loading indicator
                loadingIndicator.setVisibility(View.GONE);

                // Update RecyclerView with recommendations
                adapter.updateRecommendations(recommendations);

                // Scroll to the top of the recommendations
                recyclerView.smoothScrollToPosition(0);
            }

            @Override
            public void onError(String errorMessage) {
                // Hide loading indicator
                loadingIndicator.setVisibility(View.GONE);

                // Show error message
                Toast.makeText(MoviesActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFavoriteClick(Recommendation recommendation, boolean isFavorite) {
        if (isFavorite) {
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        }
    }
}