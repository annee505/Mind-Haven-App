package com.example.mindhaven;

import androidx.annotation.Keep;

@Keep
public class MeditationSession {
    private String id;
    private String title;
    private String description;
    private String duration;
    private String category;
    private String audioUrl;
    private String imageUrl;
    
    // Required for Firebase
    public MeditationSession() {
    }
    
    public MeditationSession(String id, String title, String description, String duration, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.category = category;
        this.audioUrl = ""; // Default empty URL
        this.imageUrl = ""; // Default empty URL
    }
    
    public MeditationSession(String id, String title, String description, String duration, String category, String audioUrl, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.category = category;
        this.audioUrl = audioUrl;
        this.imageUrl = imageUrl;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getAudioUrl() {
        return audioUrl;
    }
    
    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}