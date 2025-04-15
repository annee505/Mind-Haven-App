package com.example.mindhaven;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("completions")
    Call<AIResponse> getAIResponse(@Body Map<String, Object> request);
}