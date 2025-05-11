package com.example.mindhaven;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class UsernameRegistrationDialog extends Dialog {
    private EditText usernameInput;
    private Button registerButton;
    private ProgressBar loadingProgress;
    private FirebaseService firebaseService;
    private MindHavenApplication app;
    private OnUsernameRegisteredListener listener;

    public interface OnUsernameRegisteredListener {
        void onUsernameRegistered();
    }

    public UsernameRegistrationDialog(Context context, MindHavenApplication app, FirebaseService firebaseService) {
        super(context);
        this.app = app;
        this.firebaseService = firebaseService;
    }

    public void setOnUsernameRegisteredListener(OnUsernameRegisteredListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_username_registration);

        // Initialize views
        usernameInput = findViewById(R.id.username_input);
        registerButton = findViewById(R.id.register_button);
        loadingProgress = findViewById(R.id.loading_progress);

        // Style the register button to be brown
        registerButton.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_orange_dark));

        // Set up register button
        registerButton.setOnClickListener(v -> {
            registerUsername();
        });
    }

    private void registerUsername() {
        String username = usernameInput.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate username format (e.g., alphanumeric only)
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            Toast.makeText(getContext(), "Username can only contain letters, numbers, and underscores", Toast.LENGTH_SHORT).show();
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
                    loadingProgress.setVisibility(View.GONE);
                    registerButton.setEnabled(true);
                    Toast.makeText(getContext(), "Username already taken. Try another one.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerUsernameInFirebase(String username) {
        firebaseService.registerUsername(username, new FirebaseService.FriendRequestCallback() {
            @Override
            public void onResult(boolean success, String message) {
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

                    Toast.makeText(getContext(), "Username registered successfully!", Toast.LENGTH_SHORT).show();

                    // Notify listener
                    if (listener != null) {
                        listener.onUsernameRegistered();
                    }

                    // Dismiss dialog
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed to register username: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}