package com.example.mindhaven.repo;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mindhaven.api.ApiClient;
import com.example.mindhaven.api.MeditationApiService;
import com.example.mindhaven.api.MeditationResponse;
import com.example.mindhaven.db.AppDatabase;
import com.example.mindhaven.db.MeditationAudioDao;
import com.example.mindhaven.models.MeditationAudio;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class MeditationRepository {
    private static final String TAG = "MeditationRepository";

    private final MeditationAudioDao meditationAudioDao;
    private final ExecutorService databaseExecutor;
    private final MeditationApiService apiService;
    private final Application application;

    // LiveData for API loading state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    public MeditationRepository(Application application) {
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        meditationAudioDao = db.meditationAudioDao();
        databaseExecutor = AppDatabase.getDatabaseWriteExecutor();

        // Get API service from centralized client
        apiService = ApiClient.getMeditationApiService(application);

        // Refresh data from API if needed
        refreshMeditationsFromApi();
    }

    // Get all meditation audios
    public LiveData<List<MeditationAudio>> getAllAudios() {
        return meditationAudioDao.getAllAudios();
    }

    // Get meditations by category
    public LiveData<List<MeditationAudio>> getAudiosByCategory(String category) {
        return meditationAudioDao.getAudiosByCategory(category);
    }

    // Get favorite meditations
    public LiveData<List<MeditationAudio>> getFavoriteAudios() {
        return meditationAudioDao.getFavoriteAudios();
    }

    // Insert a new meditation
    public void insert(MeditationAudio audio) {
        databaseExecutor.execute(() -> {
            meditationAudioDao.insert(audio);
        });
    }

    // Update favorite status
    public void updateFavoriteStatus(MeditationAudio audio) {
        databaseExecutor.execute(() -> {
            meditationAudioDao.updateFavoriteStatus(audio.getId(), !audio.isFavorite());
        });
    }

    // Get loading state
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Get error messages
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Refresh the local database with data from API if needed
    public void refreshMeditationsFromApi() {
        // First check if we need to fetch data - e.g., if the database is empty or data is stale
        databaseExecutor.execute(() -> {
            int count = meditationAudioDao.getCount();
            if (count == 0) {
                // If the database is empty, fetch from API immediately
                fetchMeditationsFromApi();
            }
        });
    }

    // Fetch meditations by category
    public void fetchMeditationsByCategoryFromApi(String category) {
        isLoading.postValue(true);
        errorMessage.postValue(null);

        CompletableFuture<MeditationResponse> future = apiService.getMeditationsByCategory(category);

        future.thenAccept(response -> {
            List<MeditationAudio> meditations = response.getMeditations();

            if (meditations != null && !meditations.isEmpty()) {
                // Save to database
                databaseExecutor.execute(() -> {
                    for (MeditationAudio meditation : meditations) {
                        meditationAudioDao.insert(meditation);
                    }
                    Log.d(TAG, "Saved " + meditations.size() + " meditations for category: " + category);
                });
            }
            isLoading.postValue(false);
        }).exceptionally(e -> {
            String error = "Error getting meditations by category: " + e.getMessage();
            Log.e(TAG, error);
            errorMessage.postValue(error);
            isLoading.postValue(false);
            return null;
        });
    }

    // Fetch all meditations
    private void fetchMeditationsFromApi() {
        isLoading.postValue(true);
        errorMessage.postValue(null);

        CompletableFuture<MeditationResponse> future = apiService.getMeditations();

        future.thenAccept(response -> {
            List<MeditationAudio> meditations = response.getMeditations();

            if (meditations != null && !meditations.isEmpty()) {
                // Save to database
                databaseExecutor.execute(() -> {
                    for (MeditationAudio meditation : meditations) {
                        meditationAudioDao.insert(meditation);
                    }
                    Log.d(TAG, "Saved " + meditations.size() + " meditations");
                });
            }
            isLoading.postValue(false);
        }).exceptionally(e -> {
            String error = "Error getting meditations: " + e.getMessage();
            Log.e(TAG, error);
            errorMessage.postValue(error);
            isLoading.postValue(false);
            return null;
        });
    }

    // Fetch featured meditations
    public void fetchFeaturedMeditations() {
        isLoading.postValue(true);

        CompletableFuture<MeditationResponse> future = apiService.getFeaturedMeditations();

        future.thenAccept(response -> {
            List<MeditationAudio> meditations = response.getMeditations();

            if (meditations != null && !meditations.isEmpty()) {
                // Save to database
                databaseExecutor.execute(() -> {
                    for (MeditationAudio meditation : meditations) {
                        meditationAudioDao.insert(meditation);
                    }
                    Log.d(TAG, "Saved " + meditations.size() + " featured meditations");
                });
            }
            isLoading.postValue(false);
        }).exceptionally(e -> {
            String error = "Error getting featured meditations: " + e.getMessage();
            Log.e(TAG, error);
            errorMessage.postValue(error);
            isLoading.postValue(false);
            return null;
        });
    }
}