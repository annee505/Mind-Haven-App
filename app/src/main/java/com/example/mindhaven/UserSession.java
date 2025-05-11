package com.example.mindhaven;

public class UserSession {
    private static UserSession instance;
    private String username;


    private UserSession() {
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public boolean hasUsername() {
        return username != null && !username.isEmpty();
    }
}