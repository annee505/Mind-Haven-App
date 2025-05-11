package com.example.mindhaven;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
    private List<String> usernames;
    private Context context;
    private OnFriendRequestClickListener listener;

    public interface OnFriendRequestClickListener {
        void onFriendRequestClick(String username);
    }

    public SearchResultsAdapter(List<String> usernames, Context context) {
        this.usernames = usernames;
        this.context = context;
    }

    public void setOnFriendRequestClickListener(OnFriendRequestClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String username = usernames.get(position);
        holder.usernameText.setText(username);

        // Make sure text is black
        holder.usernameText.setTextColor(context.getResources().getColor(android.R.color.black));

        // Set the button to brown
        holder.addButton.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

        holder.addButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendRequestClick(username);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usernames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        Button addButton;

        ViewHolder(View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text);
            addButton = itemView.findViewById(R.id.add_friend_button);
        }
    }
}