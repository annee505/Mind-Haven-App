
package com.example.mindhaven;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class BedtimeReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "bedtime_channel";
    private static final int NOTIFICATION_ID = 100;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Create an intent to open the main activity
        Intent mainIntent = new Intent(context, MainActivity.class);
        // Create a pending intent to open the main activity when the notification is clicked
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create a notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                // Set the small icon for the notification
                .setSmallIcon(R.drawable.ic_notification)
                // Set the title for the notification
                .setContentTitle("Bedtime Reminder")
                // Set the text for the notification
                .setContentText("It's time to prepare for bed!")
                // Set the priority for the notification
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Set the notification to auto cancel when clicked
                .setAutoCancel(true)
                // Set the pending intent for the notification
                .setContentIntent(pendingIntent);

        // Get the notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Notify the user with the notification
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
