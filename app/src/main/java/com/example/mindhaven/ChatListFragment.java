package com.example.mindhaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {
    private EditText searchInput;
    private Button searchButton;
    private ProgressBar loadingProgress;
    private TextView friendRequestsTitle;
    private RecyclerView friendRequestsRecyclerView;
    private TextView noRequestsText;
    private TextView chatsTitle;
    private RecyclerView chatsRecyclerView;
    private TextView noChatsText;

    private FirebaseService firebaseService;
    private MindHavenApplication app;

    private FriendRequestAdapter requestAdapter;
    private ChatListAdapter chatAdapter;

    private List<FriendRequest> incomingRequests = new ArrayList<>();
    private List<FriendRequest> outgoingRequests = new ArrayList<>();
    private List<ChatPreview> chatsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        // Get application instance
        app = (MindHavenApplication) requireActivity().getApplicationContext();

        // Initialize Firebase
        firebaseService = new FirebaseService();
        firebaseService.initialize();

        // Initialize views
        searchInput = view.findViewById(R.id.search_input);
        searchButton = view.findViewById(R.id.search_button);
        loadingProgress = view.findViewById(R.id.loading_progress);
        friendRequestsTitle = view.findViewById(R.id.friend_requests_title);
        friendRequestsRecyclerView = view.findViewById(R.id.friend_requests_recycler_view);
        noRequestsText = view.findViewById(R.id.no_requests_text);
        chatsTitle = view.findViewById(R.id.chats_title);
        chatsRecyclerView = view.findViewById(R.id.chats_recycler_view);
        noChatsText = view.findViewById(R.id.no_chats_text);

        // Set up search button
        searchButton.setOnClickListener(v -> performSearch());

        // Style the search button to be brown
        searchButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));

        // Set up RecyclerViews
        setupFriendRequestsRecyclerView();
        setupChatsRecyclerView();

        // Initially hide friend requests section (will be shown if there are requests)
        friendRequestsTitle.setVisibility(View.GONE);
        friendRequestsRecyclerView.setVisibility(View.GONE);
        noRequestsText.setVisibility(View.GONE);

        // Load friend requests and chats
        loadData();

        return view;
    }

    private void setupFriendRequestsRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        friendRequestsRecyclerView.setLayoutManager(layoutManager);

        // Create adapter
        requestAdapter = new FriendRequestAdapter(incomingRequests, outgoingRequests, getContext());

        // Set listener for actions
        requestAdapter.setActionListener(new FriendRequestAdapter.RequestActionListener() {
            @Override
            public void onAccept(FriendRequest request) {
                acceptFriendRequest(request);
            }

            @Override
            public void onDecline(FriendRequest request) {
                declineFriendRequest(request);
            }

            @Override
            public void onCancel(FriendRequest request) {
                cancelFriendRequest(request);
            }
        });

        friendRequestsRecyclerView.setAdapter(requestAdapter);
    }

    private void acceptFriendRequest(FriendRequest request) {
        // Show loading indicator
        loadingProgress.setVisibility(View.VISIBLE);

        String myUsername = app.getUniqueUsername();

        firebaseService.acceptFriendRequest(request, myUsername, new FirebaseService.RequestActionCallback() {
            @Override
            public void onActionComplete(boolean success, String message) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (success) {
                        Toast.makeText(getContext(), "Friend request accepted", Toast.LENGTH_SHORT).show();
                        loadData(); // Reload data
                    } else {
                        Toast.makeText(getContext(), "Failed to accept request: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void declineFriendRequest(FriendRequest request) {
        // Show loading indicator
        loadingProgress.setVisibility(View.VISIBLE);

        String myUsername = app.getUniqueUsername();

        firebaseService.declineFriendRequest(request, myUsername, new FirebaseService.RequestActionCallback() {
            @Override
            public void onActionComplete(boolean success, String message) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (success) {
                        Toast.makeText(getContext(), "Friend request declined", Toast.LENGTH_SHORT).show();
                        loadData(); // Reload data
                    } else {
                        Toast.makeText(getContext(), "Failed to decline request: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void cancelFriendRequest(FriendRequest request) {
        // Show loading indicator
        loadingProgress.setVisibility(View.VISIBLE);

        String myUsername = app.getUniqueUsername();

        firebaseService.cancelFriendRequest(request, myUsername, new FirebaseService.RequestActionCallback() {
            @Override
            public void onActionComplete(boolean success, String message) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (success) {
                        Toast.makeText(getContext(), "Friend request canceled", Toast.LENGTH_SHORT).show();
                        loadData(); // Reload data
                    } else {
                        Toast.makeText(getContext(), "Failed to cancel request: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setupChatsRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatsRecyclerView.setLayoutManager(layoutManager);

        // Create adapter
        chatAdapter = new ChatListAdapter(chatsList, getContext());

        // Set listener for chat click
        chatAdapter.setOnChatClickListener(chat -> {
            openChat(chat);
        });

        chatsRecyclerView.setAdapter(chatAdapter);
    }

    private void openChat(ChatPreview chat) {
        if (chat.isAnonymousChat()) {
            // Open the anonymous chat
            Intent intent = new Intent(getActivity(), AnonymousChat.class);
            startActivity(intent);
        } else {
            // Open a private chat
            Intent intent = new Intent(getActivity(), PrivateChatActivity.class);
            intent.putExtra("CHAT_ID", chat.getChatId());
            intent.putExtra("OTHER_USER", chat.getOtherUser());
            startActivity(intent);
        }
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a username to search", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        loadingProgress.setVisibility(View.VISIBLE);

        // Get current username
        String currentUsername = app.getUniqueUsername();

        // Check if user has a unique username
        if (currentUsername == null || currentUsername.isEmpty()) {
            showUsernameRegistrationDialog();
            loadingProgress.setVisibility(View.GONE);
            return;
        }

        // Search for users
        firebaseService.searchUsers(query, currentUsername, new FirebaseService.SearchResultsCallback() {
            @Override
            public void onSearchResults(List<String> results) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (results.isEmpty()) {
                        Toast.makeText(getContext(), "No users found matching '" + query + "'", Toast.LENGTH_SHORT).show();
                    } else {
                        // Show the search results dialog
                        showSearchResultsDialog(results);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error searching: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showSearchResultsDialog(List<String> results) {
        SearchResultsDialog dialog = new SearchResultsDialog(getContext(), results);
        dialog.setOnFriendRequestClickListener(username -> sendFriendRequest(username));
        dialog.show();
    }

    private void sendFriendRequest(String toUsername) {
        // Show loading
        loadingProgress.setVisibility(View.VISIBLE);

        // Get current username
        String fromUsername = app.getUniqueUsername();

        // Send friend request
        firebaseService.sendFriendRequest(fromUsername, toUsername, new FirebaseService.FriendRequestCallback() {
            @Override
            public void onResult(boolean success, String message) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (success) {
                        Toast.makeText(getContext(), "Friend request sent to " + toUsername, Toast.LENGTH_SHORT).show();
                        // Reload data to show the outgoing request
                        loadData();
                    } else {
                        Toast.makeText(getContext(), "Failed to send request: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showUsernameRegistrationDialog() {
        UsernameRegistrationDialog dialog = new UsernameRegistrationDialog(getContext(), app, firebaseService);
        dialog.setOnUsernameRegisteredListener(() -> {
            // Reload data after username registration
            loadData();
        });
        dialog.show();
    }

    private void loadData() {
        // Check if user has a unique username
        if (!app.hasUniqueUsername()) {
            // Hide friend requests and chats sections
            friendRequestsTitle.setVisibility(View.GONE);
            friendRequestsRecyclerView.setVisibility(View.GONE);
            noRequestsText.setVisibility(View.GONE);

            chatsTitle.setVisibility(View.GONE);
            chatsRecyclerView.setVisibility(View.GONE);
            noChatsText.setVisibility(View.VISIBLE);
            noChatsText.setText("Register a unique username to see your chats and friend requests");

            return;
        }

        // Show loading
        loadingProgress.setVisibility(View.VISIBLE);

        // Get username
        String username = app.getUniqueUsername();

        // Load friend requests
        firebaseService.getFriendRequests(username, new FirebaseService.FriendRequestsCallback() {
            @Override
            public void onRequestsLoaded(List<FriendRequest> incoming, List<FriendRequest> outgoing) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    // Update friend requests data
                    incomingRequests.clear();
                    incomingRequests.addAll(incoming);

                    outgoingRequests.clear();
                    outgoingRequests.addAll(outgoing);

                    // Notify adapter
                    requestAdapter.notifyDataSetChanged();

                    // Show/hide friend requests section
                    boolean hasRequests = !incoming.isEmpty() || !outgoing.isEmpty();
                    friendRequestsTitle.setVisibility(hasRequests ? View.VISIBLE : View.GONE);
                    friendRequestsRecyclerView.setVisibility(hasRequests ? View.VISIBLE : View.GONE);
                    noRequestsText.setVisibility(hasRequests ? View.GONE : View.VISIBLE);

                    // Load chats after requests are loaded
                    loadChats(username);
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error loading friend requests: " + errorMessage, Toast.LENGTH_SHORT).show();

                    // Load chats even if requests failed
                    loadChats(username);
                });
            }
        });
    }

    private void loadChats(String username) {
        firebaseService.getPrivateChats(username, new FirebaseService.PrivateChatsCallback() {
            @Override
            public void onChatsLoaded(List<ChatPreview> chats) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    // Update chats data
                    chatsList.clear();

                    // Add anonymous chat preview at the top
                    ChatPreview anonymousChatPreview = new ChatPreview(
                            "anonymous_chat",
                            "Anonymous Chat",
                            "Join the public anonymous chat",
                            System.currentTimeMillis(),
                            true
                    );
                    chatsList.add(anonymousChatPreview);

                    // Add private chats
                    if (chats != null && !chats.isEmpty()) {
                        chatsList.addAll(chats);
                    }

                    // Notify adapter
                    chatAdapter.notifyDataSetChanged();

                    // Show/hide chats section
                    chatsTitle.setVisibility(View.VISIBLE);
                    chatsRecyclerView.setVisibility(View.VISIBLE);
                    noChatsText.setVisibility(chatsList.size() <= 1 ? View.VISIBLE : View.GONE);

                    if (chatsList.size() <= 1) {
                        noChatsText.setText("No private chats yet. Search for users to add friends.");
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading chats: " + errorMessage, Toast.LENGTH_SHORT).show();

                    // Even if there's an error, still show the anonymous chat
                    chatsList.clear();
                    ChatPreview anonymousChatPreview = new ChatPreview(
                            "anonymous_chat",
                            "Anonymous Chat",
                            "Join the public anonymous chat",
                            System.currentTimeMillis(),
                            true
                    );
                    chatsList.add(anonymousChatPreview);
                    chatAdapter.notifyDataSetChanged();

                    chatsTitle.setVisibility(View.VISIBLE);
                    chatsRecyclerView.setVisibility(View.VISIBLE);
                    noChatsText.setVisibility(View.GONE);
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data when returning to this fragment
        loadData();
    }
}