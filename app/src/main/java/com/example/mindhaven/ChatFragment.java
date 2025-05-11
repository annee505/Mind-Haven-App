package com.example.mindhaven;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindhaven.FirebaseService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatFragment extends Fragment {

    private String anonymousUsername;
    private FirebaseService firebaseService;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private EditText messageInput;
    private Button sendButton;
    private TextView usernameText;
    private List<Message> messageList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Initialize UI components
        messagesRecyclerView = view.findViewById(R.id.messages_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        usernameText = view.findViewById(R.id.username_label);

        // Generate random username
        generateRandomUsername();
        usernameText.setText("Your username: " + anonymousUsername);

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        messagesRecyclerView.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messageList, getContext(), anonymousUsername);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Initialize Firebase
        firebaseService = new FirebaseService();
        firebaseService.initialize();

        // Set up send button
        sendButton.setOnClickListener(v -> sendMessage());

        // Set up message listener (receives updates from Firebase)
        firebaseService.addMessageListener(messages -> {
            getActivity().runOnUiThread(() -> {
                messageList.clear();
                messageList.addAll(messages);
                messageAdapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    messagesRecyclerView.scrollToPosition(messageList.size() - 1);
                }
            });
        });

        // Load existing messages
        firebaseService.loadMessages();

        return view;
    }

    private void generateRandomUsername() {
        // Generate a random username in format user_XXXXX
        String randomSuffix = UUID.randomUUID().toString().substring(0, 5);
        anonymousUsername = "user_" + randomSuffix;
    }

    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (!content.isEmpty()) {
            long timestamp = System.currentTimeMillis();
            Message message = new Message(anonymousUsername, content, timestamp);
            firebaseService.sendMessage(message);
            messageInput.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firebaseService != null) {
            firebaseService.cleanup();
        }
    }
}