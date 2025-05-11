package com.example.mindhaven;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView statusText;
    private Button resendButton;
    private Button refreshButton;
    private Handler handler;
    private Runnable verificationChecker;
    private static final int CHECK_INTERVAL = 3000; // Check every 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // If user is null or already verified, redirect appropriately
        if (user == null) {
            startActivity(new Intent(this, SignIn.class));
            finish();
            return;
        } else if (user.isEmailVerified()) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        statusText = findViewById(R.id.verificationStatusText);
        resendButton = findViewById(R.id.resendVerificationButton);
        refreshButton = findViewById(R.id.refreshStatusButton);
        Button signOutButton = findViewById(R.id.signOutButton);

        updateStatus();

        // Set up periodic verification checks
        handler = new Handler();
        verificationChecker = new Runnable() {
            @Override
            public void run() {
                checkVerificationStatus();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };

        resendButton.setOnClickListener(v -> resendVerificationEmail());
        refreshButton.setOnClickListener(v -> checkVerificationStatus());
        signOutButton.setOnClickListener(v -> signOut());
    }

    private void updateStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            statusText.setText(String.format("Verification email sent to:\n%s\n\nPlease check your inbox and spam folder.", user.getEmail()));
        }
    }

    private void checkVerificationStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (user.isEmailVerified()) {
                        Toast.makeText(this, "Email verified! Redirecting...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Email not yet verified", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void resendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Verification email resent", Toast.LENGTH_SHORT).show();
                            resendButton.setEnabled(false);
                            handler.postDelayed(() -> resendButton.setEnabled(true), 30000); // Enable after 30 seconds
                        } else {
                            Toast.makeText(this, "Failed to resend verification email", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void signOut() {
        mAuth.signOut();
        startActivity(new Intent(this, SignIn.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(verificationChecker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(verificationChecker);
    }
}
