package com.example.mindhaven;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserProfile {
    private String userId;
    private String displayName;
    private String username;
    private String profileImageUrl;


    public UserProfile() {
    }

    public UserProfile(String userId, String displayName, String username, String profileImageUrl) {
        this.userId = userId;
        this.displayName = displayName;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
} 