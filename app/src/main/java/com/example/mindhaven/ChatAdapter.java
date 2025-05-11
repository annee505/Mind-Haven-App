package com.example.mindhaven;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private final List<ChatMessage> messages;
    private final boolean isAnonymousChat;
    private final String localUserId;



    public ChatAdapter(Context context, List<ChatMessage> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
        this.isAnonymousChat = false;
        this.localUserId = null;
    }


    // New constructor for Anonymous Chat
    public ChatAdapter(Context context, List<ChatMessage> messages, String localUserId) {
        this.messages = messages != null ? messages : new ArrayList<>();
        this.isAnonymousChat = true;
        this.localUserId = localUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (message == null) return;

        if (isAnonymousChat) {
            handleAnonymousMessage(message, holder);
        } else {
            handleAIMessage(message, holder);
        }
    }

    private void handleAnonymousMessage(ChatMessage message, MessageViewHolder holder) {
        boolean isCurrentUser = message.getSenderId().equals(localUserId);

        if (isCurrentUser) {
            // Right-aligned user message
            holder.rightContainer.setVisibility(View.VISIBLE);
            holder.leftContainer.setVisibility(View.GONE);
            holder.rightMessageText.setText(message.getText());
            holder.statusIndicator.setVisibility(View.GONE);
        } else {
            // Left-aligned other messages
            holder.leftContainer.setVisibility(View.VISIBLE);
            holder.rightContainer.setVisibility(View.GONE);
            holder.leftMessageText.setText(message.getText());
            holder.leftUsername.setVisibility(View.GONE);
            holder.leftAvatar.setVisibility(View.GONE);
        }

        // Common timestamp handling
        String timePattern = isSameDay(message.getTimestamp()) ? "HH:mm" : "MMM d, HH:mm";
        TextView timestampView = isCurrentUser ? holder.rightTimestamp : holder.leftTimestamp;
        timestampView.setText(new SimpleDateFormat(timePattern, Locale.getDefault())
                .format(new Date(message.getTimestamp())));
    }

    private void handleAIMessage(ChatMessage message, MessageViewHolder holder) {
        boolean isUserMessage = message.isCurrentUser() && !"ai_system".equals(message.getUserId());

        if (isUserMessage) {
            holder.rightContainer.setVisibility(View.VISIBLE);
            holder.leftContainer.setVisibility(View.GONE);
            holder.rightMessageText.setText(message.getText());

            // Status indicator
            if (message.getStatus() != null) {
                switch (message.getStatus()) {
                    case SENDING:
                        holder.statusIndicator.setImageResource(R.drawable.ic_clock);
                        break;
                    case SENT:
                        holder.statusIndicator.setImageResource(R.drawable.ic_check);
                        break;
                    case DELIVERED:
                        holder.statusIndicator.setImageResource(R.drawable.ic_double_check);
                        break;
                    case READ:
                        holder.statusIndicator.setImageResource(R.drawable.ic_double_check_filled);
                        break;
                }
                holder.statusIndicator.setVisibility(View.VISIBLE);
            } else {
                holder.statusIndicator.setVisibility(View.GONE);
            }
        } else {
            holder.leftContainer.setVisibility(View.VISIBLE);
            holder.rightContainer.setVisibility(View.GONE);
            holder.leftMessageText.setText(message.getText());
            holder.leftUsername.setText("AI Assistant");
            holder.leftAvatar.setVisibility(View.VISIBLE);
            holder.leftAvatarInitial.setText("AI");
        }

        // Timestamp handling
        String timePattern = isSameDay(message.getTimestamp()) ? "HH:mm" : "MMM d, HH:mm";
        TextView timestampView = isUserMessage ? holder.rightTimestamp : holder.leftTimestamp;
        timestampView.setText(new SimpleDateFormat(timePattern, Locale.getDefault())
                .format(new Date(message.getTimestamp())));
    }

    private boolean isSameDay(long timestamp) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp);
        cal2.setTimeInMillis(System.currentTimeMillis());
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        View leftContainer;
        View rightContainer;
        TextView leftMessageText;
        TextView leftTimestamp;
        TextView leftUsername;
        TextView rightMessageText;
        TextView rightTimestamp;
        ImageView statusIndicator;
        View leftAvatar;
        TextView leftAvatarInitial;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            leftContainer = itemView.findViewById(R.id.leftContainer);
            rightContainer = itemView.findViewById(R.id.rightContainer);
            leftMessageText = itemView.findViewById(R.id.leftMessageText);
            leftTimestamp = itemView.findViewById(R.id.leftTimestamp);
            leftUsername = itemView.findViewById(R.id.leftUsername);
            rightMessageText = itemView.findViewById(R.id.rightMessageText);
            rightTimestamp = itemView.findViewById(R.id.rightTimestamp);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            leftAvatar = itemView.findViewById(R.id.leftAvatar);
            leftAvatarInitial = itemView.findViewById(R.id.leftAvatarInitial);
        }
    }
}