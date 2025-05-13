package com.example.mindhaven;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import com.example.mindhaven.model.PracticalCourse;

public class CourseDetailActivity extends AppCompatActivity {
    private TextView titleTextView;
    private TextView descriptionTextView;
    private TextView durationTextView;
    private TextView typeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        durationTextView = findViewById(R.id.durationTextView);
        typeTextView = findViewById(R.id.typeTextView);

        // Get the course data from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("course")) {
            PracticalCourse course = (PracticalCourse) intent.getSerializableExtra("course");
            if (course != null) {
                titleTextView.setText(course.getTitle());
                descriptionTextView.setText(course.getDescription());
                durationTextView.setText(String.valueOf(course.getDuration()) + " minutes");
                //typeTextView.setText(course.getType());
            }
        }
    }
}
