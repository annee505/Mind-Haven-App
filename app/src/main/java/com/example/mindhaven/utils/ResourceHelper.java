package com.example.mindhaven.utils;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to help with resource loading without relying on Android's R.raw references
 */
public class ResourceHelper {
    private static final String TAG = "ResourceHelper";

    // Constants for raw resource IDs (matching our actual resource files)
    public static final int SLEEP_MEDITATION = 1;
    public static final int BEDTIME_RELAXATION = 2;
    public static final int STRESS_RELIEF = 3;
    public static final int CALM_STORM = 4;
    public static final int ANXIETY_RELIEF = 5;
    public static final int FOCUS_CONCENTRATION = 6;
    public static final int MORNING_FOCUS = 7;

    // Map resource IDs to their file names
    private static final Map<Integer, String> RESOURCE_FILES = new HashMap<>();
    static {
        RESOURCE_FILES.put(SLEEP_MEDITATION, "sleep_meditation.mp3");
        RESOURCE_FILES.put(BEDTIME_RELAXATION, "bedtime_relaxation.mp3");
        RESOURCE_FILES.put(STRESS_RELIEF, "stress_relief.mp3");
        RESOURCE_FILES.put(CALM_STORM, "calm_storm.mp3");
        RESOURCE_FILES.put(ANXIETY_RELIEF, "anxiety_relief.mp3");
        RESOURCE_FILES.put(FOCUS_CONCENTRATION, "focus_concentration.mp3");
        RESOURCE_FILES.put(MORNING_FOCUS, "morning_focus.mp3");
    }

    /**
     * Get a raw resource ID by name - this is a fallback method that tries to find
     * the resource file in our res/raw directory without using Android's R.raw references
     */
    public static int getRawResourceId(Context context, String resourceName) {
        try {
            // Look up the resource ID by file name
            for (Map.Entry<Integer, String> entry : RESOURCE_FILES.entrySet()) {
                if (entry.getValue().startsWith(resourceName)) {
                    return entry.getKey();
                }
            }

            Log.d(TAG, "Resource name not found: " + resourceName);
            return 0;
        } catch (Exception e) {
            Log.e(TAG, "Error getting resource ID for " + resourceName + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * Check if a raw resource file exists in our res/raw directory
     */
    public static boolean rawResourceExists(Context context, String resourceName) {
        try {
            File rawDir = new File("res/raw");
            if (rawDir.exists() && rawDir.isDirectory()) {
                File[] files = rawDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().startsWith(resourceName)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking resource: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the path to a raw resource file
     */
    public static String getRawResourcePath(int resourceId) {
        String fileName = RESOURCE_FILES.get(resourceId);
        if (fileName != null) {
            return "res/raw/" + fileName;
        }
        return null;
    }

    /**
     * This inner class provides static final int constants for raw resources
     * to mimic the Android R.raw behavior
     */
    public static class Raw {
        public static final int sleep_meditation = SLEEP_MEDITATION;
        public static final int bedtime_relaxation = BEDTIME_RELAXATION;
        public static final int stress_relief = STRESS_RELIEF;
        public static final int calm_storm = CALM_STORM;
        public static final int anxiety_relief = ANXIETY_RELIEF;
        public static final int focus_concentration = FOCUS_CONCENTRATION;
        public static final int morning_focus = MORNING_FOCUS;
    }
}