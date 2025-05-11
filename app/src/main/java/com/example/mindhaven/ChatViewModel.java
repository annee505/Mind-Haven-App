package com.example.mindhaven;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatViewModel extends ViewModel {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration chatListener;
    private MutableLiveData<List<ChatMessage>> messagesLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private static final String ANONYMOUS_CHAT_ROOM = "anonymous_chat_room";

    public LiveData<List<ChatMessage>> getMessages() {
        return messagesLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return loadingLiveData;
    }

    public void startAnonymousChat() {
        if (chatListener != null) chatListener.remove();
        loadingLiveData.setValue(true);
        db.collection("chats").document(ANONYMOUS_CHAT_ROOM).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                setupAnonymousListener();
            } else {
                db.collection("chats").document(ANONYMOUS_CHAT_ROOM)
                        .set(new HashMap<>())
                        .addOnSuccessListener(aVoid -> setupAnonymousListener());
            }
        });
    }

    private void setupAnonymousListener() {
        Query query = db.collection("chats")
                .document(ANONYMOUS_CHAT_ROOM)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);
        attachListener(query);
    }

    public void startPrivateChat(String currentUserId, String otherUserId) {
        if (chatListener != null) chatListener.remove();
        loadingLiveData.setValue(true);
        String chatRoomId = currentUserId.compareTo(otherUserId) < 0
                ? currentUserId + "_" + otherUserId
                : otherUserId + "_" + currentUserId;
        Query query = db.collection("private_chats")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING);
        attachListener(query);
    }

    private void attachListener(Query query) {
        chatListener = query.addSnapshotListener((snapshots, e) -> {
            loadingLiveData.setValue(false);
            if (e != null || snapshots == null) return;
            List<ChatMessage> list = new ArrayList<>();
            for (DocumentSnapshot doc : snapshots) {
                ChatMessage msg = doc.toObject(ChatMessage.class);
                if (msg != null) {
                    msg.setMessageId(doc.getId());
                    list.add(msg);
                }
            }
            messagesLiveData.setValue(list);
        });
    }

    public void sendMessage(boolean anonymous, String currentUserId, String otherUserId, String text) {
        CollectionReference ref;
        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        data.put("userId", currentUserId);
        data.put("timestamp", System.currentTimeMillis());
        if (anonymous) {
            ref = db.collection("chats").document(ANONYMOUS_CHAT_ROOM).collection("messages");
        } else {
            String chatRoomId = currentUserId.compareTo(otherUserId) < 0
                    ? currentUserId + "_" + otherUserId
                    : otherUserId + "_" + currentUserId;
            ref = db.collection("private_chats").document(chatRoomId).collection("messages");
        }
        ref.add(data);
    }

    @Override
    protected void onCleared() {
        if (chatListener != null) chatListener.remove();
        super.onCleared();
    }
}
