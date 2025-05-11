package com.example.mindhaven.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity class for meditation audio
 */
@Entity(tableName = "meditation_audio")
public class MeditationAudio {
    @PrimaryKey
    private int id;

    @NonNull
    private String title;

    private String description;

    @NonNull
    private String duration;

    private int audioResourceId;

    private String category;

    private boolean isResource = true;

    private boolean isFavorite = false;

    public MeditationAudio(String title, String description, String duration,
                           int audioResourceId, String category,
                           boolean isResource, boolean isFavorite) {
        this.id = audioResourceId; // Use the resource ID as the entity ID
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.audioResourceId = audioResourceId;
        this.category = category;
        this.isResource = isResource;
        this.isFavorite = isFavorite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    public String getDuration() {
        return duration;
    }

    public void setDuration(@NonNull String duration) {
        this.duration = duration;
    }

    public int getAudioResourceId() {
        return audioResourceId;
    }

    public void setAudioResourceId(int audioResourceId) {
        this.audioResourceId = audioResourceId;
    }

    /**
     * Get the actual URL for this audio resource
     * @return The URL string for the audio file
     */


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isResource() {
        return isResource;
    }

    public void setResource(boolean resource) {
        isResource = resource;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    /**
     * Convert duration string to milliseconds
     */
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

    public String getAudioUrl() {
        // For resources, we need to convert the resource ID to a URL
        if (isResource) {
            return "android.resource://" + "com.example.mindhaven" + "/raw/audio_" + audioResourceId;
        } else {
            // For custom uploaded meditations, this would be a direct URL
            return "audio_" + audioResourceId + ".mp3";
        }
    }


}