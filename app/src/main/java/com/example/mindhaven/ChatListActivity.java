package com.example.mindhaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {
    private RecyclerView chatListRecyclerView;
    private TextView emptyChatText;
    private ProgressBar loadingProgress;
    private ChatListAdapter chatListAdapter;
    private List<ChatPreview> chatList = new ArrayList<>();
    private FirebaseService firebaseService;
    private MindHavenApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // Set title
        setTitle("Your Chats");

        // Get application instance
        app = (MindHavenApplication) getApplicationContext();

        // Initialize Firebase
        firebaseService = new FirebaseService();
        firebaseService.initialize();

        // Initialize views
        chatListRecyclerView = findViewById(R.id.chat_list_recycler_view);
        emptyChatText = findViewById(R.id.empty_chat_text);
        loadingProgress = findViewById(R.id.loading_progress);

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatListRecyclerView.setLayoutManager(layoutManager);

        // Add the anonymous chat as the first item
        chatList.add(new ChatPreview(
                "anonymous_chat",
                "Anonymous Chat",
                "Chat anonymously with anyone",
                System.currentTimeMillis(),
                true // This indicates it's the anonymous chat, not a private one
        ));

        // Set up adapter
        chatListAdapter = new ChatListAdapter(chatList, this);
        chatListRecyclerView.setAdapter(chatListAdapter);

        // Set up chat click listener
        chatListAdapter.setOnChatClickListener(chatPreview -> {
            if (chatPreview.isAnonymousChat()) {
                // Open anonymous chat
                Intent anonymousChatIntent = new Intent(this, AnonymousChat.class);
                startActivity(anonymousChatIntent);
            } else {
                // Open private chat
                Intent privateChatIntent = new Intent(this, PrivateChatActivity.class);
                privateChatIntent.putExtra("CHAT_ID", chatPreview.getChatId());
                privateChatIntent.putExtra("OTHER_USER", chatPreview.getName());
                startActivity(privateChatIntent);
            }
        });

        // Check if user has registered a unique username
        if (!app.hasUniqueUsername()) {
            showRegisterUsernameMessage();
        } else {
            // Load private chats
            loadPrivateChats();
        }
    }

    private void showRegisterUsernameMessage() {
        emptyChatText.setText("Register a unique username to use private chats");
        emptyChatText.setVisibility(View.VISIBLE);
    }

    private void loadPrivateChats() {
        // Show loading indicator
        loadingProgress.setVisibility(View.VISIBLE);
        emptyChatText.setVisibility(View.GONE);

        String username = app.getUniqueUsername();

        firebaseService.getPrivateChats(username, new FirebaseService.PrivateChatsCallback() {
            @Override
            public void onChatsLoaded(List<ChatPreview> privateChats) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    // Keep the anonymous chat entry
                    chatList.clear();
                    chatList.add(new ChatPreview(
                            "anonymous_chat",
                            "Anonymous Chat",
                            "Chat anonymously with anyone",
                            System.currentTimeMillis(),
                            true
                    ));

                    // Add private chats
                    chatList.addAll(privateChats);

                    // Show empty text if no private chats
                    if (privateChats.isEmpty()) {
                        emptyChatText.setText("No private chats yet. Start by finding friends!");
                        emptyChatText.setVisibility(View.VISIBLE);
                    } else {
                        emptyChatText.setVisibility(View.GONE);
                    }

                    // Sort by timestamp (most recent first), but keep anonymous chat at top
                    if (chatList.size() > 1) {
                        Collections.sort(chatList.subList(1, chatList.size()),
                                (c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
                    }

                    chatListAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(ChatListActivity.this,
                            "Failed to load chats: " + errorMessage, Toast.LENGTH_SHORT).show();
                    emptyChatText.setText("Failed to load chats. Try again later.");
                    emptyChatText.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_find_friends) {
            Intent findFriendsIntent = new Intent(this, FriendSearchActivity.class);
            startActivity(findFriendsIntent);
            return true;
        } else if (id == R.id.action_friend_requests) {
            Intent friendRequestsIntent = new Intent(this, FriendRequestsActivity.class);
            startActivity(friendRequestsIntent);
            return true;
        } else if (id == R.id.action_register_username) {
            Intent registerUsernameIntent = new Intent(this, UsernameRegistrationActivity.class);
            startActivity(registerUsernameIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload private chats when returning to this screen
        if (app.hasUniqueUsername()) {
            loadPrivateChats();
        }
    }
}