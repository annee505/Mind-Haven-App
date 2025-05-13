package com.example.mindhaven;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Service to handle all Firestore operations related to recommendations
 */
public class FirestoreService {
    private static final String TAG = "FirestoreService";

    // Collection names
    private static final String FAVORITES_COLLECTION = "favorites";
    private static final String HISTORY_COLLECTION = "recommendation_history";

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    /**
     * Interface for handling Firestore callbacks
     */
    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public FirestoreService() {
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Save a recommendation as a favorite
     */
    public void saveFavorite(Recommendation recommendation, final FirestoreCallback<Void> callback) {
        if (currentUser == null) {
            callback.onError(new Exception("User not logged in"));
            return;
        }

        // Set the userId field
        recommendation.setUserId(currentUser.getUid());

        // Set the current date if not already set
        if (recommendation.getDateAdded() == null) {
            recommendation.setDateAdded(new Date());
        }

        // Generate document ID if not present
        if (recommendation.getId() == null || recommendation.getId().isEmpty()) {
            String docId = db.collection(FAVORITES_COLLECTION).document().getId();
            recommendation.setId(docId);
        }

        db.collection(FAVORITES_COLLECTION)
                .document(recommendation.getId())
                .set(recommendation.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Recommendation saved to Firestore");
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving recommendation", e);
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Remove a recommendation from favorites
     */
    public void removeFavorite(String recommendationId, final FirestoreCallback<Void> callback) {
        if (currentUser == null) {
            callback.onError(new Exception("User not logged in"));
            return;
        }

        db.collection(FAVORITES_COLLECTION)
                .document(recommendationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Recommendation removed from Firestore");
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing recommendation", e);
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Get all favorite recommendations for the current user
     */
    public void getFavorites(final FirestoreCallback<List<Recommendation>> callback) {
        if (currentUser == null) {
            callback.onError(new Exception("User not logged in"));
            return;
        }

        db.collection(FAVORITES_COLLECTION)
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Recommendation> favorites = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String title = document.getString("title");
                            String description = document.getString("description");
                            String mood = document.getString("mood");
                            String type = document.getString("type");
                            Boolean isFavorite = document.getBoolean("isFavorite");
                            Date dateAdded = document.getDate("dateAdded");
                            String userId = document.getString("userId");

                            Recommendation recommendation = new Recommendation(
                                    id, title, description, mood, type,
                                    isFavorite != null ? isFavorite : true,
                                    dateAdded, userId
                            );

                            favorites.add(recommendation);
                        }
                        callback.onSuccess(favorites);
                    } else {
                        Log.e(TAG, "Error getting favorites", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    /**
     * Get favorites filtered by content type
     */
    public void getFavoritesByType(String type, final FirestoreCallback<List<Recommendation>> callback) {
        if (currentUser == null) {
            callback.onError(new Exception("User not logged in"));
            return;
        }

        db.collection(FAVORITES_COLLECTION)
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("type", type)
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Recommendation> favorites = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String title = document.getString("title");
                            String description = document.getString("description");
                            String mood = document.getString("mood");
                            Boolean isFavorite = document.getBoolean("isFavorite");
                            Date dateAdded = document.getDate("dateAdded");
                            String userId = document.getString("userId");

                            Recommendation recommendation = new Recommendation(
                                    id, title, description, mood, type,
                                    isFavorite != null ? isFavorite : true,
                                    dateAdded, userId
                            );

                            favorites.add(recommendation);
                        }
                        callback.onSuccess(favorites);
                    } else {
                        Log.e(TAG, "Error getting favorites by type", task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    /**
     * Save recommendation to user history
     */
    public void saveToHistory(Recommendation recommendation) {
        if (currentUser == null) {
            return;
        }

        // Set the userId field
        recommendation.setUserId(currentUser.getUid());

        // Set the current date
        recommendation.setDateAdded(new Date());

        db.collection(HISTORY_COLLECTION)
                .add(recommendation.toMap())
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Recommendation added to history"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error adding recommendation to history", e));
    }

    /**
     * Check if a recommendation is in user's favorites
     */
    public void checkIfFavorite(Recommendation recommendation, final FirestoreCallback<Boolean> callback) {
        if (currentUser == null) {
            callback.onSuccess(false);
            return;
        }

        db.collection(FAVORITES_COLLECTION)
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("title", recommendation.getTitle())
                .whereEqualTo("type", recommendation.getType())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isFavorite = !task.getResult().isEmpty();
                        if (isFavorite && !task.getResult().isEmpty()) {
                            // Set the ID from Firestore to our recommendation
                            String docId = task.getResult().getDocuments().get(0).getId();
                            recommendation.setId(docId);
                        }
                        callback.onSuccess(isFavorite);
                    } else {
                        Log.e(TAG, "Error checking if favorite", task.getException());
                        callback.onSuccess(false);
                    }
                });
    }
}
