package com.example.mindhaven;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<UserProfile> users;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserProfile user);
    }

    public UserAdapter(List<UserProfile> users, OnUserClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    public void updateList(List<UserProfile> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserProfile user = users.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView usernameText;
        private final TextView displayNameText;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            displayNameText = itemView.findViewById(R.id.displayNameText);
        }

        public void bind(UserProfile user, OnUserClickListener listener) {
            usernameText.setText("@" + user.getUsername());
            displayNameText.setText(user.getDisplayName());

            itemView.setOnClickListener(v -> listener.onUserClick(user));
        }
    }
}
