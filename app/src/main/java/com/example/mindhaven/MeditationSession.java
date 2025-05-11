package com.example.mindhaven;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Data class to represent a meditation session
 */
public class MeditationSession implements Parcelable {
    private int id;
    private String title;
    private String description;
    private String duration;
    private String audioUrl;
    private String imageUrl;
    private String category;
    private boolean isFavorite;

    public MeditationSession(int id, String title, String description, String duration,
                             String audioUrl, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.audioUrl = audioUrl;
        this.imageUrl = imageUrl;
        this.category = "General";
        this.isFavorite = false;
    }

    public MeditationSession(int id, String title, String description, String duration,
                             String audioUrl, String imageUrl, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.audioUrl = audioUrl;
        this.imageUrl = imageUrl;
        this.category = "General";
        this.isFavorite = isFavorite;
    }

    public MeditationSession(int id, String title, String description, String duration,
                             String audioUrl, String imageUrl, String category, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.audioUrl = audioUrl;
        this.imageUrl = imageUrl;
        this.category = category;
        this.isFavorite = isFavorite;
    }

    protected MeditationSession(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        duration = in.readString();
        audioUrl = in.readString();
        imageUrl = in.readString();
        category = in.readString();
        isFavorite = in.readByte() != 0;
    }

    public static final Creator<MeditationSession> CREATOR = new Creator<MeditationSession>() {
        @Override
        public MeditationSession createFromParcel(Parcel in) {
            return new MeditationSession(in);
        }

        @Override
        public MeditationSession[] newArray(int size) {
            return new MeditationSession[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(duration);
        dest.writeString(audioUrl);
        dest.writeString(imageUrl);
        dest.writeString(category);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
    }
}