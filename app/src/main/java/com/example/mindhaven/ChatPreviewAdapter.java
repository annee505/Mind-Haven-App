package com.example.mindhaven;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatPreviewAdapter extends RecyclerView.Adapter<ChatPreviewAdapter.ViewHolder> {
    private List<ChatPreview> chats;
    private Context context;
    private OnChatClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());

    public interface OnChatClickListener {
        void onChatClick(ChatPreview chat);
    }

    public ChatPreviewAdapter(List<ChatPreview> chats, Context context) {
        this.chats = chats;
        this.context = context;
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatPreview chat = chats.get(position);

        // Set username/title
        if (chat.isAnonymousChat()) {
            holder.usernameText.setText("Anonymous Chat");
        } else {
            holder.usernameText.setText(chat.getOtherUser());
        }

        // Set last message
        holder.lastMessageText.setText(chat.getLastMessage());

        // Set timestamp
        Date date = new Date(chat.getTimestamp());
        holder.timestampText.setText(dateFormat.format(date));

        // Make texts black
        holder.usernameText.setTextColor(context.getResources().getColor(android.R.color.black));
        holder.lastMessageText.setTextColor(context.getResources().getColor(android.R.color.black));
        holder.timestampText.setTextColor(context.getResources().getColor(android.R.color.black));

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView usernameText;
        TextView lastMessageText;
        TextView timestampText;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            // Use the IDs that match your layout file
            usernameText = itemView.findViewById(R.id.chat_name);
            lastMessageText = itemView.findViewById(R.id.last_message);
            timestampText = itemView.findViewById(R.id.chat_timestamp);
        }
    }
}