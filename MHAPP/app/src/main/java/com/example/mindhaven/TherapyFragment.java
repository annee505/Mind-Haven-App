package com.example.mindhaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class TherapyFragment extends Fragment {
    private RecyclerView meditationRecyclerView;
    private MeditationAdapter meditationAdapter;
    private List<MeditationSession> meditationSessions;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    public TherapyFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_therapy, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        meditationRecyclerView = view.findViewById(R.id.recyclerMeditations);
        meditationRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        meditationSessions = new ArrayList<>();
        meditationAdapter = new MeditationAdapter(meditationSessions, getContext());
        meditationRecyclerView.setAdapter(meditationAdapter);


        loadMeditationSessions();

        setupButtonListeners(view);

        return view;
    }

    private void setupButtonListeners(View view) {

        Button btnViewAllMeditations = view.findViewById(R.id.btnViewAllMeditations);
        btnViewAllMeditations.setOnClickListener(v -> {

            openMeditationLibrary();
        });


        Button btnBoxBreathing = view.findViewById(R.id.btnBoxBreathing);
        btnBoxBreathing.setOnClickListener(v -> {
            openBreathingExercise("box");
        });

        Button btn478Breathing = view.findViewById(R.id.btn478Breathing);
        btn478Breathing.setOnClickListener(v -> {
            openBreathingExercise("478");
        });


        Button btnMoodCheckin = view.findViewById(R.id.btnMoodCheckin);
        btnMoodCheckin.setOnClickListener(v -> {
            openMoodTracker();
        });



    }

    private void loadMeditationSessions() {


        databaseReference.child("meditation_sessions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                meditationSessions.clear();


                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        MeditationSession session = snapshot.getValue(MeditationSession.class);
                        if (session != null) {
                            meditationSessions.add(session);
                        }
                    }
                } else {

                    addSampleMeditationData();
                }

                meditationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load meditation data", Toast.LENGTH_SHORT).show();

                addSampleMeditationData();
                meditationAdapter.notifyDataSetChanged();
            }
        });
    }

    private void addSampleMeditationData() {

        meditationSessions.add(new MeditationSession("1", "Calm Mind", "Reduce anxiety and find peace", "5 min", "anxiety"));
        meditationSessions.add(new MeditationSession("2", "Sleep Well", "Prepare for restful sleep", "10 min", "sleep"));
        meditationSessions.add(new MeditationSession("3", "Morning Energy", "Start your day with clarity", "7 min", "focus"));
        meditationSessions.add(new MeditationSession("4", "Stress Relief", "Release tension and worry", "8 min", "stress"));
    }

    private void openMeditationLibrary() {

        Toast.makeText(getContext(), "Opening meditation library...", Toast.LENGTH_SHORT).show();

    }

    private void openBreathingExercise(String type) {
        Intent intent = new Intent(getActivity(), BreathingExerciseActivity.class);
        intent.putExtra("BREATHING_TYPE", type);
        startActivity(intent);
    }

    private void openMoodTracker() {

        Fragment moodTrackerFragment = new MoodTrackerFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, moodTrackerFragment)
                .addToBackStack(null)
                .commit();
    }


}
