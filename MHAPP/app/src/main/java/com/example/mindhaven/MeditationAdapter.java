package com.example.mindhaven;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.mindhaven.R;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MeditationAdapter extends RecyclerView.Adapter<MeditationAdapter.MeditationViewHolder> {
    private List<MeditationSession> meditationSessions;
    private Context context;

    public MeditationAdapter(List<MeditationSession> meditationSessions, Context context) {
        this.meditationSessions = meditationSessions;
        this.context = context;
    }

    @NonNull
    @Override
    public MeditationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meditation, parent, false);
        return new MeditationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeditationViewHolder holder, int position) {
        MeditationSession session = meditationSessions.get(position);

        holder.tvTitle.setText(session.getTitle());
        holder.tvDescription.setText(session.getDescription());
        holder.tvDuration.setText(session.getDuration());

        // Set category icon based on meditation category
        setCategoryIcon(holder.ivCategoryIcon, session.getCategory());
        if (session.getImageUrl() != null && !session.getImageUrl().isEmpty()) {
            Glide.with(context)
                .load(session.getImageUrl())
                .placeholder(R.drawable.ic_meditation_placeholder)
                .into(holder.ivThumbnail);
        } else {
            // Set default image
            holder.ivThumbnail.setImageResource(R.drawable.ic_meditation_placeholder);
        }

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            // Open meditation player activity
            Intent intent = new Intent(context, MeditationPlayerActivity.class);
            intent.putExtra("meditation", session); // Pass the entire MeditationSession object
            context.startActivity(intent);
        });
    }

    private void setCategoryIcon(ImageView imageView, String category) {

        switch (category.toLowerCase()) {
            case "sleep":
                imageView.setImageResource(R.drawable.ic_sleep);
                break;
            case "anxiety":
                imageView.setImageResource(R.drawable.ic_anxiety);
                break;
            case "focus":
                imageView.setImageResource(R.drawable.ic_focus);
                break;
            case "stress":
                imageView.setImageResource(R.drawable.ic_stress);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_meditation);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return meditationSessions.size();
    }

    public static class MeditationViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivThumbnail;
        ImageView ivCategoryIcon;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvDuration;

        public MeditationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardMeditationItem);
            ivThumbnail = itemView.findViewById(R.id.ivMeditationThumbnail);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvTitle = itemView.findViewById(R.id.tvMeditationTitle);
            tvDescription = itemView.findViewById(R.id.tvMeditationDescription);
            tvDuration = itemView.findViewById(R.id.tvMeditationDuration);
        }
    }
}