package com.example.mindhaven;

public class MoodReport {
    private String text;
    private long timestamp;
    private String userId;

    public MoodReport() {
        // Required empty constructor for Firestore
    }

    public MoodReport(String text, long timestamp, String userId) {
        this.text = text;
        this.timestamp = timestamp;
        this.userId = userId;
    }
    
    public MoodReport(String text, long timestamp) {
        this.text = text;
        this.timestamp = timestamp;
        this.userId = null;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
} 