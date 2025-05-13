
package com.example.mindhaven.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "meditation_audios")
public class MeditationAudio {
    @PrimaryKey
    private int id;

    @NonNull
    private String title;

    private String description;

    @NonNull
    private String duration;

    private String audioUrl;

    private int audioResourceId;

    private String category;

    private boolean isResource = false;

    private boolean isFavorite = false;

    @androidx.room.Ignore
    public MeditationAudio(String title, String description, String duration,
                           String audioUrl, String category, boolean isFavorite) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.audioUrl = audioUrl;
        this.category = category;
        this.isFavorite = isFavorite;
        this.isResource = false;
    }

    public MeditationAudio(String title, String description, String duration,
                           int audioResourceId, String category, boolean isFavorite) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.audioResourceId = audioResourceId;
        this.category = category;
        this.isFavorite = isFavorite;
        this.isResource = true;
    }

    public MeditationAudio(String title, String description, String duration, int resourceId, String category, boolean b, boolean b1) {
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.audioResourceId = resourceId;
        this.category = category;
        this.isResource = b;
        this.isFavorite = b1;
    }

// Constructor for the MeditationAudio class

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getTitle() { return title; }
    public void setTitle(@NonNull String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @NonNull
    public String getDuration() { return duration; }
    public void setDuration(@NonNull String duration) { this.duration = duration; }

    public String getAudioUrl() {
        if (isResource) {
            return "android.resource://com.example.mindhaven/raw/audio_" + audioResourceId;
        }
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
        this.isResource = false;
    }

    public int getAudioResourceId() { return audioResourceId; }
    public void setAudioResourceId(int audioResourceId) {
        this.audioResourceId = audioResourceId;
        this.isResource = true;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isResource() { return isResource; }
    public void setResource(boolean resource) { isResource = resource; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public long getDurationInMs() {
        String[] parts = duration.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return (minutes * 60 + seconds) * 1000L;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MeditationAudio that = (MeditationAudio) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "MeditationAudio{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
