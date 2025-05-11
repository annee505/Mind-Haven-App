package com.example.mindhaven.model;

public class User {
    private String userId;
    private String name;
    private String email;
    private int meditationMinutes;
    private int completedCourses;

    public User() {}

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getMeditationMinutes() { return meditationMinutes; }
    public void setMeditationMinutes(int meditationMinutes) { this.meditationMinutes = meditationMinutes; }
    public int getCompletedCourses() { return completedCourses; }
    public void setCompletedCourses(int completedCourses) { this.completedCourses = completedCourses; }
}
