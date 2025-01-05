package com.example.mindhavenapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            if (currentUser.isEmailVerified()) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, SignIn.class));
                finish();
            }
        } else {
            Button getStartedButton = findViewById(R.id.btn_next);
            getStartedButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, SignIn.class));
            });
        }
    }
}
