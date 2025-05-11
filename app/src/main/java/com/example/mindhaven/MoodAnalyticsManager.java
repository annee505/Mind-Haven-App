package com.example.mindhaven;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MoodAnalyticsManager {
    private FirebaseFirestore db;
    private String userId;
    private SimpleDateFormat dateFormat;
    private List<Map<String, Object>> moodEntries;
    private AnalyticsUpdateListener listener;
    private LineChart chart;
    private DatabaseReference rtDatabase;

    public interface MoodRecommendation {
        String getTitle();
        String getDescription();
        List<String> getExercises();
    }

    public MoodRecommendation getRecommendations(String mood, float moodScore) {
        return new MoodRecommendation() {
            @Override
            public String getTitle() {
                if (moodScore < 2.0) {
                    return "Improving Low Mood";
                } else if (moodScore < 3.5) {
                    return "Maintaining Balance";
                } else {
                    return "Sustaining Positive Mood";
                }
            }

            @Override
            public String getDescription() {
                if (moodScore < 2.0) {
                    return "Here are some strategies to help lift your mood";
                } else if (moodScore < 3.5) {
                    return "Consider these activities to maintain emotional balance";
                } else {
                    return "Great mood! Here's how to maintain it";
                }
            }

            @Override
            public List<String> getExercises() {
                List<String> exercises = new ArrayList<>();
                if (moodScore < 2.0) {
                    exercises.add("Take a 10-minute walk outside");
                    exercises.add("Practice deep breathing for 5 minutes");
                    exercises.add("Call a friend or family member");
                    exercises.add("Complete one small task");
                } else if (moodScore < 3.5) {
                    exercises.add("Practice mindfulness meditation");
                    exercises.add("Write in your gratitude journal");
                    exercises.add("Do some light exercise");
                } else {
                    exercises.add("Share your positive energy with others");
                    exercises.add("Plan something you look forward to");
                    exercises.add("Practice self-care activities");
                }
                return exercises;
            }
        };
    }
    private static final String TAG = "MoodAnalyticsManager";

    public interface AnalyticsUpdateListener {
        void onAnalyticsUpdated(String averageMood, String commonMood, String productiveTime,
                                List<String> topActivities);
        void onMoodHistoryUpdated(List<Map<String, Object>> moodEntries);
    }

    public MoodAnalyticsManager(AnalyticsUpdateListener listener, LineChart chart) {
        this.listener = listener;
        this.chart = chart;
        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            rtDatabase = FirebaseDatabase.getInstance().getReference().child("moods").child(userId);
        } else {
            userId = null;
            rtDatabase = null;
        }

        dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        moodEntries = new ArrayList<>();
    }

    public void loadMoodData() {
        if (userId == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
            if (listener != null) {
                listener.onAnalyticsUpdated("No data", "No data", "No data", new ArrayList<>());
                listener.onMoodHistoryUpdated(new ArrayList<>());
            }
            return;
        }

        Log.d(TAG, "Starting to load mood data for user: " + userId);

        // Add an initial Firestore verification to check if the user's mood data exists
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "Verification: User document exists: " + documentSnapshot.exists());
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "User document data: " + documentSnapshot.getData());
                    }

                    // Check if mood_tracking collection exists by getting the first document
                    FirebaseFirestore.getInstance().collection("users")
                            .document(userId)
                            .collection("mood_tracking")
                            .limit(1)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                Log.d(TAG, "Mood tracking collection check: " +
                                        (querySnapshot.isEmpty() ? "EMPTY" : "CONTAINS DATA"));
                                Log.d(TAG, "Number of documents found: " + querySnapshot.size());

                                if (!querySnapshot.isEmpty()) {
                                    QueryDocumentSnapshot firstDoc = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                                    Log.d(TAG, "First mood document ID: " + firstDoc.getId());
                                    Log.d(TAG, "First mood document data: " + firstDoc.getData());

                                    // Get timestamp and verify format
                                    Object timestampObj = firstDoc.get("timestamp");
                                    if (timestampObj != null) {
                                        Log.d(TAG, "Timestamp class: " + timestampObj.getClass().getName());
                                        Log.d(TAG, "Timestamp value: " + timestampObj);

                                        if (timestampObj instanceof Long) {
                                            long timestamp = (Long) timestampObj;
                                            Log.d(TAG, "Formatted timestamp: " +
                                                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                                            java.util.Locale.getDefault())
                                                            .format(new java.util.Date(timestamp)));
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error checking mood_tracking collection: " + e.getMessage(), e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error verifying user document: " + e.getMessage(), e);
                });

        // Now try to get all mood entries with full details
        Log.d(TAG, "DIRECT FIRESTORE QUERY: users/" + userId + "/mood_tracking");
        db.collection("users")
                .document(userId)
                .collection("mood_tracking")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    try {
                        moodEntries.clear();
                        int entryCount = 0;

                        Log.d(TAG, "***** FIRESTORE RESULT *****");
                        Log.d(TAG, "Firestore returned " + queryDocumentSnapshots.size() + " mood entries");

                        // Log details of all documents
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Log.d(TAG, "Document ID: " + document.getId());
                            Log.d(TAG, "Document data: " + document.getData());
                        }

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Map<String, Object> entry = new HashMap<>(document.getData());

                            // Ensure document ID is set
                            if (!entry.containsKey("documentId")) {
                                entry.put("documentId", document.getId());
                                document.getReference().update("documentId", document.getId());
                                Log.d(TAG, "Added missing documentId: " + document.getId());
                            }

                            // Handle timestamp conversion
                            Object timestampObj = entry.get("timestamp");
                            long timestamp = 0;

                            if (timestampObj instanceof Long) {
                                timestamp = (Long) timestampObj;
                                Log.d(TAG, "Timestamp is Long: " + timestamp);
                            } else if (timestampObj instanceof Double) {
                                timestamp = ((Double) timestampObj).longValue();
                                Log.d(TAG, "Timestamp is Double (converted to Long): " + timestamp);
                            } else if (timestampObj instanceof String) {
                                try {
                                    timestamp = Long.parseLong((String) timestampObj);
                                    Log.d(TAG, "Timestamp is String (parsed to Long): " + timestamp);
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Invalid timestamp format: " + timestampObj);
                                    // Use current time as fallback
                                    timestamp = System.currentTimeMillis();
                                    entry.put("timestamp", timestamp);
                                    Log.d(TAG, "Using current timestamp as fallback: " + timestamp);
                                }
                            } else if (timestampObj == null) {
                                // If timestamp is missing, add current time
                                timestamp = System.currentTimeMillis();
                                entry.put("timestamp", timestamp);
                                Log.d(TAG, "Timestamp was null, using current time: " + timestamp);
                            } else {
                                Log.e(TAG, "Unknown timestamp type: " + timestampObj.getClass().getName());
                            }

                            if (timestamp > 0) {
                                entry.put("date", dateFormat.format(new Date(timestamp)));

                                // Log mood entry details for debugging
                                String dateStr = dateFormat.format(new Date(timestamp));
                                Log.d(TAG, "VALID ENTRY - ID: " + entry.get("documentId") +
                                        ", Date: " + dateStr +
                                        ", Mood: " + entry.get("mood") +
                                        ", Score: " + entry.get("moodScore"));

                                moodEntries.add(entry);
                                entryCount++;
                            } else {
                                Log.w(TAG, "Skipping entry with invalid timestamp: " + document.getId());
                            }
                        }

                        Log.d(TAG, "Successfully processed " + entryCount + " mood entries");
                        Log.d(TAG, "Final moodEntries list size: " + moodEntries.size());

                        // Notify listener to update UI even if entries are empty
                        if (!moodEntries.isEmpty()) {
                            Log.d(TAG, "Updating analytics with non-empty data");
                            updateAnalytics();
                            updateChart();
                        } else {
                            if (listener != null) {
                                Log.w(TAG, "No valid mood entries to display");
                                listener.onAnalyticsUpdated("No data", "No data", "No data", new ArrayList<>());
                            }
                        }

                        // Always update history to show what we have
                        Log.d(TAG, "Updating mood history with " + moodEntries.size() + " entries");
                        updateMoodHistory();
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing mood data: " + e.getMessage(), e);
                        if (listener != null && listener instanceof AnalyticsFragment) {
                            ((AnalyticsFragment) listener).showError("Error processing mood data: " + e.getMessage());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading from Firestore: " + e.getMessage(), e);
                    if (listener != null && listener instanceof AnalyticsFragment) {
                        ((AnalyticsFragment) listener).showError("Error loading mood data: " + e.getMessage());
                    }

                    // Fall back to Realtime Database
                    loadFromRealtimeDatabase();
                });
    }

    private void loadFromRealtimeDatabase() {
        if (rtDatabase == null) {
            Log.e(TAG, "Realtime database reference is null");
            if (listener != null) {
                listener.onAnalyticsUpdated("No data", "No data", "No data", new ArrayList<>());
                listener.onMoodHistoryUpdated(new ArrayList<>());
            }
            return;
        }

        Log.d(TAG, "Falling back to Realtime Database for mood data");

        rtDatabase.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    moodEntries.clear();
                    int entryCount = 0;

                    Log.d(TAG, "Realtime Database returned " + dataSnapshot.getChildrenCount() + " entries");

                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            MoodEntry entry = snapshot.getValue(MoodEntry.class);
                            if (entry != null) {
                                Map<String, Object> entryMap = new HashMap<>();
                                entryMap.put("mood", entry.getMood());
                                entryMap.put("moodScore", entry.getMoodScore());
                                entryMap.put("note", entry.getNote());
                                entryMap.put("activities", entry.getActivities());
                                entryMap.put("timestamp", entry.getTimestamp());
                                entryMap.put("timeOfDay", entry.getTimeOfDay());
                                entryMap.put("documentId", entry.getDocumentId());
                                entryMap.put("userId", entry.getUserId());
                                entryMap.put("date", dateFormat.format(new Date(entry.getTimestamp())));

                                moodEntries.add(entryMap);
                                entryCount++;
                            }
                        }

                        Log.d(TAG, "Successfully processed " + entryCount + " entries from Realtime Database");

                        if (!moodEntries.isEmpty()) {
                            updateAnalytics();
                            updateChart();
                        } else {
                            if (listener != null) {
                                listener.onAnalyticsUpdated("No data", "No data", "No data", new ArrayList<>());
                            }
                            Log.w(TAG, "No valid mood entries found in Realtime Database");
                        }
                    } else {
                        if (listener != null) {
                            listener.onAnalyticsUpdated("No data", "No data", "No data", new ArrayList<>());
                        }
                        Log.w(TAG, "No entries exist in Realtime Database");
                    }

                    // Always update history to show what we have
                    updateMoodHistory();
                } catch (Exception e) {
                    Log.e(TAG, "Error loading from Realtime Database: " + e.getMessage(), e);
                    if (listener != null && listener instanceof AnalyticsFragment) {
                        ((AnalyticsFragment) listener).showError("Error processing mood data: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                if (listener != null && listener instanceof AnalyticsFragment) {
                    ((AnalyticsFragment) listener).showError("Database error: " + databaseError.getMessage());
                }
            }
        });
    }

    private void updateMoodHistory() {
        if (listener != null) {
            listener.onMoodHistoryUpdated(moodEntries);
        }
    }

    private void updateAnalytics() {
        if (moodEntries.isEmpty()) {
            if (listener != null) {
                listener.onAnalyticsUpdated("No data", "No data", "No data", new ArrayList<>());
            }
            return;
        }

        try {
            double totalMoodScore = 0;
            Map<String, Integer> moodCounts = new HashMap<>();
            Map<String, Integer> activityCounts = new HashMap<>();
            Map<String, Double> timeOfDayScores = new HashMap<>();
            Map<String, Integer> timeOfDayCounts = new HashMap<>();
            int validMoodEntries = 0;

            for (Map<String, Object> entry : moodEntries) {
                Object moodScoreObj = entry.get("moodScore");
                String mood = (String) entry.get("mood");

                Long moodScore = null;
                if (moodScoreObj instanceof Long) {
                    moodScore = (Long) moodScoreObj;
                } else if (moodScoreObj instanceof Integer) {
                    moodScore = ((Integer) moodScoreObj).longValue();
                } else if (moodScoreObj instanceof Double) {
                    moodScore = ((Double) moodScoreObj).longValue();
                }

                if (moodScore != null) {
                    totalMoodScore += moodScore;
                    validMoodEntries++;

                    if (mood != null) {
                        moodCounts.put(mood, moodCounts.getOrDefault(mood, 0) + 1);
                    }
                }

                Object activitiesObj = entry.get("activities");
                if (activitiesObj instanceof List) {
                    List<?> activities = (List<?>) activitiesObj;
                    for (Object activityObj : activities) {
                        if (activityObj instanceof String) {
                            String activity = (String) activityObj;
                            activityCounts.put(activity, activityCounts.getOrDefault(activity, 0) + 1);
                        }
                    }
                }

                String timeOfDay = (String) entry.get("timeOfDay");
                if (timeOfDay != null && moodScore != null) {
                    double currentScore = timeOfDayScores.getOrDefault(timeOfDay, 0.0);
                    int currentCount = timeOfDayCounts.getOrDefault(timeOfDay, 0);
                    timeOfDayScores.put(timeOfDay, currentScore + moodScore);
                    timeOfDayCounts.put(timeOfDay, currentCount + 1);
                }
            }

            String commonMood = "No data";
            if (!moodCounts.isEmpty()) {
                commonMood = moodCounts.entrySet().stream()
                        .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                        .map(Map.Entry::getKey)
                        .orElse("No data");
            }

            List<String> topActivities = new ArrayList<>();
            if (!activityCounts.isEmpty()) {
                List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(activityCounts.entrySet());
                sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

                int count = 0;
                for (Map.Entry<String, Integer> entry : sortedEntries) {
                    if (count < 3) {
                        topActivities.add(entry.getKey());
                        count++;
                    } else {
                        break;
                    }
                }
            }

            String mostProductiveTime = "No data";
            if (!timeOfDayCounts.isEmpty()) {
                mostProductiveTime = timeOfDayCounts.entrySet().stream()
                        .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                        .map(Map.Entry::getKey)
                        .orElse("No data");
            }

            String averageMood = "No data";
            if (validMoodEntries > 0) {
                averageMood = String.format("%.1f", totalMoodScore / validMoodEntries);
            }

            if (listener != null) {
                listener.onAnalyticsUpdated(averageMood, commonMood, mostProductiveTime, topActivities);
            }
        } catch (Exception e) {
            if (listener != null && listener instanceof AnalyticsFragment) {
                ((AnalyticsFragment) listener).showError("Error calculating analytics: " + e.getMessage());
            }
        }
    }

    private void updateChart() {
        if (moodEntries.isEmpty() || chart == null) return;

        try {
            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            for (int i = 0; i < moodEntries.size(); i++) {
                Map<String, Object> entry = moodEntries.get(i);
                Object moodScoreObj = entry.get("moodScore");

                Float moodScoreFloat = null;
                if (moodScoreObj instanceof Long) {
                    moodScoreFloat = ((Long) moodScoreObj).floatValue();
                } else if (moodScoreObj instanceof Integer) {
                    moodScoreFloat = ((Integer) moodScoreObj).floatValue();
                } else if (moodScoreObj instanceof Double) {
                    moodScoreFloat = ((Double) moodScoreObj).floatValue();
                }

                if (moodScoreFloat != null) {
                    entries.add(new Entry(i, moodScoreFloat));
                    labels.add((String) entry.getOrDefault("date", ""));
                }
            }

            if (entries.isEmpty()) return;

            LineDataSet dataSet = new LineDataSet(entries, "Mood Scores");
            dataSet.setColor(Color.BLUE);
            dataSet.setCircleColor(Color.BLUE);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawCircleHole(false);
            dataSet.setValueTextSize(16f);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(Color.BLUE);
            dataSet.setFillAlpha(100);

            dataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    if (value >= 3) {
                        return "😊";
                    } else if (value >= 2) {
                        return "😐";
                    } else {
                        return "😔";
                    }
                }
            });

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.getDescription().setEnabled(false);
            chart.setDrawGridBackground(false);
            chart.animateX(1000);

            XAxis xAxis = chart.getXAxis();
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    return index >= 0 && index < labels.size() ? labels.get(index) : "";
                }
            });
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            chart.getAxisLeft().setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    if (value >= 3) {
                        return "😊";
                    } else if (value >= 2) {
                        return "😐";
                    } else if (value >= 1) {
                        return "😔";
                    } else {
                        return "";
                    }
                }
            });

            chart.getAxisLeft().setAxisMinimum(0.5f);
            chart.getAxisLeft().setAxisMaximum(3.5f);
            chart.getAxisLeft().setLabelCount(3, true);

            chart.getAxisRight().setEnabled(false);

            chart.invalidate();
        } catch (Exception e) {
            if (listener != null && listener instanceof AnalyticsFragment) {
                ((AnalyticsFragment) listener).showError("Error updating chart: " + e.getMessage());
            }
        }
    }
}