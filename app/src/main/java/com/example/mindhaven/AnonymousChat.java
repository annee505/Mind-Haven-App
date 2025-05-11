package com.example.mindhaven;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnonymousChat extends AppCompatActivity {
    private static final String TAG = "AnonymousChat";
    private static final String APP_TITLE = "Anonymous Chat";
    private static final String PREFS_NAME = "AnonymousChatPrefs";
    private static final String USERNAME_KEY = "anonymousUsername";

    private String username;
    private FirebaseService firebaseService;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private TextView usernameText;
    private List<Message> messageList = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private MindHavenApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_chat);

        // Get application instance
        app = (MindHavenApplication) getApplicationContext();

        // Set up the action bar title
        setTitle(APP_TITLE);

        // Get or generate username
        getOrGenerateUsername();

        // Initialize UI components
        initializeViews();

        // Initialize Firebase and load messages
        initializeFirebase();
        loadMessages();
    }

    private void getOrGenerateUsername() {
        // Try to get existing username from Application
        if (app.hasAnonymousUsername()) {
            username = app.getAnonymousUsername();
            Log.d(TAG, "Using existing anonymous username: " + username);
        } else {
            // Generate a new username
            String randomSuffix = UUID.randomUUID().toString().substring(0, 5);
            username = "user_" + randomSuffix;

            // Save to Application
            app.setAnonymousUsername(username);
            Log.d(TAG, "Generated new anonymous username: " + username);
        }
    }

    private void initializeViews() {
        // Find views from layout
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        usernameText = findViewById(R.id.username_label);

        // Update the UI with the username
        usernameText.setText("Your username: " + username);

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messageList, this, username);

        // Set up message click listener for private chat
        messageAdapter.setOnMessageClickListener(message -> {
            showPrivateMessageOption(message);
        });

        messagesRecyclerView.setAdapter(messageAdapter);

        // Set up click listener for send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void initializeFirebase() {
        firebaseService = new FirebaseService();
        firebaseService.initialize();

        // Set up message listener
        firebaseService.addMessageListener(messages -> {
            runOnUiThread(() -> {
                messageList.clear();
                messageList.addAll(messages);

                // Update adapter with current username
                messageAdapter.setCurrentUsername(username);

                messageAdapter.notifyDataSetChanged();

                // Scroll to the bottom of the list
                if (!messageList.isEmpty()) {
                    messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            });
        });
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (!content.isEmpty()) {
            long timestamp = System.currentTimeMillis();
            Message message = new Message(username, content, timestamp);
            firebaseService.sendMessage(message);
            messageInput.setText("");
        }
    }

    private void loadMessages() {
        firebaseService.loadMessages();
    }

    private void showPrivateMessageOption(Message message) {
        // Don't show dialog for your own messages
        if (message.getSender().equals(username)) {
            return;
        }

        // Get the sender's username
        String otherUser = message.getSender();

        // Create a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chat with " + otherUser)
                .setMessage("Would you like to start a private conversation with this user?")
                .setPositiveButton("Yes", (dialog, id) -> {
                    startPrivateChat(otherUser);
                })
                .setNegativeButton("No", (dialog, id) -> {
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startPrivateChat(String otherUsername) {
        // Check if this user has registered a unique username
        if (!app.hasUniqueUsername()) {
            // The user needs to register a unique username first
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Register Unique Username")
                    .setMessage("You need to register a unique username before starting private chats. " +
                            "This is different from your anonymous chat username (" + username + ").")
                    .setPositiveButton("Register Now", (dialog, id) -> {
                        Intent registerIntent = new Intent(this, UsernameRegistrationActivity.class);
                        startActivity(registerIntent);
                    })
                    .setNegativeButton("Later", (dialog, id) -> {
                        dialog.dismiss();
                    });

            builder.create().show();
            return;
        }

        // Use the unique username for private chats
        String myUsername = app.getUniqueUsername();

        // Send a friend request first
        firebaseService.sendFriendRequest(myUsername, otherUsername, new FirebaseService.FriendRequestCallback() {
            @Override
            public void onResult(boolean success, String message) {
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(AnonymousChat.this,
                                "Friend request sent to " + otherUsername + " using your unique username: " + myUsername,
                                Toast.LENGTH_LONG).show();

                        // Show option to view friend requests
                        AlertDialog.Builder builder = new AlertDialog.Builder(AnonymousChat.this);
                        builder.setTitle("Friend Request Sent")
                                .setMessage("You can view your friend requests and chats in the Chat List.")
                                .setPositiveButton("View Chat List", (dialog, id) -> {
                                    Intent chatListIntent = new Intent(AnonymousChat.this, ChatListActivity.class);
                                    startActivity(chatListIntent);
                                })
                                .setNegativeButton("Stay Here", (dialog, id) -> {
                                    dialog.dismiss();
                                });

                        builder.create().show();
                    } else {
                        Toast.makeText(AnonymousChat.this, "Failed to send friend request: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Make sure we're using the latest username
        if (app.hasAnonymousUsername()) {
            username = app.getAnonymousUsername();
            usernameText.setText("Your username: " + username);

            if (messageAdapter != null) {
                messageAdapter.setCurrentUsername(username);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (firebaseService != null) {
            firebaseService.cleanup();
        }
    }
}