package com.example.mindhaven;

import android.graphics.Color;
import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
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

    public interface AnalyticsUpdateListener {
        void onAnalyticsUpdated(String averageMood, String commonMood, String productiveTime, 
                              List<String> topActivities);
    }

    public MoodAnalyticsManager(AnalyticsUpdateListener listener, LineChart chart) {
        this.listener = listener;
        this.chart = chart;
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        moodEntries = new ArrayList<>();
    }

    public void loadMoodData() {
        db.collection("users")
            .document(userId)
            .collection("mood_tracking")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                moodEntries.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    moodEntries.add(document.getData());
                }
                updateAnalytics();
                updateChart();
            });
    }

    private void updateAnalytics() {
        if (moodEntries.isEmpty()) {
            if (listener != null) {
                listener.onAnalyticsUpdated("No data", "No data", "No data", new ArrayList<>());
            }
            return;
        }


        double totalMoodScore = 0;
        Map<String, Integer> moodCounts = new HashMap<>();
        Map<String, Integer> activityCounts = new HashMap<>();
        Map<String, Double> timeOfDayScores = new HashMap<>();
        Map<String, Integer> timeOfDayCounts = new HashMap<>();

        for (Map<String, Object> entry : moodEntries) {

            Long moodScore = (Long) entry.get("moodScore");
            String mood = (String) entry.get("mood");
            if (moodScore != null) {
                totalMoodScore += moodScore;
                moodCounts.put(mood, moodCounts.getOrDefault(mood, 0) + 1);
            }


            List<String> activities = (List<String>) entry.get("activities");
            if (activities != null) {
                for (String activity : activities) {
                    activityCounts.put(activity, activityCounts.getOrDefault(activity, 0) + 1);
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


        double averageMood = totalMoodScore / moodEntries.size();
        String commonMood = getMaxKey(moodCounts);

        String productiveTime = "No data";
        double maxAverage = 0;
        for (Map.Entry<String, Double> entry : timeOfDayScores.entrySet()) {
            String timeOfDay = entry.getKey();
            double average = entry.getValue() / timeOfDayCounts.get(timeOfDay);
            if (average > maxAverage) {
                maxAverage = average;
                productiveTime = timeOfDay;
            }
        }


        List<String> topActivities = getTopActivities(activityCounts, 3);

        if (listener != null) {
            String moodLevel = averageMood >= 2.5 ? "Happy" : 
                             averageMood >= 1.5 ? "Neutral" : "Sad";
            listener.onAnalyticsUpdated(
                String.format("%.1f (%s)", averageMood, moodLevel),
                commonMood,
                productiveTime,
                topActivities
            );
        }
    }

    public void updateChart(@NonNull LineChart chart) {
        if (moodEntries.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No mood data available");
            return;
        }

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < moodEntries.size(); i++) {
            Long moodScore = (Long) moodEntries.get(i).get("moodScore");
            if (moodScore != null) {
                entries.add(new Entry(i, moodScore));
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Mood Over Time");
        styleDataSet(dataSet);

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);


        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < moodEntries.size()) {
                    Long timestamp = (Long) moodEntries.get(index).get("timestamp");
                    return dateFormat.format(new Date(timestamp));
                }
                return "";
            }
        });

        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }

    private void styleDataSet(LineDataSet dataSet) {
        dataSet.setColor(Color.rgb(139, 69, 19));
        dataSet.setCircleColor(Color.rgb(139, 69, 19));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.rgb(222, 184, 135));
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
    }

    private String getMaxKey(Map<String, Integer> map) {
        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No data");
    }

    private List<String> getTopActivities(Map<String, Integer> activityCounts, int limit) {
        return activityCounts.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private void updateChart() {
        if (chart != null) {
            updateChart(chart);
        }
    }
}