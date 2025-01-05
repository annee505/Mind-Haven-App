package com.example.mindhavenapp;

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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class AnonymousChatActivity extends AppCompatActivity {

    private DatabaseReference messagesRef;
    private EditText messageInput;
    private Button sendMessageButton;
    private LinearLayout messagesLinearLayout;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_chat);

        username = "User_" + UUID.randomUUID().toString().substring(0, 6);
        messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        messageInput = findViewById(R.id.messageInput);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messagesLinearLayout = findViewById(R.id.messagesLinearLayout);

        sendMessageButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                Message message = new Message(username, messageText, System.currentTimeMillis());
                messagesRef.push().setValue(message);
                addMessageToUI(message);
                messageInput.setText("");
            } else {
                Toast.makeText(AnonymousChatActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });
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
}
