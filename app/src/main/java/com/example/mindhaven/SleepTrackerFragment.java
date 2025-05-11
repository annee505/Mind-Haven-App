package com.example.mindhaven;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BarDataSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SleepTrackerFragment extends Fragment {
    private RatingBar sleepQualityRating;
    private TimePicker bedtimePicker;
    private TimePicker wakeTimePicker;
    private EditText notesInput;
    private TextView averageQualityText;
    private TextView averageDurationText;
    private TextView sleepScoreText;
    private TextView consistencyScoreText;
    private TextView sleepDebtText;
    private LineChart sleepChart;
    private BarChart weeklyChart;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private static final float OPTIMAL_SLEEP_HOURS = 8.0f;
    private static final float CONSISTENCY_THRESHOLD = 1.0f; // hour threshold for consistency

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sleep_tracker, container, false);

        initializeViews(view);
        setupCharts();
        loadSleepAnalytics();

        Button saveButton = view.findViewById(R.id.saveSleepButton);
        saveButton.setOnClickListener(v -> saveSleepData());

        return view;
    }

    private void initializeViews(View view) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        sleepQualityRating = view.findViewById(R.id.sleepQualityRating);
        bedtimePicker = view.findViewById(R.id.bedtimePicker);
        wakeTimePicker = view.findViewById(R.id.wakeTimePicker);
        notesInput = view.findViewById(R.id.sleepNotes);
        averageQualityText = view.findViewById(R.id.averageQualityText);
        averageDurationText = view.findViewById(R.id.averageDurationText);
        sleepScoreText = view.findViewById(R.id.sleepScoreText);
        consistencyScoreText = view.findViewById(R.id.consistencyScoreText);
        sleepDebtText = view.findViewById(R.id.sleepDebtText);
        sleepChart = view.findViewById(R.id.sleepChart);
        weeklyChart = view.findViewById(R.id.weeklyChart);
    }

    private void setupCharts() {
        sleepChart.getDescription().setEnabled(false);
        sleepChart.setTouchEnabled(true);
        sleepChart.setDragEnabled(true);
        sleepChart.setScaleEnabled(true);

        weeklyChart.getDescription().setEnabled(false);
        weeklyChart.setTouchEnabled(true);
        weeklyChart.setDragEnabled(true);
        weeklyChart.setScaleEnabled(true);
    }

    private void saveSleepData() {
        if (mAuth.getCurrentUser() == null) return;

        Calendar bedtime = Calendar.getInstance();
        bedtime.set(Calendar.HOUR_OF_DAY, bedtimePicker.getHour());
        bedtime.set(Calendar.MINUTE, bedtimePicker.getMinute());

        Calendar waketime = Calendar.getInstance();
        waketime.set(Calendar.HOUR_OF_DAY, wakeTimePicker.getHour());
        waketime.set(Calendar.MINUTE, wakeTimePicker.getMinute());

        float duration = calculateDuration(bedtime, waketime);
        float quality = sleepQualityRating.getRating();
        float sleepScore = calculateSleepScore(duration, quality);

        Map<String, Object> sleepData = new HashMap<>();
        sleepData.put("quality", quality);
        sleepData.put("duration", duration);
        sleepData.put("bedtime", bedtime.getTime());
        sleepData.put("waketime", waketime.getTime());
        sleepData.put("notes", notesInput.getText().toString());
        sleepData.put("timestamp", new Date());
        sleepData.put("sleepScore", sleepScore);

        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("sleep_tracking")
                .add(sleepData)
                .addOnSuccessListener(documentReference -> {
                    loadSleepAnalytics();
                    provideSleepRecommendations(duration, quality);
                });
    }

    private float calculateDuration(Calendar bedtime, Calendar waketime) {
        long diff = waketime.getTimeInMillis() - bedtime.getTimeInMillis();
        return diff / (1000 * 60 * 60f);
    }

    private float calculateSleepScore(float duration, float quality) {
        float durationScore = Math.max(0, 100 - Math.abs(duration - OPTIMAL_SLEEP_HOURS) * 10);
        float qualityScore = quality * 20; // Convert 5-star rating to 100-point scale
        return (durationScore + qualityScore) / 2;
    }

    private void calculateSleepDebt(List<Float> durations) {
        float totalDebt = 0;
        for (Float duration : durations) {
            if (duration < OPTIMAL_SLEEP_HOURS) {
                totalDebt += (OPTIMAL_SLEEP_HOURS - duration);
            }
        }
        sleepDebtText.setText(String.format("Sleep Debt: %.1f hours", totalDebt));
    }

    private float calculateConsistencyScore(List<Date> bedtimes) {
        if (bedtimes == null || bedtimes.size() < 2) return 100;

        int consistentNights = 0;
        int validComparisons = 0;

        for (int i = 1; i < bedtimes.size(); i++) {
            Date current = bedtimes.get(i);
            Date previous = bedtimes.get(i-1);

            if (current == null || previous == null) continue;

            validComparisons++;
            long diff = Math.abs(current.getTime() - previous.getTime());
            float hourDiff = diff / (1000 * 60 * 60f);
            if (hourDiff <= CONSISTENCY_THRESHOLD) {
                consistentNights++;
            }
        }
        return validComparisons > 0 ? (consistentNights / (float)validComparisons) * 100 : 100;
    }

    private void loadSleepAnalytics() {
        if (mAuth.getCurrentUser() == null) return;

        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("sleep_tracking")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(30) // Last 30 days
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Entry> qualityEntries = new ArrayList<>();
                    List<Entry> durationEntries = new ArrayList<>();
                    List<BarEntry> weeklyEntries = new ArrayList<>();
                    List<Float> durations = new ArrayList<>();
                    List<Date> bedtimes = new ArrayList<>();

                    float totalQuality = 0;
                    float totalDuration = 0;
                    float totalScore = 0;
                    int count = 0;

                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        Map<String, Object> data = queryDocumentSnapshots.getDocuments().get(i).getData();
                        float quality = 0f;
                        float duration = 0f;
                        float score = 0f;
                        Date bedtime = null;

                        Object qualityObj = data.get("quality");
                        if (qualityObj != null) {
                            String qualityStr = qualityObj.toString().trim();
                            if (!qualityStr.isEmpty()) {
                                quality = qualityObj instanceof Number ?
                                        ((Number) qualityObj).floatValue() :
                                        Float.parseFloat(qualityStr);
                            }
                        }

                        Object durationObj = data.get("duration");
                        if (durationObj != null) {
                            String durationStr = durationObj.toString().trim();
                            if (!durationStr.isEmpty()) {
                                duration = durationObj instanceof Number ?
                                        ((Number) durationObj).floatValue() :
                                        Float.parseFloat(durationStr);
                            }
                        }

                        Object scoreObj = data.get("sleepScore");
                        if (scoreObj != null) {
                            String scoreStr = scoreObj.toString().trim();
                            if (!scoreStr.isEmpty()) {
                                score = scoreObj instanceof Number ?
                                        ((Number) scoreObj).floatValue() :
                                        Float.parseFloat(scoreStr);
                            }
                        }

                        Object bedtimeObj = data.get("bedtime");
                        if (bedtimeObj instanceof Date) {
                            bedtime = (Date) bedtimeObj;
                        }

                        qualityEntries.add(new Entry(i, quality));
                        durationEntries.add(new Entry(i, duration));
                        weeklyEntries.add(new BarEntry(i, new float[]{duration, quality}));
                        durations.add(duration);
                        bedtimes.add(bedtime);

                        totalQuality += quality;
                        totalDuration += duration;
                        totalScore += score;
                        count++;
                    }

                    if (count > 0) {
                        float avgQuality = totalQuality / count;
                        float avgDuration = totalDuration / count;
                        float avgScore = totalScore / count;
                        float consistencyScore = calculateConsistencyScore(bedtimes);

                        updateAnalyticsDisplay(avgQuality, avgDuration, avgScore, consistencyScore);
                        updateCharts(qualityEntries, durationEntries, weeklyEntries);
                        calculateSleepDebt(durations);
                    }
                });
    }

    private void updateAnalyticsDisplay(float avgQuality, float avgDuration, float avgScore, float consistencyScore) {
        averageQualityText.setText(String.format("Average Sleep Quality: %.1f", avgQuality));
        averageDurationText.setText(String.format("Average Sleep Duration: %.1f hours", avgDuration));
        sleepScoreText.setText(String.format("Sleep Score: %.1f", avgScore));
        consistencyScoreText.setText(String.format("Sleep Consistency: %.1f%%", consistencyScore));
    }

    private void updateCharts(List<Entry> qualityEntries, List<Entry> durationEntries, List<BarEntry> weeklyEntries) {
        // Line chart for trends
        LineDataSet qualityDataSet = new LineDataSet(qualityEntries, "Sleep Quality");
        qualityDataSet.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        qualityDataSet.setCircleColor(getResources().getColor(android.R.color.holo_blue_dark));

        LineDataSet durationDataSet = new LineDataSet(durationEntries, "Sleep Duration");
        durationDataSet.setColor(getResources().getColor(android.R.color.holo_green_dark));
        durationDataSet.setCircleColor(getResources().getColor(android.R.color.holo_green_dark));

        LineData lineData = new LineData(qualityDataSet, durationDataSet);
        sleepChart.setData(lineData);
        sleepChart.invalidate();

        // Bar chart for weekly view
        BarDataSet barDataSet = new BarDataSet(weeklyEntries, "Weekly Sleep Pattern");
        barDataSet.setColors(
                getResources().getColor(android.R.color.holo_blue_light),
                getResources().getColor(android.R.color.holo_green_light)
        );

        BarData barData = new BarData(barDataSet);
        weeklyChart.setData(barData);
        weeklyChart.invalidate();
    }

    private void provideSleepRecommendations(float duration, float quality) {
        StringBuilder recommendations = new StringBuilder();

        if (duration < 7) {
            recommendations.append("Try to get more sleep - aim for 7-9 hours.\n");
        }
        if (quality < 3) {
            recommendations.append("To improve sleep quality:\n");
            recommendations.append("- Maintain a consistent sleep schedule\n");
            recommendations.append("- Avoid screens before bedtime\n");
            recommendations.append("- Keep your bedroom cool and dark\n");
        }

        if (recommendations.length() > 0) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
            builder.setTitle("Sleep Recommendations")
                    .setMessage(recommendations.toString())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}