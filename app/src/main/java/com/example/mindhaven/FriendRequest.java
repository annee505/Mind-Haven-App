package com.example.mindhaven;

public class FriendRequest {
    private String requestId;
    private String fromUser;
    private String toUser;
    private long timestamp;

    // Default constructor for Firebase
    public FriendRequest() {
    }

    public FriendRequest(String requestId, String fromUser, String toUser, long timestamp) {
        this.requestId = requestId;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}