package com.example.mindhaven;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText editText;
    private Button sendButton;
    private ProgressBar progressBar;
    private List<ChatMessage> chatMessages;
    private FirebaseFirestore db;
    private ApiService apiService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatMessages = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        RetrofitClient.init(requireContext());
        apiService = RetrofitClient.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        editText = view.findViewById(R.id.editText);
        sendButton = view.findViewById(R.id.sendButton);
        progressBar = view.findViewById(R.id.progressBar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setAdapter(chatAdapter);

        loadChatHistory();

        sendButton.setOnClickListener(v -> {
            String userMessage = editText.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                sendButton.setEnabled(false);
                editText.setText("");
                sendMessage(userMessage);
            }
        });

        return view;
    }

    private void loadChatHistory() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        db.collection("chat_messages")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (queryDocumentSnapshots != null) {
                        chatMessages.clear();
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            ChatMessage chatMessage = documentSnapshot.toObject(ChatMessage.class);
                            if (chatMessage != null) {
                                chatMessage.setMessageId(documentSnapshot.getId());
                                chatMessages.add(chatMessage);
                            }
                        }
                        chatAdapter.notifyDataSetChanged();
                        if (!chatMessages.isEmpty()) {
                            recyclerView.scrollToPosition(chatMessages.size() - 1);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.e("AiFragment", "Error loading chat history: " + e.getMessage());
                });
    }

    private void sendMessage(String message) {
        ChatMessage userMessage = new ChatMessage(
            String.valueOf(System.currentTimeMillis()),
            "user",
            "You",
            message,
            System.currentTimeMillis(),
            true
        );
        chatMessages.add(userMessage);
        chatAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(chatMessages.size() - 1);

        db.collection("chat_messages")
            .add(userMessage)
            .addOnSuccessListener(documentReference -> {
                userMessage.setMessageId(documentReference.getId());
                getAiResponse(message);
            })
            .addOnFailureListener(e -> {
                Log.e("AiFragment", "Error saving user message: " + e.getMessage());
                getAiResponse(message);
            });
    }

    private void getAiResponse(String userInput) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputs", userInput);

        apiService.getAIResponse(requestBody).enqueue(new Callback<AIResponse>() {
            @Override
            public void onResponse(Call<AIResponse> call, Response<AIResponse> response) {
                if (getContext() == null) return;

                String aiResponse;
                if (response.isSuccessful() && response.body() != null) {
                    aiResponse = response.body().getGeneratedText();
                } else {
                    aiResponse = generateLocalResponse(userInput);
                    Log.e("AiFragment", "Error from API: " + response.message());
                }

                ChatMessage aiMessage = new ChatMessage(
                    String.valueOf(System.currentTimeMillis()),
                    "ai",
                    "AI Assistant",
                    aiResponse,
                    System.currentTimeMillis(),
                    false
                );

                chatMessages.add(aiMessage);
                chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(chatMessages.size() - 1);

                db.collection("chat_messages")
                    .add(aiMessage)
                    .addOnSuccessListener(documentReference -> {
                        aiMessage.setMessageId(documentReference.getId());
                    })
                    .addOnFailureListener(e -> 
                        Log.e("AiFragment", "Error saving AI message: " + e.getMessage())
                    );

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                sendButton.setEnabled(true);
            }

            @Override
            public void onFailure(Call<AIResponse> call, Throwable t) {
                if (getContext() == null) return;

                String aiResponse = generateLocalResponse(userInput);
                Log.e("AiFragment", "Network error: " + t.getMessage());

                ChatMessage aiMessage = new ChatMessage(
                    String.valueOf(System.currentTimeMillis()),
                    "ai",
                    "AI Assistant",
                    aiResponse,
                    System.currentTimeMillis(),
                    false
                );

                chatMessages.add(aiMessage);
                chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(chatMessages.size() - 1);

                db.collection("chat_messages")
                    .add(aiMessage)
                    .addOnSuccessListener(documentReference -> {
                        aiMessage.setMessageId(documentReference.getId());
                    })
                    .addOnFailureListener(e -> 
                        Log.e("AiFragment", "Error saving AI message: " + e.getMessage())
                    );

                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                sendButton.setEnabled(true);
            }
        });
    }

    private String generateLocalResponse(String userInput) {
        String input = userInput.toLowerCase();
        
        if (input.contains("anxious") || input.contains("anxiety")) {
            return "I understand you're feeling anxious. Remember to take deep breaths - try breathing in for 4 counts, holding for 4, and exhaling for 4. This can help calm your nervous system. Would you like to talk more about what's causing your anxiety?";
        } else if (input.contains("depress") || input.contains("sad") || input.contains("down")) {
            return "I hear that you're feeling down. That must be difficult. Remember that it's okay to not be okay. Have you tried talking to someone you trust about how you're feeling? Professional help is also available if you need it.";
        } else if (input.contains("stress")) {
            return "Stress can be overwhelming. Let's break this down: What's the main thing causing you stress right now? Sometimes, writing down our worries or taking a short walk can help clear our mind.";
        } else if (input.contains("sleep") || input.contains("tired")) {
            return "Sleep is crucial for mental health. Try establishing a calming bedtime routine - maybe reading, gentle stretching, or meditation. Avoid screens an hour before bed if possible. Would you like more sleep hygiene tips?";
        } else if (input.contains("hello") || input.contains("hi ") || input.equals("hi")) {
            return "Hello! I'm here to support you. How are you feeling today?";
        } else if (input.contains("help")) {
            return "I'm here to help and support you. You can talk to me about your feelings, stress, anxiety, or anything else on your mind. What would you like to discuss?";
        } else {
            return "I'm here to listen and support you. Can you tell me more about how you're feeling? Remember, while I can offer support, I'm not a substitute for professional mental health care.";
        }
    }
}
