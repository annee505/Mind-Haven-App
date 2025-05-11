package com.example.mindhaven;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class MeditationPlayerService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "MeditationService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "MeditationChannel";

    private final IBinder binder = new MeditationBinder();
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private String meditationTitle = "";
    private String meditationDuration = "";
    private int audioResourceId = 0;
    private boolean isPlaying = false;
    private AudioFocusRequest audioFocusRequest;
    private MediaSessionCompat mediaSession;
    private final Handler handler = new Handler();
    private Runnable progressUpdater;

    public class MeditationBinder extends Binder {
        public MeditationPlayerService getService() {
            return MeditationPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize audio manager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Create media session
        mediaSession = new MediaSessionCompat(this, "MeditationPlayer");

        // Initialize MediaPlayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        // Create notification channel for Android O and above
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Meditation Playback",
                    NotificationManager.IMPORTANCE_LOW); // Low importance to avoid sound
            channel.setDescription("Channel for meditation playback controls");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras() != null) {
            meditationTitle = intent.getStringExtra("title");
            meditationDuration = intent.getStringExtra("duration");
            audioResourceId = intent.getIntExtra("audioResourceId", 0);

            // Prepare the meditation audio
            prepareMediaPlayer();
        }

        return START_NOT_STICKY;
    }

    private void prepareMediaPlayer() {
        try {
            mediaPlayer.reset();

            // Set the audio source (resource or URL)
            if (audioResourceId != 0) {
                mediaPlayer.setDataSource(getApplicationContext(),
                        android.net.Uri.parse("android.resource://" + getPackageName() + "/" + audioResourceId));
            }

            // Request audio focus
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();

                audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(audioAttributes)
                        .setOnAudioFocusChangeListener(this)
                        .build();

                int result = audioManager.requestAudioFocus(audioFocusRequest);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mediaPlayer.prepareAsync();
                }
            } else {
                int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    mediaPlayer.prepareAsync();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error preparing meditation audio: " + e.getMessage());
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        startPlayback();
    }

    private void startPlayback() {
        mediaPlayer.start();
        isPlaying = true;
        startForeground(NOTIFICATION_ID, buildNotification());

        // Start progress updates
        startProgressUpdates();
    }

    private void startProgressUpdates() {
        progressUpdater = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int duration = mediaPlayer.getDuration();

                    // Broadcast progress updates
                    Intent intent = new Intent("MEDITATION_PROGRESS_UPDATE");
                    intent.putExtra("currentPosition", currentPosition);
                    intent.putExtra("duration", duration);
                    sendBroadcast(intent);

                    // Schedule next update
                    handler.postDelayed(this, 1000);
                }
            }
        };

        handler.post(progressUpdater);
    }

    private Notification buildNotification() {
        // Create intent for notification click (open player activity)
        Intent notificationIntent = new Intent(this, MeditationPlayerActivity.class);
        notificationIntent.putExtra("audioResourceId", audioResourceId);
        notificationIntent.putExtra("title", meditationTitle);
        notificationIntent.putExtra("duration", meditationDuration);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Create notification actions
        Intent playPauseIntent = new Intent("MEDITATION_PLAY_PAUSE");
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(
                this, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent("MEDITATION_STOP");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                this, 2, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        // Get notification icon
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_meditation);

        // Build the notification
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(meditationTitle)
                .setContentText("Meditation in progress")
                .setSmallIcon(R.drawable.ic_meditation)
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                        isPlaying ? "Pause" : "Play", playPausePendingIntent)
                .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    public void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            stopForeground(false); // Keep notification visible

            // Update notification
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, buildNotification());

            // Stop progress updates
            handler.removeCallbacks(progressUpdater);
        }
    }

    public void resumePlayback() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
            startForeground(NOTIFICATION_ID, buildNotification());

            // Restart progress updates
            startProgressUpdates();
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
        stopSelf();
        return true;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time
                pausePlayback();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, pause playback
                pausePlayback();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                // Focus gained, resume if paused
                if (!isPlaying) {
                    resumePlayback();
                }
                break;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        // Release resources
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Abandon audio focus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            }
        } else {
            audioManager.abandonAudioFocus(this);
        }

        // Remove callbacks
        handler.removeCallbacks(progressUpdater);

        // Release media session
        if (mediaSession != null) {
            mediaSession.release();
        }

        super.onDestroy();
    }
}