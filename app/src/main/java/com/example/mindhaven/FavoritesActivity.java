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

import com.google.android.material.chip.Chip;
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
        emptyStateView = findViewById(R.id.empty_state);

        // Debug: Check if views were found
        Log.d(TAG, "recyclerFavorites is " + (recyclerFavorites != null ? "found" : "null"));
        Log.d(TAG, "loadingIndicator is " + (loadingIndicator != null ? "found" : "null"));
        Log.d(TAG, "emptyStateView is " + (emptyStateView != null ? "found" : "null"));

        // Initialize Firestore service
        firestoreService = new FirestoreService();

        // Set up recycler view with LinearLayoutManager
        recyclerFavorites.setLayoutManager(new LinearLayoutManager(this));

        // Initialize adapter immediately with empty list (important)
        adapter = new FavoritesAdapter(this, favorites, this);
        recyclerFavorites.setAdapter(adapter);

        // Show loading state
        showLoading(true);

        // Load favorites
        loadFavorites();
    }

    private void loadFavorites() {
        Log.d(TAG, "Starting to load favorites");

        firestoreService.getFavorites(new FirestoreService.FirestoreCallback<List<Recommendation>>() {
            @Override
            public void onSuccess(List<Recommendation> result) {
                Log.d(TAG, "Favorites loaded successfully. Count: " + (result != null ? result.size() : 0));

                showLoading(false);

                if (result != null && !result.isEmpty()) {
                    // Clear existing items and add new ones
                    favorites.clear();
                    favorites.addAll(result);

                    // Debug: Print each favorite
                    for (Recommendation rec : result) {
                        Log.d(TAG, "Loaded favorite: " + rec.getTitle() + " (Type: " + rec.getType() + ")");
                    }

                    // Notify adapter of changes
                    adapter.notifyDataSetChanged();

                    // Hide empty state
                    showEmptyState(false);

                    Log.d(TAG, "Updated adapter with " + favorites.size() + " favorites");
                } else {
                    // Show empty state if no favorites found
                    showEmptyState(true);
                    Log.d(TAG, "No favorites found, showing empty state");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error loading favorites: " + e.getMessage(), e);

                showLoading(false);
                showEmptyState(true);

                // Show error message to user
                Toast.makeText(FavoritesActivity.this,
                        "Error loading favorites: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
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