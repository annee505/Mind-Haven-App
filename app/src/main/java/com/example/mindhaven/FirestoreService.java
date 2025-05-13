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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        // Enable Firestore logging for debugging
        FirebaseFirestore.setLoggingEnabled(true);

        // Log authentication status
        if (currentUser != null) {
            Log.d(TAG, "User authenticated: " + currentUser.getUid());
        } else {
            Log.w(TAG, "No user authenticated. Favorites functionality will be disabled.");
        }
    }

    /**
     * Save a recommendation as a favorite
     */
    public void saveFavorite(Recommendation recommendation, final FirestoreCallback<Void> callback) {
        // Check if the user is logged in
        if (currentUser == null) {
            Log.e(TAG, "Cannot save favorite: User not logged in");
            // If not, return an error
            callback.onError(new Exception("User not logged in"));
            return;
        }

        // Set the userId field
        recommendation.setUserId(currentUser.getUid());

        // Set the current date if not already set
        if (recommendation.getDateAdded() == null) {
            recommendation.setDateAdded(new Date());
        }

        // Ensure recommendation is marked as favorite
        recommendation.setFavorite(true);

        // Generate document ID if not present
        if (recommendation.getId() == null || recommendation.getId().isEmpty()) {
            String docId = db.collection(FAVORITES_COLLECTION).document().getId();
            recommendation.setId(docId);
            Log.d(TAG, "Generated new ID for favorite: " + docId);
        }

        Log.d(TAG, "Saving favorite: " + recommendation.getTitle() + " (ID: " + recommendation.getId() + ")");

        // Save the recommendation to Firestore
        db.collection(FAVORITES_COLLECTION)
                .document(recommendation.getId())
                .set(recommendation.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully saved recommendation to Firestore: " + recommendation.getTitle());
                    // If the callback is not null, return success
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving recommendation: " + e.getMessage(), e);
                    // If the callback is not null, return error
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
            Log.e(TAG, "Cannot remove favorite: User not logged in");
            callback.onError(new Exception("User not logged in"));
            return;
        }

        if (recommendationId == null || recommendationId.isEmpty()) {
            Log.e(TAG, "Cannot remove favorite: Invalid recommendation ID");
            callback.onError(new Exception("Invalid recommendation ID"));
            return;
        }

        Log.d(TAG, "Removing favorite with ID: " + recommendationId);

        db.collection(FAVORITES_COLLECTION)
                .document(recommendationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully removed recommendation from Firestore: " + recommendationId);
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing recommendation: " + e.getMessage(), e);
                    if (callback != null) {
                        callback.onError(e);
                    }
                });
    }

    /**
     * Get all favorite recommendations for the current user
     */
    public void getFavorites(String type, FirestoreCallback<List<Recommendation>> callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onError(new Exception("No authenticated user"));
            return;
        }

        Query query = db.collection(FAVORITES_COLLECTION)
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("isFavorite", true)
                .orderBy("dateAdded", Query.Direction.DESCENDING);

        if (type != null) {
            query = query.whereEqualTo("type", type);
        }

        query
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Recommendation> favorites = new ArrayList<>();
                    Log.d(TAG, "Retrieved " + querySnapshot.size() + " favorite documents from Firestore");

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        try {
                            String id = document.getId();
                            String title = document.getString("title");
                            String description = document.getString("description");
                            String mood = document.getString("mood");
                            String typeValue = document.getString("type");
                            Boolean isFavorite = document.getBoolean("isFavorite");
                            Date dateAdded = document.getDate("dateAdded");
                            String userId = document.getString("userId");

                            // Log each document's key fields for debugging
                            Log.d(TAG, "Processing favorite - ID: " + id + ", Title: " + title +
                                    ", Type: " + typeValue + ", Favorite: " + isFavorite);

                            // Verify required fields exist
                            if (title == null || typeValue == null) {
                                Log.w(TAG, "Skipping favorite with missing required fields. ID: " + id);
                                continue;
                            }

                            Recommendation recommendation = new Recommendation(
                                    id, title, description, mood, typeValue,
                                    isFavorite != null ? isFavorite : true,
                                    dateAdded, userId
                            );

                            favorites.add(recommendation);
                            Log.d(TAG, "Added recommendation to favorites list: " + title);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing favorite document: " + e.getMessage(), e);
                        }
                    }

                    Log.d(TAG, "Returning " + favorites.size() + " favorites to caller");
                    callback.onSuccess(favorites);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting favorites: " + e.getMessage(), e);
                    if (e instanceof FirebaseFirestoreException) {
                        FirebaseFirestoreException ffe = (FirebaseFirestoreException) e;
                        Log.e(TAG, "Firestore error code: " + ffe.getCode());
                    }
                    callback.onError(e);
                });
    }

    /**
     * Get favorites filtered by content type
     */
    public void getFavoritesByType(String type, final FirestoreCallback<List<Recommendation>> callback) {
        if (currentUser == null) {
            Log.e(TAG, "Cannot get favorites by type: User not logged in");
            callback.onError(new Exception("User not logged in"));
            return;
        }

        if (type == null || type.isEmpty()) {
            Log.e(TAG, "Cannot get favorites by type: Invalid type");
            callback.onError(new Exception("Invalid content type"));
            return;
        }

        Log.d(TAG, "Getting favorites of type '" + type + "' for user: " + currentUser.getUid());

        db.collection(FAVORITES_COLLECTION)
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("type", type)
                .orderBy("dateAdded", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Recommendation> favorites = new ArrayList<>();
                    Log.d(TAG, "Retrieved " + querySnapshot.size() + " favorites of type '" + type + "'");

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        try {
                            String id = document.getId();
                            String title = document.getString("title");
                            String description = document.getString("description");
                            String mood = document.getString("mood");
                            Boolean isFavorite = document.getBoolean("isFavorite");
                            Date dateAdded = document.getDate("dateAdded");
                            String userId = document.getString("userId");

                            // Verify title exists
                            if (title == null) {
                                Log.w(TAG, "Skipping favorite with missing title. ID: " + id);
                                continue;
                            }

                            Recommendation recommendation = new Recommendation(
                                    id, title, description, mood, type,
                                    isFavorite != null ? isFavorite : true,
                                    dateAdded, userId
                            );

                            favorites.add(recommendation);
                            Log.d(TAG, "Added typed recommendation to favorites list: " + title);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing favorite by type: " + e.getMessage(), e);
                        }
                    }

                    Log.d(TAG, "Returning " + favorites.size() + " favorites of type '" + type + "' to caller");
                    callback.onSuccess(favorites);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting favorites by type: " + e.getMessage(), e);
                    callback.onError(e);
                });
    }

    /**
     * Save recommendation to user history
     */
    public void saveToHistory(Recommendation recommendation) {
        if (currentUser == null) {
            Log.w(TAG, "Cannot save to history: User not logged in");
            return;
        }

        if (recommendation == null) {
            Log.w(TAG, "Cannot save to history: Recommendation is null");
            return;
        }

        // Set the userId field
        recommendation.setUserId(currentUser.getUid());

        // Set the current date
        recommendation.setDateAdded(new Date());

        Log.d(TAG, "Saving to history: " + recommendation.getTitle());

        db.collection(HISTORY_COLLECTION)
                .add(recommendation.toMap())
                .addOnSuccessListener(documentReference -> {
                    String newId = documentReference.getId();
                    Log.d(TAG, "Successfully added recommendation to history with ID: " + newId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding recommendation to history: " + e.getMessage(), e);
                });
    }

    /**
     * Check if a recommendation is in user's favorites
     */
    public void checkIfFavorite(Recommendation recommendation, final FirestoreCallback<Boolean> callback) {
        if (currentUser == null) {
            Log.d(TAG, "Cannot check if favorite: User not logged in");
            callback.onSuccess(false);
            return;
        }

        if (recommendation == null) {
            Log.w(TAG, "Cannot check if favorite: Recommendation is null");
            callback.onSuccess(false);
            return;
        }

        Log.d(TAG, "Checking if favorite: " + recommendation.getTitle() + " (" + recommendation.getType() + ")");

        db.collection(FAVORITES_COLLECTION)
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("title", recommendation.getTitle())
                .whereEqualTo("type", recommendation.getType())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean isFavorite = !querySnapshot.isEmpty();
                    Log.d(TAG, recommendation.getTitle() + " is " + (isFavorite ? "a favorite" : "not a favorite"));

                    if (isFavorite && !querySnapshot.isEmpty()) {
                        // Set the ID from Firestore to our recommendation
                        String docId = querySnapshot.getDocuments().get(0).getId();
                        recommendation.setId(docId);
                        Log.d(TAG, "Found favorite document ID: " + docId);
                    }
                    callback.onSuccess(isFavorite);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking if favorite: " + e.getMessage(), e);
                    callback.onSuccess(false);
                });
    }
}