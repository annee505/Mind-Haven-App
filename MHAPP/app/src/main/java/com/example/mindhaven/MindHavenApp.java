package com.example.mindhaven;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MindHavenApp extends Application {
    private MoodNotificationManager notificationManager;
    private FirebaseAnalyticsHelper analyticsHelper;
    private static final String TAG = "MindHavenApp";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseApp.initializeApp(this);
            notificationManager = new MoodNotificationManager(this);
            
            // Initialize analytics helper
            analyticsHelper = new FirebaseAnalyticsHelper();

            // Handle notification permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, 
                    android.Manifest.permission.POST_NOTIFICATIONS) == 
                    PackageManager.PERMISSION_GRANTED) {
                    notificationManager.setNotificationsEnabled(true);
                    // Check if we have saved custom notification settings before initializing
                    android.content.SharedPreferences prefs = getSharedPreferences("MoodTrackerPrefs", android.content.Context.MODE_PRIVATE);
                    String savedTime = prefs.getString("custom_notification_time", "");
                    
                    // Only initialize with custom if we have a saved time
                    if (!savedTime.isEmpty()) {
                        notificationManager.reInitializeNotificationsWithSavedPrefs();
                    } else {
                        notificationManager.reInitializeNotifications();
                    }
                } else {
                    notificationManager.setNotificationsEnabled(false);
                }
            } else {
                notificationManager.setNotificationsEnabled(true);
                // Check if we have saved custom notification settings before initializing
                android.content.SharedPreferences prefs = getSharedPreferences("MoodTrackerPrefs", android.content.Context.MODE_PRIVATE);
                String savedTime = prefs.getString("custom_notification_time", "");
                
                // Only initialize with custom if we have a saved time
                if (!savedTime.isEmpty()) {
                    notificationManager.reInitializeNotificationsWithSavedPrefs();
                } else {
                    notificationManager.reInitializeNotifications();
                }
            }
            
            // Perform database sync if user is logged in
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                syncDatabases();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // Send a broadcast to show error message
            Intent intent = new Intent("com.example.mindhaven.ERROR");
            intent.putExtra("error_message", "Failed to initialize app: " + e.getMessage());
            sendBroadcast(intent);
        }
    }
    
    private void syncDatabases() {
        if (analyticsHelper != null) {
            analyticsHelper.syncDatabases(new FirebaseAnalyticsHelper.SyncListener() {
                @Override
                public void onSyncComplete(boolean success) {
                    if (success) {
                        Log.d(TAG, "Database sync completed successfully");
                    } else {
                        Log.e(TAG, "Database sync failed");
                    }
                }
            });
        }
    }

    public void onNotificationPermissionChanged(boolean granted) {
        notificationManager.setNotificationsEnabled(granted);
        if (granted) {
            notificationManager.reInitializeNotifications();
        }
    }

    public MoodNotificationManager getNotificationManager() {
        return notificationManager;
    }
    
    public FirebaseAnalyticsHelper getAnalyticsHelper() {
        return analyticsHelper;
    }
}