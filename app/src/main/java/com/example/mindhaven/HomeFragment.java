package com.example.mindhaven;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends Fragment {

    private CardView cardMoodTracker, cardRecommendations, cardPracticalCourses;
    private FloatingActionButton fabAddEmergencyContact;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        cardMoodTracker = view.findViewById(R.id.card_mood_tracker);
        cardRecommendations = view.findViewById(R.id.card_recommendations);
        cardPracticalCourses = view.findViewById(R.id.card_courses);

        
        cardMoodTracker.setOnClickListener(v -> openFragment(new MoodTrackerFragment()));
        cardRecommendations.setOnClickListener(v -> openFragment(new RecommendationsFragment()));
        cardPracticalCourses.setOnClickListener(v -> openFragment(new PracticalCoursesFragment()));



        return view;
    }

    private void openFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}
