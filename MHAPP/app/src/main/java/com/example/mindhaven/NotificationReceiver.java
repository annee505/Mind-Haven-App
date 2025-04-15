package com.example.mindhaven;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "mood_check_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "NotificationReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received broadcast intent: " + intent.getAction());
        
        try {
            // Create an intent to open the app
            Intent notificationIntent = new Intent(context, HomeActivity.class);
            notificationIntent.putExtra("open_mood_tracker", true);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, notificationIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Create the notification
            String message = intent.getStringExtra("custom_message");
            Log.d(TAG, "Notification message: " + message);
            
            if (message == null) {
                message = "How are you feeling today?";
            }
            
            createNotificationChannel(context);
            
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Mind Haven")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Notification sent successfully");
            
            // Reschedule the notification for the next day
            try {
                Log.d(TAG, "Getting application context to reschedule notification");
                MoodNotificationManager manager = ((MindHavenApp) context.getApplicationContext())
                        .getNotificationManager();
                if (manager != null) {
                    Log.d(TAG, "Rescheduling notification for next day");
                    if (intent.hasExtra("custom_message")) {
                        // This was a custom notification, reschedule as custom
                        Log.d(TAG, "Rescheduling custom notification");
                        manager.scheduleNotification("Custom");
                    } else {
                        // This was a regular notification
                        Log.d(TAG, "Rescheduling daily notification");
                        manager.scheduleNotification("Daily");
                    }
                } else {
                    Log.e(TAG, "Notification manager is null, cannot reschedule");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error rescheduling notification: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification: " + e.getMessage(), e);
        }
    }
    
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creating notification channel");
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Mood Check Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Mood check notification channel");
            
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
