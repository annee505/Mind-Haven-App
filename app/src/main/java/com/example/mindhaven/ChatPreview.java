package com.example.mindhaven;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatPreview {
    private String chatId;
    private String otherUser;
    private String lastMessage;
    private long timestamp;
    private boolean isAnonymousChat;

    public ChatPreview() {
        // Default constructor for Firebase
    }

    public ChatPreview(String chatId, String otherUser, String lastMessage, long timestamp, boolean isAnonymousChat) {
        this.chatId = chatId;
        this.otherUser = otherUser;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.isAnonymousChat = isAnonymousChat;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(String otherUser) {
        this.otherUser = otherUser;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAnonymousChat() {
        return isAnonymousChat;
    }

    public void setAnonymousChat(boolean anonymousChat) {
        isAnonymousChat = anonymousChat;
    }

    // Helper methods
    public String getName() {
        return isAnonymousChat ? "Anonymous Chat" : otherUser;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}