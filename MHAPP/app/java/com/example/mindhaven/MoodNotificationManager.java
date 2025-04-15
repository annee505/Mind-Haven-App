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
            return;
        }

        cancelNotifications();

       
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
            !((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                .canScheduleExactAlarms()) {
            android.widget.Toast.makeText(context, 
                "Please allow exact alarms in system settings for reliable notifications", 
                android.widget.Toast.LENGTH_LONG).show();
            return;
        }

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                    );
                }
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        morningCalendar.getTimeInMillis(),
                        pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        morningCalendar.getTimeInMillis(),
                        pendingIntent
                    );
                }

                PendingIntent eveningPendingIntent = createPendingIntent();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        eveningCalendar.getTimeInMillis(),
                        eveningPendingIntent
                    );
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        eveningCalendar.getTimeInMillis(),
                        eveningPendingIntent
                    );
                }
                break;

            case "Custom":
                if (customNotificationTime != null) {
                    long intervalMillis;
                    long startTimeMillis;
                    
                    if (customIntervalHours > 0) {

                        intervalMillis = customIntervalHours * 60 * 60 * 1000L;
                        startTimeMillis = System.currentTimeMillis();
                    } else {

                        intervalMillis = AlarmManager.INTERVAL_DAY;
                        startTimeMillis = customNotificationTime.getTimeInMillis();
                        if (startTimeMillis <= System.currentTimeMillis()) {
                            startTimeMillis += AlarmManager.INTERVAL_DAY;
                        }
                    }
                    
                    Intent customIntent = new Intent(context, NotificationReceiver.class);
                    if (customNotificationMessage != null) {
                        customIntent.putExtra("custom_message", customNotificationMessage);
                    }
                    
                    PendingIntent customPendingIntent = PendingIntent.getBroadcast(
                        context, 
                        1,
                        customIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            startTimeMillis,
                            customPendingIntent
                        );
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            startTimeMillis,
                            customPendingIntent
                        );
                    }
                }
                break;
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
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
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