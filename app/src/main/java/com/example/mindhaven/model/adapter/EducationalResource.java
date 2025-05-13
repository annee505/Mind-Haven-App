package com.example.mindhaven.model.adapter;

/**
 * Model class for Educational Resource data
 */
public class EducationalResource {
    private int id;
    private String title;
    private String description;
    private String category;
    private String source;
    private String url;
    private String iconName;

    // Constructor
    public EducationalResource(int id, String title, String description, String category,
                               String source, String url, String iconName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.source = source;
        this.url = url;
        this.iconName = iconName;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
}