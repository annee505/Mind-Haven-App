package com.example.mindhaven;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to get recommendations from external APIs
 */
public class RecommendationService {
    private static final String TAG = "RecommendationService";
    
    // API endpoints
    private static final String BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes?q=subject:";
    private static final String MOVIES_API_URL = "https://api.themoviedb.org/3/discover/movie";
    private static final String MUSIC_API_URL = "https://api.deezer.com/search";
    
    // Set to false to use online APIs (only using fallback data if the APIs fail)
    private static final boolean USE_FALLBACK_DATA = false;
    
    // API keys (you would need to register for these services)
    private static final String TMDB_API_KEY = "3134ce935a11aced3e0dfa06454d9875"; // Movies API key
    
    // Mood to keyword/genre mapping
    private static final Map<String, String> BOOK_MOOD_MAP = new HashMap<String, String>() {{
        put("happy", "humor+comedy+funny");
        put("sad", "inspirational+comfort+heartwarming");
        put("excited", "adventure+thriller+suspense");
        put("scared", "cozy+comfort+relaxing");
        put("disappointed", "motivation+self-help+recovery");
        put("anxious", "mindfulness+meditation+calm");
        put("angry", "self-control+peace+forgiveness");
        put("bored", "fantasy+adventure+mystery");
        put("nostalgic", "memoir+history+classics");
        put("hopeful", "inspiration+uplifting+positive");
        put("stressed", "relaxation+self-care+peace");
        put("curious", "science+education+discovery");
    }};
    
    private static final Map<String, String> MOVIE_MOOD_MAP = new HashMap<String, String>() {{
        put("happy", "35"); // Comedy
        put("sad", "18"); // Drama with uplifting themes
        put("excited", "28,12"); // Action, Adventure
        put("scared", "10751,14"); // Family, Fantasy
        put("disappointed", "10752,36"); // War, History
        put("anxious", "99,27"); // Documentary, Horror (opposite to create distraction)
        put("angry", "10402,18"); // Music, Drama (to calm down)
        put("bored", "12,878"); // Adventure, Science Fiction
        put("nostalgic", "36,10749"); // History, Romance
        put("hopeful", "18,10751"); // Drama, Family
        put("stressed", "35,16"); // Comedy, Animation
        put("curious", "99,878"); // Documentary, Science Fiction
    }};
    
    private static final Map<String, String> MUSIC_MOOD_MAP = new HashMap<String, String>() {{
        put("happy", "happy feel good upbeat");
        put("sad", "uplifting inspirational comforting");
        put("excited", "energetic dance edm");
        put("scared", "calm relaxing ambient");
        put("disappointed", "motivational powerful inspiring");
        put("anxious", "meditation ambient slow");
        put("angry", "calm soft acoustic");
        put("bored", "upbeat catchy dance");
        put("nostalgic", "classic oldies retro");
        put("hopeful", "inspirational uplifting positive");
        put("stressed", "instrumental peaceful calm");
        put("curious", "experimental jazz world");
    }};

    private RequestQueue requestQueue;
    private Context context;

    public RecommendationService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * Get book recommendations based on mood
     */
    public void getBookRecommendations(String mood, final RecommendationCallback callback) {
        // If using fallback data, skip API call
        if (USE_FALLBACK_DATA) {
            callback.onSuccess(FallbackRecommendations.getBookRecommendations(mood));
            return;
        }
        
        String query = BOOK_MOOD_MAP.get(mood.toLowerCase());
        if (query == null) {
            callback.onError("Unsupported mood: " + mood);
            return;
        }
        
        String url = BOOKS_API_URL + query + "&maxResults=5";
        Log.d(TAG, "Fetching books with URL: " + url);
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    List<Recommendation> recommendations = new ArrayList<>();
                    try {
                        // Check if "items" exists in the response
                        if (!response.has("items")) {
                            // Use fallback data if API doesn't return valid results
                            callback.onSuccess(FallbackRecommendations.getBookRecommendations(mood));
                            return;
                        }
                        
                        JSONArray items = response.getJSONArray("items");
                        if (items.length() == 0) {
                            // Use fallback data if API doesn't return any items
                            callback.onSuccess(FallbackRecommendations.getBookRecommendations(mood));
                            return;
                        }
                        
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            
                            // Check if "volumeInfo" exists
                            if (!item.has("volumeInfo")) {
                                continue;
                            }
                            
                            JSONObject volumeInfo = item.getJSONObject("volumeInfo");
                            
                            // Check if title exists
                            if (!volumeInfo.has("title")) {
                                continue;
                            }
                            
                            String title = volumeInfo.getString("title");
                            String description = "No description available";
                            
                            if (volumeInfo.has("description") && !volumeInfo.isNull("description")) {
                                description = volumeInfo.getString("description");
                            }
                            
                            recommendations.add(new Recommendation(title, description, mood, "book"));
                        }
                        
                        if (recommendations.isEmpty()) {
                            // Use fallback data if no valid recommendations were parsed
                            callback.onSuccess(FallbackRecommendations.getBookRecommendations(mood));
                        } else {
                            callback.onSuccess(recommendations);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing book data: " + e.getMessage(), e);
                        // Use fallback data if there's a parsing error
                        callback.onSuccess(FallbackRecommendations.getBookRecommendations(mood));
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching book recommendations: " + error.getMessage(), error);
                    // Use fallback data if there's a network error
                    callback.onSuccess(FallbackRecommendations.getBookRecommendations(mood));
                });
        
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    /**
     * Get book recommendations based on multiple moods
     */
    public void getBookRecommendations(List<String> moods, final RecommendationCallback callback) {
        // If using fallback data, skip API call
        if (USE_FALLBACK_DATA) {
            callback.onSuccess(FallbackRecommendations.getBookRecommendations(moods));
            return;
        }
        
        if (moods == null || moods.isEmpty()) {
            callback.onError("Please select at least one mood");
            return;
        }

        // Use the first mood as the primary one, but combine keywords from all moods
        StringBuilder queryBuilder = new StringBuilder();
        for (int i = 0; i < moods.size(); i++) {
            String mood = moods.get(i).toLowerCase();
            String moodQuery = BOOK_MOOD_MAP.get(mood);
            if (moodQuery != null) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append("+");
                }
                queryBuilder.append(moodQuery.split("\\+")[0]); // Use first keyword from each mood
            }
        }

        String query = queryBuilder.toString();
        if (query.isEmpty()) {
            callback.onError("No valid moods selected");
            return;
        }

        String url = BOOKS_API_URL + query + "&maxResults=5";
        Log.d(TAG, "Fetching books with URL: " + url);
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    List<Recommendation> recommendations = new ArrayList<>();
                    try {
                        // Check if "items" exists in the response
                        if (!response.has("items")) {
                            // Use fallback data if API doesn't return valid results
                            callback.onSuccess(FallbackRecommendations.getBookRecommendations(moods));
                            return;
                        }
                        
                        JSONArray items = response.getJSONArray("items");
                        if (items.length() == 0) {
                            // Use fallback data if API doesn't return any items
                            callback.onSuccess(FallbackRecommendations.getBookRecommendations(moods));
                            return;
                        }
                        
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            
                            // Check if "volumeInfo" exists
                            if (!item.has("volumeInfo")) {
                                continue;
                            }
                            
                            JSONObject volumeInfo = item.getJSONObject("volumeInfo");
                            
                            // Check if title exists
                            if (!volumeInfo.has("title")) {
                                continue;
                            }
                            
                            String title = volumeInfo.getString("title");
                            String description = "No description available";
                            
                            if (volumeInfo.has("description") && !volumeInfo.isNull("description")) {
                                description = volumeInfo.getString("description");
                            }
                            
                            recommendations.add(new Recommendation(title, description, 
                                                String.join(", ", moods), "book"));
                        }
                        
                        if (recommendations.isEmpty()) {
                            // Use fallback data if no valid recommendations were parsed
                            callback.onSuccess(FallbackRecommendations.getBookRecommendations(moods));
                        } else {
                            callback.onSuccess(recommendations);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing book data: " + e.getMessage(), e);
                        // Use fallback data if there's a parsing error
                        callback.onSuccess(FallbackRecommendations.getBookRecommendations(moods));
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching book recommendations: " + error.getMessage(), error);
                    // Use fallback data if there's a network error
                    callback.onSuccess(FallbackRecommendations.getBookRecommendations(moods));
                });
        
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    /**
     * Get movie recommendations based on mood
     */
    public void getMovieRecommendations(String mood, final RecommendationCallback callback) {
        // If using fallback data, skip API call
        if (USE_FALLBACK_DATA) {
            callback.onSuccess(FallbackRecommendations.getMovieRecommendations(mood));
            return;
        }
        
        String genreIds = MOVIE_MOOD_MAP.get(mood.toLowerCase());
        if (genreIds == null) {
            callback.onError("Unsupported mood: " + mood);
            return;
        }
        
        String url = MOVIES_API_URL + "?api_key=" + TMDB_API_KEY + 
                "&with_genres=" + genreIds + "&sort_by=popularity.desc";
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    List<Recommendation> recommendations = new ArrayList<>();
                    try {
                        if (!response.has("results")) {
                            // Use fallback data if API doesn't return valid results
                            callback.onSuccess(FallbackRecommendations.getMovieRecommendations(mood));
                            return;
                        }
                        
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() == 0) {
                            // Use fallback data if API doesn't return any results
                            callback.onSuccess(FallbackRecommendations.getMovieRecommendations(mood));
                            return;
                        }
                        
                        for (int i = 0; i < Math.min(results.length(), 5); i++) {
                            JSONObject movie = results.getJSONObject(i);
                            
                            if (!movie.has("title") || !movie.has("overview")) {
                                continue;
                            }
                            
                            String title = movie.getString("title");
                            String description = movie.getString("overview");
                            
                            recommendations.add(new Recommendation(title, description, mood, "movie"));
                        }
                        
                        if (recommendations.isEmpty()) {
                            // Use fallback data if no valid recommendations were parsed
                            callback.onSuccess(FallbackRecommendations.getMovieRecommendations(mood));
                        } else {
                            callback.onSuccess(recommendations);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing movie data", e);
                        // Use fallback data if there's a parsing error
                        callback.onSuccess(FallbackRecommendations.getMovieRecommendations(mood));
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching movie recommendations", error);
                    // Use fallback data if there's a network error
                    callback.onSuccess(FallbackRecommendations.getMovieRecommendations(mood));
                });
        
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    /**
     * Get movie recommendations based on multiple moods
     */
    public void getMovieRecommendations(List<String> moods, final RecommendationCallback callback) {
        // If using fallback data, skip API call
        if (USE_FALLBACK_DATA) {
            callback.onSuccess(FallbackRecommendations.getMovieRecommendations(moods));
            return;
        }
        
        if (moods == null || moods.isEmpty()) {
            callback.onError("Please select at least one mood");
            return;
        }

        // Combine genre IDs from all moods
        StringBuilder genreBuilder = new StringBuilder();
        for (String mood : moods) {
            String genreIds = MOVIE_MOOD_MAP.get(mood.toLowerCase());
            if (genreIds != null) {
                if (genreBuilder.length() > 0) {
                    genreBuilder.append(",");
                }
                genreBuilder.append(genreIds.split(",")[0]); // Use first genre from each mood
            }
        }

        String genres = genreBuilder.toString();
        if (genres.isEmpty()) {
            callback.onError("No valid moods selected");
            return;
        }

        String url = MOVIES_API_URL + "?api_key=" + TMDB_API_KEY + 
                "&with_genres=" + genres + "&sort_by=popularity.desc";
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    List<Recommendation> recommendations = new ArrayList<>();
                    try {
                        if (!response.has("results")) {
                            // Use fallback data if API doesn't return valid results
                            callback.onSuccess(FallbackRecommendations.getMovieRecommendations(moods));
                            return;
                        }
                        
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() == 0) {
                            // Use fallback data if API doesn't return any results
                            callback.onSuccess(FallbackRecommendations.getMovieRecommendations(moods));
                            return;
                        }
                        
                        for (int i = 0; i < Math.min(results.length(), 5); i++) {
                            JSONObject movie = results.getJSONObject(i);
                            
                            if (!movie.has("title") || !movie.has("overview")) {
                                continue;
                            }
                            
                            String title = movie.getString("title");
                            String description = movie.getString("overview");
                            
                            recommendations.add(new Recommendation(title, description, 
                                               String.join(", ", moods), "movie"));
                        }
                        
                        if (recommendations.isEmpty()) {
                            // Use fallback data if no valid recommendations were parsed
                            callback.onSuccess(FallbackRecommendations.getMovieRecommendations(moods));
                        } else {
                            callback.onSuccess(recommendations);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing movie data", e);
                        // Use fallback data if there's a parsing error
                        callback.onSuccess(FallbackRecommendations.getMovieRecommendations(moods));
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching movie recommendations", error);
                    // Use fallback data if there's a network error
                    callback.onSuccess(FallbackRecommendations.getMovieRecommendations(moods));
                });
        
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    /**
     * Get music recommendations based on mood
     */
    public void getMusicRecommendations(String mood, final RecommendationCallback callback) {
        // If using fallback data, skip API call
        if (USE_FALLBACK_DATA) {
            callback.onSuccess(FallbackRecommendations.getMusicRecommendations(mood));
            return;
        }
        
        String query = MUSIC_MOOD_MAP.get(mood.toLowerCase());
        if (query == null) {
            callback.onError("Unsupported mood: " + mood);
            return;
        }
        
        String url = MUSIC_API_URL + "?q=" + query;
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    List<Recommendation> recommendations = new ArrayList<>();
                    try {
                        if (!response.has("data")) {
                            // Use fallback data if API doesn't return valid results
                            callback.onSuccess(FallbackRecommendations.getMusicRecommendations(mood));
                            return;
                        }
                        
                        JSONArray data = response.getJSONArray("data");
                        if (data.length() == 0) {
                            // Use fallback data if API doesn't return any data
                            callback.onSuccess(FallbackRecommendations.getMusicRecommendations(mood));
                            return;
                        }
                        
                        for (int i = 0; i < Math.min(data.length(), 5); i++) {
                            JSONObject track = data.getJSONObject(i);
                            
                            if (!track.has("title") || !track.has("artist") || !track.has("album")) {
                                continue;
                            }
                            
                            String title = track.getString("title");
                            String artist = track.getJSONObject("artist").getString("name");
                            String album = track.getJSONObject("album").getString("title");
                            String description = "Artist: " + artist + " | Album: " + album;
                            
                            recommendations.add(new Recommendation(title, description, mood, "music"));
                        }
                        
                        if (recommendations.isEmpty()) {
                            // Use fallback data if no valid recommendations were parsed
                            callback.onSuccess(FallbackRecommendations.getMusicRecommendations(mood));
                        } else {
                            callback.onSuccess(recommendations);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing music data", e);
                        // Use fallback data if there's a parsing error
                        callback.onSuccess(FallbackRecommendations.getMusicRecommendations(mood));
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching music recommendations", error);
                    // Use fallback data if there's a network error
                    callback.onSuccess(FallbackRecommendations.getMusicRecommendations(mood));
                });
        
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    /**
     * Get music recommendations based on multiple moods
     */
    public void getMusicRecommendations(List<String> moods, final RecommendationCallback callback) {
        // If using fallback data, skip API call
        if (USE_FALLBACK_DATA) {
            callback.onSuccess(FallbackRecommendations.getMusicRecommendations(moods));
            return;
        }
        
        if (moods == null || moods.isEmpty()) {
            callback.onError("Please select at least one mood");
            return;
        }

        // Combine keywords from all moods
        StringBuilder queryBuilder = new StringBuilder();
        for (String mood : moods) {
            String moodQuery = MUSIC_MOOD_MAP.get(mood.toLowerCase());
            if (moodQuery != null) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append(" ");
                }
                queryBuilder.append(moodQuery.split(" ")[0]); // Use first keyword from each mood
            }
        }

        String query = queryBuilder.toString();
        if (query.isEmpty()) {
            callback.onError("No valid moods selected");
            return;
        }

        String url = MUSIC_API_URL + "?q=" + query;
        
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    List<Recommendation> recommendations = new ArrayList<>();
                    try {
                        if (!response.has("data")) {
                            // Use fallback data if API doesn't return valid results
                            callback.onSuccess(FallbackRecommendations.getMusicRecommendations(moods));
                            return;
                        }
                        
                        JSONArray data = response.getJSONArray("data");
                        if (data.length() == 0) {
                            // Use fallback data if API doesn't return any data
                            callback.onSuccess(FallbackRecommendations.getMusicRecommendations(moods));
                            return;
                        }
                        
                        for (int i = 0; i < Math.min(data.length(), 5); i++) {
                            JSONObject track = data.getJSONObject(i);
                            
                            if (!track.has("title") || !track.has("artist") || !track.has("album")) {
                                continue;
                            }
                            
                            String title = track.getString("title");
                            String artist = track.getJSONObject("artist").getString("name");
                            String album = track.getJSONObject("album").getString("title");
                            String description = "Artist: " + artist + " | Album: " + album;
                            
                            recommendations.add(new Recommendation(title, description, 
                                               String.join(", ", moods), "music"));
                        }
                        
                        if (recommendations.isEmpty()) {
                            // Use fallback data if no valid recommendations were parsed
                            callback.onSuccess(FallbackRecommendations.getMusicRecommendations(moods));
                        } else {
                            callback.onSuccess(recommendations);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing music data", e);
                        // Use fallback data if there's a parsing error
                        callback.onSuccess(FallbackRecommendations.getMusicRecommendations(moods));
                    }
                },
                error -> {
                    Log.e(TAG, "Error fetching music recommendations", error);
                    // Use fallback data if there's a network error
                    callback.onSuccess(FallbackRecommendations.getMusicRecommendations(moods));
                });
        
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    /**
     * Callback interface for recommendation requests
     */
    public interface RecommendationCallback {
        void onSuccess(List<Recommendation> recommendations);
        void onError(String errorMessage);
    }
} 