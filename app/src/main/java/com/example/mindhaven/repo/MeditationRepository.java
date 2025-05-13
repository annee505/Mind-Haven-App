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

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<List<MeditationAudio>> meditationsLiveData = new MutableLiveData<>();

    public MeditationRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        meditationAudioDao = db.meditationAudioDao();
        databaseExecutor = AppDatabase.getDatabaseWriteExecutor();
        apiService = ApiClient.getMeditationApiService(application);
    }

    // Fetch all audio meditations from Firestore
    public LiveData<List<MeditationAudio>> getAllAudios() {
        // Use a separate method to retrieve all our audios from firestore.
        // This method needs to be implemented consistently with fetching logic
        return meditationAudioDao.getAllAudios();
    }

    // Fetch featured meditations from the API and update the database
    public void fetchFeaturedMeditations() {
        isLoading.postValue(true);
        CompletableFuture<MeditationResponse> future = apiService.getFeaturedMeditations();

        future.thenAccept(response -> {
            List<MeditationAudio> meditations = response.getMeditations();
            if (meditations != null && !meditations.isEmpty()) {
                databaseExecutor.execute(() -> {
                    for (MeditationAudio meditation : meditations) {
                        meditationAudioDao.insert(meditation);
                    }
                    Log.d(TAG, "Saved " + meditations.size() + " featured meditations");
                });
            }
            isLoading.postValue(false);
            meditationsLiveData.postValue(meditations);
        }).exceptionally(e -> {
            String error = "Error getting featured meditations: " + e.getMessage();
            Log.e(TAG, error);
            errorMessage.postValue(error);
            isLoading.postValue(false);
            return null;
        });
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<List<MeditationAudio>> getFavoriteAudios() {
        return meditationAudioDao.getFavoriteAudios();
    }

    public LiveData<List<MeditationAudio>> getAudiosByCategory(String category) {
        return meditationAudioDao.getAudiosByCategory(category);
    }

    public void updateFavoriteStatus(MeditationAudio audio) {
        databaseExecutor.execute(() -> {
            meditationAudioDao.updateFavoriteStatus(audio.getId(), !audio.isFavorite());
        });
    }
}