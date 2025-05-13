package com.example.mindhaven;

import android.os.Bundle;
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

/**
 * Activity to display user favorites
 */
public class FavoritesActivity extends AppCompatActivity implements FavoritesAdapter.OnFavoriteClickListener {

    private RecyclerView recyclerFavorites;
    private FavoritesAdapter adapter;
    private ProgressBar loadingIndicator;
    private TextView emptyText;
    private ChipGroup filterChipGroup;
    private Chip allChip, booksChip, musicChip, moviesChip;

    private FirestoreService firestoreService;
    private FirebaseUser currentUser;
    private List<Recommendation> favorites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestoreService = new FirestoreService();

        // Initialize views
        recyclerFavorites = findViewById(R.id.recycler_favorites);
        loadingIndicator = findViewById(R.id.loading_indicator);
        emptyText = findViewById(R.id.empty_text);
        filterChipGroup = findViewById(R.id.filter_chip_group);
        allChip = findViewById(R.id.chip_all);
        booksChip = findViewById(R.id.chip_books);
        musicChip = findViewById(R.id.chip_music);
        moviesChip = findViewById(R.id.chip_movies);

        // Set up RecyclerView
        recyclerFavorites.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FavoritesAdapter(this, favorites, this);
        recyclerFavorites.setAdapter(adapter);

        // Set up chip filters
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_all) {
                loadAllFavorites();
            } else if (checkedId == R.id.chip_books) {
                loadFavoritesByType("book");
            } else if (checkedId == R.id.chip_music) {
                loadFavoritesByType("music");
            } else if (checkedId == R.id.chip_movies) {
                loadFavoritesByType("movie");
            }
        });

        // Load all favorites by default
        loadAllFavorites();
    }

    /**
     * Load all favorites from Firestore
     */
    private void loadAllFavorites() {
        showLoading(true);

        firestoreService.getFavorites(new FirestoreService.FirestoreCallback<List<Recommendation>>() {
            @Override
            public void onSuccess(List<Recommendation> result) {
                showLoading(false);
                favorites.clear();
                favorites.addAll(result);
                adapter.notifyDataSetChanged();

                if (favorites.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                }
            }

            @Override
            public void onError(Exception e) {
                showLoading(false);
                showEmptyState(true);
                Toast.makeText(FavoritesActivity.this,
                        "Error loading favorites: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load favorites filtered by type
     */
    private void loadFavoritesByType(String type) {
        showLoading(true);

        firestoreService.getFavoritesByType(type, new FirestoreService.FirestoreCallback<List<Recommendation>>() {
            @Override
            public void onSuccess(List<Recommendation> result) {
                showLoading(false);
                favorites.clear();
                favorites.addAll(result);
                adapter.notifyDataSetChanged();

                if (favorites.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                }
            }

            @Override
            public void onError(Exception e) {
                showLoading(false);
                showEmptyState(true);
                Toast.makeText(FavoritesActivity.this,
                        "Error loading favorites: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show or hide loading indicator
     */
    private void showLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerFavorites.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    /**
     * Show or hide empty state
     */
    private void showEmptyState(boolean isEmpty) {
        emptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerFavorites.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * Handle unfavorite click from adapter
     */
    @Override
    public void onFavoriteClick(Recommendation recommendation, int position) {
        firestoreService.removeFavorite(recommendation.getId(), new FirestoreService.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                favorites.remove(position);
                adapter.notifyItemRemoved(position);

                if (favorites.isEmpty()) {
                    showEmptyState(true);
                }

                Toast.makeText(FavoritesActivity.this,
                        "Removed from favorites",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(FavoritesActivity.this,
                        "Error removing from favorites: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
