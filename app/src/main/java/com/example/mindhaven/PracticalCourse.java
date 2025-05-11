
package com.example.mindhaven;

import java.util.List;

public class PracticalCourse {
    private String title;
    private String description;
    private List<String> lessons;
    private List<String> exercises;
    private String difficulty;

    public PracticalCourse(String title, String description, List<String> lessons,
                           List<String> exercises, String difficulty) {
        this.title = title;
        this.description = description;
        this.lessons = lessons;
        this.exercises = exercises;
        this.difficulty = difficulty;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getLessons() { return lessons; }
    public List<String> getExercises() { return exercises; }
    public String getDifficulty() { return difficulty; }
}
