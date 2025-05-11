package com.example.mindhaven.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class AudioPlayer {
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private SeekBar seekBar;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    private ImageButton playPauseButton;
    private boolean isPlaying = false;
    private OnPlaybackCompletionListener completionListener;

    public interface OnPlaybackCompletionListener {
        void onCompletion();
    }

    public AudioPlayer(Context context, SeekBar seekBar, TextView currentTimeTextView,
                       TextView totalTimeTextView, ImageButton playPauseButton) {
        this.seekBar = seekBar;
        this.currentTimeTextView = currentTimeTextView;
        this.totalTimeTextView = totalTimeTextView;
        this.playPauseButton = playPauseButton;
        this.handler = new Handler();

        setupSeekBar();
    }

    private void setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    updateTimeLabels();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public void setAudio(Context context, int resourceId) {
        stop();
        try {
            mediaPlayer = MediaPlayer.create(context, resourceId);
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                updatePlayPauseButton();
                handler.removeCallbacks(updateSeekBarRunnable);
                if (completionListener != null) {
                    completionListener.onCompletion();
                }
            });
            seekBar.setMax(mediaPlayer.getDuration());
            updateTimeLabels();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                pause();
            } else {
                play();
            }
        }
    }

    public void play() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            updatePlayPauseButton();
            handler.post(updateSeekBarRunnable);
        }
    }

    public void pause() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            updatePlayPauseButton();
            handler.removeCallbacks(updateSeekBarRunnable);
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            updatePlayPauseButton();
            handler.removeCallbacks(updateSeekBarRunnable);
        }
    }

    private void updatePlayPauseButton() {
        playPauseButton.setImageResource(isPlaying ?
                android.R.drawable.ic_media_pause :
                android.R.drawable.ic_media_play);
    }

    private void updateTimeLabels() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();
            currentTimeTextView.setText(formatTime(currentPosition));
            totalTimeTextView.setText(formatTime(duration));
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private final Runnable updateSeekBarRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                updateTimeLabels();
                handler.postDelayed(this, 1000);
            }
        }
    };

    public void setOnPlaybackCompletionListener(OnPlaybackCompletionListener listener) {
        this.completionListener = listener;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
