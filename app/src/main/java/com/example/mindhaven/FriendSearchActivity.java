package com.example.mindhaven;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FriendSearchActivity extends AppCompatActivity {
    private EditText searchInput;
    private Button searchButton;
    private ProgressBar loadingProgress;
    private TextView noResultsText;
    private RecyclerView resultsRecyclerView;
    private FirebaseService firebaseService;
    private FriendSearchAdapter searchAdapter;
    private MindHavenApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_search);

        // Set title
        setTitle("Find Friends");

        // Get application instance
        app = (MindHavenApplication) getApplicationContext();

        // Initialize Firebase
        firebaseService = new FirebaseService();
        firebaseService.initialize();

        // Initialize views
        searchInput = findViewById(R.id.search_input);
        searchButton = findViewById(R.id.search_button);
        loadingProgress = findViewById(R.id.loading_progress);
        noResultsText = findViewById(R.id.no_results_text);
        resultsRecyclerView = findViewById(R.id.results_recycler_view);

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        resultsRecyclerView.setLayoutManager(layoutManager);

        // Initialize adapter with empty list
        searchAdapter = new FriendSearchAdapter(new ArrayList<>(), this);
        searchAdapter.setOnFriendRequestClickListener(username -> sendFriendRequest(username));
        resultsRecyclerView.setAdapter(searchAdapter);

        // Set up search button
        searchButton.setOnClickListener(v -> {
            performSearch();
        });

        // Set up text change listener for search input
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Enable search button only if there's text
                searchButton.setEnabled(s.length() > 0);
            }
        });

        // Initially disable search button
        searchButton.setEnabled(false);
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            return;
        }

        // Show loading
        loadingProgress.setVisibility(View.VISIBLE);
        noResultsText.setVisibility(View.GONE);

        // Get current username
        String currentUsername = app.getUniqueUsername();

        // Search for users
        firebaseService.searchUsers(query, currentUsername, new FirebaseService.SearchResultsCallback() {
            @Override
            public void onSearchResults(List<String> results) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (results.isEmpty()) {
                        noResultsText.setText("No users found matching '" + query + "'");
                        noResultsText.setVisibility(View.VISIBLE);
                    } else {
                        noResultsText.setVisibility(View.GONE);
                    }

                    // Update adapter with results
                    searchAdapter.updateResults(results);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    noResultsText.setText("Error searching: " + errorMessage);
                    noResultsText.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void sendFriendRequest(String toUsername) {
        // Show loading
        loadingProgress.setVisibility(View.VISIBLE);

        // Get current username
        String fromUsername = app.getUniqueUsername();

        // Send friend request
        firebaseService.sendFriendRequest(fromUsername, toUsername, new FirebaseService.FriendRequestCallback() {
            @Override
            public void onResult(boolean success, String message) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (success) {
                        // Show toast
                        android.widget.Toast.makeText(FriendSearchActivity.this,
                                "Friend request sent to " + toUsername,
                                android.widget.Toast.LENGTH_SHORT).show();
                    } else {
                        // Show error
                        android.widget.Toast.makeText(FriendSearchActivity.this,
                                "Failed to send request: " + message,
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}