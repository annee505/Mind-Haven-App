package com.example.mindhaven;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodReportActivity extends AppCompatActivity {
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView moodReportsRecyclerView;
    private MoodReportAdapter moodReportAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_reports);
        
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        EditText moodReportInput = findViewById(R.id.moodReportInput);
        Button submitReportButton = findViewById(R.id.submitReportButton);
        
        submitReportButton.setOnClickListener(v -> {
            String reportText = moodReportInput.getText().toString().trim();
            if (!reportText.isEmpty()) {
                saveMoodReport(reportText);
                moodReportInput.setText("");
            }
        });
        
        moodReportsRecyclerView = findViewById(R.id.moodReportsRecyclerView);
        moodReportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodReportAdapter = new MoodReportAdapter();
        moodReportsRecyclerView.setAdapter(moodReportAdapter);
        
        // Load mood reports when activity is created
        loadMoodReports();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the mood reports list when returning to this activity
        loadMoodReports();
    }
    
    private void saveMoodReport(String reportText) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please sign in to save report", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userId = mAuth.getCurrentUser().getUid();
        
        // Create a MoodReport object
        long timestamp = System.currentTimeMillis();
        MoodReport moodReport = new MoodReport(reportText, timestamp, userId);
        
        // Save to Firestore in two collections:
        // 1. Users collection (nested)
        // 2. Main mood_reports collection (for easier querying)
        
        // First save to users collection (nested)
        db.collection("users")
                .document(userId)
                .collection("mood_reports")
                .add(moodReport)
                .addOnSuccessListener(documentReference -> {
                    // Next save to main mood_reports collection
                    db.collection("mood_reports")
                            .add(moodReport)
                            .addOnSuccessListener(mainDocRef -> {
                                Toast.makeText(this, "Report saved successfully", Toast.LENGTH_SHORT).show();
                                loadMoodReports();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("MoodReportActivity", "Error saving to main collection: " + e.getMessage());
                                // Still show success since it was saved to user collection
                                Toast.makeText(this, "Report saved, but may not appear in global views", Toast.LENGTH_SHORT).show();
                                loadMoodReports();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void loadMoodReports() {
        if (mAuth.getCurrentUser() == null) return;
        
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("mood_reports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MoodReport> reports = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Convert document to MoodReport object directly
                            MoodReport report = document.toObject(MoodReport.class);
                            if (report != null) {
                                reports.add(report);
                            }
                        }
                        moodReportAdapter.setReports(reports);
                    } else {
                        Toast.makeText(this, "Error loading reports", Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 