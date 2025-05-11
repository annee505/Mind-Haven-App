package com.example.mindhaven;

import android.content.Context;
import android.content.res.AssetManager;
import java.util.Properties;
import java.io.InputStream;

public class Constants {
    private static String HUGGINGFACE_API_KEY = "YOUR_API_KEY_HERE";

    public static String getHuggingFaceApiKey(Context context) {
        if (HUGGINGFACE_API_KEY.equals("YOUR_API_KEY_HERE")) {
            try {
                Properties properties = new Properties();
                AssetManager assetManager = context.getAssets();
                InputStream inputStream = assetManager.open("config.properties");
                properties.load(inputStream);
                HUGGINGFACE_API_KEY = properties.getProperty("huggingface.api.key");
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return HUGGINGFACE_API_KEY;
    }
}
