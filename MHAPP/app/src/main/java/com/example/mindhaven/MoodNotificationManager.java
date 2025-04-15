package com.example.mindhaven;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.util.Calendar;
import java.util.Random;

public class MoodNotificationManager {
    private static final String CHANNEL_ID = "mood_check_channel";
    private static final String CHANNEL_NAME = "Mood Check Notifications";
    private static final int NOTIFICATION_ID = 1;
    private final Context context;
    private final NotificationManager notificationManager;
    private final AlarmManager alarmManager;
    private boolean notificationsEnabled = true;

    private static final String[] MOOD_CHECK_MESSAGES = {
            "How are you feeling right now?",
            "Time for a quick mood check! How's your day going?",
            "Let's check in on your mood. How are you?",
            "Take a moment to reflect. How's your emotional state?",
            "Ready for your mood check-in?",
            "How would you describe your mood at this moment?"
    };

    private Calendar customNotificationTime;
    private String customNotificationMessage;
    private int customIntervalHours;

    public MoodNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for mood check notifications");
            channel.enableLights(true);
            channel.setLightColor(android.graphics.Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 300, 200, 300});
            channel.setBypassDnd(true);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void scheduleNotification(String frequency) {
        if (!notificationsEnabled) {
            android.util.Log.d("MoodNotificationManager", "Notifications are disabled, not scheduling");
            return;
        }

        cancelNotifications();

        // Check for exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !requestExactAlarmPermission()) {
            android.util.Log.e("MoodNotificationManager", "Cannot schedule exact alarms - permission not granted");
            return;
        }

        // Debug logging
        android.util.Log.d("MoodNotificationManager", "Scheduling notification with frequency: " + frequency);

        Calendar calendar = Calendar.getInstance();
        PendingIntent pendingIntent = createPendingIntent();

        switch (frequency) {
            case "Daily":
                calendar.set(Calendar.HOUR_OF_DAY, 10);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                
                android.util.Log.d("MoodNotificationManager", "Setting daily notification for: " + 
                        java.text.DateFormat.getDateTimeInstance().format(calendar.getTime()));
                
                scheduleExactAlarmWithRetry(calendar.getTimeInMillis(), pendingIntent);
                break;

            case "Twice Daily":
                Calendar morningCalendar = Calendar.getInstance();
                Calendar eveningCalendar = Calendar.getInstance();

                morningCalendar.set(Calendar.HOUR_OF_DAY, 10);
                morningCalendar.set(Calendar.MINUTE, 0);
                eveningCalendar.set(Calendar.HOUR_OF_DAY, 18);
                eveningCalendar.set(Calendar.MINUTE, 0);

                if (morningCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    morningCalendar.add(Calendar.DAY_OF_MONTH, 1);
                }
                if (eveningCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    eveningCalendar.add(Calendar.DAY_OF_MONTH, 1);
                }

                scheduleExactAlarmWithRetry(morningCalendar.getTimeInMillis(), pendingIntent);
                scheduleExactAlarmWithRetry(eveningCalendar.getTimeInMillis(), pendingIntent);
                break;

            case "Custom":
                android.util.Log.d("MoodNotificationManager", "Setting up custom notification");
                if (customNotificationTime != null) {
                    long startTimeMillis;

                    if (customIntervalHours > 0) {
                        startTimeMillis = System.currentTimeMillis() + (customIntervalHours * 60 * 60 * 1000L);
                        android.util.Log.d("MoodNotificationManager", "Using interval hours: " + customIntervalHours);
                    } else {
                        startTimeMillis = customNotificationTime.getTimeInMillis();
                        if (startTimeMillis <= System.currentTimeMillis()) {
                            startTimeMillis += AlarmManager.INTERVAL_DAY;
                            android.util.Log.d("MoodNotificationManager", "Time is in the past, scheduling for tomorrow");
                        }
                        
                        // Log the hour and minute
                        android.util.Log.d("MoodNotificationManager", "Scheduling for time: " + 
                            customNotificationTime.get(Calendar.HOUR_OF_DAY) + ":" + 
                            customNotificationTime.get(Calendar.MINUTE));
                    }

                    Intent customIntent = new Intent(context, NotificationReceiver.class);
                    if (customNotificationMessage != null) {
                        customIntent.putExtra("custom_message", customNotificationMessage);
                        android.util.Log.d("MoodNotificationManager", "Custom message: " + customNotificationMessage);
                    } else {
                        android.util.Log.d("MoodNotificationManager", "No custom message set");
                    }

                    PendingIntent customPendingIntent = PendingIntent.getBroadcast(
                            context,
                            1,
                            customIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );

                    // Log the custom time
                    Calendar logCal = Calendar.getInstance();
                    logCal.setTimeInMillis(startTimeMillis);
                    android.util.Log.d("MoodNotificationManager", "Setting custom notification for: " + 
                            java.text.DateFormat.getDateTimeInstance().format(logCal.getTime()) + 
                            " with message: " + customNotificationMessage);
                    
                    try {
                        scheduleExactAlarmWithRetry(startTimeMillis, customPendingIntent);
                        android.util.Log.d("MoodNotificationManager", "Successfully scheduled custom notification");
                    } catch (Exception e) {
                        android.util.Log.e("MoodNotificationManager", "Error scheduling custom notification: " + e.getMessage(), e);
                    }
                } else {
                    android.util.Log.d("MoodNotificationManager", "No custom notification time set - falling back to daily notification");
                    // Fall back to daily notification if no custom time is set
                    scheduleNotification("Daily");
                }
                break;
        }
    }
    
    // Helper method to schedule exact alarms with all available Android versions and options
    private void scheduleExactAlarmWithRetry(long triggerTimeMillis, PendingIntent pendingIntent) {
        boolean success = false;
        
        // Try different approaches based on API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTimeMillis,
                            pendingIntent
                    );
                    success = true;
                    android.util.Log.d("MoodNotificationManager", "Successfully scheduled exact alarm using setExactAndAllowWhileIdle");
                } catch (Exception e) {
                    android.util.Log.e("MoodNotificationManager", "Error scheduling exact alarm: " + e.getMessage());
                }
            } else {
                // Try using AlarmClock API as it doesn't require special permissions
                try {
                    // Create info for AlarmClock API
                    AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                            triggerTimeMillis,
                            pendingIntent // Show app as the clock
                    );
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
                    success = true;
                    android.util.Log.d("MoodNotificationManager", "Successfully scheduled alarm using setAlarmClock API");
                } catch (Exception e) {
                    android.util.Log.e("MoodNotificationManager", "Error scheduling alarm clock: " + e.getMessage());
                }
                
                // Show message to user about exact alarm permission
                android.widget.Toast.makeText(context, 
                        "For more reliable notifications, please enable exact alarms for Mind Haven in system settings", 
                        android.widget.Toast.LENGTH_LONG).show();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                );
                success = true;
                android.util.Log.d("MoodNotificationManager", "Successfully scheduled exact alarm using setExactAndAllowWhileIdle");
            } catch (Exception e) {
                android.util.Log.e("MoodNotificationManager", "Error scheduling exact alarm: " + e.getMessage());
            }
        }
        
        // Fallback if the exact methods failed
        if (!success) {
            try {
                // Try using setAlarmClock as it's generally more reliable
                AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(
                        triggerTimeMillis,
                        pendingIntent
                );
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
                android.util.Log.d("MoodNotificationManager", "Fallback: Scheduled using setAlarmClock");
            } catch (Exception e) {
                android.util.Log.e("MoodNotificationManager", "Error with setAlarmClock fallback: " + e.getMessage());
                
                // Last resort - use inexact alarm
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                );
                android.util.Log.d("MoodNotificationManager", "Last resort: Scheduled using regular set method");
            }
        }
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(context, NotificationReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, 0, intent, flags);
    }

    public void cancelNotifications() {
        PendingIntent pendingIntent = createPendingIntent();
        alarmManager.cancel(pendingIntent);


        Intent customIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent customPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                customIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(customPendingIntent);
    }


    public void setCustomIntervalNotification(int intervalHours, String message) {
        customIntervalHours = intervalHours;
        customNotificationTime = Calendar.getInstance();
        this.customNotificationMessage = message;
        scheduleNotification("Custom");
    }


    public void setCustomNotification(int hourOfDay, int minute, String message) {
        customIntervalHours = 0;
        customNotificationTime = Calendar.getInstance();
        customNotificationTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        customNotificationTime.set(Calendar.MINUTE, minute);
        customNotificationTime.set(Calendar.SECOND, 0);
        customNotificationTime.set(Calendar.MILLISECOND, 0);


        if (customNotificationTime.getTimeInMillis() <= System.currentTimeMillis()) {
            customNotificationTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        this.customNotificationMessage = message;
        scheduleNotification("Custom");
    }

    /**
     * Set a custom notification and send a test notification to verify it works
     */
    public void setCustomNotificationWithTest(int hourOfDay, int minute, String message) {
        // Set up the custom notification
        setCustomNotification(hourOfDay, minute, message);
        
        // Send a test notification immediately to verify it works
        Intent testIntent = new Intent(context, NotificationReceiver.class);
        testIntent.putExtra("custom_message", "Test: " + message + " (Will be sent daily at " + 
                String.format("%02d:%02d", hourOfDay, minute) + ")");
        context.sendBroadcast(testIntent);
        
        // Make sure our custom notification is actually scheduled for the future
        // This makes sure the alarm is properly registered
        scheduleNotification("Custom");
    }

    public void setNotificationsEnabled(boolean enabled) {
        this.notificationsEnabled = enabled;
        if (!enabled) {
            cancelNotifications();
        } else {

            if (customNotificationTime != null) {
                scheduleNotification("Custom");
            } else {
                scheduleNotification("Daily");
            }
        }
    }

    public boolean areNotificationsEnabled() {
        return notificationsEnabled;
    }

    public boolean requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                android.util.Log.d("MoodNotificationManager", "Requesting exact alarm permission");
                try {
                    android.widget.Toast.makeText(context, 
                        "Please enable exact alarms for Mind Haven to receive reliable notifications", 
                        android.widget.Toast.LENGTH_LONG).show();
                    
                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("MoodNotificationManager", "Error opening alarm settings: " + e.getMessage());
                }
                return false;
            }
        }
        return true;
    }

    public void reInitializeNotifications() {
        createNotificationChannel();
        if (notificationsEnabled && requestExactAlarmPermission()) {
            scheduleNotification("Daily");
        }
    }
    
    /**
     * Reinitialize notifications using saved preferences
     */
    public void reInitializeNotificationsWithSavedPrefs() {
        createNotificationChannel();
        if (!notificationsEnabled || !requestExactAlarmPermission()) {
            return;
        }
        
        try {
            // Load saved preferences
            android.content.SharedPreferences prefs = context.getSharedPreferences("MoodTrackerPrefs", android.content.Context.MODE_PRIVATE);
            String savedTime = prefs.getString("custom_notification_time", "");
            String savedMessage = prefs.getString("custom_notification_message", "Time for your mood check!");
            
            if (!savedTime.isEmpty()) {
                android.util.Log.d("MoodNotificationManager", "Restoring saved notification time: " + savedTime);
                
                // Parse the time and set the notification
                String[] parts = savedTime.split(":");
                if (parts.length == 2) {
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    
                    // Set the notification without sending a test
                    setCustomNotification(hour, minute, savedMessage);
                    android.util.Log.d("MoodNotificationManager", "Successfully restored custom notification for " + hour + ":" + minute);
                }
            } else {
                // No saved custom time, use daily
                scheduleNotification("Daily");
            }
        } catch (Exception e) {
            android.util.Log.e("MoodNotificationManager", "Error restoring saved notifications: " + e.getMessage(), e);
            // Fall back to daily notification
            scheduleNotification("Daily");
        }
    }

    public static class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("custom_message");
            if (message == null) {
                Random random = new Random();
                message = MOOD_CHECK_MESSAGES[random.nextInt(MOOD_CHECK_MESSAGES.length)];
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Mind Haven")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);


            Intent notificationIntent = new Intent(context, HomeActivity.class);
            notificationIntent.putExtra("open_mood_tracker", true);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


            long[] pattern = {0, 300, 200, 300};
            builder.setVibrate(pattern);

            notificationManager.notify(NOTIFICATION_ID, builder.build());


            MoodNotificationManager manager = ((MindHavenApp) context.getApplicationContext())
                    .getNotificationManager();

            if (manager.customNotificationTime != null) {
                manager.scheduleNotification("Custom");
            } else if (manager.customIntervalHours > 0) {
                manager.scheduleNotification("Custom");
            } else {
                manager.scheduleNotification("Daily");
            }
        }
    }
}