package com.example.mindhaven.model.adapter;

import java.util.List;

/**
 * Model class for Practical Exercise data
 */
public class PracticalExercise {
    private int id;
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private String timeRequired;
    private String iconName;
    private List<String> steps;

    // Constructor
    public PracticalExercise(int id, String title, String description, String category,
                             String difficulty, String timeRequired, String iconName,
                             List<String> steps) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.timeRequired = timeRequired;
        this.iconName = iconName;
        this.steps = steps;
    }

    // Getters and Setters
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getTimeRequired() {
        return timeRequired;
    }

    public void setTimeRequired(String timeRequired) {
        this.timeRequired = timeRequired;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }
}