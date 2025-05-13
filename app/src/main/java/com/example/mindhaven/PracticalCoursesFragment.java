
package com.example.mindhaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.chip.ChipGroup;
import com.example.mindhaven.model.adapter.CourseAdapter;
import com.example.mindhaven.PracticalCoursesViewModel;

import java.util.ArrayList;

public class PracticalCoursesFragment extends Fragment {
    private RecyclerView coursesRecycler;
    private CourseAdapter adapter;
    private View emptyState;
    private ChipGroup filterChipGroup;
    private PracticalCoursesViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_practical_courses, container, false);

        coursesRecycler = view.findViewById(R.id.rvCourses);
        emptyState = view.findViewById(R.id.empty_state);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);

        coursesRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel = new ViewModelProvider(this).get(PracticalCoursesViewModel.class);

        setupFilterChips();
        setupRecyclerView();
        observeViewModel();

        return view;
    }

    private void setupFilterChips() {
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                viewModel.clearFilters();
            } else if (checkedId == R.id.chipMeditation) {
                viewModel.filterByType("Meditation");
            } else if (checkedId == R.id.chipCBT) {
                viewModel.filterByType("CBT");
            } else if (checkedId == R.id.chipExercise) {
                viewModel.filterByType("Exercise");
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new CourseAdapter(new ArrayList<>()); // Initialize with an empty list or fetched data
        coursesRecycler.setAdapter(adapter);

        // Assuming you have a method to fetch courses
        FirebaseHelper.getInstance().fetchCourses(courses -> {
            adapter.updateCourses(courses); // You may need to implement this method in CourseAdapter
        });
    }

    private void observeViewModel() {
        viewModel.getCourses().observe(getViewLifecycleOwner(), courses -> {
            adapter.submitList(courses);
            emptyState.setVisibility(courses.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
