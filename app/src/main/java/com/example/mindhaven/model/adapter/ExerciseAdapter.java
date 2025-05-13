package com.example.mindhaven.model.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindhaven.R;
import com.example.mindhaven.model.adapter.PracticalExercise;

import java.util.List;

/**
 * Adapter for displaying practical exercises in a RecyclerView
 */
public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private final Context context;
    private final List<PracticalExercise> exerciseList;

    public ExerciseAdapter(Context context, List<PracticalExercise> exerciseList) {
        this.context = context;
        this.exerciseList = exerciseList;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        PracticalExercise exercise = exerciseList.get(position);

        // Set exercise data to views
        holder.titleTextView.setText(exercise.getTitle());
        holder.descriptionTextView.setText(exercise.getDescription());
        holder.categoryTextView.setText(exercise.getCategory());
        holder.timeTextView.setText(exercise.getTimeRequired());
        holder.difficultyTextView.setText(exercise.getDifficulty());

        // Set difficulty badge color
        setDifficultyBadgeColor(holder.difficultyTextView, exercise.getDifficulty());

        // Set exercise image based on category
        int imageResId = getImageResourceForCategory(exercise.getCategory());
        holder.iconImageView.setImageResource(imageResId);

        // Set up steps list
        setupStepsList(holder, exercise);

        // Set expand/collapse button click listener
        holder.expandButton.setOnClickListener(v -> {
            if (holder.stepsLayout.getVisibility() == View.VISIBLE) {
                holder.stepsLayout.setVisibility(View.GONE);
                holder.expandButton.setText(R.string.show_steps);
            } else {
                holder.stepsLayout.setVisibility(View.VISIBLE);
                holder.expandButton.setText(R.string.hide_steps);
            }
        });
    }

    /**
     * Set up the expandable steps list
     */
    private void setupStepsList(ExerciseViewHolder holder, PracticalExercise exercise) {
        List<String> steps = exercise.getSteps();
        holder.stepsLayout.removeAllViews();

        for (int i = 0; i < steps.size(); i++) {
            View stepView = LayoutInflater.from(context).inflate(R.layout.item_exercise_step, holder.stepsLayout, false);
            TextView stepNumberTextView = stepView.findViewById(R.id.stepNumberTextView);
            TextView stepDescriptionTextView = stepView.findViewById(R.id.stepDescriptionTextView);

            stepNumberTextView.setText(String.valueOf(i + 1));
            stepDescriptionTextView.setText(steps.get(i));

            holder.stepsLayout.addView(stepView);
        }
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    /**
     * Maps exercise categories to appropriate icon resources
     */
    private int getImageResourceForCategory(String category) {
        switch (category.toLowerCase()) {
            case "mindfulness":
                return R.drawable.ic_mindfulness;
            case "relaxation":
                return R.drawable.ic_relaxation;
            case "positivity":
                return R.drawable.ic_positivity;
            case "cognitive techniques":
                return R.drawable.ic_cognitive;
            case "anxiety management":
                return R.drawable.ic_anxiety;
            case "self-discovery":
                return R.drawable.ic_self_discovery;
            default:
                return R.drawable.ic_default_exercise;
        }
    }

    /**
     * Sets the background color for the difficulty badge
     */
    private void setDifficultyBadgeColor(TextView textView, String difficulty) {
        int colorResId;

        switch (difficulty.toLowerCase()) {
            case "easy":
                colorResId = R.color.difficulty_easy;
                break;
            case "moderate":
                colorResId = R.color.difficulty_moderate;
                break;
            case "challenging":
                colorResId = R.color.difficulty_challenging;
                break;
            default:
                colorResId = R.color.difficulty_default;
                break;
        }

        textView.setBackgroundResource(colorResId);
    }

    /**
     * ViewHolder for practical exercise items
     */
    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView iconImageView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView categoryTextView;
        TextView timeTextView;
        TextView difficultyTextView;
        Button expandButton;
        LinearLayout stepsLayout;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.exerciseCardView);
            iconImageView = itemView.findViewById(R.id.exerciseIconImageView);
            titleTextView = itemView.findViewById(R.id.exerciseTitleTextView);
            descriptionTextView = itemView.findViewById(R.id.exerciseDescriptionTextView);
            categoryTextView = itemView.findViewById(R.id.exerciseCategoryTextView);
            timeTextView = itemView.findViewById(R.id.exerciseTimeTextView);
            difficultyTextView = itemView.findViewById(R.id.exerciseDifficultyTextView);
            expandButton = itemView.findViewById(R.id.exerciseExpandButton);
            stepsLayout = itemView.findViewById(R.id.exerciseStepsLayout);
        }
    }
}