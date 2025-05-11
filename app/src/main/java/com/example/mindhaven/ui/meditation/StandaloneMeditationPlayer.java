package com.example.mindhaven.ui.meditation;

import com.example.mindhaven.utils.DirectMediaPlayer;
import com.example.mindhaven.utils.ResourceHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A standalone meditation player that doesn't rely on Android components
 */
public class StandaloneMeditationPlayer {
    private DirectMediaPlayer mediaPlayer;
    private MeditationInfo currentMeditation;
    private List<ProgressListener> progressListeners = new ArrayList<>();
    private List<StateChangeListener> stateListeners = new ArrayList<>();
    private ScheduledExecutorService progressExecutor;
    private boolean isInitialized = false;

    public interface ProgressListener {
        void onProgressUpdate(int currentPosition, int duration);
    }

    public interface StateChangeListener {
        void onStateChanged(State state, String message);
    }

    public enum State {
        LOADING,
        PLAYING,
        PAUSED,
        STOPPED,
        ERROR,
        COMPLETED
    }

    public static class MeditationInfo {
        private int resourceId;
        private String title;
        private String description;
        private String category;
        private String duration;

        public MeditationInfo(int resourceId, String title, String description,
                              String category, String duration) {
            this.resourceId = resourceId;
            this.title = title;
            this.description = description;
            this.category = category;
            this.duration = duration;
        }

        public int getResourceId() {
            return resourceId;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getCategory() {
            return category;
        }

        public String getDuration() {
            return duration;
        }
    }

    public StandaloneMeditationPlayer() {
        mediaPlayer = new DirectMediaPlayer();

        // Set up media player listeners
        mediaPlayer.setCompletionListener(() -> {
            notifyStateChanged(State.COMPLETED, "Meditation completed");
            stopProgressUpdates();
        });

        mediaPlayer.setErrorListener(errorMessage -> {
            notifyStateChanged(State.ERROR, errorMessage);
            stopProgressUpdates();
        });

        mediaPlayer.setPreparedListener(durationMs -> {
            isInitialized = true;
            startProgressUpdates();
            play();
        });
    }

    public void loadMeditation(MeditationInfo meditation) {
        this.currentMeditation = meditation;
        isInitialized = false;
        notifyStateChanged(State.LOADING, "Loading " + meditation.getTitle());
        boolean success = mediaPlayer.playAudio(meditation.getResourceId());
        if (!success) {
            notifyStateChanged(State.ERROR, "Failed to load meditation");
        }
    }

    public void play() {
        if (!isInitialized) {
            return;
        }

        mediaPlayer.start();
        notifyStateChanged(State.PLAYING, "Playing " + currentMeditation.getTitle());
    }

    public void pause() {
        if (!isInitialized) {
            return;
        }

        mediaPlayer.pause();
        notifyStateChanged(State.PAUSED, "Paused " + currentMeditation.getTitle());
    }

    public void stop() {
        if (!isInitialized) {
            return;
        }

        mediaPlayer.stop();
        stopProgressUpdates();
        notifyStateChanged(State.STOPPED, "Stopped " + currentMeditation.getTitle());
    }

    public void seekTo(int position) {
        if (!isInitialized) {
            return;
        }

        mediaPlayer.seekTo(position);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public MeditationInfo getCurrentMeditation() {
        return currentMeditation;
    }

    public void addProgressListener(ProgressListener listener) {
        progressListeners.add(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        progressListeners.remove(listener);
    }

    public void addStateChangeListener(StateChangeListener listener) {
        stateListeners.add(listener);
    }

    public void removeStateChangeListener(StateChangeListener listener) {
        stateListeners.remove(listener);
    }

    private void notifyProgressUpdate(int currentPosition, int duration) {
        for (ProgressListener listener : progressListeners) {
            listener.onProgressUpdate(currentPosition, duration);
        }
    }

    private void notifyStateChanged(State state, String message) {
        for (StateChangeListener listener : stateListeners) {
            listener.onStateChanged(state, message);
        }
    }

    private void startProgressUpdates() {
        stopProgressUpdates(); // Stop any existing updates

        progressExecutor = Executors.newSingleThreadScheduledExecutor();
        progressExecutor.scheduleAtFixedRate(() -> {
            if (mediaPlayer != null && isInitialized) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                notifyProgressUpdate(currentPosition, duration);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void stopProgressUpdates() {
        if (progressExecutor != null) {
            progressExecutor.shutdown();
            progressExecutor = null;
        }
    }

    public void release() {
        stopProgressUpdates();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        progressListeners.clear();
        stateListeners.clear();
    }

    /**
     * Create a meditation info object from resource ID
     */
    public static MeditationInfo createFromResourceId(int resourceId) {
        String title;
        String description;
        String category;
        String duration = DirectMediaPlayer.getDurationForResource(resourceId);

        switch (resourceId) {
            case ResourceHelper.SLEEP_MEDITATION:
                title = "Deep Sleep Meditation";
                description = "A calming forest ambience to help you fall into a deep, restful sleep.";
                category = "Sleep";
                break;
            case ResourceHelper.BEDTIME_RELAXATION:
                title = "Bedtime Relaxation";
                description = "Gentle flowing water sounds to prepare your mind and body for sleep.";
                category = "Sleep";
                break;
            case ResourceHelper.STRESS_RELIEF:
                title = "Stress Relief";
                description = "Summer afternoon ambience to reduce stress and anxiety.";
                category = "Stress";
                break;
            case ResourceHelper.CALM_STORM:
                title = "Calm in the Storm";
                description = "Gentle rain sounds to help you find your center of calm.";
                category = "Stress";
                break;
            case ResourceHelper.ANXIETY_RELIEF:
                title = "Anxiety Relief";
                description = "Peaceful meadow ambience to relieve anxious thoughts.";
                category = "Anxiety";
                break;
            case ResourceHelper.FOCUS_CONCENTRATION:
                title = "Focus and Concentration";
                description = "Soft wind through trees to improve concentration.";
                category = "Focus";
                break;
            case ResourceHelper.MORNING_FOCUS:
                title = "Morning Focus Routine";
                description = "Night birds sounds to start your day with clarity.";
                category = "Focus";
                break;
            default:
                title = "Meditation";
                description = "A guided meditation session.";
                category = "General";
        }

        return new MeditationInfo(resourceId, title, description, category, duration);
    }
}