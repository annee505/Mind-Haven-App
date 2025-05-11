package com.example.mindhaven;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AIResponse {
    @SerializedName("generated_text")
    private String generatedText;

    public String getGeneratedText() {
        return generatedText != null ? generatedText : "I apologize, but I'm having trouble understanding. Could you please rephrase that?";
    }

    public void setGeneratedText(String generatedText) {
        this.generatedText = generatedText;
    }
}
