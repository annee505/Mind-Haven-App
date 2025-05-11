package com.example.mindhaven;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<ChatMessage> messages;
    private SimpleDateFormat timeFormat;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
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

        holder.textMessage.setText(message.getText());
        holder.textTimestamp.setText(timeFormat.format(new Date(message.getTimestamp())));

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageContainer.getLayoutParams();
        if (message.isCurrentUser()) {
            params.gravity = Gravity.END;
            holder.messageContainer.setBackgroundResource(R.drawable.chat_bubble_sent);
            holder.textUsername.setVisibility(View.GONE);
        } else {
            params.gravity = Gravity.START;
            holder.messageContainer.setBackgroundResource(R.drawable.chat_bubble_received);
            
            if (message.isShowUsername()) {
                holder.textUsername.setVisibility(View.VISIBLE);
                holder.textUsername.setText(message.getUsername());
            } else {
                holder.textUsername.setVisibility(View.GONE);
            }
        }
        holder.messageContainer.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textUsername, textMessage, textTimestamp;
        LinearLayout messageContainer;

        MessageViewHolder(View itemView) {
            super(itemView);
            textUsername = itemView.findViewById(R.id.textUsername);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTimestamp = itemView.findViewById(R.id.textTimestamp);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }
    }
}
