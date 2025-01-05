package com.example.mindhavenapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference messagesRef;
    private EditText messageInput;
    private Button sendMessageButton;
    private LinearLayout messagesLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(HomeActivity.this, SignIn.class));
            finish();
            return;
        }

        messageInput = findViewById(R.id.messageInput);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messagesLinearLayout = findViewById(R.id.messagesLinearLayout);

        messagesRef = FirebaseDatabase.getInstance().getReference("messages");

        sendMessageButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();

            if (!TextUtils.isEmpty(messageText)) {
                String anonymousUsername = "User_" + UUID.randomUUID().toString().substring(0, 6);
                Message message = new Message(anonymousUsername, messageText, System.currentTimeMillis());
                messagesRef.push().setValue(message);
                addMessageToUI(message);
                messageInput.setText("");
            } else {
                Toast.makeText(HomeActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });

        Button openAnonymousChatButton = findViewById(R.id.openAnonymousChatButton);
        openAnonymousChatButton.setOnClickListener(v -> openAnonymousChat());
    }

    private void addMessageToUI(Message message) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View messageView = inflater.inflate(R.layout.message_item, null);

        TextView userTextView = messageView.findViewById(R.id.userTextView);
        TextView messageTextView = messageView.findViewById(R.id.messageTextView);

        userTextView.setText(message.getUser());
        messageTextView.setText(message.getText());

        messagesLinearLayout.addView(messageView);
    }

    private void openAnonymousChat() {
        Intent intent = new Intent(HomeActivity.this, AnonymousChatActivity.class);
        startActivity(intent);
    }
}
