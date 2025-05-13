package com.example.mindhaven.api;

import android.content.Context;
import android.util.Log;
import android.content.res.Resources;

import com.example.mindhaven.R;
import com.example.mindhaven.models.MeditationAudio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Service class for providing meditation data from local resources
 */
public class MeditationApiService {
    private static final String TAG = "MeditationApiService";

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Context context;

    // Sample category data
    private final List<String> CATEGORIES = Arrays.asList(
            "Sleep", "Stress", "Anxiety", "Focus", "Mindfulness"
    );

    public MeditationApiService(Context context) {
        this.context = context;
    }

    /**
     * Get all meditation content from local resources
     */
    public CompletableFuture<MeditationResponse> getMeditations() {
        CompletableFuture<MeditationResponse> future = new CompletableFuture<>();

        executor.execute(() -> {
            try {
                MeditationResponse response = createDefaultMeditations();
                future.complete(response);
            } catch (Exception e) {
                Log.e(TAG, "Error getting meditations: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Get meditation content by category
     */
    public CompletableFuture<MeditationResponse> getMeditationsByCategory(String category) {
        CompletableFuture<MeditationResponse> future = new CompletableFuture<>();

        executor.execute(() -> {
            try {
                MeditationResponse allMeditations = createDefaultMeditations();
                List<MeditationAudio> filteredList = new ArrayList<>();

                for (MeditationAudio audio : allMeditations.getMeditations()) {
                    if (audio.getCategory().equalsIgnoreCase(category)) {
                        filteredList.add(audio);
                    }
                }

                MeditationResponse response = new MeditationResponse();
                response.setStatus("success");
                response.setMeditations(filteredList);
                future.complete(response);
            } catch (Exception e) {
                Log.e(TAG, "Error getting meditations by category: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Get featured meditation content
     */
    public CompletableFuture<MeditationResponse> getFeaturedMeditations() {
        CompletableFuture<MeditationResponse> future = new CompletableFuture<>();

        executor.execute(() -> {
            try {
                MeditationResponse allMeditations = createDefaultMeditations();
                List<MeditationAudio> featuredList = new ArrayList<>();

                // Take the first meditation from each category
                String lastCategory = null;
                int countInCategory = 0;

                for (MeditationAudio audio : allMeditations.getMeditations()) {
                    if (!audio.getCategory().equals(lastCategory)) {
                        lastCategory = audio.getCategory();
                        countInCategory = 0;
                    }

                    if (countInCategory < 1) {  // Only take 1 from each category
                        featuredList.add(audio);
                    }

                    countInCategory++;
                }

                MeditationResponse response = new MeditationResponse();
                response.setStatus("success");
                response.setMeditations(featuredList);
                future.complete(response);
            } catch (Exception e) {
                Log.e(TAG, "Error getting featured meditations: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Create default meditation data
     */
    private MeditationResponse createDefaultMeditations() {
        MeditationResponse response = new MeditationResponse();
        response.setStatus("success");

        List<MeditationAudio> meditations = new ArrayList<>();

        // Create some default meditations for each category
        // In a real app, these would come from actual audio resources
        int idCounter = 1;

        for (String category : CATEGORIES) {
            // Add a few meditations for each category
            for (int i = 1; i <= 3; i++) {
                String title = category + " Meditation " + i;
                String description = "A meditation session to help with " + category.toLowerCase();
                String duration = (i + 3) + ":00"; // Random duration

                // Create a resource ID (this would be a real resource in a real app)
                int resourceId = idCounter++;

                MeditationAudio meditation;
                meditation = new MeditationAudio(
                        title,
                        description,
                        duration,
                        resourceId,
                        category,
                        true,  // isResource
                        false  // isFavorite
                );

                meditations.add(meditation);
            }
        }

        response.setMeditations(meditations);
        return response;
    }
}