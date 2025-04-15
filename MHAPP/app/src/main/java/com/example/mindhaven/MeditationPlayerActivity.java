package com.example.mindhaven;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MeditationPlayerActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 1001;
    private MeditationSession currentMeditation;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable runnable;

    private TextView tvTitle, tvDescription, tvDuration, tvCurrentTime;
    private ImageView ivCover, ivPlayPause, ivBack;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation_player);

        tvTitle = findViewById(R.id.tv_title);
        tvDescription = findViewById(R.id.tv_description);
        tvDuration = findViewById(R.id.tv_duration);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        ivCover = findViewById(R.id.iv_cover);
        ivPlayPause = findViewById(R.id.iv_play_pause);
        ivBack = findViewById(R.id.iv_back);
        seekBar = findViewById(R.id.seek_bar);

        // Get meditation data from intent
        currentMeditation = (MeditationSession) getIntent().getSerializableExtra("meditation");

        // Set up UI with meditation data
        if (currentMeditation != null) {
            tvTitle.setText(currentMeditation.getTitle());
            tvDescription.setText(currentMeditation.getDescription());
            tvDuration.setText(currentMeditation.getDuration());
            Glide.with(this).load(currentMeditation.getImageUrl()).into(ivCover);
        }

        // Initialize MediaPlayer and handler
        mediaPlayer = new MediaPlayer();
        handler = new Handler();

        // Set up seek bar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Set up play/pause button
        ivPlayPause.setOnClickListener(v -> togglePlayPause());

        // Set up back button
        ivBack.setOnClickListener(v -> onBackPressed());

        // Request storage permission
        requestStoragePermission();
    }

    private void togglePlayPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            ivPlayPause.setImageResource(R.drawable.ic_play);
        } else {
            if (currentMeditation != null) {
                try {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(currentMeditation.getAudioUrl());
                        mediaPlayer.prepareAsync();
                        mediaPlayer.setOnPreparedListener(mp -> {
                            mp.start();
                            ivPlayPause.setImageResource(R.drawable.ic_pause);
                            updateSeekBar();
                        });
                    } else {
                        mediaPlayer.start();
                        ivPlayPause.setImageResource(R.drawable.ic_pause);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error playing meditation", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null) {
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            tvCurrentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));

            runnable = () -> {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    updateSeekBar();
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, continue with meditation playback
            } else {
                Toast.makeText(this, "Storage permission is required to play meditations", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }
}