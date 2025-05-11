package com.example.mindhaven;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SearchResultsDialog extends Dialog {
    private List<String> results;
    private RecyclerView resultsRecyclerView;
    private TextView titleText;
    private SearchResultsAdapter adapter;
    private OnFriendRequestClickListener listener;

    public interface OnFriendRequestClickListener {
        void onFriendRequestClick(String username);
    }

    public SearchResultsDialog(Context context, List<String> results) {
        super(context);
        this.results = results;
    }

    public void setOnFriendRequestClickListener(OnFriendRequestClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_search_results);

        // Initialize views
        titleText = findViewById(R.id.title_text);
        resultsRecyclerView = findViewById(R.id.results_recycler_view);

        // Update title
        titleText.setText("Found " + results.size() + " user" + (results.size() == 1 ? "" : "s"));

        // Set up RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        resultsRecyclerView.setLayoutManager(layoutManager);

        // Create adapter
        adapter = new SearchResultsAdapter(results, getContext());
        adapter.setOnFriendRequestClickListener(username -> {
            if (listener != null) {
                listener.onFriendRequestClick(username);
                dismiss();
            }
        });

        resultsRecyclerView.setAdapter(adapter);
    }
}