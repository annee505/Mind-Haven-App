package com.example.mindhaven;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class SimpleAnonymousChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private DatabaseReference chatRef;
    private String randomUserId; // Random identifier for this session
    private ChildEventListener chatListener; // Listener reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_chat);

        randomUserId = "user_" + System.currentTimeMillis();

        chatRef = FirebaseDatabase.getInstance().getReference("public_chats");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Use the correct constructor for anonymous chat
        adapter = new ChatAdapter(this, messages, randomUserId);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.sendButton).setOnClickListener(v -> {
            EditText input = findViewById(R.id.messageInput);
            String messageText = input.getText().toString().trim();
            if (!messageText.isEmpty()) {
                ChatMessage message = new ChatMessage(messageText, randomUserId, System.currentTimeMillis());
                chatRef.push().setValue(message);
                input.setText("");
            }
        });

        // Add listener to receive messages
        attachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        if (chatListener == null) {
            chatListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    if (chatMessage != null) {
                        messages.add(chatMessage);
                        adapter.notifyItemInserted(messages.size() - 1);
                        recyclerView.scrollToPosition(messages.size() - 1); // Scroll to bottom
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) { }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
                @Override
                public void onCancelled(DatabaseError databaseError) { }
            };
            chatRef.addChildEventListener(chatListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detach listener when activity is destroyed
        if (chatListener != null) {
            chatRef.removeEventListener(chatListener);
            chatListener = null;
        }
    }
}