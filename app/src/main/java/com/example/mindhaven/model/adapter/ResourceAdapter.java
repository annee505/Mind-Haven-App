package com.example.mindhaven.model.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindhaven.R;
import com.example.mindhaven.model.adapter.EducationalResource;

import java.util.List;

/**
 * Adapter for displaying educational resources in a RecyclerView
 */
public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ResourceViewHolder> {

    private final Context context;
    private final List<EducationalResource> resourceList;

    public ResourceAdapter(Context context, List<EducationalResource> resourceList) {
        this.context = context;
        this.resourceList = resourceList;
    }

    @NonNull
    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_resource, parent, false);
        return new ResourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourceViewHolder holder, int position) {
        EducationalResource resource = resourceList.get(position);

        // Set resource data to views
        holder.titleTextView.setText(resource.getTitle());
        holder.descriptionTextView.setText(resource.getDescription());
        holder.categoryTextView.setText(resource.getCategory());
        holder.sourceTextView.setText("Source: " + resource.getSource());

        // Set resource image based on category or use default
        int imageResId = getImageResourceForCategory(resource.getCategory());
        holder.iconImageView.setImageResource(imageResId);

        // Set click listener for the learn more button
        holder.learnMoreButton.setOnClickListener(v -> {
            // Open the URL in a browser
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(resource.getUrl()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return resourceList.size();
    }

    /**
     * Maps resource categories to appropriate icon resources
     */
    private int getImageResourceForCategory(String category) {
        switch (category.toLowerCase()) {
            case "mental health basics":
                return R.drawable.ic_mental_health_basics;
            case "disorders":
                return R.drawable.ic_disorders;
            case "coping skills":
                return R.drawable.ic_coping_skills;
            case "wellness practices":
                return R.drawable.ic_wellness;
            case "physical health":
                return R.drawable.ic_physical_health;
            case "treatments":
                return R.drawable.ic_treatments;
            default:
                return R.drawable.ic_default_resource;
        }
    }

    /**
     * ViewHolder for educational resource items
     */
    static class ResourceViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView iconImageView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView categoryTextView;
        TextView sourceTextView;
        Button learnMoreButton;

        public ResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.resourceCardView);
            iconImageView = itemView.findViewById(R.id.resourceIconImageView);
            titleTextView = itemView.findViewById(R.id.resourceTitleTextView);
            descriptionTextView = itemView.findViewById(R.id.resourceDescriptionTextView);
            categoryTextView = itemView.findViewById(R.id.resourceCategoryTextView);
            sourceTextView = itemView.findViewById(R.id.resourceSourceTextView);
            learnMoreButton = itemView.findViewById(R.id.resourceLearnMoreButton);
        }
    }
}