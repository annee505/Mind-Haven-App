package com.example.mindhaven.utils;

import android.media.MediaPlayer;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A direct media player that works with file paths instead of Android resource IDs
 */
public class DirectMediaPlayer {
    private static final String TAG = "DirectMediaPlayer";

    // Map of resource IDs to their file paths
    private static final Map<Integer, String> AUDIO_FILES = new HashMap<>();
    static {
        AUDIO_FILES.put(ResourceHelper.SLEEP_MEDITATION, "res/raw/sleep_meditation.mp3");
        AUDIO_FILES.put(ResourceHelper.BEDTIME_RELAXATION, "res/raw/bedtime_relaxation.mp3");
        AUDIO_FILES.put(ResourceHelper.STRESS_RELIEF, "res/raw/stress_relief.mp3");
        AUDIO_FILES.put(ResourceHelper.CALM_STORM, "res/raw/calm_storm.mp3");
        AUDIO_FILES.put(ResourceHelper.ANXIETY_RELIEF, "res/raw/anxiety_relief.mp3");
        AUDIO_FILES.put(ResourceHelper.FOCUS_CONCENTRATION, "res/raw/focus_concentration.mp3");
        AUDIO_FILES.put(ResourceHelper.MORNING_FOCUS, "res/raw/morning_focus.mp3");
    }

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private int currentResourceId = 0;
    private OnCompletionListener completionListener;
    private OnErrorListener errorListener;
    private OnPreparedListener preparedListener;

    public interface OnCompletionListener {
        void onCompletion();
    }

    public interface OnErrorListener {
        void onError(String errorMessage);
    }

    public interface OnPreparedListener {
        void onPrepared(int durationMs);
    }

    public DirectMediaPlayer() {
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(mp -> {
            isPlaying = false;
            if (completionListener != null) {
                completionListener.onCompletion();
            }
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            isPlaying = false;
            String errorMsg = "Media player error: " + what + ", " + extra;
            Log.e(TAG, errorMsg);
            if (errorListener != null) {
                errorListener.onError(errorMsg);
            }
            return true;
        });

        mediaPlayer.setOnPreparedListener(mp -> {
            if (preparedListener != null) {
                preparedListener.onPrepared(mp.getDuration());
            }
        });
    }

    public void setCompletionListener(OnCompletionListener listener) {
        this.completionListener = listener;
    }

    public void setErrorListener(OnErrorListener listener) {
        this.errorListener = listener;
    }

    public void setPreparedListener(OnPreparedListener listener) {
        this.preparedListener = listener;
    }

    public boolean playAudio(int resourceId) {
        // First check if the resource exists
        String filePath = AUDIO_FILES.get(resourceId);
        if (filePath == null) {
            Log.e(TAG, "No file path for resource ID: " + resourceId);
            if (errorListener != null) {
                errorListener.onError("Audio file not found");
            }
            return false;
        }

        File audioFile = new File(filePath);
        if (!audioFile.exists()) {
            Log.e(TAG, "Audio file doesn't exist: " + filePath);
            if (errorListener != null) {
                errorListener.onError("Audio file not found at: " + filePath);
            }
            return false;
        }

        try {
            // Reset the media player and set the data source
            mediaPlayer.reset();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepareAsync();
            currentResourceId = resourceId;
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error playing audio: " + e.getMessage());
            if (errorListener != null) {
                errorListener.onError("Error playing audio: " + e.getMessage());
            }
            return false;
        }
    }

    public void start() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    public void pause() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getCurrentResourceId() {
        return currentResourceId;
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * Get the duration for a specific meditation without loading it
     */
    public static String getDurationForResource(int resourceId) {
        // Return predefined durations
        switch (resourceId) {
            case ResourceHelper.SLEEP_MEDITATION:
                return "20:00";
            case ResourceHelper.BEDTIME_RELAXATION:
                return "15:00";
            case ResourceHelper.STRESS_RELIEF:
                return "10:00";
            case ResourceHelper.CALM_STORM:
                return "18:00";
            case ResourceHelper.ANXIETY_RELIEF:
                return "12:00";
            case ResourceHelper.FOCUS_CONCENTRATION:
                return "15:00";
            case ResourceHelper.MORNING_FOCUS:
                return "8:00";
            default:
                return "10:00";
        }
    }
}