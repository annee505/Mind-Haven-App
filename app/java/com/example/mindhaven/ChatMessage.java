Reinitialized existing Git repository in C:/Users/Dell/Mind Haven app/.g
it/
PS C:\Users\Dell\Mind Haven app>package com.example.mindhaven;

public class ChatMessage {
    private String messageId;
    private String userId;
    private String username;
    private String text;
    private long timestamp;
    private boolean isCurrentUser;
    private boolean showUsername;

    public ChatMessage() {}

    public ChatMessage(String messageId, String userId, String username, String text, long timestamp, boolean isCurrentUser) {
        this.messageId = messageId;
        this.userId = userId;
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
        this.isCurrentUser = isCurrentUser;
        this.showUsername = true; // Default to showing username
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        isCurrentUser = currentUser;
    }
    
    public boolean isShowUsername() {
        return showUsername;
    }
    
    public void setShowUsername(boolean showUsername) {
        this.showUsername = showUsername;
    }
}
