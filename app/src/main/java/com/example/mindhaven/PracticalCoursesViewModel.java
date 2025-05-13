
package com.example.mindhaven;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.mindhaven.model.PracticalCourse;
import com.example.mindhaven.data.CourseDataProvider;
import java.util.List;
import java.util.stream.Collectors;

public class PracticalCoursesViewModel extends ViewModel {
    private final MutableLiveData<List<PracticalCourse>> courses = new MutableLiveData<>();
    private List<PracticalCourse> allCourses;
    private String currentFilter = null;

    public PracticalCoursesViewModel() {
        loadCourses();
    }

    public LiveData<List<PracticalCourse>> getCourses() {
        return courses;
    }

    public void loadCourses() {
        allCourses = CourseDataProvider.getMentalHealthCourses();
        applyFilter();
    }

    public void clearFilters() {
        currentFilter = null;
        applyFilter();
    }

    public void filterByType(String type) {
        currentFilter = type;
        applyFilter();
    }

    private void applyFilter() {
        if (currentFilter == null) {
            courses.setValue(allCourses);
        } else {
            List<PracticalCourse> filtered = allCourses.stream()
                    .filter(course -> course.getCategory().equals(currentFilter))
                    .collect(Collectors.toList());
            courses.setValue(filtered);
        }
    }
}
