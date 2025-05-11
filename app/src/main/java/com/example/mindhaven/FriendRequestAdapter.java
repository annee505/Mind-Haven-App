package com.example.mindhaven;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    private List<FriendRequest> requests;
    private boolean isIncoming;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault());

    // Lists for the second constructor
    private List<FriendRequest> incomingRequests;
    private List<FriendRequest> outgoingRequests;

    // Flag to determine which constructor was used
    private boolean usingListMode = false;

    // Original interface for FriendRequestsActivity
    public interface OnRequestActionListener {
        void onAccept(FriendRequest request);
        void onDecline(FriendRequest request);
    }

    // New interface for ChatListFragment
    public interface RequestActionListener {
        void onAccept(FriendRequest request);
        void onDecline(FriendRequest request);
        void onCancel(FriendRequest request);
    }

    private OnRequestActionListener requestActionListener;
    private RequestActionListener actionListener;

    // Original constructor for FriendRequestsActivity
    public FriendRequestAdapter(List<FriendRequest> requests, boolean isIncoming, Context context) {
        this.requests = requests;
        this.isIncoming = isIncoming;
        this.context = context;
        this.usingListMode = false;
    }

    // New constructor for ChatListFragment
    public FriendRequestAdapter(List<FriendRequest> incomingRequests, List<FriendRequest> outgoingRequests, Context context) {
        this.incomingRequests = incomingRequests;
        this.outgoingRequests = outgoingRequests;
        this.context = context;
        this.usingListMode = true;
    }

    // Original setter for FriendRequestsActivity
    public void setOnRequestActionListener(OnRequestActionListener listener) {
        this.requestActionListener = listener;
    }

    // New setter for ChatListFragment
    public void setActionListener(RequestActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest request;
        boolean currentIsIncoming;

        if (usingListMode) {
            // Logic for ChatListFragment mode
            currentIsIncoming = position < incomingRequests.size();
            request = currentIsIncoming ?
                    incomingRequests.get(position) :
                    outgoingRequests.get(position - incomingRequests.size());
        } else {
            // Logic for FriendRequestsActivity mode
            currentIsIncoming = isIncoming;
            request = requests.get(position);
        }

        // Set username
        String username = currentIsIncoming ? request.getFromUser() : request.getToUser();
        holder.usernameText.setText(username);

        // Set timestamp
        Date date = new Date(request.getTimestamp());
        holder.timestampText.setText(dateFormat.format(date));

        // Set message based on request type
        if (currentIsIncoming) {
            holder.messageText.setText("wants to be your friend");

            // Set up accept button
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.acceptButton.setText("Accept");
            holder.acceptButton.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onAccept(request);
                }
                if (requestActionListener != null) {
                    requestActionListener.onAccept(request);
                }
            });

            // Set up decline button
            holder.declineButton.setText("Decline");
            holder.declineButton.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onDecline(request);
                }
                if (requestActionListener != null) {
                    requestActionListener.onDecline(request);
                }
            });
        } else {
            holder.messageText.setText("request sent");

            // Hide accept button for outgoing requests
            holder.acceptButton.setVisibility(View.GONE);

            // Set up cancel button
            holder.declineButton.setText("Cancel");
            holder.declineButton.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onCancel(request);
                }
                if (requestActionListener != null) {
                    requestActionListener.onDecline(request);
                }
            });
        }

        // Make texts black
        holder.usernameText.setTextColor(context.getResources().getColor(android.R.color.black));
        holder.messageText.setTextColor(context.getResources().getColor(android.R.color.black));
        holder.timestampText.setTextColor(context.getResources().getColor(android.R.color.black));

        // Make buttons brown
        holder.acceptButton.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        holder.declineButton.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));
    }

    @Override
    public int getItemCount() {
        if (usingListMode) {
            return incomingRequests.size() + outgoingRequests.size();
        } else {
            return requests.size();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView messageText;
        TextView timestampText;
        Button acceptButton;
        Button declineButton;

        ViewHolder(View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text);
            messageText = itemView.findViewById(R.id.message_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
            acceptButton = itemView.findViewById(R.id.accept_button);
            declineButton = itemView.findViewById(R.id.decline_button);
        }
    }
}