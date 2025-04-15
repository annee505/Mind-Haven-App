package com.example.mindhaven;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SimpleAnonymousChatActivity extends AppCompatActivity {

    // UI Components
    private RecyclerView messageRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private TextView statusTextView;
    
    // Firebase
    private DatabaseReference chatRef;
    
    // Data
    private String userId;
    private String username;
    private List<Map<String, Object>> messages = new ArrayList<>();
    private SimpleChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anonymous_chat);
        
        // Initialize Firebase Realtime Database (simpler than Firestore)
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        chatRef = database.getReference("anonymous_chat_messages");
        
        // Find views
        messageRecyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        statusTextView = findViewById(R.id.typingIndicator);
        
        // Generate random username and ID if not saved
        userId = UUID.randomUUID().toString().substring(0, 8);
        username = "User" + userId.substring(0, 4);
        
        Toast.makeText(this, "Chatting as: " + username, Toast.LENGTH_SHORT).show();
        
        // Set up RecyclerView
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleChatAdapter(messages, userId);
        messageRecyclerView.setAdapter(adapter);
        
        // Set up send button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        
        // Load messages
        loadMessages();
    }
    
    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        
        // Clear input immediately
        messageInput.setText("");
        
        // Create message
        Map<String, Object> message = new HashMap<>();
        message.put("userId", userId);
        message.put("username", username);
        message.put("text", text);
        message.put("timestamp", System.currentTimeMillis());
        
        // Generate a unique key for the message
        String messageId = chatRef.push().getKey();
        
        if (messageId != null) {
            // Send message to Firebase
            chatRef.child(messageId).setValue(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Message sent successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SimpleAnonymousChatActivity.this, 
                                "Failed to send: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }
    
    private void loadMessages() {
        statusTextView.setText("Loading messages...");
        statusTextView.setVisibility(View.VISIBLE);
        
        // Listen for messages
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> message = new HashMap<>();
                    message.put("messageId", snapshot.getKey());
                    
                    // Get all fields from the snapshot
                    for (DataSnapshot child : snapshot.getChildren()) {
                        message.put(child.getKey(), child.getValue());
                    }
                    
                    messages.add(message);
                }
                
                // Update UI
                adapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    messageRecyclerView.scrollToPosition(messages.size() - 1);
                }
                
                statusTextView.setVisibility(View.GONE);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                statusTextView.setText("Error: " + databaseError.getMessage());
            }
        });
    }
    
    // Simple adapter for the chat messages
    private static class SimpleChatAdapter extends RecyclerView.Adapter<SimpleChatAdapter.MessageViewHolder> {
        private List<Map<String, Object>> messages;
        private String currentUserId;
        
        public SimpleChatAdapter(List<Map<String, Object>> messages, String currentUserId) {
            this.messages = messages;
            this.currentUserId = currentUserId;
        }
        
        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message, parent, false);
            return new MessageViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            Map<String, Object> message = messages.get(position);
            
            String userId = (String) message.get("userId");
            String username = (String) message.get("username");
            String text = (String) message.get("text");
            
            holder.usernameTextView.setText(username);
            holder.messageTextView.setText(text);
            
            // Set alignment based on whether it's the current user
            boolean isCurrentUser = currentUserId.equals(userId);
            
            // Assuming your layout has these views and properties
            if (isCurrentUser) {
                holder.messageContainer.setBackgroundResource(R.drawable.bg_message_sent);
                holder.itemView.setPadding(100, 10, 10, 10); // Push to right
            } else {
                holder.messageContainer.setBackgroundResource(R.drawable.bg_message_received);
                holder.itemView.setPadding(10, 10, 100, 10); // Push to left
            }
            
            // Show username for first message or when sender changes
            boolean showUsername = true;
            if (position > 0) {
                Map<String, Object> previousMessage = messages.get(position - 1);
                String previousUserId = (String) previousMessage.get("userId");
                showUsername = !userId.equals(previousUserId);
            }
            
            holder.usernameTextView.setVisibility(showUsername ? View.VISIBLE : View.GONE);
        }
        
        @Override
        public int getItemCount() {
            return messages.size();
        }
        
        static class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView usernameTextView;
            TextView messageTextView;
            View messageContainer;
            
            MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                usernameTextView = itemView.findViewById(R.id.textUsername);
                messageTextView = itemView.findViewById(R.id.textMessage);
                messageContainer = itemView.findViewById(R.id.messageContainer);
            }
        }
    }
} 