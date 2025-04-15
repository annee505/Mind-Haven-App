package com.example.mindhaven;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class MoodNotificationService extends Service {
    private static final String TAG = "MoodNotificationService";
    private static final String CHANNEL_ID = "mood_service_channel";
    private static final int FOREGROUND_SERVICE_ID = 1001;
    private static final int NOTIFICATION_ID = 1002;
    
    private Handler handler;
    private Runnable notificationRunnable;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        
        handler = new Handler();
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        
        // Start as foreground service
        startForeground(FOREGROUND_SERVICE_ID, createServiceNotification());
        
        // Schedule notification check
        scheduleNotificationCheck();
        
        // Return sticky to ensure service restarts if killed
        return START_STICKY;
    }
    
    private void scheduleNotificationCheck() {
        if (notificationRunnable != null) {
            handler.removeCallbacks(notificationRunnable);
        }
        
        notificationRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Checking if notification should be shown");
                
                // Get notification settings
                SharedPreferences prefs = getSharedPreferences("MoodTrackerPrefs", Context.MODE_PRIVATE);
                String frequency = prefs.getString("tracking_frequency", "Daily");
                boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
                
                if (!notificationsEnabled) {
                    Log.d(TAG, "Notifications are disabled");
                    scheduleNextCheck(60 * 60 * 1000); // Check again in 1 hour
                    return;
                }
                
                Calendar now = Calendar.getInstance();
                boolean shouldNotify = false;
                String message = "How are you feeling today?";
                
                if ("Daily".equals(frequency)) {
                    // Check if it's 10:00 AM
                    if (now.get(Calendar.HOUR_OF_DAY) == 10 && now.get(Calendar.MINUTE) == 0) {
                        shouldNotify = true;
                    }
                } else if ("Twice Daily".equals(frequency)) {
                    // Check if it's 10:00 AM or 6:00 PM
                    if ((now.get(Calendar.HOUR_OF_DAY) == 10 && now.get(Calendar.MINUTE) == 0) ||
                        (now.get(Calendar.HOUR_OF_DAY) == 18 && now.get(Calendar.MINUTE) == 0)) {
                        shouldNotify = true;
                    }
                } else if ("Custom".equals(frequency)) {
                    String customTime = prefs.getString("custom_notification_time", "");
                    String customMessage = prefs.getString("custom_notification_message", "");
                    
                    if (!customTime.isEmpty()) {
                        String[] parts = customTime.split(":");
                        if (parts.length == 2) {
                            int hour = Integer.parseInt(parts[0]);
                            int minute = Integer.parseInt(parts[1]);
                            
                            if (now.get(Calendar.HOUR_OF_DAY) == hour && now.get(Calendar.MINUTE) == minute) {
                                shouldNotify = true;
                                if (!customMessage.isEmpty()) {
                                    message = customMessage;
                                }
                            }
                        }
                    }
                }
                
                if (shouldNotify) {
                    Log.d(TAG, "Sending notification now");
                    sendMoodNotification(message);
                }
                
                // Schedule next check (check every minute)
                scheduleNextCheck(60 * 1000);
            }
        };
        
        // Start checking immediately
        handler.post(notificationRunnable);
    }
    
    private void scheduleNextCheck(long delayMillis) {
        Log.d(TAG, "Scheduling next notification check in " + (delayMillis / 1000) + " seconds");
        handler.postDelayed(notificationRunnable, delayMillis);
    }
    
    private void sendMoodNotification(String message) {
        // Create intent to open the app
        Intent notificationIntent = new Intent(this, HomeActivity.class);
        notificationIntent.putExtra("open_mood_tracker", true);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Mind Haven")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
                
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
    
    private Notification createServiceNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Mind Haven")
                .setContentText("Mood tracking active")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);
                
        return builder.build();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Mood Service Channel",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Channel for mood check service");
            
            NotificationManager notificationManager = 
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "Service destroyed");
        if (handler != null && notificationRunnable != null) {
            handler.removeCallbacks(notificationRunnable);
        }
        
        // Restart the service if it gets killed
        Intent restartServiceIntent = new Intent(this, MoodNotificationService.class);
        startService(restartServiceIntent);
        
        super.onDestroy();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
} 