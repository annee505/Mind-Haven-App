package com.example.mindhaven;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AnonymousChatActivity extends AppCompatActivity {
    private static final String TAG = "AnonymousChatActivity";
    private static final String PREFS_NAME = "MindHavenPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageInput;
    private Button sendButton;
    private ProgressBar progressBar;
    private TextView statusText;
    
    private FirebaseFirestore db;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private String currentUserId;
    private String currentUsername;
    private ListenerRegistration chatListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_chat);
        
        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        progressBar = findViewById(R.id.progressBar);
        statusText = findViewById(R.id.typingIndicator); // Repurpose this as status text
        
        // Show loading state
        progressBar.setVisibility(View.VISIBLE);
        statusText.setText("Connecting to chat...");
        statusText.setVisibility(View.VISIBLE);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        
        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setAdapter(chatAdapter);
        
        // Get or create user identity
        setupUserIdentity();
        
        // Set up send button
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                messageInput.setText("");
                sendMessageToFirestore(message);
            }
        });
        
        // Start listening for messages
        startListeningForMessages();
    }
    
    private void setupUserIdentity() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getString(KEY_USER_ID, null);
        currentUsername = prefs.getString(KEY_USERNAME, null);
        
        if (currentUserId == null || currentUsername == null) {
            currentUserId = UUID.randomUUID().toString();
            currentUsername = generateRandomUsername();
            
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_ID, currentUserId);
            editor.putString(KEY_USERNAME, currentUsername);
            editor.apply();
        }
        
        Log.d(TAG, "User identity: " + currentUsername + " (ID: " + currentUserId + ")");
        Toast.makeText(this, "Chatting as: " + currentUsername, Toast.LENGTH_SHORT).show();
    }
    
    private String generateRandomUsername() {
        String[] adjectives = {"Happy", "Clever", "Brave", "Calm", "Kind", "Wise", "Gentle", "Bold", "Bright", "Swift"};
        String[] nouns = {"Panda", "Tiger", "Eagle", "Dolphin", "Wolf", "Fox", "Rabbit", "Turtle", "Lion", "Owl"};
        
        Random random = new Random();
        String adjective = adjectives[random.nextInt(adjectives.length)];
        String noun = nouns[random.nextInt(nouns.length)];
        
        return adjective + noun;
    }
    
    private void sendMessageToFirestore(String messageText) {
        // Disable button while sending
        sendButton.setEnabled(false);
        
        // Create message data
        Map<String, Object> message = new HashMap<>();
        message.put("userId", currentUserId);
        message.put("username", currentUsername);
        message.put("text", messageText);
        message.put("timestamp", System.currentTimeMillis());
        
        // Send to Firestore
        db.collection("anonymous_chat")
            .add(message)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "Message sent with ID: " + documentReference.getId());
                sendButton.setEnabled(true);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error sending message", e);
                Toast.makeText(AnonymousChatActivity.this, 
                    "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                sendButton.setEnabled(true);
            });
    }
    
    private void startListeningForMessages() {
        if (chatListener != null) {
            chatListener.remove();
        }
        
        chatListener = db.collection("anonymous_chat")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Log.e(TAG, "Listen failed", error);
                    statusText.setText("Error loading messages");
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                
                if (snapshots == null) {
                    return;
                }
                
                // Hide loading indicators
                progressBar.setVisibility(View.GONE);
                statusText.setVisibility(View.GONE);
                
                // Process new messages
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        // Convert Firestore document to ChatMessage
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.setMessageId(dc.getDocument().getId());
                        chatMessage.setUserId(dc.getDocument().getString("userId"));
                        chatMessage.setUsername(dc.getDocument().getString("username"));
                        chatMessage.setText(dc.getDocument().getString("text"));
                        
                        // Set timestamp (handle potential null)
                        Object timestamp = dc.getDocument().get("timestamp");
                        if (timestamp != null) {
                            chatMessage.setTimestamp(Long.parseLong(timestamp.toString()));
                        } else {
                            chatMessage.setTimestamp(System.currentTimeMillis());
                        }
                        
                        // Mark if message is from current user
                        chatMessage.setCurrentUser(currentUserId.equals(chatMessage.getUserId()));
                        
                        // Add to list and notify adapter
                        chatMessages.add(chatMessage);
                    }
                }
                
                // Process messages for UI display (grouping, etc)
                processMessagesForDisplay();
            });
    }
    
    private void processMessagesForDisplay() {
        // Set username visibility based on message grouping
        if (!chatMessages.isEmpty()) {
            String lastUserId = "";
            for (int i = 0; i < chatMessages.size(); i++) {
                ChatMessage message = chatMessages.get(i);
                boolean showUsername = !message.getUserId().equals(lastUserId);
                message.setShowUsername(showUsername);
                lastUserId = message.getUserId();
            }
        }
        
        // Update UI
        chatAdapter.notifyDataSetChanged();
        if (!chatMessages.isEmpty()) {
            recyclerView.scrollToPosition(chatMessages.size() - 1);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) {
            chatListener.remove();
        }
    }
}