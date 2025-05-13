package com.example.mindhaven;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
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
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MoodTrackerFragment extends Fragment {
    private RadioGroup radioGroupFrequency;
    private Button btnHappy, btnNeutral, btnSad, btnSavePreferences, btnSaveMood, btnMoodReport;
    private FirebaseFirestore db;
    private FirebaseAnalyticsHelper analyticsHelper;

    private TextInputLayout customInputLayout;
    private TextInputEditText customInputField;
    private TextInputLayout customMessageLayout;
    private TextInputEditText customMessageField;
    private Button timePickerButton;

    private FirebaseAuth mAuth;
    private String selectedMood = "";
    private String selectedFrequency = "Daily";

    private TextWatcher customTimeTextWatcher;

    private RecyclerView moodReportsRecyclerView;
    private MoodReportAdapter moodReportAdapter;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(getActivity(), "Notification permission is required for alarms", Toast.LENGTH_LONG).show();
                }
            });

    public MoodTrackerFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_tracker, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        analyticsHelper = new FirebaseAnalyticsHelper();

        radioGroupFrequency = view.findViewById(R.id.radioGroupFrequency);
        btnHappy = view.findViewById(R.id.btnHappy);
        btnNeutral = view.findViewById(R.id.btnNeutral);
        btnSad = view.findViewById(R.id.btnSad);
        btnSavePreferences = view.findViewById(R.id.btnSavePreferences);
        btnSaveMood = view.findViewById(R.id.btnSaveMood);
        btnMoodReport = view.findViewById(R.id.btnMoodReport);

        customInputLayout = view.findViewById(R.id.customInputLayout);
        //customInputField = view.findViewById(R.id.customInputField);
        customMessageLayout = view.findViewById(R.id.customMessageLayout);
        customMessageField = view.findViewById(R.id.customMessageField);
        timePickerButton = view.findViewById(R.id.timePickerButton);

        // Set up Mood Reports RecyclerView if it exists in the layout
        moodReportsRecyclerView = view.findViewById(R.id.moodReportsRecyclerView);
        if (moodReportsRecyclerView != null) {
            try {
                moodReportsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                moodReportAdapter = new MoodReportAdapter();
                moodReportsRecyclerView.setAdapter(moodReportAdapter);
                // Load mood reports from Firestore
                loadMoodReports();
            } catch (Exception e) {
                Log.e("MoodTracker", "Error setting up mood reports recycler view", e);
            }
        } else {
            Log.d("MoodTracker", "moodReportsRecyclerView not found in layout");
        }

        timePickerButton.setOnClickListener(v -> showTimePickerDialog());

        setupMoodButtons();
        loadPreferences(view);

        setupRadioChangeListener();
        loadFrequencyPreference();
        btnSavePreferences.setOnClickListener(v -> savePreferences());
        btnSaveMood.setOnClickListener(v -> savePreferences());

        // Set click listener for Mood Report button
        btnMoodReport.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MoodReportActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add test button

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

        Toast.makeText(getActivity(), "Saving your mood...", Toast.LENGTH_SHORT).show();

        try {
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
            if (selectedFrequencyId != -1 && getView() != null) {
                RadioButton selectedRadioButton = getView().findViewById(selectedFrequencyId);
                if (selectedRadioButton != null) {
                    moodData.put("frequency", selectedRadioButton.getText().toString());
                }
            }

            com.google.android.material.textfield.TextInputEditText noteInput =
                    getView() != null ? getView().findViewById(R.id.noteInput) : null;
            if (noteInput != null && noteInput.getText() != null && !noteInput.getText().toString().trim().isEmpty()) {
                moodData.put("note", noteInput.getText().toString().trim());
            }

            List<String> selectedActivities = new ArrayList<>();
            com.google.android.material.chip.ChipGroup activitiesChipGroup =
                    getView() != null ? getView().findViewById(R.id.activitiesChipGroup) : null;
            if (activitiesChipGroup != null) {
                for (int i = 0; i < activitiesChipGroup.getChildCount(); i++) {
                    View child = activitiesChipGroup.getChildAt(i);
                    if (child instanceof com.google.android.material.chip.Chip) {
                        com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) child;
                        if (chip.isChecked()) {
                            selectedActivities.add(chip.getText().toString());
                        }
                    }
                }
            }
            moodData.put("activities", selectedActivities);

            RadioGroup timeOfDayGroup = getView() != null ? getView().findViewById(R.id.timeOfDayGroup) : null;
            if (timeOfDayGroup != null) {
                int selectedTimeId = timeOfDayGroup.getCheckedRadioButtonId();
                if (selectedTimeId != -1) {
                    RadioButton selectedTime = getView().findViewById(selectedTimeId);
                    if (selectedTime != null) {
                        moodData.put("timeOfDay", selectedTime.getText().toString());
                    }
                }
            }

            long timestamp = System.currentTimeMillis();
            moodData.put("timestamp", timestamp);
            moodData.put("userId", userId);

            // Use the FirebaseAnalyticsHelper to save data to both databases
            analyticsHelper.saveMoodEntry(moodData, new FirebaseAnalyticsHelper.SaveListener() {
                @Override
                public void onSaveComplete(boolean success, String documentId) {
                    if (success) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getActivity(), "Mood saved successfully with ID: " + documentId, Toast.LENGTH_SHORT).show();
                                clearForm();

                                // Ask user if they want to view analytics
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                                builder.setTitle("View Analytics")
                                        .setMessage("Would you like to view your mood analytics now?")
                                        .setPositiveButton("Yes", (dialog, id) -> {
                                            // Navigate to Analytics tab
                                            if (getActivity() instanceof HomeActivity) {
                                                try {
                                                    // First force a sync to ensure data is available
                                                    FirebaseAnalyticsHelper syncHelper = new FirebaseAnalyticsHelper();
                                                    syncHelper.syncDatabases(syncSuccess -> {
                                                        // Then navigate to analytics tab
                                                        if (getActivity() != null) {
                                                            getActivity().runOnUiThread(() -> {
                                                                Toast.makeText(getActivity(), "Loading analytics...", Toast.LENGTH_SHORT).show();
                                                                com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                                                                        getActivity().findViewById(R.id.bottomNavigationView);

                                                                // Short delay to allow data to be processed
                                                                new Handler().postDelayed(() -> {
                                                                    bottomNav.setSelectedItemId(R.id.nav_analytics);
                                                                }, 500);
                                                            });
                                                        }
                                                    });
                                                } catch (Exception e) {
                                                    Log.e("MoodTracker", "Error navigating to analytics: " + e.getMessage());
                                                    // Fallback to direct navigation
                                                    com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                                                            getActivity().findViewById(R.id.bottomNavigationView);
                                                    bottomNav.setSelectedItemId(R.id.nav_analytics);
                                                }
                                            }
                                        })
                                        .setNegativeButton("No", (dialog, id) -> {
                                            dialog.dismiss();
                                        })
                                        .create()
                                        .show();
                            });
                        }
                    } else {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getActivity(), "Error saving mood. Please try again.", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        selectedMood = "";
        btnHappy.setBackgroundResource(R.drawable.default_button_background);
        btnNeutral.setBackgroundResource(R.drawable.default_button_background);
        btnSad.setBackgroundResource(R.drawable.default_button_background);

        com.google.android.material.textfield.TextInputEditText noteInput = getView().findViewById(R.id.noteInput);
        if (noteInput != null) {
            noteInput.setText("");
        }

        com.google.android.material.chip.ChipGroup activitiesChipGroup = getView().findViewById(R.id.activitiesChipGroup);
        for (int i = 0; i < activitiesChipGroup.getChildCount(); i++) {
            View child = activitiesChipGroup.getChildAt(i);
            if (child instanceof com.google.android.material.chip.Chip) {
                com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) child;
                chip.setChecked(false);
            }
        }

        RadioGroup timeOfDayGroup = getView().findViewById(R.id.timeOfDayGroup);
        if (timeOfDayGroup != null) {
            timeOfDayGroup.clearCheck();
        }
    }

    private void loadPreferences(View view) {
        if (mAuth.getCurrentUser() == null) return;

        RadioButton radioDaily = view.findViewById(R.id.radioDaily);
        if (radioDaily != null) {
            radioDaily.setChecked(true);
        }

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("mood_tracking")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String frequency = document.getString("frequency");
                        if (frequency != null && getView() != null) {
                            setFrequencySelection(getView(), frequency);
                        }
                    }
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
        String frequency = prefs.getString("tracking_frequency", "Daily");
        if (getView() != null) {
            setFrequencySelection(getView(), frequency);
        }
    }

    private void setFrequencySelection(View view, String frequency) {
        RadioButton radioDaily = view.findViewById(R.id.radioDaily);
        RadioButton radioTwiceDaily = view.findViewById(R.id.radioTwiceDaily);
        RadioButton radioCustom = view.findViewById(R.id.radioCustom);

        switch (frequency) {
            case "Daily":
                if (radioDaily != null) radioDaily.setChecked(true);
                break;
            case "Twice a day":
                if (radioTwiceDaily != null) radioTwiceDaily.setChecked(true);
                break;
            case "Custom":
                if (radioCustom != null) radioCustom.setChecked(true);
                break;
        }
    }

    private void saveCustomTimePreference(String time) {
        SharedPreferences prefs = requireContext().getSharedPreferences("MoodTrackerPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("custom_notification_time", time).apply();
    }

    private void loadCustomTimePreference() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MoodTrackerPrefs", Context.MODE_PRIVATE);
        String savedTime = prefs.getString("custom_notification_time", "");
        if (!savedTime.isEmpty()) {
            timePickerButton.setText(savedTime);
        } else {
            timePickerButton.setText("Select Time");
        }
    }

    private void saveCustomMessagePreference(String message) {
        SharedPreferences prefs = requireContext().getSharedPreferences("MoodTrackerPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("custom_notification_message", message).apply();
    }

    private void loadCustomMessagePreference() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MoodTrackerPrefs", Context.MODE_PRIVATE);
        String savedMessage = prefs.getString("custom_notification_message", "Time for your mood check!");
        customMessageField.setText(savedMessage);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Minimal channel setup
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                    "mood_reminder",
                    "Mood Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                );
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }
            
            // Simple notification
            Notification notification = new NotificationCompat.Builder(context, "mood_reminder")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Mood Check")
                .setContentText("How are you feeling?")
                .build();
                
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(1, notification);
        }
    }

    private void showTimePickerDialog() {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (view, hour, minute) -> {
            // Save time
            SharedPreferences prefs = requireContext().getSharedPreferences("MoodPrefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("notify_hour", hour).putInt("notify_minute", minute).apply();
            
            // Schedule notification
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(requireContext(), NotificationReceiver.class);
            intent.setAction("MOOD_REMINDER"); // Unique action
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(), 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            
            // Use most reliable method available
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
            
            Toast.makeText(requireContext(), "Notification set for " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
        }, 
        now.get(Calendar.HOUR_OF_DAY), 
        now.get(Calendar.MINUTE), 
        true).show();
    }

    private void setupRadioChangeListener() {
        radioGroupFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioDaily = getView().findViewById(R.id.radioDaily);
            RadioButton radioTwiceDaily = getView().findViewById(R.id.radioTwiceDaily);
            RadioButton radioCustom = getView().findViewById(R.id.radioCustom);

            if (checkedId == R.id.radioDaily) {
                selectedFrequency = "Daily";
                scheduleNotification(10, 0);
                Toast.makeText(requireContext(), "Daily notifications set for 10:00", Toast.LENGTH_SHORT).show();
                customInputLayout.setVisibility(View.GONE);
                customMessageLayout.setVisibility(View.GONE);
                timePickerButton.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioTwiceDaily) {
                selectedFrequency = "Twice a day";
                scheduleNotification(10, 0);
                scheduleNotification(18, 0);
                Toast.makeText(requireContext(), "Twice a day notifications set for 10:00 and 18:00", Toast.LENGTH_SHORT).show();
                customInputLayout.setVisibility(View.GONE);
                customMessageLayout.setVisibility(View.GONE);
                timePickerButton.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioCustom) {
                selectedFrequency = "Custom";
                customInputLayout.setVisibility(View.VISIBLE);
                customMessageLayout.setVisibility(View.VISIBLE);
                timePickerButton.setVisibility(View.VISIBLE);
                loadCustomTimePreference();
            }
        });
    }

    private void scheduleNotification(int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), NotificationReceiver.class);
        intent.setAction("MOOD_REMINDER_" + hour); // Unique action for each notification
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 
            hour, // Different requestCode for each notification
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        
        // If time already passed today, set for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private void loadMoodReports() {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) return;

            if (moodReportAdapter == null) {
                Log.d("MoodTracker", "moodReportAdapter is null, skipping loadMoodReports");
                return;
            }

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
                            try {
                                List<MoodReport> reports = new ArrayList<>();
                                for (DocumentSnapshot doc : value.getDocuments()) {
                                    MoodReport report = doc.toObject(MoodReport.class);
                                    if (report != null) {
                                        reports.add(report);
                                    }
                                }

                                if (moodReportAdapter != null) {
                                    moodReportAdapter.setReports(reports);
                                }
                            } catch (Exception e) {
                                Log.e("MoodTracker", "Error processing mood reports", e);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e("MoodTracker", "Error in loadMoodReports", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != 
                    PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        } catch (Exception e) {
            Log.e("MoodTracker", "Error requesting alarm permission", e);
        }
    }
}
