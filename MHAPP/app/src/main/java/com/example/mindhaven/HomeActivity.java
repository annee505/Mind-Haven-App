package com.example.mindhaven;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private TextView logOutTextView;
    private ImageView profileImage;
    private TextInputEditText displayNameInput;
    private TextInputEditText usernameInput;
    private Button saveProfileButton;
    private Switch notificationSwitch;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private Uri selectedImageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_home);

            mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                startActivity(new Intent(this, SignIn.class));
                finish();
                return;
            }

            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            storageRef = FirebaseStorage.getInstance().getReference("profile_images").child(currentUser.getUid());

            // Initialize UI components
            bottomNavigationView = findViewById(R.id.bottomNavigationView);
            drawerLayout = findViewById(R.id.drawerLayout);
            logOutTextView = findViewById(R.id.logout);
            profileImage = findViewById(R.id.profileImage);
            displayNameInput = findViewById(R.id.displayNameInput);
            usernameInput = findViewById(R.id.usernameInput);
            saveProfileButton = findViewById(R.id.saveProfileButton);
            notificationSwitch = findViewById(R.id.notificationSwitch);

            // Check and request notification permission
            checkAndRequestNotificationPermission();
            
            // Initialize notification manager
            MoodNotificationManager notificationManager = ((MindHavenApp) getApplication()).getNotificationManager();
            if (notificationManager == null) {
                throw new IllegalStateException("Notification manager not initialized");
            }
            notificationSwitch.setChecked(notificationManager.areNotificationsEnabled());
            
            // Set up notification switch listener
            notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                MoodNotificationManager manager = ((MindHavenApp) getApplication()).getNotificationManager();
                if (manager != null) {
                    manager.setNotificationsEnabled(isChecked);
                    if (isChecked) {
                        // If enabled, send a test notification right away
                        sendTestNotification();
                    }
                    Toast.makeText(this, 
                        isChecked ? "Notifications enabled" : "Notifications disabled", 
                        Toast.LENGTH_SHORT).show();
                }
            });

            // Initialize image picker
            imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        try {
                            Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(profileImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            );

            loadUserProfile();

            profileImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
            
            saveProfileButton.setOnClickListener(v -> saveUserProfile());

            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_chat) {
                    startActivity(new Intent(HomeActivity.this, AnonymousChatActivity.class));
                    return true;
                } else {
                    Fragment selectedFragment = null;

                    if (itemId == R.id.nav_analytics) {
                        try {
                            // Check for Firebase authentication first
                            if (mAuth.getCurrentUser() == null) {
                                Toast.makeText(this, "Please log in to view analytics", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, SignIn.class));
                                return false;
                            }
                            
                            // Add a slight delay to ensure Firebase operations complete
                            Toast.makeText(this, "Loading analytics...", Toast.LENGTH_SHORT).show();
                            selectedFragment = new AnalyticsFragment();
                        } catch (Exception e) {
                            Toast.makeText(this, "Error loading analytics: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                            // Fallback to home fragment
                            selectedFragment = new HomeFragment();
                        }
                    } else if (itemId == R.id.nav_home) {
                        selectedFragment = new HomeFragment();
                    } else if (itemId == R.id.nav_ai) {
                        selectedFragment = new AiFragment();
                    } else if (itemId == R.id.nav_therapy) {
                        selectedFragment = new TherapyFragment();
                    }

                    if (selectedFragment != null) {
                        try {
                            final Fragment fragmentToShow = selectedFragment;
                            // For analytics, add a small delay to ensure data has synced
                            if (itemId == R.id.nav_analytics) {
                                new android.os.Handler().postDelayed(() -> {
                                    try {
                                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                        transaction.replace(R.id.fragmentContainer, fragmentToShow);
                                        transaction.commit();
                                    } catch (Exception e) {
                                        Toast.makeText(HomeActivity.this, "Error showing fragment: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    }
                                }, 300); // 300ms delay to allow Firebase operations to complete
                            } else {
                                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragmentContainer, fragmentToShow);
                                transaction.commit();
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "Error navigating: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
            });

            if (savedInstanceState == null) {
                // Clear any existing fragments first
                getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                
                // Check if we need to navigate to a specific fragment
                String navigateTo = getIntent().getStringExtra("navigate_to");
                if (navigateTo != null && navigateTo.equals("mood_tracker")) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new MoodTrackerFragment())
                        .commit();
                    Toast.makeText(this, "Opening mood tracker...", Toast.LENGTH_SHORT).show();
                } else if (getIntent().getBooleanExtra("open_mood_tracker", false)) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new MoodTrackerFragment())
                        .commit();
                } else {
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                    getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new HomeFragment())
                        .commit();
                }
            }

            logOutTextView.setOnClickListener(v -> logOut());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to initialize app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadUserProfile() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserProfile profile = snapshot.getValue(UserProfile.class);
                if (profile != null) {
                    displayNameInput.setText(profile.getDisplayName());
                    usernameInput.setText(profile.getUsername());
                    if (profile.getProfileImageUrl() != null) {
                        Glide.with(HomeActivity.this)
                            .load(profile.getProfileImageUrl())
                            .circleCrop()
                            .into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Failed to load profile: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserProfile() {
        String displayName = displayNameInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();

        if (displayName.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        saveProfileButton.setEnabled(false);

        if (selectedImageUri != null) {

            StorageReference imageRef = storageRef.child("profile.jpg");
            imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveProfileData(displayName, username, uri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    saveProfileButton.setEnabled(true);
                    Toast.makeText(HomeActivity.this, "Failed to upload image: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        } else {

            saveProfileData(displayName, username, null);
        }
    }

    private void saveProfileData(String displayName, String username, String imageUrl) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        UserProfile profile = new UserProfile(
            currentUser.getUid(),
            displayName,
            username,
            imageUrl != null ? imageUrl : getCurrentProfileImageUrl()
        );

        userRef.setValue(profile)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(HomeActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                saveProfileButton.setEnabled(true);
                selectedImageUri = null;
            })
            .addOnFailureListener(e -> {
                Toast.makeText(HomeActivity.this, "Failed to update profile: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                saveProfileButton.setEnabled(true);
            });
    }

    private String getCurrentProfileImageUrl() {

        Object tag = profileImage.getTag();
        return tag != null ? tag.toString() : null;
    }

    private void logOut() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, SignIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                // Permission already granted, ensure notifications are enabled
                ensureNotificationsEnabled();
            }
        } else {
            // For versions below TIRAMISU, notification permission is granted by default
            ensureNotificationsEnabled();
        }
    }

    private void ensureNotificationsEnabled() {
        MoodNotificationManager notificationManager = ((MindHavenApp) getApplication()).getNotificationManager();
        if (notificationManager != null) {
            notificationManager.setNotificationsEnabled(true);
            
            // Check if we have saved custom notification settings
            android.content.SharedPreferences prefs = getSharedPreferences("MoodTrackerPrefs", android.content.Context.MODE_PRIVATE);
            String savedTime = prefs.getString("custom_notification_time", "");
            
            if (!savedTime.isEmpty()) {
                // We have a saved custom notification, use it
                notificationManager.reInitializeNotificationsWithSavedPrefs();
            } else {
                // No saved custom notification, use daily
                notificationManager.reInitializeNotifications();
            }
            
            // Send a test notification to verify permissions are working
            sendTestNotification();
        }
    }
    
    private void sendTestNotification() {
        // Create an intent that will be sent to the NotificationReceiver
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("custom_message", "Test notification - Tap to track your mood now!");
        
        // Send the broadcast to trigger the notification
        sendBroadcast(intent);
        
        Toast.makeText(this, "Test notification sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                ensureNotificationsEnabled();
            } else {
                Toast.makeText(this, "Notifications will not work without permission", Toast.LENGTH_SHORT).show();
                notificationSwitch.setChecked(false);
            }
        }
    }
}
