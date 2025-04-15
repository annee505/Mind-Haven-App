package com.example.mindhaven;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnonymousChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageInput;
    private Button sendButton;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private List<ChatMessage> chatMessages;
    private String userId;
    private ListenerRegistration chatListener;
    private static final String CHAT_ROOM_ID = "global_chat"; // You can make this dynamic for multiple chat rooms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_chat);

        // Generate a unique ID for this user session
        userId = UUID.randomUUID().toString();

        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        progressBar = findViewById(R.id.progressBar);

        chatMessages = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setAdapter(chatAdapter);

        setupChatListener();

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendButton.setEnabled(false);
                messageInput.setText("");
                sendMessage(message);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) {
            chatListener.remove(); // Clean up the listener when the activity is destroyed
        }
    }

    private void setupChatListener() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        Query query = db.collection("chat_rooms")
                .document(CHAT_ROOM_ID)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);

        chatListener = query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (e != null) {
                Log.e("AnonymousChat", "Listen failed.", e);
                return;
            }

            if (queryDocumentSnapshots != null) {
                chatMessages.clear();
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    ChatMessage message = document.toObject(ChatMessage.class);
                    if (message != null) {
                        message.setMessageId(document.getId());
                        chatMessages.add(message);
                    }
                }
                chatAdapter.notifyDataSetChanged();
                if (!chatMessages.isEmpty()) {
                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                }
            }
        });
    }

    private void sendMessage(String messageText) {
        ChatMessage message = new ChatMessage(
                userId, // Use the unique user ID
                "anonymous_" + userId.substring(0, 6), // Create a shorter anonymous username
                messageText,
                System.currentTimeMillis(),
                true
        );

        db.collection("chat_rooms")
                .document(CHAT_ROOM_ID)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    message.setMessageId(documentReference.getId());
                    sendButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("AnonymousChat", "Error sending message: " + e.getMessage());
                    sendButton.setEnabled(true);
                });
    }
}
