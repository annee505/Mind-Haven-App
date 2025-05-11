package com.example.mindhaven;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.mindhaven.model.Course;
import java.util.List;

public class PracticalCoursesViewModel extends ViewModel {
    private final MutableLiveData<List<Course>> courses = new MutableLiveData<>();

    public LiveData<List<Course>> getCourses() {
        return courses;
    }

    public void loadCourses() {
        // Implement your data loading logic here
        // Example using FirebaseHelper:
        FirebaseHelper.getInstance().fetchCourses(new FirebaseHelper.CourseCallback() {
            @Override
            public void onCallback(List<Course> courseList) {
                courses.postValue(courseList);
            }
        });
    }
}
