package com.example.mindhaven.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MeditationControlsReceiver extends BroadcastReceiver {
    private static final String TAG = "MeditationControls";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);

        if (action == null) return;

        // Forward the action to the MeditationPlayerActivity
        Intent forwardIntent = new Intent(context, com.example.mindhaven.MeditationPlayerActivity.class);
        forwardIntent.setAction(action);
        forwardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(forwardIntent);
    }
}