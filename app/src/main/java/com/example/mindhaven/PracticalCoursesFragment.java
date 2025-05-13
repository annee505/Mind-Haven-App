package com.example.mindhaven;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mindhaven.R;
import com.example.mindhaven.model.adapter.TabAdapter;
import com.example.mindhaven.data.DataRepository;
import com.example.mindhaven.model.adapter.EducationalResource;
import com.example.mindhaven.model.adapter.PracticalExercise;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

/**
 * Fragment for displaying mental health educational resources and practical exercises
 * with a calm, soothing interface using beige and brown color scheme.
 */
public class PracticalCoursesFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private TextView errorMessage;
    private DataRepository dataRepository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_practical_courses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        progressBar = view.findViewById(R.id.progressBar);
        errorMessage = view.findViewById(R.id.errorMessage);

        // Initialize data repository
        dataRepository = new DataRepository();

        // Set up tabs and viewpager
        setupViewPager();

        // Load data
        loadMentalHealthData();
    }

    private void setupViewPager() {
        TabAdapter tabAdapter = new TabAdapter(this);
        viewPager.setAdapter(tabAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Educational Resources");
                            tab.setIcon(R.drawable.ic_education);
                            break;
                        case 1:
                            tab.setText("Practical Exercises");
                            tab.setIcon(R.drawable.ic_exercise);
                            break;
                    }
                }).attach();
    }

    private void loadMentalHealthData() {
        showLoading();

        // Load educational resources
        dataRepository.getEducationalResources(new DataRepository.DataCallback<List<EducationalResource>>() {
            @Override
            public void onSuccess(List<EducationalResource> data) {
                // Update the educational resources tab
                ((TabAdapter) viewPager.getAdapter()).updateEducationalResources(data);
                hideLoading();
            }

            @Override
            public void onError(Exception e) {
                showError("Failed to load educational resources. Please try again later.");
            }
        });

        // Load practical exercises
        dataRepository.getPracticalExercises(new DataRepository.DataCallback<List<PracticalExercise>>() {
            @Override
            public void onSuccess(List<PracticalExercise> data) {
                // Update the practical exercises tab
                ((TabAdapter) viewPager.getAdapter()).updatePracticalExercises(data);
                hideLoading();
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        errorMessage.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setText(message);
    }
}