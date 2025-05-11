package com.example.mindhaven;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MoodAnalyticsFragment extends Fragment {
    private LineChart moodChart;
    private TextView textAverageMood;
    private TextView textCommonMood;
    private TextView textProductiveTime;
    private TextView textTopActivities;
    
    private MoodStatisticsManager statisticsManager;
    private DatabaseReference moodRef;
    private String userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        statisticsManager = new MoodStatisticsManager();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        moodRef = FirebaseDatabase.getInstance().getReference()
                .child("moods").child(userId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_analytics, container, false);

        moodChart = view.findViewById(R.id.analyticsChart);
        textAverageMood = view.findViewById(R.id.textAverageMood);
        textCommonMood = view.findViewById(R.id.textCommonMood);
        textProductiveTime = view.findViewById(R.id.textProductiveTime);
        textTopActivities = view.findViewById(R.id.textTopActivities);

        loadMoodData();

        return view;
    }

    private void loadMoodData() {
        moodRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<MoodEntry> entries = new ArrayList<>();
                for (DataSnapshot moodSnapshot : snapshot.getChildren()) {
                    MoodEntry entry = moodSnapshot.getValue(MoodEntry.class);
                    if (entry != null) {
                        entries.add(entry);
                    }
                }
                
                updateAnalytics(entries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateAnalytics(List<MoodEntry> entries) {
        statisticsManager.setMoodEntries(entries);
        

        statisticsManager.setupMoodChart(moodChart);

        double averageMood = statisticsManager.getAverageMoodScore();
        String commonMood = statisticsManager.getMostCommonMood();
        String productiveTime = statisticsManager.getMostProductiveTimeOfDay();
        

        String moodLevel;
        if (averageMood >= 2.5) moodLevel = "Happy";
        else if (averageMood >= 1.5) moodLevel = "Neutral";
        else moodLevel = "Sad";
        
        textAverageMood.setText(String.format("Average Mood: %s (%.1f)", moodLevel, averageMood));
        textCommonMood.setText(String.format("Most Common Mood: %s", commonMood));
        textProductiveTime.setText(String.format("Most Productive Time: %s", productiveTime));

        Map<String, Integer> activityStats = statisticsManager.getActivityStats();
        StringBuilder activities = new StringBuilder("Top Activities:\n");
        activityStats.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .forEach(entry -> activities.append(String.format("• %s (%d times)\n", 
                        entry.getKey(), entry.getValue())));
        
        textTopActivities.setText(activities.toString());
    }
}