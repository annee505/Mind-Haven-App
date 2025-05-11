package com.example.mindhaven;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;

public class FirebaseService {
    private FirebaseDatabase firebaseDatabase;
    private ValueEventListener messagesListener;
    private DatabaseReference messagesRef;
    public DatabaseReference privateChatsRef;

    // Message listener interface
    public interface MessageListener {
        void onMessagesUpdated(List<Message> messages);
    }

    // Search results callback
    public interface SearchResultsCallback {
        void onSearchResults(List<String> results);
        void onError(String errorMessage);
    }

    // Friend request callback
    public interface FriendRequestCallback {
        void onResult(boolean success, String message);
    }

    // Private chats callback
    public interface PrivateChatsCallback {
        void onChatsLoaded(List<ChatPreview> privateChats);
        void onError(String errorMessage);
    }

    // Private chat messages callback
    public interface PrivateChatMessagesCallback {
        void onMessagesLoaded(List<Message> messages);
        void onError(String errorMessage);
    }

    // Friend requests callback
    public interface FriendRequestsCallback {
        void onRequestsLoaded(List<FriendRequest> incoming, List<FriendRequest> outgoing);
        void onError(String errorMessage);
    }

    // Request action callback
    public interface RequestActionCallback {
        void onActionComplete(boolean success, String message);
    }

    // Username lookup callback
    public interface UsernameCallback {
        void onResult(String username, boolean exists);
    }

    // Initialize Firebase
    public void initialize() {
        firebaseDatabase = FirebaseDatabase.getInstance("https://mindhavenappjava-default-rtdb.firebaseio.com/");
        messagesRef = firebaseDatabase.getReference("messages");
        privateChatsRef = firebaseDatabase.getReference("private_chats");
    }

    // Send message to Firebase
    public void sendMessage(Message message) {
        String key = messagesRef.push().getKey();
        if (key != null) {
            messagesRef.child(key).setValue(message);
        }
    }

    // Load messages
    public void loadMessages() {
        // No implementation needed as we're using a listener
    }

    // Add message listener
    public void addMessageListener(MessageListener listener) {
        if (messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
        }

        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        String sender = snapshot.child("sender").getValue(String.class);
                        String content = snapshot.child("content").getValue(String.class);
                        Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                        if (sender != null && content != null && timestamp != null) {
                            Message message = new Message(sender, content, timestamp);
                            messages.add(message);
                        }
                    } catch (Exception e) {
                        // Try direct conversion as fallback
                        Message message = snapshot.getValue(Message.class);
                        if (message != null) {
                            messages.add(message);
                        }
                    }
                }

                // Sort messages by timestamp
                Collections.sort(messages, (m1, m2) ->
                        Long.compare(m1.getTimestamp(), m2.getTimestamp()));

                listener.onMessagesUpdated(messages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("Firebase Error: " + databaseError.getMessage());
            }
        };

        messagesRef.addValueEventListener(messagesListener);
    }

    // Cleanup resources
    public void cleanup() {
        if (messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
            messagesListener = null;
        }
    }

    // Search for users by username
    public void searchUsers(String query, String currentUsername, SearchResultsCallback callback) {
        DatabaseReference usersRef = firebaseDatabase.getReference("users");

        // Query for users whose usernames contain the search query
        Query searchQuery = usersRef.orderByKey();

        searchQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> results = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String username = snapshot.getKey();

                    // Skip the current user and only include usernames that match the query
                    if (username != null && !username.equals(currentUsername) &&
                            username.toLowerCase().contains(query.toLowerCase())) {
                        results.add(username);
                    }
                }

                callback.onSearchResults(results);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    // Check if a username is available
    public void checkUsernameAvailability(String username, FriendRequestCallback callback) {
        DatabaseReference usersRef = firebaseDatabase.getReference("users");

        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Username already exists
                    callback.onResult(false, "Username already taken");
                } else {
                    // Username is available
                    callback.onResult(true, "Username is available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onResult(false, "Error checking username: " + databaseError.getMessage());
            }
        });
    }

    // Register a unique username
    public void registerUsername(String username, FriendRequestCallback callback) {
        DatabaseReference usersRef = firebaseDatabase.getReference("users");

        usersRef.child(username).setValue(true)
                .addOnSuccessListener(aVoid -> callback.onResult(true, "Username registered successfully"))
                .addOnFailureListener(e -> callback.onResult(false, "Failed to register username: " + e.getMessage()));
    }

    // Send a friend request
    public void sendFriendRequest(String fromUser, String toUser, FriendRequestCallback callback) {
        // Create unique request ID
        String requestId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();

        // References to both incoming and outgoing requests
        DatabaseReference incomingRef = firebaseDatabase.getReference("friend_requests/incoming/" + toUser + "/" + requestId);
        DatabaseReference outgoingRef = firebaseDatabase.getReference("friend_requests/outgoing/" + fromUser + "/" + requestId);

        // Create request data
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("fromUser", fromUser);
        requestData.put("toUser", toUser);
        requestData.put("timestamp", timestamp);

        // Start transaction to update both locations
        Map<String, Object> updates = new HashMap<>();
        updates.put("friend_requests/incoming/" + toUser + "/" + requestId, requestData);
        updates.put("friend_requests/outgoing/" + fromUser + "/" + requestId, requestData);

        // Execute the transaction
        firebaseDatabase.getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onResult(true, "Friend request sent"))
                .addOnFailureListener(e -> callback.onResult(false, "Failed to send friend request: " + e.getMessage()));
    }

    // Get private chats for a user
    public void getPrivateChats(String username, PrivateChatsCallback callback) {
        DatabaseReference userChatsRef = firebaseDatabase.getReference("private_chats/" + username);

        userChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ChatPreview> privateChatsList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String chatId = snapshot.getKey();
                    String otherUser = snapshot.child("otherUser").getValue(String.class);
                    String lastMessage = snapshot.child("lastMessage").getValue(String.class);
                    Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                    if (chatId != null && otherUser != null && lastMessage != null && timestamp != null) {
                        ChatPreview chatPreview = new ChatPreview(
                                chatId,
                                otherUser,
                                lastMessage,
                                timestamp,
                                false // not anonymous chat
                        );
                        privateChatsList.add(chatPreview);
                    }
                }

                callback.onChatsLoaded(privateChatsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    // Get messages for a private chat
    public void getPrivateChatMessages(String chatId, PrivateChatMessagesCallback callback) {
        DatabaseReference chatMessagesRef = firebaseDatabase.getReference("chat_messages/" + chatId);

        chatMessagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Message> messagesList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String sender = snapshot.child("sender").getValue(String.class);
                    String content = snapshot.child("content").getValue(String.class);
                    Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                    if (sender != null && content != null && timestamp != null) {
                        Message message = new Message(sender, content, timestamp);
                        messagesList.add(message);
                    }
                }

                // Sort messages by timestamp
                Collections.sort(messagesList, new Comparator<Message>() {
                    @Override
                    public int compare(Message m1, Message m2) {
                        return Long.compare(m1.getTimestamp(), m2.getTimestamp());
                    }
                });

                callback.onMessagesLoaded(messagesList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError.getMessage());
            }
        });
    }

    // Send a message in a private chat
    public void sendPrivateMessage(String chatId, String sender, String content) {
        DatabaseReference chatMessagesRef = firebaseDatabase.getReference("chat_messages/" + chatId);
        String messageId = chatMessagesRef.push().getKey();
        long timestamp = System.currentTimeMillis();

        if (messageId != null) {
            // Create message data
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("sender", sender);
            messageData.put("content", content);
            messageData.put("timestamp", timestamp);

            // Add message to chat
            chatMessagesRef.child(messageId).setValue(messageData);

            // Update last message and timestamp in both users' chat previews
            updateChatPreview(chatId, content, timestamp);
        }
    }

    // Overload for sendPrivateMessage with callback
    public void sendPrivateMessage(String chatId, String sender, String content, FriendRequestCallback callback) {
        DatabaseReference chatMessagesRef = firebaseDatabase.getReference("chat_messages/" + chatId);
        String messageId = chatMessagesRef.push().getKey();
        long timestamp = System.currentTimeMillis();

        if (messageId != null) {
            // Create message data
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("sender", sender);
            messageData.put("content", content);
            messageData.put("timestamp", timestamp);

            // Add message to chat
            chatMessagesRef.child(messageId).setValue(messageData)
                    .addOnSuccessListener(aVoid -> {
                        // Update last message and timestamp in both users' chat previews
                        updateChatPreview(chatId, content, timestamp);

                        if (callback != null) {
                            callback.onResult(true, "Message sent successfully");
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) {
                            callback.onResult(false, e.getMessage());
                        }
                    });
        } else {
            if (callback != null) {
                callback.onResult(false, "Failed to generate message ID");
            }
        }
    }

    // Update the chat preview for both users
    private void updateChatPreview(String chatId, String lastMessage, long timestamp) {
        // The chatId is in format "username1_username2" where usernames are in alphabetical order
        String[] usernames = chatId.split("_");

        if (usernames.length == 2) {
            // Update for both users
            DatabaseReference user1ChatRef = firebaseDatabase.getReference("private_chats/" + usernames[0] + "/" + chatId);
            DatabaseReference user2ChatRef = firebaseDatabase.getReference("private_chats/" + usernames[1] + "/" + chatId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("lastMessage", lastMessage);
            updates.put("timestamp", timestamp);

            user1ChatRef.updateChildren(updates);
            user2ChatRef.updateChildren(updates);
        }
    }

    // Get all friend requests
    public void getFriendRequests(String username, FriendRequestsCallback callback) {
        // Firebase reference path for friend requests
        DatabaseReference incomingRef = firebaseDatabase.getReference("friend_requests/incoming/" + username);
        DatabaseReference outgoingRef = firebaseDatabase.getReference("friend_requests/outgoing/" + username);

        List<FriendRequest> incomingRequests = new ArrayList<>();
        List<FriendRequest> outgoingRequests = new ArrayList<>();

        // First get incoming requests
        incomingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get all incoming requests
                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    String requestId = requestSnapshot.getKey();
                    String fromUser = requestSnapshot.child("fromUser").getValue(String.class);
                    String toUser = requestSnapshot.child("toUser").getValue(String.class);
                    Long timestamp = requestSnapshot.child("timestamp").getValue(Long.class);

                    if (requestId != null && fromUser != null && toUser != null && timestamp != null) {
                        FriendRequest request = new FriendRequest(requestId, fromUser, toUser, timestamp);
                        incomingRequests.add(request);
                    }
                }

                // Now get outgoing requests
                outgoingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Get all outgoing requests
                        for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                            String requestId = requestSnapshot.getKey();
                            String fromUser = requestSnapshot.child("fromUser").getValue(String.class);
                            String toUser = requestSnapshot.child("toUser").getValue(String.class);
                            Long timestamp = requestSnapshot.child("timestamp").getValue(Long.class);

                            if (requestId != null && fromUser != null && toUser != null && timestamp != null) {
                                FriendRequest request = new FriendRequest(requestId, fromUser, toUser, timestamp);
                                outgoingRequests.add(request);
                            }
                        }

                        // Return the results
                        callback.onRequestsLoaded(incomingRequests, outgoingRequests);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError("Failed to load outgoing requests: " + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError("Failed to load incoming requests: " + databaseError.getMessage());
            }
        });
    }

    // Accept a friend request
    public void acceptFriendRequest(FriendRequest request, String myUsername, RequestActionCallback callback) {
        // Remove from incoming requests
        DatabaseReference incomingRef = firebaseDatabase.getReference("friend_requests/incoming/" + myUsername + "/" + request.getRequestId());
        DatabaseReference outgoingRef = firebaseDatabase.getReference("friend_requests/outgoing/" + request.getFromUser() + "/" + request.getRequestId());

        // Create unique chat ID (combining both usernames alphabetically)
        String[] usernames = {myUsername, request.getFromUser()};
        Arrays.sort(usernames);
        String chatId = usernames[0] + "_" + usernames[1];

        // Create timestamp
        long timestamp = System.currentTimeMillis();

        // Create friend connection for both users
        DatabaseReference myFriendsRef = firebaseDatabase.getReference("friends/" + myUsername + "/" + request.getFromUser());
        DatabaseReference theirFriendsRef = firebaseDatabase.getReference("friends/" + request.getFromUser() + "/" + myUsername);

        // Create chat entry for both users
        DatabaseReference myChatsRef = firebaseDatabase.getReference("private_chats/" + myUsername + "/" + chatId);
        DatabaseReference theirChatsRef = firebaseDatabase.getReference("private_chats/" + request.getFromUser() + "/" + chatId);

        // Create welcome message in chat
        DatabaseReference chatMessagesRef = firebaseDatabase.getReference("chat_messages/" + chatId);
        String welcomeMessageId = chatMessagesRef.push().getKey();

        // Start transaction to update multiple locations
        Map<String, Object> updates = new HashMap<>();

        // Remove request
        updates.put("friend_requests/incoming/" + myUsername + "/" + request.getRequestId(), null);
        updates.put("friend_requests/outgoing/" + request.getFromUser() + "/" + request.getRequestId(), null);

        // Add as friends
        updates.put("friends/" + myUsername + "/" + request.getFromUser(), true);
        updates.put("friends/" + request.getFromUser() + "/" + myUsername, true);

        // Create chat entry
        updates.put("private_chats/" + myUsername + "/" + chatId + "/otherUser", request.getFromUser());
        updates.put("private_chats/" + myUsername + "/" + chatId + "/lastMessage", "You are now connected!");
        updates.put("private_chats/" + myUsername + "/" + chatId + "/timestamp", timestamp);

        updates.put("private_chats/" + request.getFromUser() + "/" + chatId + "/otherUser", myUsername);
        updates.put("private_chats/" + request.getFromUser() + "/" + chatId + "/lastMessage", "You are now connected!");
        updates.put("private_chats/" + request.getFromUser() + "/" + chatId + "/timestamp", timestamp);

        // Add welcome message
        if (welcomeMessageId != null) {
            updates.put("chat_messages/" + chatId + "/" + welcomeMessageId + "/sender", "system");
            updates.put("chat_messages/" + chatId + "/" + welcomeMessageId + "/content", "You are now connected! Say hello!");
            updates.put("chat_messages/" + chatId + "/" + welcomeMessageId + "/timestamp", timestamp);
        }

        // Execute all updates
        firebaseDatabase.getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onActionComplete(true, "Friend request accepted"))
                .addOnFailureListener(e -> callback.onActionComplete(false, e.getMessage()));
    }

    // Decline a friend request
    public void declineFriendRequest(FriendRequest request, String myUsername, RequestActionCallback callback) {
        // Remove from incoming requests
        DatabaseReference incomingRef = firebaseDatabase.getReference("friend_requests/incoming/" + myUsername + "/" + request.getRequestId());
        DatabaseReference outgoingRef = firebaseDatabase.getReference("friend_requests/outgoing/" + request.getFromUser() + "/" + request.getRequestId());

        // Start transaction to update multiple locations
        Map<String, Object> updates = new HashMap<>();

        // Remove request
        updates.put("friend_requests/incoming/" + myUsername + "/" + request.getRequestId(), null);
        updates.put("friend_requests/outgoing/" + request.getFromUser() + "/" + request.getRequestId(), null);

        // Execute all updates
        firebaseDatabase.getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onActionComplete(true, "Friend request declined"))
                .addOnFailureListener(e -> callback.onActionComplete(false, e.getMessage()));
    }

    // Cancel a friend request
    public void cancelFriendRequest(FriendRequest request, String myUsername, RequestActionCallback callback) {
        // Remove from outgoing requests
        DatabaseReference outgoingRef = firebaseDatabase.getReference("friend_requests/outgoing/" + myUsername + "/" + request.getRequestId());
        DatabaseReference incomingRef = firebaseDatabase.getReference("friend_requests/incoming/" + request.getToUser() + "/" + request.getRequestId());

        // Start transaction to update multiple locations
        Map<String, Object> updates = new HashMap<>();

        // Remove request
        updates.put("friend_requests/outgoing/" + myUsername + "/" + request.getRequestId(), null);
        updates.put("friend_requests/incoming/" + request.getToUser() + "/" + request.getRequestId(), null);

        // Execute all updates
        firebaseDatabase.getReference().updateChildren(updates)
                .addOnSuccessListener(aVoid -> callback.onActionComplete(true, "Friend request canceled"))
                .addOnFailureListener(e -> callback.onActionComplete(false, e.getMessage()));
    }

    /**
     * Maps an anonymous username to a unique permanent username
     * This allows the system to know which unique username belongs to which anonymous user
     */
    public void mapAnonymousToUniqueUsername(String anonymousUsername, String uniqueUsername) {
        if (anonymousUsername == null || uniqueUsername == null) {
            return; // Avoid null references
        }

        // Create mapping in Firebase
        DatabaseReference anonymousToUniqueRef = firebaseDatabase.getReference("username_mappings/anonymous_to_unique/" + anonymousUsername);
        DatabaseReference uniqueToAnonymousRef = firebaseDatabase.getReference("username_mappings/unique_to_anonymous/" + uniqueUsername);

        // Create mapping in both directions
        anonymousToUniqueRef.setValue(uniqueUsername);
        uniqueToAnonymousRef.setValue(anonymousUsername);
    }

    /**
     * Gets the unique username corresponding to an anonymous username
     */
    public void getUniqueFromAnonymous(String anonymousUsername, UsernameCallback callback) {
        if (anonymousUsername == null) {
            callback.onResult(null, false);
            return;
        }

        DatabaseReference ref = firebaseDatabase.getReference("username_mappings/anonymous_to_unique/" + anonymousUsername);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String uniqueUsername = dataSnapshot.getValue(String.class);
                    callback.onResult(uniqueUsername, true);
                } else {
                    callback.onResult(null, false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onResult(null, false);
            }
        });
    }

    /**
     * Gets the anonymous username corresponding to a unique username
     */
    public void getAnonymousFromUnique(String uniqueUsername, UsernameCallback callback) {
        if (uniqueUsername == null) {
            callback.onResult(null, false);
            return;
        }

        DatabaseReference ref = firebaseDatabase.getReference("username_mappings/unique_to_anonymous/" + uniqueUsername);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String anonymousUsername = dataSnapshot.getValue(String.class);
                    callback.onResult(anonymousUsername, true);
                } else {
                    callback.onResult(null, false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onResult(null, false);
            }
        });
    }
}