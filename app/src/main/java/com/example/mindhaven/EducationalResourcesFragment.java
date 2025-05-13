package com.example.mindhaven;

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
import com.example.mindhaven.model.adapter.ResourceAdapter;
import com.example.mindhaven.model.adapter.EducationalResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying educational resources related to mental health
 */
public class EducationalResourcesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ResourceAdapter adapter;
    private TextView emptyView;
    private List<EducationalResource> resourceList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_educational_resources, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewResources);
        emptyView = view.findViewById(R.id.emptyViewResources);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ResourceAdapter(getContext(), resourceList);
        recyclerView.setAdapter(adapter);

        // Check if we have data
        updateEmptyState();
    }

    /**
     * Updates the resource data and refreshes the UI
     */
    public void updateData(List<EducationalResource> resources) {
        resourceList.clear();

        if (resources != null) {
            resourceList.addAll(resources);
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
        if (resourceList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}