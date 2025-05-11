package com.example.mindhaven;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final String TAG = "MessageAdapter";
    private List<Message> messageList;
    private Context context;
    private String currentUsername;

    private static final int VIEW_TYPE_MY_MESSAGE = 1;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 2;

    // Interface for message click
    public interface OnMessageClickListener {
        void onMessageClick(Message message);
    }

    private OnMessageClickListener messageClickListener;

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.messageClickListener = listener;
    }

    public MessageAdapter(List<Message> messageList, Context context, String currentUsername) {
        this.messageList = messageList;
        this.context = context;
        this.currentUsername = currentUsername;
        Log.d(TAG, "Adapter created with username: " + currentUsername);
    }

    // Add this method to update the username if needed
    public void setCurrentUsername(String username) {
        if (this.currentUsername == null || !this.currentUsername.equals(username)) {
            Log.d(TAG, "Updating currentUsername from " + this.currentUsername + " to " + username);
            this.currentUsername = username;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getSender().equals(currentUsername)) {
            return VIEW_TYPE_MY_MESSAGE;
        } else {
            return VIEW_TYPE_OTHER_MESSAGE;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MY_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.messageText.setText(message.getContent());
        holder.senderName.setText(message.getSender());
        holder.timeText.setText(message.getFormattedTime());

        // Set click listener for other users' messages
        if (!message.getSender().equals(currentUsername) && messageClickListener != null) {
            holder.itemView.setOnClickListener(v -> {
                messageClickListener.onMessageClick(message);
            });
        } else {
            // Remove click listener for own messages
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView senderName;
        TextView timeText;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            senderName = itemView.findViewById(R.id.sender_name);
            timeText = itemView.findViewById(R.id.time_text);
        }
    }
}