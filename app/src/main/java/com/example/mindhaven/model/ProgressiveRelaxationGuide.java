
package com.example.mindhaven.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "relaxation_guides")
public class ProgressiveRelaxationGuide {
    @PrimaryKey
    private int id;
    private String title;
    private String description;
    private String videoUrl;
    private boolean isFavorite;
    private int durationMinutes;

    public ProgressiveRelaxationGuide(int id, String title, String description, String videoUrl, int durationMinutes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.durationMinutes = durationMinutes;
        this.isFavorite = false;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
}
