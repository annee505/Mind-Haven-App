package com.example.mindhaven;

import android.graphics.Color;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MoodStatisticsManager {
    private List<MoodEntry> moodEntries;
    private SimpleDateFormat dateFormat;

    public MoodStatisticsManager() {
        moodEntries = new ArrayList<>();
        dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
    }

    public void setMoodEntries(List<MoodEntry> entries) {
        this.moodEntries = new ArrayList<>(entries);
        Collections.sort(moodEntries, (a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
    }

    public void setupMoodChart(LineChart chart) {

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);


        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value < 0 || value >= moodEntries.size()) return "";
                return dateFormat.format(new Date(moodEntries.get((int) value).getTimestamp()));
            }
        });


        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMaximum(3.5f);
        leftAxis.setAxisMinimum(0.5f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 3) return "Happy";
                if (value >= 2) return "Neutral";
                return "Sad";
            }
        });

        chart.getAxisRight().setEnabled(false);
        updateChartData(chart);
    }

    private void updateChartData(LineChart chart) {
        List<Entry> entries = new ArrayList<>();
        
        for (int i = 0; i < moodEntries.size(); i++) {
            entries.add(new Entry(i, moodEntries.get(i).getMoodScore()));
        }

        LineDataSet dataSet;
        if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
            dataSet = (LineDataSet) chart.getData().getDataSetByIndex(0);
            dataSet.setValues(entries);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            dataSet = new LineDataSet(entries, "Mood Over Time");
            dataSet.setDrawIcons(false);
            dataSet.setColor(Color.rgb(139, 69, 19));
            dataSet.setCircleColor(Color.rgb(139, 69, 19));
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawCircleHole(false);
            dataSet.setValueTextSize(9f);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(Color.rgb(222, 184, 135));
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            LineData data = new LineData(dataSet);
            chart.setData(data);
        }
        
        chart.animateX(1000);
    }

    public Map<String, Integer> getActivityStats() {
        Map<String, Integer> activityCounts = new HashMap<>();
        for (MoodEntry entry : moodEntries) {
            if (entry.getActivities() != null) {
                for (String activity : entry.getActivities()) {
                    activityCounts.put(activity, activityCounts.getOrDefault(activity, 0) + 1);
                }
            }
        }
        return activityCounts;
    }

    public Map<String, Double> getTimeOfDayMoodAverages() {
        Map<String, List<Integer>> timeOfDayScores = new HashMap<>();
        for (MoodEntry entry : moodEntries) {
            String timeOfDay = entry.getTimeOfDay();
            if (!timeOfDayScores.containsKey(timeOfDay)) {
                timeOfDayScores.put(timeOfDay, new ArrayList<>());
            }
            timeOfDayScores.get(timeOfDay).add(entry.getMoodScore());
        }

        return timeOfDayScores.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().stream().mapToDouble(Integer::doubleValue).average().orElse(0.0)
            ));
    }

    public double getAverageMoodScore() {
        if (moodEntries.isEmpty()) return 0.0;
        return moodEntries.stream()
            .mapToInt(MoodEntry::getMoodScore)
            .average()
            .orElse(0.0);
    }

    public String getMostCommonMood() {
        if (moodEntries.isEmpty()) return "No data";
        
        Map<String, Integer> moodCounts = new HashMap<>();
        for (MoodEntry entry : moodEntries) {
            moodCounts.put(entry.getMood(), moodCounts.getOrDefault(entry.getMood(), 0) + 1);
        }

        return Collections.max(moodCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public String getMostProductiveTimeOfDay() {
        Map<String, Double> timeOfDayAverages = getTimeOfDayMoodAverages();
        if (timeOfDayAverages.isEmpty()) return "No data";

        return Collections.max(timeOfDayAverages.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
}