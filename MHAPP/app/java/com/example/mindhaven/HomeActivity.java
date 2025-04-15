package com.example.mindhaven;

import android.content.Intent;
import android.net.Uri;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        drawerLayout = findViewById(R.id.drawerLayout);
        logOutTextView = findViewById(R.id.logout);
        profileImage = findViewById(R.id.profileImage);
        displayNameInput = findViewById(R.id.displayNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        notificationSwitch = findViewById(R.id.notificationSwitch);

        MoodNotificationManager notificationManager = ((MindHavenApp) getApplication()).getNotificationManager();
        notificationSwitch.setChecked(notificationManager.areNotificationsEnabled());
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            notificationManager.setNotificationsEnabled(isChecked);
            Toast.makeText(this, 
                isChecked ? "Notifications enabled" : "Notifications disabled", 
                Toast.LENGTH_SHORT).show();
        });

        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this)
                        .load(uri)
                        .circleCrop()
                        .into(profileImage);
                }
            }
        );


        loadUserProfile();


        profileImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        
        saveProfileButton.setOnClickListener(v -> saveUserProfile());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_chat) {
                startActivity(new Intent(HomeActivity.this, SimpleAnonymousChatActivity.class));
                return true;
            } else {
                Fragment selectedFragment = null;

                if (itemId == R.id.nav_analytics) {
                    selectedFragment = new AnalyticsFragment();
                } else if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_ai) {
                    selectedFragment = new AiFragment();
                } else if (itemId == R.id.nav_therapy) {
                    selectedFragment = new MeditationFragment();
                }

                if (selectedFragment != null) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragmentContainer, selectedFragment);
                    transaction.commit();
                }
                return true;
            }
        });

        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra("open_mood_tracker", false)) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
                getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new MoodTrackerFragment())
                    .commit();
            } else {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }

        logOutTextView.setOnClickListener(v -> logOut());
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
}
