package com.example.mindhaven.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to manage audio resources and their metadata
 */
public class AudioResourceProvider {
    private static final String TAG = "AudioResourceProvider";
    private Context context;

    // Hardcoded resource IDs - these will be resolved at runtime
    public static final int SLEEP_MEDITATION = 1;
    public static final int BEDTIME_RELAXATION = 2;
    public static final int STRESS_RELIEF = 3;
    public static final int CALM_STORM = 4;
    public static final int ANXIETY_RELIEF = 5;
    public static final int FOCUS_CONCENTRATION = 6;
    public static final int MORNING_FOCUS = 7;

    // Map audio resource IDs to resource names
    private static final Map<Integer, String> RESOURCE_NAME_MAP = new HashMap<>();
    static {
        RESOURCE_NAME_MAP.put(SLEEP_MEDITATION, "sleep_meditation");
        RESOURCE_NAME_MAP.put(BEDTIME_RELAXATION, "bedtime_relaxation");
        RESOURCE_NAME_MAP.put(STRESS_RELIEF, "stress_relief");
        RESOURCE_NAME_MAP.put(CALM_STORM, "calm_storm");
        RESOURCE_NAME_MAP.put(ANXIETY_RELIEF, "anxiety_relief");
        RESOURCE_NAME_MAP.put(FOCUS_CONCENTRATION, "focus_concentration");
        RESOURCE_NAME_MAP.put(MORNING_FOCUS, "morning_focus");
    }

    public AudioResourceProvider(Context context) {
        this.context = context;
    }

    /**
     * Get the raw resource name for an ID
     */
    public static String getResourceName(int resourceId) {
        return RESOURCE_NAME_MAP.get(resourceId);
    }

    /**
     * Get the duration of an audio resource in milliseconds
     */
    public long getAudioDuration(int resourceId) {
        // This would normally access the raw resource, but since we're working with a
        // non-standard project setup, we'll return a default duration
        switch (resourceId) {
            case SLEEP_MEDITATION:
            case BEDTIME_RELAXATION:
                return 15 * 60 * 1000; // 15 minutes
            case STRESS_RELIEF:
            case FOCUS_CONCENTRATION:
                return 10 * 60 * 1000; // 10 minutes
            case CALM_STORM:
                return 18 * 60 * 1000; // 18 minutes
            case ANXIETY_RELIEF:
                return 12 * 60 * 1000; // 12 minutes
            case MORNING_FOCUS:
                return 8 * 60 * 1000; // 8 minutes
            default:
                return 10 * 60 * 1000; // Default 10 minutes
        }
    }

    /**
     * Format milliseconds duration to mm:ss format
     */
    public static String formatDuration(long milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) (milliseconds / (1000 * 60));
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Get a list of all available meditation resources with their IDs,
     * names, and categories
     */
    public static Map<Integer, MeditationResourceInfo> getAllMeditationResources() {
        Map<Integer, MeditationResourceInfo> resources = new HashMap<>();

        resources.put(SLEEP_MEDITATION, new MeditationResourceInfo(
                SLEEP_MEDITATION,
                "Deep Sleep Meditation",
                "Sleep",
                "A calming forest ambience to help you fall into a deep, restful sleep.",
                "20:00"
        ));

        resources.put(BEDTIME_RELAXATION, new MeditationResourceInfo(
                BEDTIME_RELAXATION,
                "Bedtime Relaxation",
                "Sleep",
                "Gentle flowing water sounds to prepare your mind and body for sleep.",
                "15:00"
        ));

        resources.put(STRESS_RELIEF, new MeditationResourceInfo(
                STRESS_RELIEF,
                "Stress Relief",
                "Stress",
                "Summer afternoon ambience to reduce stress and anxiety.",
                "10:00"
        ));

        resources.put(CALM_STORM, new MeditationResourceInfo(
                CALM_STORM,
                "Calm in the Storm",
                "Stress",
                "Gentle rain sounds to help you find your center of calm.",
                "18:00"
        ));

        resources.put(ANXIETY_RELIEF, new MeditationResourceInfo(
                ANXIETY_RELIEF,
                "Anxiety Relief",
                "Anxiety",
                "Peaceful meadow ambience to relieve anxious thoughts and feelings.",
                "12:00"
        ));

        resources.put(FOCUS_CONCENTRATION, new MeditationResourceInfo(
                FOCUS_CONCENTRATION,
                "Focus and Concentration",
                "Focus",
                "Soft wind through trees to sharpen your mind and improve concentration.",
                "15:00"
        ));

        resources.put(MORNING_FOCUS, new MeditationResourceInfo(
                MORNING_FOCUS,
                "Morning Focus Routine",
                "Focus",
                "Night birds sounds to help you start your day with clarity and purpose.",
                "8:00"
        ));

        return resources;
    }

    /**
     * Helper class to store information about meditation resources
     */
    public static class MeditationResourceInfo {
        private int id;
        private String title;
        private String category;
        private String description;
        private String duration;

        public MeditationResourceInfo(int id, String title, String category, String description, String duration) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.description = description;
            this.duration = duration;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }

        public String getDuration() {
            return duration;
        }
    }
}