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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText editText;
    private Button sendButton;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private RequestQueue requestQueue;

    private static final String HUGGING_FACE_API = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.3";
    private String apiKey;
    private List<ChatMessage> chatMessages;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatMessages = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(requireContext());
        apiKey = getString(R.string.huggingface_api_key);
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

        chatAdapter = new ChatAdapter(requireContext(), chatMessages);
        recyclerView.setAdapter(chatAdapter);

        loadChatHistory();

        sendButton.setOnClickListener(v -> {
            String userMessage = editText.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                editText.setText("");
                sendMessage(userMessage);
            }
        });

        return view;
    }

    private void loadChatHistory() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (currentUid == null) {
            Log.e("AiFragment", "User not logged in, cannot load chat history.");

            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        db.collection("ai_chats")
                .document(currentUid)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((snapshots, e) -> {
                    progressBar.setVisibility(View.GONE);
                    if (e != null) {
                        Log.e("AiFragment", "Listen failed.", e);
                        return;
                    }
                    if (snapshots == null) {
                        Log.w("AiFragment", "Snapshot listener returned null snapshots.");
                        return;
                    }

                    // Log snapshot content
                    Log.d("AiFragment", "Received " + snapshots.size() + " messages from Firestore");

                    // Create a temporary list to hold the messages
                    List<ChatMessage> updatedMessages = new ArrayList<>();

                    for (var document : snapshots) {
                        try {
                            Log.d("AiFragment", "Processing message: " + document.getId());
                            ChatMessage chatMessage = document.toObject(ChatMessage.class);
                            if (chatMessage != null) {
                                chatMessage.setMessageId(document.getId());

                                // Preserve the existing isCurrentUser value from Firestore
                                boolean originalIsCurrentUser = chatMessage.isCurrentUser();

                                // Check userId to determine sender type
                                String userId = chatMessage.getUserId();
                                if (userId != null) {
                                    if (userId.equals("ai_system") || userId.equals("ai_system_error")) {
                                        // This is an AI message
                                        chatMessage.setUsername("AI Assistant");
                                        chatMessage.setSenderType("AI");
                                        if (originalIsCurrentUser) {
                                            // This is wrong - AI messages should never be from current user
                                            Log.e("AiFragment", "AI message incorrectly marked as current user: " + chatMessage.getMessageId());
                                            chatMessage.isCurrentUser = false;
                                        }
                                    } else if (userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        // This is a user message
                                        chatMessage.setUsername("You");
                                        chatMessage.setSenderType("user");
                                        if (!originalIsCurrentUser) {
                                            // This is wrong - user messages should be from current user
                                            Log.e("AiFragment", "User message incorrectly marked as not current user: " + chatMessage.getMessageId());
                                            chatMessage.isCurrentUser = true;
                                        }
                                    } else {
                                        // Some other sender (should not happen in AI chat)
                                        Log.w("AiFragment", "Unknown sender type: " + userId);
                                        chatMessage.setUsername("Unknown");
                                        chatMessage.setSenderType("unknown");
                                    }
                                }

                                updatedMessages.add(chatMessage);
                                Log.d("AiFragment", "Added message: " + chatMessage.getText() +
                                        " - isCurrentUser: " + chatMessage.isCurrentUser() +
                                        " - sender: " + chatMessage.getUsername());
                            }
                        } catch (Exception ex) {
                            Log.e("AiFragment", "Error mapping chat history document", ex);
                        }
                    }

                    // Replace the entire list instead of individual updates
                    chatMessages.clear();
                    chatMessages.addAll(updatedMessages);

                    // Make sure we're on the UI thread before updating RecyclerView
                    requireActivity().runOnUiThread(() -> {
                        Log.d("AiFragment", "Notifying adapter with " + chatMessages.size() + " messages");
                        chatAdapter.notifyDataSetChanged();
                        if (!chatMessages.isEmpty()) {
                            recyclerView.scrollToPosition(chatMessages.size() - 1);
                        }
                    });
                });
    }

    private void sendMessage(String message) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.e("AiFragment", "User not logged in, cannot send message.");
            return;
        }

        // Create message with explicit user identification
        ChatMessage userMessage = new ChatMessage(
                message,
                System.currentTimeMillis(),
                true, // This message is from the current user
                "user" // Set sender type explicitly
        );

        // Set the actual Firebase UID (this is critical for proper identification)
        userMessage.setUserId(userId);
        userMessage.setUsername("You");
        userMessage.setSenderType("user"); // Ensure sender type is set

        // Log message creation for debugging
        Log.d("AiFragment", "Created user message: " + message +
                " | userId set to: " + userId +
                " | currentUser: " + userMessage.isCurrentUser());

        chatMessages.add(userMessage);
        chatAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(chatMessages.size() - 1);

        // Save to user-specific subcollection
        db.collection("ai_chats")
                .document(userId)
                .collection("messages")
                .add(userMessage)
                .addOnSuccessListener(documentReference -> {
                    userMessage.setMessageId(documentReference.getId()); // Store the Firestore ID
                })
                .addOnFailureListener(e -> {
                    Log.e("AiFragment", "Error saving user message", e);
                });

        // Add a small delay before getting AI response to ensure UI updates correctly
        new android.os.Handler().postDelayed(() -> {
            // Get AI response only once, with a delay for better UI experience
            getAiResponse(message);
        }, 500); // 500ms delay
    }

    private void getAiResponse(String userInput) {
        progressBar.setVisibility(View.VISIBLE);

        try {
            // Add debug logging
            Log.d("AiFragment", "Making AI request for: " + userInput);

            JSONObject requestBody = new JSONObject();
            String prompt = "<s>[INST] <<SYS>>\n" +
                    "You are an empathetic virtual therapist. Support users emotionally with concise, nurturing responses.\n" +
                    "<</SYS>>\n\n" +
                    userInput + " [/INST]";
            requestBody.put("inputs", prompt);

            JSONObject parameters = new JSONObject();
            parameters.put("max_new_tokens", 150);
            parameters.put("temperature", 0.7);
            requestBody.put("parameters", parameters);

            // Log the full request payload for debugging
            Log.d("AiFragment", "AI request payload: " + requestBody.toString());

            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    HUGGING_FACE_API,
                    response -> {
                        Log.d("AiFragment", "AI response received. Length: " + response.length());
                        if (response.length() < 200) {
                            // If response is short enough, log the whole thing
                            Log.d("AiFragment", "AI response content: " + response);
                        } else {
                            // Otherwise just log the beginning
                            Log.d("AiFragment", "AI response content (truncated): " +
                                    response.substring(0, 200) + "...");
                        }
                        handleAiResponse(response, userInput);
                    },
                    error -> {
                        Log.e("AiFragment", "AI request error: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e("AiFragment", "Error status code: " + error.networkResponse.statusCode);
                            Log.e("AiFragment", "Error response data: " +
                                    new String(error.networkResponse.data));
                        }
                        handleAiError(userInput);
                    }
            ) {
                @Override
                public byte[] getBody() {
                    return requestBody.toString().getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + apiKey);
                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    60000, // Timeout 60 seconds
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));
            requestQueue.add(request);
        } catch (Exception e) {
            Log.e("AiFragment", "Request creation error", e);
            handleAiError(userInput);
        }
    }

    private void handleAiResponse(String response, String originalUserInput) {
        Log.d("AiFragment", "Received raw AI response: " + response.substring(0, Math.min(100, response.length())) + "...");

        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);

            // Check for API authentication errors
            if (response.contains("error") && response.contains("authorization")) {
                Log.e("AiFragment", "API Authentication error: " + response);
                handleApiAuthError();
                return;
            }

            String aiText = parseAiResponse(response);
            if (aiText.isEmpty()) {
                Log.e("AiFragment", "Empty AI response after parsing");
                handleAiError(originalUserInput);
                return;
            }

            // Create AI message with explicit AI identification
            ChatMessage aiMessage = new ChatMessage(
                    aiText,
                    System.currentTimeMillis(),
                    false, // This is NOT from the current user (it's from AI)
                    "AI" // Set correct role
            );

            // AI system messages use this special ID that will never match a Firebase UID
            aiMessage.setUserId("ai_system"); // Special ID for AI
            aiMessage.setUsername("AI Assistant");
            aiMessage.setSenderType("AI"); // Explicitly set sender type for AI messages

            // Log message creation for debugging
            Log.d("AiFragment", "Created AI message: " + aiText.substring(0, Math.min(20, aiText.length())) + "..." +
                    " | userId set to: ai_system" +
                    " | currentUser: " + aiMessage.isCurrentUser());

            chatMessages.add(aiMessage);
            chatAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(chatMessages.size() - 1);

            // Save AI message to the specific user's chat history
            String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (currentUid != null) {
                db.collection("ai_chats")
                        .document(currentUid)
                        .collection("messages")
                        .add(aiMessage)
                        .addOnSuccessListener(ref -> aiMessage.setMessageId(ref.getId())) // Store Firestore ID
                        .addOnFailureListener(e -> Log.e("AiFragment", "Error saving AI message", e));
            } else {
                Log.e("AiFragment", "User not logged in, cannot save AI message.");
            }
        });
    }

    private void handleApiAuthError() {
        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            String errorText = "Sorry, I'm having trouble accessing the AI service. Please check your API key in the settings.";
            // Create API auth error message with explicit AI identification
            ChatMessage errorMessage = new ChatMessage(
                    errorText,
                    System.currentTimeMillis(),
                    false,
                    "AI"
            );

            // AI error messages use this special ID that will never match a Firebase UID
            errorMessage.setUserId("ai_system_error");
            errorMessage.setUsername("AI Assistant");
            errorMessage.setSenderType("AI");

            // Log message creation for debugging
            Log.d("AiFragment", "Created API auth error message: " + errorText +
                    " | userId set to: ai_system_error" +
                    " | currentUser: " + errorMessage.isCurrentUser());

            chatMessages.add(errorMessage);
            chatAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(chatMessages.size() - 1);

            // Save to Firestore
            String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (currentUid != null) {
                db.collection("ai_chats")
                        .document(currentUid)
                        .collection("messages")
                        .add(errorMessage)
                        .addOnFailureListener(e -> Log.e("AiFragment", "Error saving auth error message", e));
            }

            // Show an error dialog
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("API Authentication Error")
                    .setMessage("There was a problem authenticating with the AI service. Please check your API key in the settings.")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    private void handleAiError(String originalUserInput) {
        requireActivity().runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            String errorText = "Sorry, I encountered an error. Please try again.";
            // Create error message with explicit AI identification
            ChatMessage errorMessage = new ChatMessage(
                    errorText,
                    System.currentTimeMillis(),
                    false, // This is NOT from the current user (it's from AI)
                    "AI" // Set correct role
            );

            // AI error messages use this special ID that will never match a Firebase UID
            errorMessage.setUserId("ai_system_error");
            errorMessage.setUsername("AI Assistant");
            errorMessage.setSenderType("AI"); // Explicitly set sender type for AI error messages

            // Log message creation for debugging
            Log.d("AiFragment", "Created error message: " + errorText +
                    " | userId set to: ai_system_error" +
                    " | currentUser: " + errorMessage.isCurrentUser());

            chatMessages.add(errorMessage);
            chatAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(chatMessages.size() - 1);

            // Optionally save error message to the user's chat history
            String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (currentUid != null) {
                db.collection("ai_chats")
                        .document(currentUid)
                        .collection("messages")
                        .add(errorMessage)
                        .addOnFailureListener(e -> Log.e("AiFragment", "Error saving error message", e));
            } else {
                Log.e("AiFragment", "User not logged in, cannot save AI error message.");
            }
        });
    }

    private String parseAiResponse(String response) {
        try {
            Log.d("AiFragment", "Parsing AI response: " + response);

            // First try to parse as JSONArray (common format)
            try {
                JSONArray arr = new JSONArray(response);
                if (arr.length() > 0) {
                    String aiText = arr.getJSONObject(0).optString("generated_text", "");
                    String parsedText = aiText.replaceAll("(?s)^.*\\[/INST\\]\\s*", "").replaceAll("^<s>", "").trim();
                    Log.d("AiFragment", "Successfully parsed AI response as JSONArray: " + parsedText);
                    return parsedText;
                }
            } catch (Exception e) {
                Log.d("AiFragment", "Not a JSONArray, trying as JSONObject");
            }

            // If not a JSONArray, try as JSONObject
            try {
                JSONObject obj = new JSONObject(response);
                String parsedText = obj.optString("generated_text", "").trim();
                String cleanedText = parsedText.replaceAll("(?s)^.*\\[/INST\\]\\s*", "").replaceAll("^<s>", "").trim();
                Log.d("AiFragment", "Successfully parsed AI response as JSONObject: " + cleanedText);
                return cleanedText;
            } catch (Exception e) {
                Log.d("AiFragment", "Not a JSONObject either, trying as plain text");
            }

            // If neither, try to extract content directly with regex
            String plainTextParsed = response.replaceAll("(?s)^.*\\[/INST\\]\\s*", "").replaceAll("^<s>", "").trim();
            if (!plainTextParsed.isEmpty()) {
                Log.d("AiFragment", "Extracted response using regex: " + plainTextParsed);
                return plainTextParsed;
            }

            // If all else fails, just return the raw response if it's not too long
            if (response.length() < 500) {
                Log.d("AiFragment", "Using raw response: " + response);
                return response.trim();
            }

            Log.e("AiFragment", "Could not parse AI response in any format");
            return "I apologize, but I'm having trouble understanding. Could you please rephrase that?";
        } catch (Exception e) {
            Log.e("AiFragment", "Error parsing AI response", e);
            return "I apologize, but I'm having trouble understanding. Could you please rephrase that?";
        }
    }
}