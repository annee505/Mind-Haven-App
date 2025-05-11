package com.example.mindhaven.api;

import com.example.mindhaven.models.MeditationAudio;

import java.util.List;

/**
 * Response model for meditation API calls
 */
public class MeditationResponse {
    private String status;
    private String message;
    private List<MeditationAudio> meditations;
    private int page;
    private int totalPages;
    private int totalCount;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<MeditationAudio> getMeditations() {
        return meditations;
    }

    public void setMeditations(List<MeditationAudio> meditations) {
        this.meditations = meditations;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}