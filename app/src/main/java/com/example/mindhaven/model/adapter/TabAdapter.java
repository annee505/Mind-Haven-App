package com.example.mindhaven.model.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mindhaven.EducationalResourcesFragment;
import com.example.mindhaven.model.adapter.EducationalResource;
import com.example.mindhaven.model.adapter.PracticalExercise;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for handling tabs in the PracticalCoursesFragment
 */
public class TabAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragments;
    private com.example.mindhaven.EducationalResourcesFragment educationalResourcesFragment;
    private com.example.mindhaven.model.adapter.PracticalExercisesFragment practicalExercisesFragment;

    public TabAdapter(@NonNull Fragment fragment) {
        super(fragment);
        fragments = new ArrayList<>();

        // Initialize fragments
        educationalResourcesFragment = new EducationalResourcesFragment();
        practicalExercisesFragment = new PracticalExercisesFragment();

        // Add fragments to list
        fragments.add(educationalResourcesFragment);
        fragments.add(practicalExercisesFragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    /**
     * Updates the educational resources data in the corresponding fragment
     */
    public void updateEducationalResources(List<EducationalResource> resources) {
        educationalResourcesFragment.updateData(resources);
    }

    /**
     * Updates the practical exercises data in the corresponding fragment
     */
    public void updatePracticalExercises(List<PracticalExercise> exercises) {
        practicalExercisesFragment.updateData(exercises);
    }
}