
package com.example.mindhaven.model;

import java.util.List;

public class PracticalCourse {
    private String id;
    private String title;
    private String subtitle;
    private String description;
    private String category;
    private String difficulty;
    private String duration;
    private String instructor;
    private String source;
    private String coverUrl;
    private List<Module> modules;
    private List<String> prerequisites;
    private List<String> learningOutcomes;
    private boolean isCertified;
    private int completionRate;

    public PracticalCourse() {} // Required for Firebase

    public static class Module {
        private String id;
        private String title;
        private String description;
        private String contentType; // video, text, exercise, quiz
        private String content;
        private int durationMinutes;
        private List<Resource> resources;

        public Module() {}

        public Module(String id, String title, String description, String contentType, String content, int durationMinutes) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.contentType = contentType;
            this.content = content;
            this.durationMinutes = durationMinutes;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getContentType() { return contentType; }
        public String getContent() { return content; }
        public int getDurationMinutes() { return durationMinutes; }
        public List<Resource> getResources() { return resources; }
    }

    public static class Resource {
        private String type; // pdf, link, video
        private String title;
        private String url;

        public Resource() {}

        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getUrl() { return url; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public List<Module> getModules() { return modules; }
    public void setModules(List<Module> modules) { this.modules = modules; }
    public List<String> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }
    public List<String> getLearningOutcomes() { return learningOutcomes; }
    public void setLearningOutcomes(List<String> learningOutcomes) { this.learningOutcomes = learningOutcomes; }
    public boolean getIsCertified() { return isCertified; }
    public void setIsCertified(boolean certified) { this.isCertified = certified; }
    public int getCompletionRate() { return completionRate; }
    public void setCompletionRate(int completionRate) { this.completionRate = completionRate; }
}
