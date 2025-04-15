package com.example.mindhaven;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.List;

@IgnoreExtraProperties
public class MoodEntry {
    private String userId;
    private String mood; 
    private String note; 
    private List<String> activities; 
    private long timestamp;
    private int moodScore; 
    private String timeOfDay; 

    public MoodEntry() {
    }

    public MoodEntry(String userId, String mood, String note, List<String> activities, 
                    long timestamp, int moodScore, String timeOfDay) {
        this.userId = userId;
        this.mood = mood;
        this.note = note;
        this.activities = activities;
        this.timestamp = timestamp;
        this.moodScore = moodScore;
        this.timeOfDay = timeOfDay;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<String> getActivities() {
        return activities;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(int moodScore) {
        this.moodScore = moodScore;
    }

    public String getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(String timeOfDay) {
        this.timeOfDay = timeOfDay;
    }
} 