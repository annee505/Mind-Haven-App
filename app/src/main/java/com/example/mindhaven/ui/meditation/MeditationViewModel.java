package com.example.mindhaven.ui.meditation;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.mindhaven.models.MeditationAudio;
import com.example.mindhaven.repo.MeditationRepository;

import java.util.List;

public class MeditationViewModel extends AndroidViewModel {
    private MeditationRepository repository;
    private LiveData<List<MeditationAudio>> allAudios;
    private LiveData<Boolean> isLoading;
    private LiveData<String> errorMessage;

    // MutableLiveData to track the current category
    private final MutableLiveData<String> currentCategory = new MutableLiveData<>("All");

    // Filtered meditations based on current category
    private final LiveData<List<MeditationAudio>> filteredAudios;

    public MeditationViewModel(Application application) {
        super(application);
        repository = new MeditationRepository(application);

        // Initialize LiveData from repository
        allAudios = repository.getAllAudios();
        isLoading = repository.getIsLoading();
        errorMessage = repository.getErrorMessage();

        // Create a transformation to get meditations by selected category
        filteredAudios = Transformations.switchMap(currentCategory, category -> {
            if ("All".equals(category)) {
                return repository.getAllAudios();
            } else if ("Favorites".equals(category)) {
                return repository.getFavoriteAudios();
            } else {
                return repository.getAudiosByCategory(category);
            }
        });
    }

    // Get all meditations
    public LiveData<List<MeditationAudio>> getAllAudios() {
        return allAudios;
    }

    // Get meditations filtered by the current category
    public LiveData<List<MeditationAudio>> getFilteredAudios() {
        return filteredAudios;
    }

    // Set the current category and trigger loading from API
    public void setCategory(String category) {
        if (!category.equals(currentCategory.getValue())) {
            currentCategory.setValue(category);

            // Load data from API for the selected category (unless it's "All" or "Favorites")
            if (!"All".equals(category) && !"Favorites".equals(category)) {
                repository.fetchMeditationsByCategoryFromApi(category);
            }
        }
    }

    // Get the current category
    public LiveData<String> getCurrentCategory() {
        return currentCategory;
    }

    // Get meditations by category
    public LiveData<List<MeditationAudio>> getAudiosByCategory(String category) {
        return repository.getAudiosByCategory(category);
    }

    // Insert new meditation
    public void insert(MeditationAudio audio) {
        repository.insert(audio);
    }

    // Get favorite meditations
    public LiveData<List<MeditationAudio>> getFavoriteAudios() {
        return repository.getFavoriteAudios();
    }

    // Update favorite status
    public void updateFavoriteStatus(MeditationAudio audio) {
        repository.updateFavoriteStatus(audio);
    }

    // Get loading state
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Get error messages
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Refresh data from API
    public void refreshData() {
        repository.refreshMeditationsFromApi();
    }

    // Load featured meditations
    public void loadFeaturedMeditations() {
        repository.fetchFeaturedMeditations();
    }

    // Search meditations (this would typically be handled by the database)
    public void searchMeditations(String query) {
        // This would typically be implemented with a Room query with LIKE
        // For now, we'll rely on the UI to filter the results from filteredAudios
    }
}