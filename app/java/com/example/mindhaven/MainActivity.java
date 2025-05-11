package com.example.mindhaven;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private MoodNotificationManager notificationManager;
    private static final int NOTIFICATION_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        notificationManager = ((MindHavenApp) getApplication()).getNotificationManager();

        // Set up permission launcher
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                ((MindHavenApp) getApplication()).onNotificationPermissionChanged(isGranted);
                if (isGranted) {
                    // Also check for exact alarm permission
                    notificationManager.requestExactAlarmPermission();
                }
            }
        );

        // Check and request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, 
                android.Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            } else {
                // Permission already granted, check exact alarm permission
                notificationManager.requestExactAlarmPermission();
            }
        }

        // Check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else {
            Button getStartedButton = findViewById(R.id.btn_next);
            getStartedButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SignIn.class));
            });
        }
    }
}