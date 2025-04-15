package com.example.mindhaven;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;
import com.google.firebase.FirebaseApp;

public class MindHavenApp extends Application {
    private MoodNotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseApp.initializeApp(this);
            notificationManager = new MoodNotificationManager(this);
            
        
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, 
                    android.Manifest.permission.POST_NOTIFICATIONS) == 
                    PackageManager.PERMISSION_GRANTED) {
                    notificationManager.reInitializeNotifications();
                }
            } else {
                notificationManager.reInitializeNotifications();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onNotificationPermissionChanged(boolean granted) {
        if (granted) {
            notificationManager.reInitializeNotifications();
        } else {
            notificationManager.setNotificationsEnabled(false);
        }
    }

    public MoodNotificationManager getNotificationManager() {
        return notificationManager;
    }
}