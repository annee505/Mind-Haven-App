package com.example.mindhaven.api;

import android.content.Context;
import android.util.Log;

/**
 * Service provider for local meditation data
 */
public class ApiClient {
    private static final String TAG = "ApiClient";

    private static MeditationApiService apiService = null;

    public static MeditationApiService getMeditationApiService(Context context) {
        if (apiService == null) {
            apiService = new MeditationApiService(context);
            Log.d(TAG, "Created new MeditationApiService instance");
        }
        return apiService;
    }
}