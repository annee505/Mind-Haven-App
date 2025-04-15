package com.example.mindhaven;

public class MoodHistoryItem {
    private final String mood;
    private final long timestamp;

    public MoodHistoryItem(String mood, long timestamp) {
        this.mood = mood;
        this.timestamp = timestamp;
    }

    public String getMood() {
        return mood;
    }

    public long getTimestamp() {
        return timestamp;
    }
}