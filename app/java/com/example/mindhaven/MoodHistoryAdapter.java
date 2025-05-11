package com.example.mindhaven;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder> {
    private List<Map<String, Object>> moodEntries;
    private SimpleDateFormat dateFormat;

    public MoodHistoryAdapter() {
        this.moodEntries = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood_history, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        Map<String, Object> entry = moodEntries.get(position);
        
     
        String mood = (String) entry.get("mood");
        if (mood != null) {
            switch (mood) {
                case "Happy":
                    holder.moodEmoji.setText("😊");
                    break;
                case "Neutral":
                    holder.moodEmoji.setText("😐");
                    break;
                case "Sad":
                    holder.moodEmoji.setText("😔");
                    break;
            }
        }

        
        Long timestamp = (Long) entry.get("timestamp");
        if (timestamp != null) {
            holder.dateTime.setText(dateFormat.format(new Date(timestamp)));
        }

        Set note if exists
        String note = (String) entry.get("note");
        if (note != null && !note.isEmpty()) {
            holder.noteText.setVisibility(View.VISIBLE);
            holder.noteText.setText(note);
        } else {
            holder.noteText.setVisibility(View.GONE);
        }

     
        holder.activitiesChipGroup.removeAllViews();
        List<String> activities = (List<String>) entry.get("activities");
        if (activities != null && !activities.isEmpty()) {
            for (String activity : activities) {
                Chip chip = new Chip(holder.activitiesChipGroup.getContext());
                chip.setText(activity);
                chip.setClickable(false);
                holder.activitiesChipGroup.addView(chip);
            }
        }

        String timeOfDay = (String) entry.get("timeOfDay");
        if (timeOfDay != null) {
            holder.timeOfDay.setText(timeOfDay);
            holder.timeOfDay.setVisibility(View.VISIBLE);
        } else {
            holder.timeOfDay.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return moodEntries.size();
    }

    public void updateEntries(List<Map<String, Object>> newEntries) {
        this.moodEntries = new ArrayList<>(newEntries);
        notifyDataSetChanged();
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView moodEmoji;
        TextView dateTime;
        TextView noteText;
        ChipGroup activitiesChipGroup;
        TextView timeOfDay;

        MoodViewHolder(View itemView) {
            super(itemView);
            moodEmoji = itemView.findViewById(R.id.moodEmoji);
            dateTime = itemView.findViewById(R.id.dateTime);
            noteText = itemView.findViewById(R.id.noteText);
            activitiesChipGroup = itemView.findViewById(R.id.activitiesChipGroup);
            timeOfDay = itemView.findViewById(R.id.timeOfDay);
        }
    }
} 