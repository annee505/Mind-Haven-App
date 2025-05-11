package com.example.mindhaven;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Button;
import android.widget.Toast;

import com.example.mindhaven.models.MeditationAudio;
import com.example.mindhaven.ui.meditation.MeditationFragment;
import com.example.mindhaven.ui.meditation.MeditationViewModel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TherapyFragment extends Fragment implements MeditationAdapter.OnMeditationItemClickListener {
    private RecyclerView resourcesRecyclerView;
    private RecyclerView worksheetsRecyclerView;
    private FirebaseFirestore db;

    private static final String[] RESOURCE_TITLES = {
            "Understanding Anxiety",
            "Depression Awareness",
            "Stress Management",
            "Mindfulness Basics",
            "Sleep Hygiene"
    };

    private RecyclerView meditationRecyclerView;
    private MeditationAdapter meditationAdapter;
    private MeditationViewModel meditationViewModel;

    public TherapyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        meditationViewModel = new ViewModelProvider(this).get(MeditationViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_therapy, container, false);

        meditationRecyclerView = view.findViewById(R.id.recyclerMeditations);
        meditationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        meditationAdapter = new MeditationAdapter(new ArrayList<>(), this);
        meditationRecyclerView.setAdapter(meditationAdapter);

        observeViewModel();
        setupButtonListeners(view);
        setupCBTResources(view);

        return view;
    }

    private void setupCBTResources(View view) {
        resourcesRecyclerView = view.findViewById(R.id.resourcesRecyclerView);
        worksheetsRecyclerView = view.findViewById(R.id.worksheetsRecyclerView);

        resourcesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        worksheetsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<CBTWorksheet> resources = new ArrayList<>();
        for (String title : RESOURCE_TITLES) {
            resources.add(new CBTWorksheet(title, "Resource for " + title,
                    "Learn about " + title, "Read and practice the concepts",
                    "Improve understanding of " + title));
        }

        List<CBTWorksheet> worksheets = CBTWorksheetManager.getWorksheets();

        CBTResourceAdapter resourceAdapter = new CBTResourceAdapter(resources, worksheet -> {
            Toast.makeText(getContext(), "Clicked on: " + worksheet.getTitle(), Toast.LENGTH_SHORT).show();
        });

        CBTResourceAdapter worksheetAdapter = new CBTResourceAdapter(worksheets, worksheet -> {
            Toast.makeText(getContext(), "Clicked on worksheet: " + worksheet.getTitle(), Toast.LENGTH_SHORT).show();
        });

        resourcesRecyclerView.setAdapter(resourceAdapter);
        worksheetsRecyclerView.setAdapter(worksheetAdapter);
    }

    private void observeViewModel() {
        meditationViewModel.getAllAudios().observe(getViewLifecycleOwner(), audios -> {
            if (audios != null) {
                Log.d("TherapyFragment", "Audios loaded: " + audios.size());
                List<MeditationAudio> limitedAudios = audios.size() > 5 ? audios.subList(0, 5) : audios;
                meditationAdapter.updateAudios(limitedAudios);
            } else {
                Log.d("TherapyFragment", "Audios list is null");
            }
        });
    }

    @Override
    public void onMeditationItemClick(MeditationAudio audio) {
        Log.d("TherapyFragment", "Clicked: " + audio.getTitle());
        Toast.makeText(getContext(), "Selected: " + audio.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void setupButtonListeners(View view) {
        Button btnViewAllMeditations = view.findViewById(R.id.btnViewAllMeditations);
        btnViewAllMeditations.setOnClickListener(v -> openMeditationLibrary());

        Button btnBoxBreathing = view.findViewById(R.id.btnBoxBreathing);
        btnBoxBreathing.setOnClickListener(v -> openBreathingExercise("box"));

        Button btn478Breathing = view.findViewById(R.id.btn478Breathing);
        btn478Breathing.setOnClickListener(v -> openBreathingExercise("478"));

        Button btnMoodCheckin = view.findViewById(R.id.btnMoodCheckin);
        btnMoodCheckin.setOnClickListener(v -> openMoodTracker());

        Button btnSleepTracker = view.findViewById(R.id.btnSleepTracker);
        btnSleepTracker.setOnClickListener(v -> openSleepTracker());
    }

    private void openSleepTracker() {
        Fragment sleepTrackerFragment = new SleepTrackerFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, sleepTrackerFragment)
                .addToBackStack(null)
                .commit();
    }

    private void openMeditationLibrary() {
        Intent intent = new Intent(getActivity(), MeditationLibraryActivity.class);
        startActivity(intent);
    }

    private void openBreathingExercise(String type) {
        Intent intent = new Intent(getActivity(), BreathingExerciseActivity.class);
        intent.putExtra("exerciseType", type);
        startActivity(intent);
    }

    private void openMoodTracker() {
        Fragment moodTrackerFragment = new MoodTrackerFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, moodTrackerFragment) // Ensure R.id.fragmentContainer exists in your Activity layout
                .addToBackStack(null)
                .commit();
    }
}