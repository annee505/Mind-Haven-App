package com.example.mindhaven;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    private List<ChatPreview> chatList;
    private Context context;

    // Interface for click events
    public interface OnChatClickListener {
        void onChatClick(ChatPreview chatPreview);
    }

    private OnChatClickListener chatClickListener;

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.chatClickListener = listener;
    }

    public ChatListAdapter(List<ChatPreview> chatList, Context context) {
        this.chatList = chatList;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_preview, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatPreview chatPreview = chatList.get(position);

        holder.chatName.setText(chatPreview.getName());
        holder.lastMessage.setText(chatPreview.getLastMessage());
        holder.timestamp.setText(chatPreview.getFormattedTime());

        // Set special styling for the anonymous chat
        if (chatPreview.isAnonymousChat()) {
            holder.chatIcon.setImageResource(R.drawable.chattt);
            holder.itemView.setBackgroundResource(R.drawable.anonymous_chat_background);
        } else {
            holder.chatIcon.setImageResource(R.drawable.chat);
            holder.itemView.setBackgroundResource(R.drawable.private_chat_background);
        }

        // Set click listener
        if (chatClickListener != null) {
            holder.itemView.setOnClickListener(v -> {
                chatClickListener.onChatClick(chatPreview);
            });
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView chatName;
        TextView lastMessage;
        TextView timestamp;
        ImageView chatIcon;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatName = itemView.findViewById(R.id.chat_name);
            lastMessage = itemView.findViewById(R.id.last_message);
            timestamp = itemView.findViewById(R.id.chat_timestamp);
            chatIcon = itemView.findViewById(R.id.chat_icon);
        }
    }
}