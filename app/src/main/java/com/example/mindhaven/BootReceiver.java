/*package com.example.mindhaven;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationManagerCompat;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Reschedule all notifications
            if (hasNotificationPermission(context)) {
                MoodNotificationManager.rescheduleAllNotifications(context);
            } else {
                Log.d(TAG, "No notification permission, skipping rescheduling");
            }
        } else {
            Log.d(TAG, "Device rebooted, restoring notifications");
            MindHavenApp app = (MindHavenApp) context.getApplicationContext();
            if (app.getNotificationManager() != null) {
                app.getNotificationManager().reInitializeNotifications();
            }
        }
    }

    private boolean hasNotificationPermission(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }
}
*/