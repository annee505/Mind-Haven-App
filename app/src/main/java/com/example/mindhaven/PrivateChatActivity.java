package com.example.mindhaven;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PrivateChatActivity extends AppCompatActivity {
    private String chatId;
    private String otherUsername;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private ProgressBar loadingProgress;
    private TextView noMessagesText;
    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private FirebaseService firebaseService;
    private MindHavenApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        // Get the chat ID and other user from the intent
        chatId = getIntent().getStringExtra("CHAT_ID");
        otherUsername = getIntent().getStringExtra("OTHER_USER");

        if (chatId == null || otherUsername == null) {
            Toast.makeText(this, "Error: Invalid chat data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get application instance
        app = (MindHavenApplication) getApplicationContext();

        // Set the title to the other user's name
        setTitle("Chat with " + otherUsername);

        // Initialize Firebase
        firebaseService = new FirebaseService();
        firebaseService.initialize();

        // Initialize views
        messagesRecyclerView = findViewById(R.id.private_messages_recycler_view);
        messageInput = findViewById(R.id.private_message_input);
        sendButton = findViewById(R.id.private_send_button);
        loadingProgress = findViewById(R.id.private_loading_progress);
        noMessagesText = findViewById(R.id.private_no_messages_text);

        // Style the send button to be brown
        sendButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        messagesRecyclerView.setLayoutManager(layoutManager);

        String currentUsername = app.getUniqueUsername();
        messageAdapter = new MessageAdapter(messageList, this, currentUsername);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Load messages
        loadMessages();

        // Set up send button
        sendButton.setOnClickListener(v -> {
            sendPrivateMessage();
        });
    }

    private void loadMessages() {
        // Show loading indicator
        loadingProgress.setVisibility(View.VISIBLE);
        noMessagesText.setVisibility(View.GONE);

        firebaseService.getPrivateChatMessages(chatId, new FirebaseService.PrivateChatMessagesCallback() {
            @Override
            public void onMessagesLoaded(List<Message> messages) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    messageList.clear();
                    messageList.addAll(messages);

                    if (messages.isEmpty()) {
                        noMessagesText.setText("No messages yet. Start the conversation!");
                        noMessagesText.setVisibility(View.VISIBLE);
                    } else {
                        noMessagesText.setVisibility(View.GONE);
                    }

                    messageAdapter.notifyDataSetChanged();

                    // Scroll to the bottom
                    if (!messageList.isEmpty()) {
                        messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(PrivateChatActivity.this,
                            "Failed to load messages: " + errorMessage, Toast.LENGTH_SHORT).show();
                    noMessagesText.setText("Failed to load messages. Try again later.");
                    noMessagesText.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void sendPrivateMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        // Disable send button
        sendButton.setEnabled(false);

        // Current username
        String currentUsername = app.getUniqueUsername();

        // Send message
        firebaseService.sendPrivateMessage(chatId, currentUsername, content, new FirebaseService.FriendRequestCallback() {
            @Override
            public void onResult(boolean success, String message) {
                runOnUiThread(() -> {
                    sendButton.setEnabled(true);

                    if (success) {
                        messageInput.setText("");
                    } else {
                        Toast.makeText(PrivateChatActivity.this,
                                "Failed to send message: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}