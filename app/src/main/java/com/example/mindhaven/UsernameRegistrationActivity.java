package com.example.mindhaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class UsernameRegistrationActivity extends AppCompatActivity {
    private EditText usernameInput;
    private Button registerButton;
    private ProgressBar loadingProgress;
    private FirebaseService firebaseService;
    private MindHavenApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username_registration);

        // Set title
        setTitle("Register Unique Username");

        // Get application instance
        app = (MindHavenApplication) getApplicationContext();

        // Initialize Firebase
        firebaseService = new FirebaseService();
        firebaseService.initialize();

        // Initialize views
        usernameInput = findViewById(R.id.username_input);
        registerButton = findViewById(R.id.register_button);
        loadingProgress = findViewById(R.id.loading_progress);

        // Set up register button
        registerButton.setOnClickListener(v -> {
            registerUsername();
        });
    }

    private void registerUsername() {
        String username = usernameInput.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate username format (e.g., alphanumeric only)
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            Toast.makeText(this, "Username can only contain letters, numbers, and underscores", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        loadingProgress.setVisibility(View.VISIBLE);
        registerButton.setEnabled(false);

        // Check if username is available
        firebaseService.checkUsernameAvailability(username, new FirebaseService.FriendRequestCallback() {
            @Override
            public void onResult(boolean success, String message) {
                if (success) {
                    // Username is available, register it
                    registerUsernameInFirebase(username);
                } else {
                    // Username is not available
                    runOnUiThread(() -> {
                        loadingProgress.setVisibility(View.GONE);
                        registerButton.setEnabled(true);
                        Toast.makeText(UsernameRegistrationActivity.this,
                                "Username already taken. Try another one.", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void registerUsernameInFirebase(String username) {
        firebaseService.registerUsername(username, new FirebaseService.FriendRequestCallback() {
            @Override
            public void onResult(boolean success, String message) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    registerButton.setEnabled(true);

                    if (success) {
                        // Save username in the application
                        app.setUniqueUsername(username);

                        // Get anonymous username from application if available
                        String anonymousUsername = app.getAnonymousUsername();
                        if (anonymousUsername != null) {
                            // Map the anonymous username to the unique username
                            firebaseService.mapAnonymousToUniqueUsername(anonymousUsername, username);
                        }

                        Toast.makeText(UsernameRegistrationActivity.this,
                                "Username registered successfully!", Toast.LENGTH_SHORT).show();

                        // Go to friend search instead of finishing
                        Intent searchIntent = new Intent(UsernameRegistrationActivity.this, FriendSearchActivity.class);
                        startActivity(searchIntent);
                        finish();
                    } else {
                        Toast.makeText(UsernameRegistrationActivity.this,
                                "Failed to register username: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}