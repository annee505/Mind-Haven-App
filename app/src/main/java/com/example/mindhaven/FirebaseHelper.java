package com.example.mindhaven;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.mindhaven.model.User;
import com.example.mindhaven.model.Course;
import com.example.mindhaven.model.MeditationSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private final FirebaseFirestore db;
    private final DatabaseReference database;
    private static final String USERS_COLLECTION = "users";

    private FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public interface UserCallback {
        void onCallback(User user);
    }

    public interface CourseCallback {
        void onCallback(List<Course> courses);
    }

    public interface MeditationCallback {
        void onCallback(List<MeditationSession> sessions);
    }

    public void saveUserProgress(String userId, User userData) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Handle success
                    }
                });
    }

    public void getUserData(String userId, UserCallback callback) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            callback.onCallback(user);
                        }
                    }
                });
    }

    public void fetchCourses(CourseCallback callback) {
        database.child("courses")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Course> courses = new ArrayList<>();
                        for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                            Course course = courseSnapshot.getValue(Course.class);
                            courses.add(course);
                        }
                        callback.onCallback(courses);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error fetching courses", error.toException());
                    }
                });
    }

    public void saveMeditationSession(String userId, MeditationSession session) {
        database.child("meditation").child(userId).push().setValue(session);
    }

    public void getMeditationHistory(String userId, MeditationCallback callback) {
        database.child("meditation").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<MeditationSession> sessions = new ArrayList<>();
                        for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                            MeditationSession session = sessionSnapshot.getValue(MeditationSession.class);
                            sessions.add(session);
                        }
                        callback.onCallback(sessions);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Error fetching meditation history", error.toException());
                    }
                });
    }
}
