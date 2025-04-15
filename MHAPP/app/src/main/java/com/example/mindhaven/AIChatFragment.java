package com.example.mindhaven;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AIChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText messageInput;
    private Button sendButton;
    private ProgressBar progressBar;
    private List<ChatMessage> chatMessages;
    private RequestQueue requestQueue;
    
    // Hugging Face API endpoint
    private static final String HUGGING_FACE_API = "https://api-inference.huggingface.co/models/UKURIKIYEYEZU/Help_chatbot";
    private static final String API_KEY = "YOUR_HUGGING_FACE_API_KEY"; // Replace with your API key

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_chat, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        progressBar = view.findViewById(R.id.progressBar);
        
        chatMessages = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(requireContext());

        setupRecyclerView();
        setupSendButton();
        
        // Add welcome message
        addBotMessage("Hi! I'm here to support you. How are you feeling today?");
        
        return view;
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageInput.setText("");
            }
        });
    }

    private void sendMessage(String messageText) {
        // Add user message to chat
        ChatMessage userMessage = new ChatMessage(
            "user",
            "You",
            messageText,
            System.currentTimeMillis(),
            false
        );
        addMessage(userMessage);
        
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);

        // Prepare request to Hugging Face API
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("inputs", messageText);

            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                HUGGING_FACE_API,
                requestBody,
                response -> {
                    try {
                        String botResponse = response.getString("generated_text");
                        addBotMessage(botResponse);
                    } catch (Exception e) {
                        showError("Couldn't understand the AI response");
                    } finally {
                        progressBar.setVisibility(View.GONE);
                        sendButton.setEnabled(true);
                    }
                },
                error -> {
                    showError("Failed to get AI response");
                    progressBar.setVisibility(View.GONE);
                    sendButton.setEnabled(true);
                }
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Authorization", "Bearer " + API_KEY);
                    return headers;
                }
            };

            requestQueue.add(request);
        } catch (Exception e) {
            showError("Failed to send message");
            progressBar.setVisibility(View.GONE);
            sendButton.setEnabled(true);
        }
    }

    private void addBotMessage(String messageText) {
        ChatMessage botMessage = new ChatMessage(
            "ai_bot",
            "AI Support",
            messageText,
            System.currentTimeMillis(),
            true
        );
        addMessage(botMessage);
    }

    private void addMessage(ChatMessage message) {
        chatMessages.add(message);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(request -> true);
        }
    }
}
