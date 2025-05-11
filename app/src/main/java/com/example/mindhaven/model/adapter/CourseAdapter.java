package com.example.mindhaven.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindhaven.R;
import com.example.mindhaven.model.Course;
import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
    private List<Course> courses;
    private View emptyView;
    public CourseAdapter(){}

    public CourseAdapter(List<Course> courses) {
        this.courses = courses;
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }

    private void checkIfEmpty() {
        if (emptyView != null) {
            emptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.title.setText(course.getTitle());
        holder.progress.setProgress(course.getProgress());
        holder.duration.setText(String.format("%d mins", course.getDuration()));

        if (course.getType().equals("CBT")) {
            holder.icon.setImageResource(R.drawable.ic_cbt);
        } else {
            holder.icon.setImageResource(R.drawable.ic_sleep);
        }
    }

    @Override
    public int getItemCount() {
        return (courses != null) ? courses.size() : 0;
    }

    public void submitList(List<Course> courses) {
        this.courses = courses;
        notifyDataSetChanged();
        checkIfEmpty();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;
        public final ProgressBar progress;
        public final TextView duration;
        public final ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            progress = itemView.findViewById(R.id.progress);
            duration = itemView.findViewById(R.id.duration);
            icon = itemView.findViewById(R.id.icon);
        }

        public void bind(Course course) {
            // This method is not cc anymore
        }
    }
}
