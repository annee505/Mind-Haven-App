package com.example.mindhaven;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Model class for recommendation items with Firestore integration
 */
public class Recommendation {
    private String id;
    private String title;
    private String description;
    private String mood;
    private String type; // book, movie, or music
    private boolean isFavorite;
    private Date dateAdded;
    private String userId;

    // Empty constructor for Firestore
    public Recommendation() {
    }

    public Recommendation(String title, String description, String mood, String type) {
        this.title = title;
        this.description = description;
        this.mood = mood;
        this.type = type;
        this.isFavorite = false;
        this.dateAdded = new Date();
    }

    // Constructor with all fields for Firestore
    public Recommendation(String id, String title, String description,
                          String mood, String type, boolean isFavorite,
                          Date dateAdded, String userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.mood = mood;
        this.type = type;
        this.isFavorite = isFavorite;
        this.dateAdded = dateAdded;
        this.userId = userId;
    }

    // Convert to Firestore document
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("description", description);
        map.put("mood", mood);
        map.put("type", type);
        map.put("isFavorite", isFavorite);
        map.put("dateAdded", dateAdded);
        map.put("userId", userId);
        return map;
    }

    // Getters and setters
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

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
