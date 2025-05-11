package com.example.mindhaven;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;

public class AnalyticsFragment extends Fragment implements MoodAnalyticsManager.AnalyticsUpdateListener {
    private LineChart moodChart;
    private PieChart moodDistributionChart;
    private MoodAnalyticsManager analyticsManager;
    private TextView averageMoodText, commonMoodText, productiveTimeText, topActivitiesText;
    private TextView dateRangeText;
    private Button filterButton, exportButton;
    private Spinner chartTypeSpinner;
    private RecyclerView moodHistoryRecyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private long startDate = 0;
    private long endDate = System.currentTimeMillis();
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private List<Map<String, Object>> allMoodEntries = new ArrayList<>();

    public AnalyticsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        moodChart = view.findViewById(R.id.moodChart);
        moodDistributionChart = view.findViewById(R.id.moodDistributionChart);
        averageMoodText = view.findViewById(R.id.averageMoodText);
        commonMoodText = view.findViewById(R.id.commonMoodText);
        productiveTimeText = view.findViewById(R.id.productiveTimeText);
        topActivitiesText = view.findViewById(R.id.topActivitiesText);
        
        dateRangeText = view.findViewById(R.id.dateRangeText);
        filterButton = view.findViewById(R.id.filterButton);
        exportButton = view.findViewById(R.id.exportButton);
        chartTypeSpinner = view.findViewById(R.id.chartTypeSpinner);
        

        startDate = 0;
        endDate = System.currentTimeMillis();
        updateDateRangeText();
        

        filterButton.setOnClickListener(v -> showDatePickerDialog());
        
        // Set up export button
        exportButton.setOnClickListener(v -> exportMoodData());
        
        // Set up mood history recycler view
        moodHistoryRecyclerView = view.findViewById(R.id.moodHistoryRecyclerView);
        moodHistoryAdapter = new MoodHistoryAdapter();
        moodHistoryRecyclerView.setAdapter(moodHistoryAdapter);
        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Set up chart type spinner
        chartTypeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                updateChartVisibility(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // Add an "empty data" view with retry button
        View emptyView = view.findViewById(R.id.emptyDataView);
        Button retryButton = view.findViewById(R.id.retryButton);
        if (retryButton != null) {
            retryButton.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Trying all data sources and showing ALL entries...", Toast.LENGTH_SHORT).show();
                
                // Reset date filter to show ALL entries
                startDate = 0;
                endDate = System.currentTimeMillis();
                updateDateRangeText();

                FirebaseAnalyticsHelper analyticsHelper = new FirebaseAnalyticsHelper();
                analyticsHelper.syncDatabases(success -> {
                    // Then try manual refresh which gets data directly from Firestore
                    manualRefresh();
                });
            });
        }

        // Add a "show all" button below the date range text
        Button showAllButton = view.findViewById(R.id.showAllEntriesButton);
        if (showAllButton == null) {
            // If button doesn't exist in layout, add it programmatically
            showAllButton = new Button(getContext());
            showAllButton.setId(View.generateViewId());
            showAllButton.setText("Show All Entries");
            showAllButton.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            
            // Find where to insert the button (after date range elements)
            ViewGroup parent = (ViewGroup) dateRangeText.getParent();
            int dateRangeIndex = 0;
            for (int i = 0; i < parent.getChildCount(); i++) {
                if (parent.getChildAt(i) == dateRangeText) {
                    dateRangeIndex = i;
                    break;
                }
            }
            parent.addView(showAllButton, dateRangeIndex + 2); // After dateRangeText and filterButton
        }
        
        showAllButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Showing ALL mood entries regardless of date", Toast.LENGTH_SHORT).show();
            startDate = 0;
            endDate = System.currentTimeMillis();
            updateDateRangeText();
            
            // Show all entries from our complete list
            if (!allMoodEntries.isEmpty()) {
                moodHistoryAdapter.updateEntries(allMoodEntries);
                calculateAnalytics(allMoodEntries);
                updateLineChart(allMoodEntries);
                updatePieChart();
            } else {
                // If we don't have entries yet, do a manual refresh
                manualRefresh();
            }
        });

        // Force data synchronization before loading analytics
        FirebaseAnalyticsHelper analyticsHelper = new FirebaseAnalyticsHelper();
        analyticsHelper.syncDatabases(success -> {
            if (getContext() == null) return; // Fragment might be detached
            
            Toast.makeText(getContext(), "Data sync " + (success ? "successful" : "failed") + ", loading analytics now", Toast.LENGTH_SHORT).show();
            
            // Now load the analytics data
            analyticsManager = new MoodAnalyticsManager(this, moodChart);
            analyticsManager.loadMoodData();
        });

        return view;
    }
    
    private void updateChartVisibility(int position) {
        if (position == 0) {  // Line chart
            moodChart.setVisibility(View.VISIBLE);
            moodDistributionChart.setVisibility(View.GONE);
        } else {  // Pie chart
            moodChart.setVisibility(View.GONE);
            moodDistributionChart.setVisibility(View.VISIBLE);
            updatePieChart();
        }
    }
    
    private void updateDateRangeText() {
        if (startDate == 0) {
            dateRangeText.setText("Showing ALL entries (no date filter)");
        } else {
            String startDateStr = sdf.format(new Date(startDate));
            String endDateStr = sdf.format(new Date(endDate));
            dateRangeText.setText(String.format("From %s to %s", startDateStr, endDateStr));
        }
    }
    
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startDate);
        
        DatePickerDialog startDatePicker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar startCal = Calendar.getInstance();
                    startCal.set(year, month, dayOfMonth);
                    startDate = startCal.getTimeInMillis();
                    
                    // After setting start date, show end date picker
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTimeInMillis(endDate);
                    
                    DatePickerDialog endDatePicker = new DatePickerDialog(
                            requireContext(),
                            (view2, endYear, endMonth, endDayOfMonth) -> {
                                Calendar newEndCal = Calendar.getInstance();
                                newEndCal.set(endYear, endMonth, endDayOfMonth);
                                newEndCal.set(Calendar.HOUR_OF_DAY, 23);
                                newEndCal.set(Calendar.MINUTE, 59);
                                newEndCal.set(Calendar.SECOND, 59);
                                endDate = newEndCal.getTimeInMillis();
                                
                                updateDateRangeText();
                                filterMoodData();
                            },
                            endCal.get(Calendar.YEAR),
                            endCal.get(Calendar.MONTH),
                            endCal.get(Calendar.DAY_OF_MONTH)
                    );
                    
                    endDatePicker.getDatePicker().setMinDate(startCal.getTimeInMillis());
                    endDatePicker.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        startDatePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        startDatePicker.show();
    }
    
    private void filterMoodData() {
        if (allMoodEntries.isEmpty()) {
            Toast.makeText(getContext(), "No data to filter", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Set time boundaries for more precise filtering
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(startDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        
        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        
        long filteredStartTime = startCal.getTimeInMillis();
        long filteredEndTime = endCal.getTimeInMillis();
        
        List<Map<String, Object>> filteredEntries = new ArrayList<>();
        
        // Debug information
        SimpleDateFormat debugFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Log.d("AnalyticsFragment", "Filtering from " + 
              debugFormat.format(new Date(filteredStartTime)) + " to " + 
              debugFormat.format(new Date(filteredEndTime)));
        
        for (Map<String, Object> entry : allMoodEntries) {
            long timestamp = 0;
            Object timestampObj = entry.get("timestamp");
            
            if (timestampObj instanceof Long) {
                timestamp = (Long) timestampObj;
            } else if (timestampObj instanceof Double) {
                timestamp = ((Double) timestampObj).longValue();
            } else if (timestampObj instanceof String) {
                try {
                    timestamp = Long.parseLong((String) timestampObj);
                } catch (NumberFormatException e) {
                    Log.e("AnalyticsFragment", "Invalid timestamp format: " + timestampObj);
                }
            }
            
            if (timestamp > 0) {
                Log.d("AnalyticsFragment", "Entry timestamp: " + debugFormat.format(new Date(timestamp)) + 
                    ", mood: " + entry.get("mood"));
                
                // Check if timestamp is within filter range
                if (timestamp >= filteredStartTime && timestamp <= filteredEndTime) {
                    filteredEntries.add(entry);
                    Log.d("AnalyticsFragment", "Entry MATCHES filter");
                } else {
                    Log.d("AnalyticsFragment", "Entry outside filter range");
                }
            }
        }
        
        Toast.makeText(getContext(), "Found " + filteredEntries.size() + " entries for selected date range", 
                Toast.LENGTH_SHORT).show();
        
        if (filteredEntries.isEmpty() && !allMoodEntries.isEmpty()) {

            new AlertDialog.Builder(requireContext())
                .setTitle("No data in selected range")
                .setMessage("No mood entries found for the selected date range. Would you like to view all your mood entries instead?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Reset date filter
                    startDate = 0;
                    Calendar cal = Calendar.getInstance();
                    endDate = cal.getTimeInMillis();
                    updateDateRangeText();
                    
                    // Show all entries
                    moodHistoryAdapter.updateEntries(allMoodEntries);
                    calculateAnalytics(allMoodEntries);
                    updateLineChart(allMoodEntries);
                    updatePieChart();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
        } else {
            updateAnalyticsWithFilteredData(filteredEntries);
        }
    }
    
    private void updateAnalyticsWithFilteredData(List<Map<String, Object>> filteredEntries) {
        if (filteredEntries.isEmpty()) {
            Toast.makeText(getContext(), "No data in selected date range", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Update mood history adapter
        moodHistoryAdapter.updateEntries(filteredEntries);
        
        // Calculate analytics
        calculateAnalytics(filteredEntries);
        
        // Update charts
        int selectedChartType = chartTypeSpinner.getSelectedItemPosition();
        if (selectedChartType == 0) {
            updateLineChart(filteredEntries);
        } else {
            updatePieChart();
        }
    }
    
    private void calculateAnalytics(List<Map<String, Object>> entries) {
        try {
            double totalMoodScore = 0;
            Map<String, Integer> moodCounts = new HashMap<>();
            Map<String, Integer> activityCounts = new HashMap<>();
            Map<String, Double> timeOfDayScores = new HashMap<>();
            Map<String, Integer> timeOfDayCounts = new HashMap<>();
            int validMoodEntries = 0;

            for (Map<String, Object> entry : entries) {
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
                int maxCount = 0;
                for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
                    if (entry.getValue() > maxCount) {
                        maxCount = entry.getValue();
                        commonMood = entry.getKey();
                    }
                }
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
                int maxCount = 0;
                for (Map.Entry<String, Integer> entry : timeOfDayCounts.entrySet()) {
                    if (entry.getValue() > maxCount) {
                        maxCount = entry.getValue();
                        mostProductiveTime = entry.getKey();
                    }
                }
            }

            String averageMood = "No data";
            if (validMoodEntries > 0) {
                averageMood = String.format("%.1f", totalMoodScore / validMoodEntries);
            }

            // Create final copies of all the variables needed in the lambda
            final String finalAverageMood = averageMood;
            final String finalCommonMood = commonMood;
            final String finalMostProductiveTime = mostProductiveTime;
            final List<String> finalTopActivities = new ArrayList<>(topActivities);

            // Update UI
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (averageMoodText != null) {
                        String emojiAverage = getEmojiForMoodValue(finalAverageMood);
                        averageMoodText.setText("Average Mood: " + emojiAverage);
                    }
                    if (commonMoodText != null) {
                        String emojiCommon = getEmojiForMoodType(finalCommonMood);
                        commonMoodText.setText("Common Mood: " + emojiCommon);
                    }
                    if (productiveTimeText != null) {
                        productiveTimeText.setText("Most Productive: " + finalMostProductiveTime);
                    }
                    if (topActivitiesText != null) {
                        topActivitiesText.setText("Top Activities: " + String.join(", ", finalTopActivities));
                    }
                });
            }
        } catch (Exception e) {
            showError("Error calculating analytics: " + e.getMessage());
        }
    }
    
    private String getEmojiForMoodType(String mood) {
        if (mood == null || mood.equals("No data")) {
            return "No data";
        }
        
        switch (mood) {
            case "Happy":
                return "😊 Happy";
            case "Neutral":
                return "😐 Neutral";
            case "Sad":
                return "😢 Sad";
            default:
                return mood;
        }
    }
    
    private String getEmojiForMoodValue(String moodValue) {
        if (moodValue == null || moodValue.equals("No data")) {
            return "No data";
        }
        
        try {
            float value = Float.parseFloat(moodValue);
            if (value >= 2.5) {
                return moodValue + " 😊";
            } else if (value >= 1.5) {
                return moodValue + " 😐";
            } else {
                return moodValue + " 😢";
            }
        } catch (NumberFormatException e) {
            return moodValue;
        }
    }
    
    private void updateLineChart(List<Map<String, Object>> entries) {
        if (entries.isEmpty() || moodChart == null) return;

        try {
            moodChart.clear();
            
            List<com.github.mikephil.charting.data.Entry> chartEntries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            List<String> valueLabels = new ArrayList<>(); // To store emoji labels for data points
            
            SimpleDateFormat chartDateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

            for (int i = 0; i < entries.size(); i++) {
                Map<String, Object> entry = entries.get(i);
                Object moodScoreObj = entry.get("moodScore");
                Object timestampObj = entry.get("timestamp");
                String mood = (String) entry.get("mood");
                
                Float moodScoreFloat = null;
                if (moodScoreObj instanceof Long) {
                    moodScoreFloat = ((Long) moodScoreObj).floatValue();
                } else if (moodScoreObj instanceof Integer) {
                    moodScoreFloat = ((Integer) moodScoreObj).floatValue();
                } else if (moodScoreObj instanceof Double) {
                    moodScoreFloat = ((Double) moodScoreObj).floatValue();
                }
                
                if (moodScoreFloat != null && timestampObj instanceof Long) {
                    long timestamp = (Long) timestampObj;
                    chartEntries.add(new com.github.mikephil.charting.data.Entry(i, moodScoreFloat));
                    labels.add(chartDateFormat.format(new Date(timestamp)));
                    
                    // Add emoji based on mood or score
                    String emoji;
                    if (mood != null) {
                        switch (mood) {
                            case "Happy":
                                emoji = "😊";
                                break;
                            case "Neutral":
                                emoji = "😐";
                                break;
                            case "Sad":
                                emoji = "😢";
                                break;
                            default:
                                emoji = getMoodEmoji(moodScoreFloat.intValue());
                        }
                    } else {
                        emoji = getMoodEmoji(moodScoreFloat.intValue());
                    }
                    valueLabels.add(emoji);
                }
            }
            
            if (chartEntries.isEmpty()) return;
            
            com.github.mikephil.charting.data.LineDataSet dataSet = new com.github.mikephil.charting.data.LineDataSet(chartEntries, "Mood");
            dataSet.setColor(Color.BLUE);
            dataSet.setCircleColor(Color.BLUE);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawCircleHole(false);
            dataSet.setValueTextSize(16f);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(Color.BLUE);
            dataSet.setFillAlpha(100);
            
            // Custom value formatter to show emojis instead of numbers
            dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    for (com.github.mikephil.charting.data.Entry entry : chartEntries) {
                        if (entry.getX() == index) {
                            int position = chartEntries.indexOf(entry);
                            if (position >= 0 && position < valueLabels.size()) {
                                return valueLabels.get(position);
                            }
                        }
                    }
                    return getMoodEmoji((int) value);
                }
            });
            
            com.github.mikephil.charting.data.LineData lineData = new com.github.mikephil.charting.data.LineData(dataSet);
            
            // Format X-axis labels
            XAxis xAxis = moodChart.getXAxis();
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    if (index >= 0 && index < labels.size()) {
                        return labels.get(index);
                    }
                    return "";
                }
            });
            
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setLabelRotationAngle(-45);
            
            moodChart.setData(lineData);
            moodChart.getDescription().setEnabled(false);
            moodChart.getLegend().setEnabled(false);
            moodChart.setExtraOffsets(10, 10, 10, 20);
            moodChart.animateY(1000);
            moodChart.invalidate();
        } catch (Exception e) {
            showError("Error updating chart: " + e.getMessage());
        }
    }
    
    private String getMoodEmoji(int score) {
        switch (score) {
            case 3:
                return "😊";
            case 2:
                return "😐";
            case 1:
                return "😢";
            default:
                return "❓";
        }
    }
    
    private void updatePieChart() {
        if (moodHistoryAdapter == null || moodDistributionChart == null) return;
        
        List<Map<String, Object>> entries = moodHistoryAdapter.getEntries();
        if (entries.isEmpty()) return;
        
        try {
            moodDistributionChart.clear();
            
            Map<String, Integer> moodCounts = new HashMap<>();
            Map<String, String> moodEmojis = new HashMap<>();
            
            // Define mood types and their emojis
            moodEmojis.put("Happy", "😊 Happy");
            moodEmojis.put("Neutral", "😐 Neutral");
            moodEmojis.put("Sad", "😢 Sad");
            
            for (Map<String, Object> entry : entries) {
                String mood = (String) entry.get("mood");
                if (mood != null) {
                    moodCounts.put(mood, moodCounts.getOrDefault(mood, 0) + 1);
                }
            }
            
            List<PieEntry> pieEntries = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
                String label = moodEmojis.getOrDefault(entry.getKey(), entry.getKey());
                pieEntries.add(new PieEntry(entry.getValue(), label));
            }
            
            if (pieEntries.isEmpty()) return;
            
            PieDataSet dataSet = new PieDataSet(pieEntries, "");
            int[] colors = {
                Color.rgb(102, 225, 102), // Green for happy
                Color.rgb(255, 204, 0),   // Yellow for neutral
                Color.rgb(255, 102, 102)  // Red for sad
            };
            dataSet.setColors(colors);
            dataSet.setValueTextColor(Color.WHITE);
            dataSet.setValueTextSize(14f);
            
            PieData data = new PieData(dataSet);
            
            moodDistributionChart.setData(data);
            moodDistributionChart.getDescription().setEnabled(false);
            moodDistributionChart.setCenterText("Mood\nDistribution");
            moodDistributionChart.setCenterTextSize(16f);
            moodDistributionChart.setHoleRadius(40f);
            moodDistributionChart.setTransparentCircleRadius(45f);
            moodDistributionChart.animateY(1000);
            moodDistributionChart.invalidate();
        } catch (Exception e) {
            showError("Error updating pie chart: " + e.getMessage());
        }
    }
    
    private void exportMoodData() {
        if (moodHistoryAdapter == null || moodHistoryAdapter.getEntries().isEmpty()) {
            Toast.makeText(getContext(), "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            List<Map<String, Object>> entries = moodHistoryAdapter.getEntries();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            
            StringBuilder csvData = new StringBuilder();
            // Add CSV header
            csvData.append("Date,Mood,Score,Time of Day,Note,Activities\n");
            
            // Add data rows
            for (Map<String, Object> entry : entries) {
                long timestamp = entry.containsKey("timestamp") ? (long) entry.get("timestamp") : 0;
                String dateStr = timestamp > 0 ? dateFormat.format(new Date(timestamp)) : "Unknown";
                String mood = (String) entry.getOrDefault("mood", "");
                Object moodScoreObj = entry.get("moodScore");
                String moodScore = moodScoreObj != null ? moodScoreObj.toString() : "";
                String timeOfDay = (String) entry.getOrDefault("timeOfDay", "");
                String note = (String) entry.getOrDefault("note", "");
                // Clean commas from note to avoid CSV issues
                note = note.replace(",", ";");
                
                // Handle activities list
                List<String> activities = new ArrayList<>();
                Object activitiesObj = entry.get("activities");
                if (activitiesObj instanceof List) {
                    for (Object act : (List<?>) activitiesObj) {
                        if (act instanceof String) {
                            activities.add(((String) act).replace(",", ";"));
                        }
                    }
                }
                String activitiesStr = String.join(";", activities);
                
                // Add row to CSV
                csvData.append(String.format("%s,%s,%s,%s,%s,%s\n", 
                        dateStr, mood, moodScore, timeOfDay, note, activitiesStr));
            }
            
            // Create file in external storage
            String fileName = "MoodData_" + fileNameFormat.format(new Date()) + ".csv";
            File exportDir = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "MoodData");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            File file = new File(exportDir, fileName);
            
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(csvData.toString().getBytes());
            fos.close();
            
            // Share the file
            shareFile(file);
            
            Toast.makeText(getContext(), "Data exported to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            showError("Error exporting data: " + e.getMessage());
        }
    }
    
    private void shareFile(File file) {
        try {
            Uri fileUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.mindhaven.fileprovider",
                    file);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "MindHaven Mood Data");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Share Mood Data"));
        } catch (Exception e) {
            showError("Error sharing file: " + e.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (analyticsManager != null) {
            // Force a fresh load each time we resume
            allMoodEntries.clear();
            
            // First synchronize databases to ensure latest data
            FirebaseAnalyticsHelper helper = new FirebaseAnalyticsHelper();
            helper.syncDatabases(success -> {
                // Then load mood data
                analyticsManager.loadMoodData();
                
                // Display a loading message
                Toast.makeText(getContext(), "Loading your mood data...", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onAnalyticsUpdated(String averageMood, String commonMood,
                                   String productiveTime, List<String> topActivities) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (averageMoodText != null) {
                    String emojiAverage = getEmojiForMoodValue(averageMood);
                    averageMoodText.setText("Average Mood: " + emojiAverage);
                }
                if (commonMoodText != null) {
                    String emojiCommon = getEmojiForMoodType(commonMood);
                    commonMoodText.setText("Common Mood: " + emojiCommon);
                }
                if (productiveTimeText != null) {
                    productiveTimeText.setText("Most Productive: " + productiveTime);
                }
                if (topActivitiesText != null) {
                    topActivitiesText.setText("Top Activities: " + String.join(", ", topActivities));
                }
            });
        }
    }

    @Override
    public void onMoodHistoryUpdated(List<Map<String, Object>> moodEntries) {
        // Store all entries for filtering
        allMoodEntries = new ArrayList<>(moodEntries);
        
        // Show count of all mood entries
        if (getContext() != null) {
            Toast.makeText(getContext(), "Found " + moodEntries.size() + " mood entries in total", Toast.LENGTH_SHORT).show();
            
            // If no entries were found, create a "No Data" toast that stays on screen longer
            if (moodEntries.isEmpty()) {
                Toast.makeText(getContext(), "NO MOOD DATA FOUND IN YOUR ACCOUNT. Try adding a mood entry first.", Toast.LENGTH_LONG).show();
            }
        }
        
        // Apply date filter only if a specific date range is set (startDate > 0)
        List<Map<String, Object>> filteredEntries;
        if (startDate > 0) {
            filteredEntries = new ArrayList<>();
            
            // Debug timestamps for filter
            SimpleDateFormat debugFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Log.d("AnalyticsFragment", "Filtering from " + 
                debugFormat.format(new Date(startDate)) + " to " + 
                debugFormat.format(new Date(endDate)));
            
            // Set time boundaries for more precise filtering
            Calendar startCal = Calendar.getInstance();
            startCal.setTimeInMillis(startDate);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            
            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis(endDate);
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);
            endCal.set(Calendar.MILLISECOND, 999);
            
            long filteredStartTime = startCal.getTimeInMillis();
            long filteredEndTime = endCal.getTimeInMillis();
            
            for (Map<String, Object> entry : moodEntries) {
                Object timestampObj = entry.get("timestamp");
                long timestamp = 0;
                
                if (timestampObj instanceof Long) {
                    timestamp = (Long) timestampObj;
                } else if (timestampObj instanceof Double) {
                    timestamp = ((Double) timestampObj).longValue();
                } else if (timestampObj instanceof String) {
                    try {
                        timestamp = Long.parseLong((String) timestampObj);
                    } catch (NumberFormatException e) {
                        Log.e("AnalyticsFragment", "Invalid timestamp: " + timestampObj);
                    }
                }
                
                if (timestamp > 0) {
                    Log.d("AnalyticsFragment", "Entry timestamp: " + debugFormat.format(new Date(timestamp)) + 
                        ", mood: " + entry.get("mood"));
                    
                    // Check if timestamp is within filter range
                    if (timestamp >= filteredStartTime && timestamp <= filteredEndTime) {
                        filteredEntries.add(entry);
                        Log.d("AnalyticsFragment", "Entry MATCHES filter");
                    } else {
                        Log.d("AnalyticsFragment", "Entry outside filter range");
                    }
                }
            }
            
            // Show count of filtered entries
            if (getContext() != null) {
                Toast.makeText(getContext(), filteredEntries.size() + " entries match the date filter", Toast.LENGTH_SHORT).show();
            }
            
            // If no entries match the filter, ask to show all
            if (filteredEntries.isEmpty() && !moodEntries.isEmpty()) {
                new AlertDialog.Builder(requireContext())
                    .setTitle("No data in selected range")
                    .setMessage("No mood entries found for the selected date range. Would you like to view all your mood entries instead?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Reset date filter
                        startDate = 0;
                        endDate = System.currentTimeMillis();
                        updateDateRangeText();
                        
                        // Show all entries
                        moodHistoryAdapter.updateEntries(moodEntries);
                        calculateAnalytics(moodEntries);
                        updateLineChart(moodEntries);
                        updatePieChart();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
            }
        } else {
            // No date filter, show all entries
            filteredEntries = moodEntries;
            if (getContext() != null) {
                Toast.makeText(getContext(), "Showing all " + moodEntries.size() + " entries (no date filter)", Toast.LENGTH_SHORT).show();
            }
        }
        
        if (moodHistoryAdapter != null) {
            moodHistoryAdapter.updateEntries(filteredEntries);
        }
        
        // Calculate analytics with the filtered entries
        calculateAnalytics(filteredEntries);
        
        // Update line chart with the filtered entries
        updateLineChart(filteredEntries);
        
        // Update pie chart if visible
        if (chartTypeSpinner != null && chartTypeSpinner.getSelectedItemPosition() == 1) {
            updatePieChart();
        }
    }
    
    private void manualRefresh() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Please sign in to view analytics", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Toast.makeText(getContext(), "Manually refreshing ALL data from Firestore...", Toast.LENGTH_SHORT).show();
        
        // Remove date filter for this direct query - get ALL entries
        db.collection("users")
                .document(userId)
                .collection("mood_tracking")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> entries = new ArrayList<>();
                    
                    Log.d("AnalyticsFragment", "Direct Firestore query returned " + queryDocumentSnapshots.size() + " documents");
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> data = document.getData();
                        Log.d("AnalyticsFragment", "Document ID: " + document.getId() + ", Data: " + data);
                        
                        // Ensure documentId is set
                        if (!data.containsKey("documentId")) {
                            data.put("documentId", document.getId());
                        }
                        
                        entries.add(data);
                    }
                    
                    Toast.makeText(getContext(), "Direct query found " + entries.size() + " entries", Toast.LENGTH_SHORT).show();
                    
                    if (!entries.isEmpty()) {
                        // Store all entries
                        allMoodEntries = new ArrayList<>(entries);
                        
                        // Apply filter for display only if date filter is active
                        if (startDate > 0) {
                            List<Map<String, Object>> filteredEntries = new ArrayList<>();
                            for (Map<String, Object> entry : entries) {
                                long timestamp = 0;
                                Object timestampObj = entry.get("timestamp");
                                
                                if (timestampObj instanceof Long) {
                                    timestamp = (Long) timestampObj;
                                } else if (timestampObj instanceof Double) {
                                    timestamp = ((Double) timestampObj).longValue();
                                } else if (timestampObj instanceof String) {
                                    try {
                                        timestamp = Long.parseLong((String) timestampObj);
                                    } catch (NumberFormatException e) {
                                        Log.e("AnalyticsFragment", "Invalid timestamp: " + timestampObj);
                                    }
                                }
                                
                                if (timestamp >= startDate && timestamp <= endDate) {
                                    filteredEntries.add(entry);
                                }
                            }
                            
                            if (filteredEntries.isEmpty()) {
                                // If no entries match the filter, show all entries
                                Toast.makeText(getContext(), "No entries in date range, showing all entries", Toast.LENGTH_SHORT).show();
                                moodHistoryAdapter.updateEntries(entries);
                                calculateAnalytics(entries);
                                updateLineChart(entries);
                                updatePieChart();
                                
                                // Reset date filter
                                startDate = 0;
                                endDate = System.currentTimeMillis();
                                updateDateRangeText();
                            } else {
                                moodHistoryAdapter.updateEntries(filteredEntries);
                                calculateAnalytics(filteredEntries);
                                updateLineChart(filteredEntries);
                                updatePieChart();
                            }
                        } else {
                            // No date filter, show all entries
                            moodHistoryAdapter.updateEntries(entries);
                            calculateAnalytics(entries);
                            updateLineChart(entries);
                            updatePieChart();
                        }
                    } else {
                        Toast.makeText(getContext(), "No mood data found in your account. Try adding a mood entry first.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading mood data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("AnalyticsFragment", "Error in manual refresh: " + e.getMessage(), e);
                });
    }
    
    public void showError(String errorMessage) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
            });
        }
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.analytics_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.menu_refresh) {
            // Call manual refresh instead of default refresh
            manualRefresh();
            return true;
        } else if (id == R.id.menu_export) {
            exportMoodData();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
