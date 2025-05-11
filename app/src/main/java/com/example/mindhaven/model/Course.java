package com.example.mindhaven.model;

import java.util.List;

public class Course {
    private String id;
    private String title;
    private String description;
    private int duration;
    private int progress;
    private String type; // CBT, Sleep, etc

    public Course(String id, String title, String description, int progress, String type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.progress = progress;
        this.type = type;
    }
    public Course(){}

    public Course(String stressManagement, String title, List<String> stressLessons, List<String> stressExercises, String beginner) {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
