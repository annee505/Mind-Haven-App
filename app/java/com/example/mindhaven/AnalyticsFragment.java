package com.example.mindhaven;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.LineChart;
import java.util.List;

public class AnalyticsFragment extends Fragment implements MoodAnalyticsManager.AnalyticsUpdateListener {
    private LineChart moodChart;
    private MoodAnalyticsManager analyticsManager;
    private TextView averageMoodText, commonMoodText, productiveTimeText, topActivitiesText;

    public AnalyticsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);


        moodChart = view.findViewById(R.id.moodChart);
        averageMoodText = view.findViewById(R.id.averageMoodText);
        commonMoodText = view.findViewById(R.id.commonMoodText);
        productiveTimeText = view.findViewById(R.id.productiveTimeText);
        topActivitiesText = view.findViewById(R.id.topActivitiesText);


        analyticsManager = new MoodAnalyticsManager(this, moodChart);
        analyticsManager.loadMoodData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (analyticsManager != null) {
            analyticsManager.loadMoodData();
        }
    }

    @Override
    public void onAnalyticsUpdated(String averageMood, String commonMood, 
                                 String productiveTime, List<String> topActivities) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (averageMoodText != null) {
                    averageMoodText.setText("Average Mood: " + averageMood);
                }
                if (commonMoodText != null) {
                    commonMoodText.setText("Common Mood: " + commonMood);
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
}
