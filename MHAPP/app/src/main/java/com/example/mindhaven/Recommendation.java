package com.example.mindhaven;

/**
 * Model class for recommendation items
 */
public class Recommendation {
    private String title;
    private String description;
    private String mood;
    private String type; // book, movie, or music

    public Recommendation(String title, String description, String mood, String type) {
        this.title = title;
        this.description = description;
        this.mood = mood;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getMood() {
        return mood;
    }

    public String getType() {
        return type;
    }
} 