package com.example.mindhaven.model.adapter;

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

import com.example.mindhaven.R;
import com.example.mindhaven.model.adapter.ExerciseAdapter;
import com.example.mindhaven.model.adapter.PracticalExercise;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying practical exercises related to mental health
 */
public class PracticalExercisesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ExerciseAdapter adapter;
    private TextView emptyView;
    private List<PracticalExercise> exerciseList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_practical_exercises, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewExercises);
        emptyView = view.findViewById(R.id.emptyViewExercises);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExerciseAdapter(getContext(), exerciseList);
        recyclerView.setAdapter(adapter);

        // Check if we have data
        updateEmptyState();
    }

    /**
     * Updates the exercise data and refreshes the UI
     */
    public void updateData(List<com.example.mindhaven.model.adapter.PracticalExercise> exercises) {
        exerciseList.clear();

        if (exercises != null) {
            exerciseList.addAll(exercises);
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        updateEmptyState();
    }

    /**
     * Shows empty state if no data is available
     */
    private void updateEmptyState() {
        if (exerciseList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}