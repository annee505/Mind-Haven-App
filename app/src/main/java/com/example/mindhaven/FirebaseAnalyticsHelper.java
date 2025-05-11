package com.example.mindhaven;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helper class to manage Firebase analytics data for mood tracking
 * Ensures data consistency between Firestore and Realtime Database
 */
public class FirebaseAnalyticsHelper {
    private static final String TAG = "FirebaseAnalytics";
    
    // Database references
    private final FirebaseFirestore firestoreDb;
    private final DatabaseReference realtimeDb;
    private final String userId;
    
    public interface SyncListener {
        void onSyncComplete(boolean success);
    }
    
    public FirebaseAnalyticsHelper() {
        firestoreDb = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        
        if (user != null) {
            userId = user.getUid();
            realtimeDb = FirebaseDatabase.getInstance().getReference()
                    .child("moods").child(userId);
        } else {
            userId = null;
            realtimeDb = null;
        }
    }
    
    /**
     * Synchronize data between Firestore and Realtime Database
     * Ensures all mood data is available in both databases for redundancy
     */
    public void syncDatabases(SyncListener listener) {
        if (userId == null || realtimeDb == null) {
            Log.e(TAG, "Cannot sync: User not logged in");
            if (listener != null) {
                listener.onSyncComplete(false);
            }
            return;
        }
        
        Log.d(TAG, "Starting database synchronization...");
        
        // First verify that the user document exists in Firestore
        firestoreDb.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                // If user document doesn't exist, create it
                if (!documentSnapshot.exists()) {
                    Log.d(TAG, "User document doesn't exist, creating it");
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", userId);
                    userData.put("lastSync", System.currentTimeMillis());
                    
                    firestoreDb.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "User document created successfully");
                            // Now continue with regular sync
                            performFullSync(listener);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error creating user document: " + e.getMessage());
                            if (listener != null) {
                                listener.onSyncComplete(false);
                            }
                        });
                } else {
                    // User document exists, proceed with sync
                    Log.d(TAG, "User document exists, proceeding with sync");
                    performFullSync(listener);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error checking user document: " + e.getMessage());
                if (listener != null) {
                    listener.onSyncComplete(false);
                }
            });
    }
    
    /**
     * Perform full synchronization between databases after verifying user document
     */
    private void performFullSync(SyncListener listener) {
        // Create atomic counters for tracking sync completion
        AtomicInteger pendingTasks = new AtomicInteger(2);
        AtomicBoolean success = new AtomicBoolean(true);
        
        // First, get all Firestore entries
        firestoreDb.collection("users")
                .document(userId)
                .collection("mood_tracking")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<MoodEntry> firestoreEntries = new ArrayList<>();
                    Map<String, Boolean> firestoreDocIds = new HashMap<>();
                    
                    Log.d(TAG, "Firestore query returned " + queryDocumentSnapshots.size() + " documents");
                    
                    // If we have no entries, try to copy from Realtime Database
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No entries in Firestore, checking Realtime Database");
                        syncFromRealtimeToFirestore(listener);
                        return;
                    }
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Map<String, Object> data = doc.getData();
                        String docId = doc.getId();
                        firestoreDocIds.put(docId, true);
                        
                        // Ensure document has its ID field
                        if (!data.containsKey("documentId")) {
                            doc.getReference().update("documentId", docId);
                            data.put("documentId", docId);
                            Log.d(TAG, "Added missing documentId for document: " + docId);
                        }
                        
                        // Convert to MoodEntry
                        MoodEntry entry = convertMapToMoodEntry(data, docId);
                        if (entry != null) {
                            firestoreEntries.add(entry);
                        }
                    }
                    
                    Log.d(TAG, "Converted " + firestoreEntries.size() + " valid entries from Firestore");
                    
                    // Now fetch Realtime Database entries
                    realtimeDb.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<MoodEntry> rtdbEntries = new ArrayList<>();
                            Map<String, Boolean> rtdbDocIds = new HashMap<>();
                            
                            Log.d(TAG, "RTDB query returned " + snapshot.getChildrenCount() + " entries");
                            
                            for (DataSnapshot child : snapshot.getChildren()) {
                                MoodEntry entry = child.getValue(MoodEntry.class);
                                if (entry != null && entry.getDocumentId() != null) {
                                    rtdbEntries.add(entry);
                                    rtdbDocIds.put(entry.getDocumentId(), true);
                                }
                            }
                            
                            int syncedToRtdb = 0;
                            int syncedToFirestore = 0;
                            
                            // Sync missing entries from Firestore to RTDB
                            for (MoodEntry entry : firestoreEntries) {
                                if (entry.getDocumentId() != null && 
                                        !rtdbDocIds.containsKey(entry.getDocumentId())) {
                                    realtimeDb.child(entry.getDocumentId()).setValue(entry);
                                    syncedToRtdb++;
                                    Log.d(TAG, "Synced entry from Firestore to RTDB: " + entry.getDocumentId());
                                }
                            }
                            
                            // Sync missing entries from RTDB to Firestore
                            for (MoodEntry entry : rtdbEntries) {
                                if (entry.getDocumentId() != null && 
                                        !firestoreDocIds.containsKey(entry.getDocumentId())) {
                                    Map<String, Object> data = convertMoodEntryToMap(entry);
                                    firestoreDb.collection("users")
                                            .document(userId)
                                            .collection("mood_tracking")
                                            .document(entry.getDocumentId())
                                            .set(data);
                                    syncedToFirestore++;
                                    Log.d(TAG, "Synced entry from RTDB to Firestore: " + entry.getDocumentId());
                                }
                            }
                            
                            Log.d(TAG, "Sync complete. Added " + syncedToRtdb + " entries to RTDB and " 
                                    + syncedToFirestore + " entries to Firestore");
                            
                            if (pendingTasks.decrementAndGet() == 0 && listener != null) {
                                listener.onSyncComplete(success.get());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "RTDB sync error: " + error.getMessage());
                            success.set(false);
                            if (pendingTasks.decrementAndGet() == 0 && listener != null) {
                                listener.onSyncComplete(false);
                            }
                        }
                    });
                    
                    if (pendingTasks.decrementAndGet() == 0 && listener != null) {
                        listener.onSyncComplete(success.get());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore sync error: " + e.getMessage());
                    success.set(false);
                    pendingTasks.decrementAndGet();
                    if (listener != null) {
                        listener.onSyncComplete(false);
                    }
                });
    }
    
    /**
     * Special sync operation from Realtime Database to Firestore when Firestore is empty
     */
    private void syncFromRealtimeToFirestore(SyncListener listener) {
        Log.d(TAG, "Attempting to restore Firestore collection from Realtime Database");
        
        realtimeDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() == 0) {
                    Log.d(TAG, "Both databases are empty, no entries to sync");
                    if (listener != null) {
                        listener.onSyncComplete(true);
                    }
                    return;
                }
                
                Log.d(TAG, "Found " + snapshot.getChildrenCount() + " entries in RTDB to restore to Firestore");
                AtomicInteger pendingWrites = new AtomicInteger((int) snapshot.getChildrenCount());
                AtomicInteger successCount = new AtomicInteger(0);
                
                for (DataSnapshot child : snapshot.getChildren()) {
                    MoodEntry entry = child.getValue(MoodEntry.class);
                    if (entry != null && entry.getDocumentId() != null) {
                        Map<String, Object> data = convertMoodEntryToMap(entry);
                        
                        firestoreDb.collection("users")
                                .document(userId)
                                .collection("mood_tracking")
                                .document(entry.getDocumentId())
                                .set(data)
                                .addOnSuccessListener(aVoid -> {
                                    successCount.incrementAndGet();
                                    Log.d(TAG, "Restored entry to Firestore: " + entry.getDocumentId());
                                    
                                    if (pendingWrites.decrementAndGet() == 0) {
                                        Log.d(TAG, "Restored " + successCount.get() + " entries to Firestore from RTDB");
                                        if (listener != null) {
                                            listener.onSyncComplete(true);
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error restoring entry: " + e.getMessage());
                                    
                                    if (pendingWrites.decrementAndGet() == 0) {
                                        Log.d(TAG, "Restored " + successCount.get() + " entries to Firestore from RTDB");
                                        if (listener != null) {
                                            listener.onSyncComplete(successCount.get() > 0);
                                        }
                                    }
                                });
                    } else {
                        pendingWrites.decrementAndGet();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error reading RTDB for restoration: " + error.getMessage());
                if (listener != null) {
                    listener.onSyncComplete(false);
                }
            }
        });
    }
    
    /**
     * Convert map of data from Firestore to MoodEntry object
     */
    private MoodEntry convertMapToMoodEntry(Map<String, Object> data, String docId) {
        try {
            MoodEntry entry = new MoodEntry();
            
            entry.setDocumentId(docId);
            entry.setUserId(userId);
            entry.setMood((String) data.get("mood"));
            entry.setNote((String) data.getOrDefault("note", ""));
            
            // Handle activities list
            Object activitiesObj = data.get("activities");
            if (activitiesObj instanceof List) {
                List<String> activities = new ArrayList<>();
                for (Object item : (List<?>) activitiesObj) {
                    if (item instanceof String) {
                        activities.add((String) item);
                    }
                }
                entry.setActivities(activities);
            } else {
                entry.setActivities(new ArrayList<>());
            }
            
            // Handle timestamp
            Object timestampObj = data.get("timestamp");
            if (timestampObj instanceof Long) {
                entry.setTimestamp((Long) timestampObj);
            } else {
                entry.setTimestamp(System.currentTimeMillis());
            }
            
            // Handle mood score
            Object moodScoreObj = data.get("moodScore");
            if (moodScoreObj instanceof Long) {
                entry.setMoodScore(((Long) moodScoreObj).intValue());
            } else if (moodScoreObj instanceof Integer) {
                entry.setMoodScore((Integer) moodScoreObj);
            } else if (moodScoreObj instanceof Double) {
                entry.setMoodScore(((Double) moodScoreObj).intValue());
            } else {
                entry.setMoodScore(0);
            }
            
            entry.setTimeOfDay((String) data.getOrDefault("timeOfDay", ""));
            
            return entry;
        } catch (Exception e) {
            Log.e(TAG, "Error converting map to MoodEntry: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert MoodEntry object to map for Firestore
     */
    private Map<String, Object> convertMoodEntryToMap(MoodEntry entry) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("documentId", entry.getDocumentId());
        data.put("userId", entry.getUserId());
        data.put("mood", entry.getMood());
        data.put("note", entry.getNote());
        data.put("activities", entry.getActivities());
        data.put("timestamp", entry.getTimestamp());
        data.put("moodScore", entry.getMoodScore());
        data.put("timeOfDay", entry.getTimeOfDay());
        
        return data;
    }
    
    /**
     * Save a new mood entry to both Firestore and Realtime Database
     */
    public void saveMoodEntry(Map<String, Object> moodData, SaveListener listener) {
        if (userId == null || realtimeDb == null) {
            Log.e(TAG, "Cannot save: User not logged in");
            if (listener != null) {
                listener.onSaveComplete(false, null);
            }
            return;
        }
        
        Log.d(TAG, "=== SAVING MOOD DATA ===");
        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "Data: " + moodData.toString());
        
        moodData.put("userId", userId);
        
        // Debug mood data in detail
        String moodValue = (String) moodData.get("mood");
        Long timestamp = (Long) moodData.get("timestamp");
        Integer moodScore = (Integer) moodData.get("moodScore");
        List<String> activities = (List<String>) moodData.get("activities");
        String timeOfDay = (String) moodData.get("timeOfDay");
        String note = (String) moodData.get("note");
        
        Log.d(TAG, "MOOD DETAILS:");
        Log.d(TAG, "- Mood: " + moodValue);
        Log.d(TAG, "- Score: " + moodScore);
        Log.d(TAG, "- Timestamp: " + timestamp + " (" + 
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        .format(new java.util.Date(timestamp)) + ")");
        Log.d(TAG, "- Time of Day: " + timeOfDay);
        Log.d(TAG, "- Note: " + note);
        Log.d(TAG, "- Activities: " + (activities != null ? String.join(", ", activities) : "none"));
        
        Log.d(TAG, "Saving to Firestore: users/" + userId + "/mood_tracking/");
        
        firestoreDb.collection("users")
                .document(userId)
                .collection("mood_tracking")
                .add(moodData)
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId();
                    Log.d(TAG, "*** FIRESTORE SUCCESS *** Document added with ID: " + documentId);
                    
                    moodData.put("documentId", documentId);
                    
                    // Update the document with its ID
                    documentReference.update("documentId", documentId)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Document ID updated successfully in Firestore");
                                
                                // Double-check the document exists and has correct data
                                documentReference.get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            Log.d(TAG, "VERIFICATION: Document exists in Firestore");
                                            Log.d(TAG, "Document data: " + documentSnapshot.getData());
                                            
                                            // Verify critical fields
                                            Object verifyMood = documentSnapshot.get("mood");
                                            Object verifyScore = documentSnapshot.get("moodScore");
                                            Object verifyTimestamp = documentSnapshot.get("timestamp");
                                            
                                            Log.d(TAG, "Verified mood: " + verifyMood);
                                            Log.d(TAG, "Verified score: " + verifyScore);
                                            Log.d(TAG, "Verified timestamp: " + verifyTimestamp);
                                            
                                            // Save to Realtime Database
                                            MoodEntry entry = convertMapToMoodEntry(moodData, documentId);
                                            if (entry != null) {
                                                Log.d(TAG, "Saving to Realtime Database: moods/" + userId + "/" + documentId);
                                                realtimeDb.child(documentId).setValue(entry)
                                                        .addOnSuccessListener(aVoid2 -> {
                                                            Log.d(TAG, "Mood data saved to Realtime Database successfully");
                                                            // Verify data was saved to both databases
                                                            Log.d(TAG, "FULL VERIFICATION: Mood data saved to both Firestore and RTDB");
                                                            Log.d(TAG, "DocumentId: " + documentId);
                                                            Log.d(TAG, "Firestore path: users/" + userId + "/mood_tracking/" + documentId);
                                                            Log.d(TAG, "RTDB path: moods/" + userId + "/" + documentId);
                                                            
                                                            if (listener != null) {
                                                                listener.onSaveComplete(true, documentId);
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e(TAG, "Error saving to RTDB: " + e.getMessage(), e);
                                                            if (listener != null) {
                                                                listener.onSaveComplete(true, documentId);
                                                            }
                                                        });
                                            } else {
                                                Log.e(TAG, "Failed to convert mood data to MoodEntry");
                                                if (listener != null) {
                                                    listener.onSaveComplete(true, documentId);
                                                }
                                            }
                                        } else {
                                            Log.e(TAG, "Document does not exist after saving!");
                                            if (listener != null) {
                                                listener.onSaveComplete(false, documentId);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error verifying document: " + e.getMessage(), e);
                                        if (listener != null) {
                                            listener.onSaveComplete(true, documentId);
                                        }
                                    });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating documentId: " + e.getMessage(), e);
                                if (listener != null) {
                                    listener.onSaveComplete(true, documentId);
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving to Firestore: " + e.getMessage(), e);
                    Log.e(TAG, "Error details: ", e);
                    if (listener != null) {
                        listener.onSaveComplete(false, null);
                    }
                });
    }
    
    /**
     * Callback interface for save operations
     */
    public interface SaveListener {
        void onSaveComplete(boolean success, String documentId);
    }
} 