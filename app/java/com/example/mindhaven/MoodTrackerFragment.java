package com.example.mindhaven;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodTrackerFragment extends Fragment {
    private RadioGroup radioGroupFrequency;
    private Button btnHappy, btnNeutral, btnSad, btnSavePreferences;
    private FirebaseFirestore db;

    private TextInputLayout customInputLayout;
    private TextInputEditText customInputField;

    private FirebaseAuth mAuth;
    private String selectedMood = "";
    private String selectedFrequency = "Daily";
    private RecyclerView moodHistoryRecyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private MoodNotificationManager notificationManager;

    private TextWatcher customTimeTextWatcher;

    private RecyclerView moodReportsRecyclerView;
    private MoodReportAdapter moodReportAdapter;

    public MoodTrackerFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_tracker, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        notificationManager = new MoodNotificationManager(requireContext());
        
        // Check for notification and alarm permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Request notification permission for Android 13+
            if (requireActivity().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        
        // Check for exact alarm permission
        if (!notificationManager.requestExactAlarmPermission()) {
            Toast.makeText(requireContext(), 
                "Please allow exact alarms for custom notifications to work", 
                Toast.LENGTH_LONG).show();
        }
        
        radioGroupFrequency = view.findViewById(R.id.radioGroupFrequency);
        btnHappy = view.findViewById(R.id.btnHappy);
        btnNeutral = view.findViewById(R.id.btnNeutral);
        btnSad = view.findViewById(R.id.btnSad);
        btnSavePreferences = view.findViewById(R.id.btnSavePreferences);

        customInputLayout = view.findViewById(R.id.customInputLayout);
        customInputField = view.findViewById(R.id.customInputField);

       
        customTimeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String timeStr = s.toString().trim();
                if (timeStr.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                    try {
                        String[] parts = timeStr.split(":");
                        int hour = Integer.parseInt(parts[0]);
                        int minute = Integer.parseInt(parts[1]);
                        
                        // Schedule the notification
                        notificationManager.setCustomNotification(hour, minute, "Time for your mood check!");
                        saveCustomTimePreference(timeStr);
                        
                        // Show confirmation to user
                        Toast.makeText(getContext(), 
                            "Custom notification set for " + timeStr, 
                            Toast.LENGTH_SHORT).show();
                        
                        // Log for debugging
                        Log.d("MoodTracker", "Custom notification scheduled for " + hour + ":" + minute);
                        
                        // Check if notification permissions are granted
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (requireActivity().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) 
                                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(getContext(), 
                                    "Notification permission required", 
                                    Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("MoodTracker", "Error scheduling notification", e);
                        Toast.makeText(getContext(), 
                            "Error setting notification: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        customInputField.addTextChangedListener(customTimeTextWatcher);
        customInputField.setHint("Enter time (HH:mm)");

        moodHistoryRecyclerView = view.findViewById(R.id.moodHistoryRecyclerView);
        moodHistoryAdapter = new MoodHistoryAdapter();
        moodHistoryRecyclerView.setAdapter(moodHistoryAdapter);
        moodHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupMoodButtons();
        loadPreferences();
        loadMoodHistory();

        radioGroupFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioDaily) {
                selectedFrequency = "Daily";
                customInputLayout.setVisibility(View.GONE);
                notificationManager.scheduleNotification(selectedFrequency);
            } else if (checkedId == R.id.radioTwiceDaily) {
                selectedFrequency = "Twice Daily";
                customInputLayout.setVisibility(View.GONE);
                notificationManager.scheduleNotification(selectedFrequency);
            } else if (checkedId == R.id.radioCustom) {
                selectedFrequency = "Custom";
                customInputLayout.setVisibility(View.VISIBLE);
                loadCustomTimePreference();
            }
            saveFrequencyPreference();
        });

        loadFrequencyPreference();
        btnSavePreferences.setOnClickListener(v -> savePreferences());

        EditText moodReportInput = view.findViewById(R.id.moodReportInput);
        Button submitReportButton = view.findViewById(R.id.submitReportButton);

        submitReportButton.setOnClickListener(v -> {
            String reportText = moodReportInput.getText().toString().trim();
            if (!reportText.isEmpty()) {
                saveMoodReport(reportText);
                moodReportInput.setText("");
            }
        });

        moodReportsRecyclerView = view.findViewById(R.id.moodReportsRecyclerView);
        moodReportsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodReportAdapter = new MoodReportAdapter();
        moodReportsRecyclerView.setAdapter(moodReportAdapter);

     
        loadMoodReports();

        return view;
    }

    private void setupMoodButtons() {
        View.OnClickListener moodClickListener = v -> {
            btnHappy.setBackgroundResource(R.drawable.default_button_background);
            btnNeutral.setBackgroundResource(R.drawable.default_button_background);
            btnSad.setBackgroundResource(R.drawable.default_button_background);

            v.setBackgroundResource(R.drawable.selected_button_background);

            if (v.getId() == R.id.btnHappy) selectedMood = "Happy";
            else if (v.getId() == R.id.btnNeutral) selectedMood = "Neutral";
            else if (v.getId() == R.id.btnSad) selectedMood = "Sad";
        };

        btnHappy.setOnClickListener(moodClickListener);
        btnNeutral.setOnClickListener(moodClickListener);
        btnSad.setOnClickListener(moodClickListener);
    }

    private void savePreferences() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getActivity(), "Please sign in to save preferences", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedMood.isEmpty()) {
            Toast.makeText(getActivity(), "Please select a mood", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> moodData = new HashMap<>();

        moodData.put("mood", selectedMood);
        int moodScore;
        switch (selectedMood) {
            case "Happy":
                moodScore = 3;
                break;
            case "Neutral":
                moodScore = 2;
                break;
            case "Sad":
                moodScore = 1;
                break;
            default:
                moodScore = 0;
        }
        moodData.put("moodScore", moodScore);


        int selectedFrequencyId = radioGroupFrequency.getCheckedRadioButtonId();
        if (selectedFrequencyId != -1) {
            RadioButton selectedRadioButton = getView().findViewById(selectedFrequencyId);
            moodData.put("frequency", selectedRadioButton.getText().toString());
        }

        com.google.android.material.textfield.TextInputEditText noteInput = getView().findViewById(R.id.noteInput);
        if (noteInput != null && noteInput.getText() != null && !noteInput.getText().toString().trim().isEmpty()) {
            moodData.put("note", noteInput.getText().toString().trim());
        }


        List<String> selectedActivities = new ArrayList<>();
        com.google.android.material.chip.ChipGroup activitiesChipGroup = getView().findViewById(R.id.activitiesChipGroup);
        for (int i = 0; i < activitiesChipGroup.getChildCount(); i++) {
            View child = activitiesChipGroup.getChildAt(i);
            if (child instanceof com.google.android.material.chip.Chip) {
                com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) child;
                if (chip.isChecked()) {
                    selectedActivities.add(chip.getText().toString());
                }
            }
        }
        moodData.put("activities", selectedActivities);

        RadioGroup timeOfDayGroup = getView().findViewById(R.id.timeOfDayGroup);
        int selectedTimeId = timeOfDayGroup.getCheckedRadioButtonId();
        if (selectedTimeId != -1) {
            RadioButton selectedTime = getView().findViewById(selectedTimeId);
            moodData.put("timeOfDay", selectedTime.getText().toString());
        }


        moodData.put("timestamp", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .collection("mood_tracking")
                .add(moodData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getActivity(), "Mood saved successfully", Toast.LENGTH_SHORT).show();
                    clearForm();
                    loadMoodHistory();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error saving mood: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        btnHappy.setBackgroundResource(R.drawable.default_button_background);
        btnNeutral.setBackgroundResource(R.drawable.default_button_background);
        btnSad.setBackgroundResource(R.drawable.default_button_background);
        selectedMood = "";

        com.google.android.material.textfield.TextInputEditText noteInput = getView().findViewById(R.id.noteInput);
        if (noteInput != null) {
            noteInput.setText("");
        }

        com.google.android.material.chip.ChipGroup activitiesChipGroup = getView().findViewById(R.id.activitiesChipGroup);
        for (int i = 0; i < activitiesChipGroup.getChildCount(); i++) {
            View child = activitiesChipGroup.getChildAt(i);
            if (child instanceof com.google.android.material.chip.Chip) {
                ((com.google.android.material.chip.Chip) child).setChecked(false);
            }
        }

        RadioGroup timeOfDayGroup = getView().findViewById(R.id.timeOfDayGroup);
        timeOfDayGroup.clearCheck();
    }

    private void loadPreferences() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("mood_tracking")
                .orderBy("timestamp")
                .limitToLast(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Map<String, Object> data = queryDocumentSnapshots.getDocuments().get(0).getData();

                        String savedFrequency = (String) data.get("frequency");
                        String savedMood = (String) data.get("mood");

                        if (savedFrequency != null) {
                            for (int i = 0; i < radioGroupFrequency.getChildCount(); i++) {
                                View child = radioGroupFrequency.getChildAt(i);
                                if (child instanceof RadioButton) {
                                    RadioButton radioButton = (RadioButton) child;
                                    if (radioButton.getText().toString().equals(savedFrequency)) {
                                        radioButton.setChecked(true);
                                        break;
                                    }
                                }
                            }
                        }

                        if (savedMood != null) {
                            selectedMood = savedMood;
                            switch (savedMood) {
                                case "Happy":
                                    btnHappy.setBackgroundResource(R.drawable.selected_button_background);
                                    break;
                                case "Neutral":
                                    btnNeutral.setBackgroundResource(R.drawable.selected_button_background);
                                    break;
                                case "Sad":
                                    btnSad.setBackgroundResource(R.drawable.selected_button_background);
                                    break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error loading preferences: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showCustomFrequencyDialog() {
        
    }

    private void saveFrequencyPreference() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MoodTrackerPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("tracking_frequency", selectedFrequency).apply();
    }

    private void loadFrequencyPreference() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MoodTrackerPrefs", Context.MODE_PRIVATE);
        String savedFrequency = prefs.getString("tracking_frequency", "Daily");

        switch (savedFrequency) {
            case "Daily":
                radioGroupFrequency.check(R.id.radioDaily);
                break;
            case "Twice Daily":
                radioGroupFrequency.check(R.id.radioTwiceDaily);
                break;
            case "Custom":
                radioGroupFrequency.check(R.id.radioCustom);
                break;
        }


        notificationManager.scheduleNotification(savedFrequency);
    }

    private void loadMoodHistory() {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("mood_tracking")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> entries = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        entries.add(document.getData());
                    }

                    moodHistoryAdapter.updateEntries(entries);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error loading mood history: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveCustomTimePreference(String time) {
        SharedPreferences prefs = requireContext().getSharedPreferences("MoodTrackerPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("custom_notification_time", time).apply();
    }

    private void loadCustomTimePreference() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MoodTrackerPrefs", Context.MODE_PRIVATE);
        String savedTime = prefs.getString("custom_notification_time", "");
        if (!savedTime.isEmpty()) {
            customInputField.setText(savedTime);
        }
    }

    private void saveMoodReport(String reportText) {
        if (mAuth.getCurrentUser() == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> report = new HashMap<>();
        report.put("text", reportText);
        report.put("timestamp", System.currentTimeMillis());
        report.put("userId", userId);

        db.collection("mood_reports")
            .add(report)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(getContext(), "Mood report saved successfully", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error saving mood report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loadMoodReports() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FirebaseFirestore.getInstance()
                .collection("mood_reports")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("MoodTracker", "Error loading mood reports", error);
                        return;
                    }

                    if (value != null) {
                        List<MoodReport> reports = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            MoodReport report = doc.toObject(MoodReport.class);
                            if (report != null) {
                                reports.add(report);
                            }
                        }
                        moodReportAdapter.setReports(reports);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (notificationManager != null) {
            notificationManager.cancelNotifications();
        }
    }

    private void scheduleNotification(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(requireContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
