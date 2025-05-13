package com.example.mindhaven;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
public class FavoritesActivity extends AppCompatActivity implements FavoritesAdapter.OnFavoriteClickListener {
    private static final String TAG = "FavoritesDebug";

    private RecyclerView recyclerFavorites;
    private ProgressBar loadingIndicator;
    private View emptyStateView;
    private FavoritesAdapter adapter;
    private FirestoreService firestoreService;
    private List<Recommendation> favorites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Debug: Check authentication status
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User is authenticated: " + currentUser.getUid());
        } else {
            Log.e(TAG, "NO USER AUTHENTICATED in FavoritesActivity!");
        }

        // Initialize views
        recyclerFavorites = findViewById(R.id.recycler_favorites);
        loadingIndicator = findViewById(R.id.loading_indicator);
        emptyStateView = findViewById(R.id.empty_text);

        // Debug: Check if views were found
        Log.d(TAG, "recyclerFavorites is " + (recyclerFavorites != null ? "found" : "null"));
        Log.d(TAG, "loadingIndicator is " + (loadingIndicator != null ? "found" : "null"));
        Log.d(TAG, "emptyStateView is " + (emptyStateView != null ? "found" : "null"));

        // Initialize Firestore service
        firestoreService = new FirestoreService();

        // Set up chips
        ChipGroup filterChipGroup = findViewById(R.id.filter_chip_group);
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_books) {
                currentType = "book";
            } else if (checkedId == R.id.chip_music) {
                currentType = "music";
            } else if (checkedId == R.id.chip_movies) {
                currentType = "movie";
            } else {
                currentType = null;
            }
            loadFavorites();
        });

        // Initialize adapter and set up RecyclerView
        adapter = new FavoritesAdapter(this, favorites, this);
        recyclerFavorites.setLayoutManager(new LinearLayoutManager(this));
        recyclerFavorites.setAdapter(adapter);

        // Show loading state and load favorites
        showLoading(true);
        loadFavorites();
    }

    private String currentType = null; // Track current filter type

    private void loadFavorites() {
        Log.d(TAG, "Starting to load favorites with type: " + currentType);
        showLoading(true);
        showEmptyState(false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "No authenticated user found");
            showLoading(false);
            showEmptyState(true);
            return;
        }

        firestoreService.getFavorites(currentType, new FirestoreService.FirestoreCallback<List<Recommendation>>() {
            @Override
            public void onSuccess(List<Recommendation> result) {
                Log.d(TAG, "Favorites loaded successfully. Count: " + (result != null ? result.size() : 0));

                runOnUiThread(() -> {
                    showLoading(false);

                    // Clear existing items
                    favorites.clear();

                    if (result != null && !result.isEmpty()) {
                        favorites.addAll(result);

                        // Debug: Print each favorite
                        for (Recommendation rec : result) {
                            Log.d(TAG, "Loaded favorite: " + rec.getTitle() + " (Type: " + rec.getType() + ")");
                        }

                        adapter.notifyDataSetChanged();
                        showEmptyState(false);
                        recyclerFavorites.setVisibility(View.VISIBLE);

                        Log.d(TAG, "Updated adapter with " + favorites.size() + " favorites");
                    } else {
                        adapter.notifyDataSetChanged();
                        showEmptyState(true);
                        Log.d(TAG, "No favorites found, showing empty state");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading favorites: " + e.getMessage(), e);

                runOnUiThread(() -> {
                    showLoading(false);

                    // Update empty state text to show error
                    TextView emptyText = findViewById(R.id.empty_text);
                    if (emptyText != null) {
                        emptyText.setText("Error loading favorites. Please try again.");
                    }
                    showEmptyState(true);

                    // Show more detailed error message to user
                    String errorMsg = e.getMessage();
                    if (errorMsg == null || errorMsg.isEmpty()) {
                        errorMsg = "Failed to connect to database";
                    }
                    Toast.makeText(FavoritesActivity.this,
                            "Error loading favorites: " + errorMsg,
                            Toast.LENGTH_LONG).show();

                    Log.e(TAG, "Full error details:", e);
                });
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (recyclerFavorites != null) {
            recyclerFavorites.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }

        Log.d(TAG, "Loading indicator " + (isLoading ? "shown" : "hidden"));
    }

    private void showEmptyState(boolean isEmpty) {
        if (emptyStateView != null) {
            emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (recyclerFavorites != null) {
            recyclerFavorites.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }

        Log.d(TAG, "Empty state " + (isEmpty ? "shown" : "hidden"));
    }

    @Override
    public void onFavoriteClick(Recommendation recommendation, int position) {
        Log.d(TAG, "Favorite click at position " + position + ": " + recommendation.getTitle());

        // Remove from favorites
        firestoreService.removeFavorite(recommendation.getId(), new FirestoreService.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Successfully removed from favorites: " + recommendation.getTitle());

                // Remove from local list
                favorites.remove(position);
                adapter.notifyItemRemoved(position);

                // Show empty state if no favorites left
                if (favorites.isEmpty()) {
                    showEmptyState(true);
                }

                Toast.makeText(FavoritesActivity.this,
                        "Removed from favorites",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error removing from favorites: " + e.getMessage(), e);

                Toast.makeText(FavoritesActivity.this,
                        "Error removing from favorites: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}