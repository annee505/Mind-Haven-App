
package com.example.mindhaven;

public class CBTWorksheet {
    private String title;
    private String description;
    private String purpose;
    private String instructions;
    private String benefits;

    public CBTWorksheet(String title, String description, String purpose, String instructions, String benefits) {
        this.title = title;
        this.description = description;
        this.purpose = purpose;
        this.instructions = instructions;
        this.benefits = benefits;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPurpose() { return purpose; }
    public String getInstructions() { return instructions; }
    public String getBenefits() { return benefits; }
}
