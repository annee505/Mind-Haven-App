package com.example.mindhaven;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestsActivity extends AppCompatActivity {
    private RecyclerView incomingRecycler;
    private RecyclerView outgoingRecycler;
    private TextView noIncoming;
    private TextView noOutgoing;
    private TextView statusText;
    private ProgressBar loadingProgress;

    private FriendRequestAdapter incomingAdapter;
    private FriendRequestAdapter outgoingAdapter;

    private List<FriendRequest> incomingRequests = new ArrayList<>();
    private List<FriendRequest> outgoingRequests = new ArrayList<>();

    private FirebaseService firebaseService;
    private MindHavenApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        // Set title
        setTitle("Friend Requests");

        // Get application instance
        app = (MindHavenApplication) getApplicationContext();

        // Initialize Firebase
        firebaseService = new FirebaseService();
        firebaseService.initialize();

        // Initialize views
        incomingRecycler = findViewById(R.id.incoming_recycler);
        outgoingRecycler = findViewById(R.id.outgoing_recycler);
        noIncoming = findViewById(R.id.no_incoming);
        noOutgoing = findViewById(R.id.no_outgoing);
        statusText = findViewById(R.id.status_text);
        loadingProgress = findViewById(R.id.loading_progress);

        // Set up RecyclerViews
        setupRecyclerViews();

        // Check if user has registered unique username
        if (!app.hasUniqueUsername()) {
            showNoUsernameMessage();
            return;
        }

        // Load friend requests
        loadFriendRequests();
    }

    private void setupRecyclerViews() {
        // Incoming requests
        LinearLayoutManager incomingLayoutManager = new LinearLayoutManager(this);
        incomingRecycler.setLayoutManager(incomingLayoutManager);
        incomingAdapter = new FriendRequestAdapter(incomingRequests, true, this);
        incomingRecycler.setAdapter(incomingAdapter);

        // Outgoing requests
        LinearLayoutManager outgoingLayoutManager = new LinearLayoutManager(this);
        outgoingRecycler.setLayoutManager(outgoingLayoutManager);
        outgoingAdapter = new FriendRequestAdapter(outgoingRequests, false, this);
        outgoingRecycler.setAdapter(outgoingAdapter);

        // Set up action listeners
        incomingAdapter.setOnRequestActionListener(new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest request) {
                acceptFriendRequest(request);
            }

            @Override
            public void onDecline(FriendRequest request) {
                declineFriendRequest(request);
            }
        });

        outgoingAdapter.setOnRequestActionListener(new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest request) {
                // Not applicable for outgoing requests
            }

            @Override
            public void onDecline(FriendRequest request) {
                cancelFriendRequest(request);
            }
        });
    }

    private void showNoUsernameMessage() {
        loadingProgress.setVisibility(View.GONE);
        noIncoming.setText("You need to register a unique username\nto use friend requests");
        noIncoming.setVisibility(View.VISIBLE);
        noOutgoing.setVisibility(View.GONE);
        statusText.setText("Register a unique username in Settings");
    }

    private void loadFriendRequests() {
        // Show loading
        loadingProgress.setVisibility(View.VISIBLE);
        noIncoming.setVisibility(View.GONE);
        noOutgoing.setVisibility(View.GONE);

        String myUsername = app.getUniqueUsername();

        // Load requests from Firebase
        firebaseService.getFriendRequests(myUsername, new FirebaseService.FriendRequestsCallback() {
            @Override
            public void onRequestsLoaded(List<FriendRequest> incoming, List<FriendRequest> outgoing) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    // Update incoming requests
                    incomingRequests.clear();
                    incomingRequests.addAll(incoming);
                    incomingAdapter.notifyDataSetChanged();

                    if (incoming.isEmpty()) {
                        noIncoming.setVisibility(View.VISIBLE);
                    } else {
                        noIncoming.setVisibility(View.GONE);
                    }

                    // Update outgoing requests
                    outgoingRequests.clear();
                    outgoingRequests.addAll(outgoing);
                    outgoingAdapter.notifyDataSetChanged();

                    if (outgoing.isEmpty()) {
                        noOutgoing.setVisibility(View.VISIBLE);
                    } else {
                        noOutgoing.setVisibility(View.GONE);
                    }

                    // Update status
                    if (incoming.isEmpty() && outgoing.isEmpty()) {
                        statusText.setText("Use the Find Friends screen to send new friend requests");
                    } else {
                        statusText.setText("");
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(FriendRequestsActivity.this,
                            "Failed to load requests: " + errorMessage, Toast.LENGTH_SHORT).show();

                    noIncoming.setText("Failed to load incoming requests");
                    noIncoming.setVisibility(View.VISIBLE);

                    noOutgoing.setText("Failed to load outgoing requests");
                    noOutgoing.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void acceptFriendRequest(FriendRequest request) {
        // Show loading
        loadingProgress.setVisibility(View.VISIBLE);

        String myUsername = app.getUniqueUsername();

        firebaseService.acceptFriendRequest(request, myUsername, new FirebaseService.RequestActionCallback() {
            @Override
            public void onActionComplete(boolean success, String message) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (success) {
                        Toast.makeText(FriendRequestsActivity.this,
                                "Friend request accepted", Toast.LENGTH_SHORT).show();
                        // Reload friend requests
                        loadFriendRequests();
                    } else {
                        Toast.makeText(FriendRequestsActivity.this,
                                "Failed to accept request: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void declineFriendRequest(FriendRequest request) {
        // Show loading
        loadingProgress.setVisibility(View.VISIBLE);

        String myUsername = app.getUniqueUsername();

        firebaseService.declineFriendRequest(request, myUsername, new FirebaseService.RequestActionCallback() {
            @Override
            public void onActionComplete(boolean success, String message) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (success) {
                        Toast.makeText(FriendRequestsActivity.this,
                                "Friend request declined", Toast.LENGTH_SHORT).show();
                        // Reload friend requests
                        loadFriendRequests();
                    } else {
                        Toast.makeText(FriendRequestsActivity.this,
                                "Failed to decline request: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void cancelFriendRequest(FriendRequest request) {
        // Show loading
        loadingProgress.setVisibility(View.VISIBLE);

        String myUsername = app.getUniqueUsername();

        firebaseService.cancelFriendRequest(request, myUsername, new FirebaseService.RequestActionCallback() {
            @Override
            public void onActionComplete(boolean success, String message) {
                runOnUiThread(() -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (success) {
                        Toast.makeText(FriendRequestsActivity.this,
                                "Friend request canceled", Toast.LENGTH_SHORT).show();
                        // Reload friend requests
                        loadFriendRequests();
                    } else {
                        Toast.makeText(FriendRequestsActivity.this,
                                "Failed to cancel request: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload friend requests when returning to this screen
        if (app.hasUniqueUsername()) {
            loadFriendRequests();
        }
    }
}