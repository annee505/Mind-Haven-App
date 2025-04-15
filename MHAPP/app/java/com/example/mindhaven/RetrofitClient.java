package com.example.mindhaven;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.Interceptor;
import okhttp3.Request;
import android.content.Context;
import android.util.Log;

public class RetrofitClient {
    private static final String BASE_URL = "https://api-inference.huggingface.co/models/facebook/blenderbot-400M-distill/";
    private static Retrofit retrofit;
    private static final String TAG = "RetrofitClient";
    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static ApiService getInstance() {
        if (retrofit == null) {
            if (appContext == null) {
                throw new IllegalStateException("RetrofitClient must be initialized with a Context");
            }

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                if (message.startsWith("--> POST")) {
                    Log.d(TAG, "Request URL: " + message);
                } else if (message.startsWith("<--")) {
                    Log.d(TAG, "Response: " + message);
                } else if (message.startsWith("{")) {
                    Log.d(TAG, "Request/Response Body: " + message);
                }
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("Authorization", "Bearer " + Constants.getHuggingFaceApiKey(appContext))
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            };

            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
