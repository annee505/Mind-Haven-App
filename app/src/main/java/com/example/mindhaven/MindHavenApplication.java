package com.example.mindhaven;

import android.app.Application;
import android.content.SharedPreferences;

public class MindHavenApplication extends Application {
    private String anonymousUsername;
    private String uniqueUsername;

    @Override
    public void onCreate() {
        super.onCreate();

        // Load usernames from SharedPreferences if available
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        uniqueUsername = prefs.getString("uniqueUsername", null);
        anonymousUsername = prefs.getString("anonymousUsername", null);
    }

    public void setAnonymousUsername(String username) {
        this.anonymousUsername = username;

        // Save to SharedPreferences for persistence
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("anonymousUsername", username);
        editor.apply();
    }

    public String getAnonymousUsername() {
        return anonymousUsername;
    }

    public boolean hasAnonymousUsername() {
        return anonymousUsername != null && !anonymousUsername.isEmpty();
    }

    public void setUniqueUsername(String username) {
        this.uniqueUsername = username;

        // Save to SharedPreferences for persistence
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("uniqueUsername", username);
        editor.apply();
    }

    public String getUniqueUsername() {
        return uniqueUsername;
    }

    public boolean hasUniqueUsername() {
        return uniqueUsername != null && !uniqueUsername.isEmpty();
    }
}