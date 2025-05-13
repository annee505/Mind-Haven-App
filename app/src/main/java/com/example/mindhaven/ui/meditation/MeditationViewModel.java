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
    private final MutableLiveData<String> currentCategory = new MutableLiveData<>("All");
    private final LiveData<List<MeditationAudio>> filteredAudios;

    public MeditationViewModel(Application application) {
        super(application);
        repository = new MeditationRepository(application);

        allAudios = repository.getAllAudios();
        isLoading = repository.getIsLoading();
        errorMessage = repository.getErrorMessage();

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

    public LiveData<List<MeditationAudio>> getAllAudios() {
        return allAudios;
    }

    public LiveData<List<MeditationAudio>> getFilteredAudios() {
        return filteredAudios;
    }

    public void setCategory(String category) {
        if (!category.equals(currentCategory.getValue())) {
            currentCategory.setValue(category);
        }
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void refreshData() {
        repository.fetchFeaturedMeditations();
    }

    public void loadFeaturedMeditations() {
        repository.fetchFeaturedMeditations(); // Load featured contents
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<List<MeditationAudio>> getFavoriteAudios() {
        return repository.getFavoriteAudios();
    }

    public LiveData<List<MeditationAudio>> getAudiosByCategory(String category) {
        return repository.getAudiosByCategory(category);
    }

    public void updateFavoriteStatus(MeditationAudio audio) {
        repository.updateFavoriteStatus(audio);
    }

    public void searchMeditations(String query) {
        // This would typically be implemented with a Room query with LIKE
        // For now, reliance on UI filtering is needed
    }
}