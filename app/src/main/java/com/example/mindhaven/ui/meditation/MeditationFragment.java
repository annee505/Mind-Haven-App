package com.example.mindhaven.ui.meditation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mindhaven.MeditationAdapter;
import com.example.mindhaven.MeditationPlayerActivity;
import com.example.mindhaven.MeditationSession;
import com.example.mindhaven.R;
import com.example.mindhaven.models.MeditationAudio;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class MeditationFragment extends Fragment implements MeditationAdapter.OnMeditationItemClickListener {

    private MeditationViewModel viewModel;
    private RecyclerView recyclerView;
    private MeditationAdapter adapter;
    private ProgressBar loadingProgress;
    private TextView emptyStateView;
    private ChipGroup categoryChipGroup;
    private EditText searchEditText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CardView featuredMeditationCard;
    private TextView featuredTitle;
    private TextView featuredDuration;
    private MeditationAudio featuredMeditation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meditation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MeditationViewModel.class);

        // Find views
        recyclerView = view.findViewById(R.id.recyclerMeditations);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyStateView = view.findViewById(R.id.emptyStateText);
        categoryChipGroup = view.findViewById(R.id.category_chip_group);
        searchEditText = view.findViewById(R.id.search_meditation);
        // Initialize SwipeRefreshLayout with the correct ID
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        featuredMeditationCard = view.findViewById(R.id.featured_meditation_card);
        featuredTitle = view.findViewById(R.id.featured_title);
        featuredDuration = view.findViewById(R.id.featured_duration);

        // Set up RecyclerView
        setupRecyclerView();

        // Set up swipe refresh layout
        setupSwipeRefresh();

        // Set up featured meditation
        setupFeaturedMeditation();

        // Set up category chips
        setupCategoryChips();

        // Set up search functionality
        setupSearch();

        // Observe LiveData
        observeViewModel();
    }

    private void setupRecyclerView() {
        int spanCount = getResources().getConfiguration().orientation ==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MeditationAdapter(new ArrayList<>(), this);
        adapter.setOnFavoriteClickListener(audio -> {
            viewModel.updateFavoriteStatus(audio);
        });

        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        // Add null check to prevent NullPointerException
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                viewModel.refreshData();
                viewModel.loadFeaturedMeditations();
            });

            // Set colors for refresh animation
            swipeRefreshLayout.setColorSchemeResources(
                    R.color.primary,
                    R.color.brown,
                    R.color.wbrown
            );
        }
    }

    private void setupFeaturedMeditation() {
        // Set up click listener for featured meditation
        featuredMeditationCard.setOnClickListener(v -> {
            if (featuredMeditation != null) {
                onMeditationItemClick(featuredMeditation);
            }
        });

        // Load featured meditations from API
        viewModel.loadFeaturedMeditations();
    }

    private void setupCategoryChips() {
        // Add default categories
        addCategoryChip("All", true);
        addCategoryChip("Favorites", false);

        // Standard categories
        String[] categories = {"Sleep", "Stress", "Anxiety", "Focus"};
        for (String category : categories) {
            addCategoryChip(category, false);
        }
    }

    private void addCategoryChip(String category, boolean isSelected) {
        Chip chip = new Chip(requireContext());
        chip.setText(category);
        chip.setCheckable(true);
        chip.setChecked(isSelected);
        chip.setClickable(true);

        chip.setOnClickListener(v -> {
            // Clear all chip selections
            for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                ((Chip) categoryChipGroup.getChildAt(i)).setChecked(false);
            }

            // Select this chip
            chip.setChecked(true);

            // Update ViewModel with selected category
            viewModel.setCategory(category);
        });

        categoryChipGroup.addView(chip);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                filterMeditations(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterMeditations(String query) {
        if (query.isEmpty()) {
            // If search is cleared, observe filtered meditations based on current category
            viewModel.getFilteredAudios().observe(getViewLifecycleOwner(), this::updateMeditationsList);
        } else {
            // Filter meditations locally based on search query
            viewModel.getFilteredAudios().observe(getViewLifecycleOwner(), meditations -> {
                if (meditations == null) return;

                List<MeditationAudio> filteredList = new ArrayList<>();
                String lowerQuery = query.toLowerCase();

                for (MeditationAudio audio : meditations) {
                    if (audio.getTitle().toLowerCase().contains(lowerQuery) ||
                            (audio.getDescription() != null && audio.getDescription().toLowerCase().contains(lowerQuery)) ||
                            (audio.getCategory() != null && audio.getCategory().toLowerCase().contains(lowerQuery))) {
                        filteredList.add(audio);
                    }
                }

                updateMeditationsList(filteredList);
            });
        }
    }

    private void observeViewModel() {
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                showLoading();
            } else {
                hideLoading();
                // Add null check before setting refreshing state
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe filtered meditations based on category
        viewModel.getFilteredAudios().observe(getViewLifecycleOwner(), this::updateMeditationsList);

        // Observe all meditations for featured meditation selection
        viewModel.getAllAudios().observe(getViewLifecycleOwner(), meditations -> {
            if (meditations != null && !meditations.isEmpty()) {
                // Choose a meditation for featured section (first from Sleep category or first overall)
                featuredMeditation = findFeaturedMeditation(meditations);
                updateFeaturedMeditation();
            }
        });

        // Observe current category
        /*viewModel.getCurrentCategory().observe(getViewLifecycleOwner(), category -> {
            // Update UI to reflect category change if needed
        });*/
    }

    private MeditationAudio findFeaturedMeditation(List<MeditationAudio> meditations) {
        // First try to find a Sleep category meditation
        for (MeditationAudio audio : meditations) {
            if ("Sleep".equals(audio.getCategory())) {
                return audio;
            }
        }

        // If no Sleep category, return the first meditation
        return meditations.get(0);
    }

    private void updateFeaturedMeditation() {
        if (featuredMeditation == null) return;

        featuredTitle.setText(featuredMeditation.getTitle());
        featuredDuration.setText(featuredMeditation.getDuration());

        // Set background color based on category
        int colorResId;
        String category = featuredMeditation.getCategory();
        if ("Sleep".equals(category)) {
            colorResId = R.color.beige;
        } else if ("Stress".equals(category)) {
            colorResId = R.color.brown;
        } else if ("Anxiety".equals(category)) {
            colorResId = R.color.mood_button_brown;
        } else if ("Focus".equals(category)) {
            colorResId = R.color.wbrown;
        } else {
            colorResId = R.color.primary;
        }

        featuredMeditationCard.setCardBackgroundColor(getResources().getColor(colorResId));
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
        // Add null check for swipeRefreshLayout
        if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
            loadingProgress.setVisibility(View.VISIBLE);
        }
        emptyStateView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingProgress.setVisibility(View.GONE);
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
        Intent intent = new Intent(getActivity(), MeditationPlayerActivity.class);

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
}