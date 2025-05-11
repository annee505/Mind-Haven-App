package com.example.mindhaven;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignIn extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        EditText emailEditText = findViewById(R.id.editTextTextEmailAddress2);
        EditText passwordEditText = findViewById(R.id.editTextTextEmailAddress4);
        Button loginButton = findViewById(R.id.button3);
        TextView forgotPasswordTextView = findViewById(R.id.textForgotPassword);
        TextView signUpTextView = findViewById(R.id.textSignUp);


        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(SignIn.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null && user.isEmailVerified()) {
                                    startActivity(new Intent(SignIn.this, HomeActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(SignIn.this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(SignIn.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        signUpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(SignIn.this, SignUp.class);
            startActivity(intent);
        });


        forgotPasswordTextView.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(SignIn.this, "Please enter your email address to reset password", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignIn.this, "Password reset email sent. Check your inbox.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignIn.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
