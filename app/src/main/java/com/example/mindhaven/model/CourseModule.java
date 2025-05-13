
package com.example.mindhaven.model;

public class CourseModule {
    private String id;
    private String title;
    private String content;
    private String type; // video, text, exercise, quiz
    private int order;
    private boolean isCompleted;
    private String resourceUrl; // URL for videos or other resources

    public CourseModule() {}

    public CourseModule(String id, String title, String content, String type, int order) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.order = order;
        this.isCompleted = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public String getResourceUrl() { return resourceUrl; }
    public void setResourceUrl(String resourceUrl) { this.resourceUrl = resourceUrl; }
}
