package com.example.mindhaven;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private EditText usernameInput;
    private Button saveButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        usernameInput = findViewById(R.id.usernameInput);
        saveButton = findViewById(R.id.saveButton);

        // Load existing username if any
        loadExistingUsername();

        saveButton.setOnClickListener(v -> saveUsername());
    }

    private void loadExistingUsername() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && document.getString("username") != null) {
                        usernameInput.setText(document.getString("username"));
                    }
                });
    }

    private void saveUsername() {
        String username = usernameInput.getText().toString().trim();
        if (username.isEmpty()) {
            usernameInput.setError("Username cannot be empty");
            return;
        }

        // Check if username is already taken
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty() &&
                            !queryDocumentSnapshots.getDocuments().get(0).getId().equals(auth.getCurrentUser().getUid())) {
                        usernameInput.setError("Username already taken");
                        return;
                    }

                    // Save the username
                    Map<String, Object> user = new HashMap<>();
                    user.put("username", username);
                    user.put("userId", auth.getCurrentUser().getUid());

                    db.collection("users").document(auth.getCurrentUser().getUid())
                            .set(user)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Username saved successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error saving username", Toast.LENGTH_SHORT).show());
                });
    }
}
