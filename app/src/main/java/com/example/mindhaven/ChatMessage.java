package com.example.mindhaven;

import android.graphics.Color;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ChatMessage {
    private String messageId;
    private String userId;
    private String username;
    private String text;
    private long timestamp;
    boolean isCurrentUser;
    private MessageStatus status;
    private int avatarColor;
    private String avatarInitial;
    private String senderType; // Add this field to track sender type (user/AI)

    public ChatMessage() {}

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
        if (username != null && !username.isEmpty()) {
            this.avatarColor = generateColorFromUserId(username);
            this.avatarInitial = username.substring(0, 1).toUpperCase();
        }
    }

    public Object getSenderId() {
        return userId;
    }

    public enum MessageStatus {
        SENDING, SENT, DELIVERED, READ
    }

    // Constructor for AI Fragment
    public ChatMessage(String text, long timestamp, boolean isCurrentUser) {
        this(text, timestamp, isCurrentUser, null);
    }

    // Constructor for Anonymous Chat
    public ChatMessage(String text, String userId, long timestamp) {
        this.text = text;
        this.userId = userId;
        this.timestamp = timestamp;
        this.avatarColor = generateColorFromUserId(userId);
        this.avatarInitial = "?"; // Anonymous avatar
        this.status = MessageStatus.SENT;
    }

    // Shared constructor
    ChatMessage(String text, long timestamp, boolean isCurrentUser, String senderType) {
        this.text = text;
        this.timestamp = timestamp;
        this.isCurrentUser = isCurrentUser;
        this.senderType = senderType;
        this.status = MessageStatus.SENT;
    }

    private int generateColorFromUserId(String userId) {
        int hash = userId.hashCode();
        return Color.rgb(
                Math.abs(hash % 200) + 55,
                Math.abs((hash >> 8) % 200) + 55,
                Math.abs((hash >> 16) % 200) + 55
        );
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    @Exclude
    public int getAvatarColor() {
        return avatarColor;
    }

    @Exclude
    public String getAvatarInitial() {
        return avatarInitial;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public boolean isAnonymous() {
        return userId != null && userId.startsWith("anon_");
    }
}