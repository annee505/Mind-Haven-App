package com.example.mindhaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindhaven.models.MeditationAudio;
import com.example.mindhaven.ui.meditation.MeditationViewModel;

import java.util.ArrayList;
import java.util.List;

public class MeditationLibraryActivity extends AppCompatActivity implements MeditationAdapter.OnMeditationItemClickListener {

    private MeditationViewModel viewModel;
    private RecyclerView recyclerView;
    private MeditationAdapter adapter;
    private ProgressBar loadingProgress;
    private TextView emptyStateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation_library);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Meditation Library");
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MeditationViewModel.class);

        // Find views
        recyclerView = findViewById(R.id.recyclerLibraryMeditations);
        loadingProgress = findViewById(R.id.loadingProgressLibrary);
        emptyStateView = findViewById(R.id.emptyStateTextLibrary);

        // Set up RecyclerView
        setupRecyclerView();

        // Observe LiveData
        observeViewModel();

        // Load all meditations
        viewModel.refreshData();
    }

    private void setupRecyclerView() {
        int spanCount = getResources().getConfiguration().orientation ==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MeditationAdapter(new ArrayList<>(), this);
        adapter.setOnFavoriteClickListener(audio -> {
            viewModel.updateFavoriteStatus(audio);
        });

        recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe all meditations
        viewModel.getAllAudios().observe(this, this::updateMeditationsList);
    }

    private void updateMeditationsList(List<MeditationAudio> meditations) {
        if (meditations == null || meditations.isEmpty()) {
            showEmptyState();
            return;
        }

        hideEmptyState();
        adapter.updateAudios(meditations);
    }

    private void showLoading() {
        loadingProgress.setVisibility(View.VISIBLE);
        emptyStateView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingProgress.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        emptyStateView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyStateView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMeditationItemClick(MeditationAudio audio) {
        // Start meditation player activity
        Intent intent = new Intent(this, MeditationPlayerActivity.class);

        // Create a MeditationSession from MeditationAudio
        MeditationSession session = new MeditationSession(
                audio.getId(),
                audio.getTitle(),
                audio.getDescription() != null ? audio.getDescription() : "",
                audio.getDuration(),
                audio.getAudioUrl(),  // Get URL from audio resource ID
                "", // No image URL in MeditationAudio, leave empty
                audio.getCategory(),  // Pass the category
                audio.isFavorite()
        );

        // Add both for compatibility
        intent.putExtra("session", session);
        intent.putExtra("meditation_id", audio.getId());

        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}