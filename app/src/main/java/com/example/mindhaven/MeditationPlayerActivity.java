package com.example.mindhaven;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.IOException;

public class MeditationPlayerActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 1001;
    private MeditationSession currentMeditation;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable runnable;

    private TextView tvTitle, tvDescription, tvDuration, tvCurrentTime, tvError;
    private ImageView ivCover, ivPlayPause, ivBack;
    private SeekBar seekBar;
    private ProgressBar loadingProgress;
    private Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation_player);

        // Make activity display in fullscreen to fix the guided meditation display issue
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        // Initialize UI elements with the correct IDs from activity_meditation_player.xml
        tvTitle = findViewById(R.id.tv_title);
        tvDescription = findViewById(R.id.tv_description);
        tvDuration = findViewById(R.id.tv_duration);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvError = findViewById(R.id.tv_error);
        ivCover = findViewById(R.id.iv_cover);
        ivPlayPause = findViewById(R.id.iv_play_pause);
        ivBack = findViewById(R.id.iv_back);
        seekBar = findViewById(R.id.seek_bar);
        loadingProgress = findViewById(R.id.loading_progress);
        retryButton = findViewById(R.id.btn_retry);

        // Get meditation data from intent
        currentMeditation = getIntent().getParcelableExtra("session");
        if (currentMeditation == null) {
            // No session provided
            Toast.makeText(this, "Error loading meditation", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up UI with meditation data
        tvTitle.setText(currentMeditation.getTitle());
        tvDescription.setText(currentMeditation.getDescription());
        tvDuration.setText(currentMeditation.getDuration());

        // Only load image if URL is not empty
        if (currentMeditation.getImageUrl() != null && !currentMeditation.getImageUrl().isEmpty()) {
            Glide.with(this).load(currentMeditation.getImageUrl()).into(ivCover);
        } else {
            // Set a default background color instead of an image
            // This avoids the need for a default_meditation drawable resource
            int colorResId;
            String category = currentMeditation.getCategory();
            if ("Sleep".equalsIgnoreCase(category)) {
                colorResId = getResources().getColor(R.color.beige);
            } else if ("Stress".equalsIgnoreCase(category)) {
                colorResId = getResources().getColor(R.color.brown);
            } else if ("Anxiety".equalsIgnoreCase(category)) {
                colorResId = getResources().getColor(R.color.wbrown);
            } else if ("Focus".equalsIgnoreCase(category)) {
                colorResId = getResources().getColor(R.color.mood_button_brown);
            } else {
                colorResId = getResources().getColor(R.color.primary);
            }
            ivCover.setBackgroundColor(colorResId);
        }

        // Initialize MediaPlayer and handler
        mediaPlayer = new MediaPlayer();
        handler = new Handler();

        // Add retry button
        retryButton.setVisibility(View.GONE);
        retryButton.setOnClickListener(v -> {
            retryButton.setVisibility(View.GONE);
            loadAudio();
        });

        // Add progress bar
        loadingProgress.setVisibility(View.VISIBLE);

        mediaPlayer.setOnPreparedListener(mp -> {
            loadingProgress.setVisibility(View.GONE);
            mediaPlayer.start();
            ivPlayPause.setImageResource(R.drawable.ic_pause);
            updateSeekBar();
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            loadingProgress.setVisibility(View.GONE);
            tvError.setText("Error loading audio: " + what);
            tvError.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.VISIBLE);
            return true;
        });

        // Set up play/pause button
        ivPlayPause.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                ivPlayPause.setImageResource(R.drawable.ic_play);
                handler.removeCallbacks(runnable);
            } else {
                mediaPlayer.start();
                ivPlayPause.setImageResource(R.drawable.ic_pause);
                updateSeekBar();
            }
        });

        // Set up seek bar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(runnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.postDelayed(runnable, 1000);
            }
        });

        // Initialize runnable for seek bar updates
        runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition));
                    handler.postDelayed(this, 1000);
                }
            }
        };

        // Set up back button
        ivBack.setOnClickListener(v -> onBackPressed());

        // Request storage permission
        requestStoragePermission();
    }

    private void updateSeekBar() {
        if (mediaPlayer != null) {
            seekBar.setMax(mediaPlayer.getDuration());
            handler.postDelayed(runnable, 1000);
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.INTERNET},
                    STORAGE_PERMISSION_CODE);
        } else {
            loadAudio();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAudio();
            } else {
                tvError.setText("Permission required to stream audio");
                tvError.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadAudio() {
        try {
            if (currentMeditation.getAudioUrl() == null || currentMeditation.getAudioUrl().isEmpty()) {
                throw new IOException("No audio URL provided");
            }

            loadingProgress.setVisibility(View.VISIBLE);
            tvError.setVisibility(View.GONE);

            mediaPlayer.reset();
            mediaPlayer.setDataSource(currentMeditation.getAudioUrl());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            loadingProgress.setVisibility(View.GONE);
            tvError.setText("Error: " + e.getMessage());
            tvError.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    // Added to fix the issue with guided meditation not opening in full page
    // This method makes the activity display in full screen
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}