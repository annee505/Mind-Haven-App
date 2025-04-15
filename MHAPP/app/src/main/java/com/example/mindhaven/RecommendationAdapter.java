package com.example.mindhaven;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for displaying recommendations in a RecyclerView
 */
public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.ViewHolder> {
    
    private List<Recommendation> recommendations;
    private Context context;
    
    public RecommendationAdapter(Context context, List<Recommendation> recommendations) {
        this.context = context;
        this.recommendations = recommendations;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recommendation recommendation = recommendations.get(position);
        holder.titleTextView.setText(recommendation.getTitle());
        holder.descriptionTextView.setText(recommendation.getDescription());
    }
    
    @Override
    public int getItemCount() {
        return recommendations.size();
    }
    
    /**
     * Update recommendations list with new data
     */
    public void updateRecommendations(List<Recommendation> newRecommendations) {
        this.recommendations = newRecommendations;
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder for recommendation items
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_recommendation_title);
            descriptionTextView = itemView.findViewById(R.id.text_recommendation_description);
        }
    }
} 