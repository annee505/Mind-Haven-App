package com.example.mindhaven;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoodReportAdapter extends RecyclerView.Adapter<MoodReportAdapter.MoodReportViewHolder> {
    private List<MoodReport> reports = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    @NonNull
    @Override
    public MoodReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood_report, parent, false);
        return new MoodReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodReportViewHolder holder, int position) {
        try {
            MoodReport report = reports.get(position);
            if (report != null) {
                holder.reportText.setText(report.getText() != null ? report.getText() : "");
                holder.reportDate.setText(dateFormat.format(new Date(report.getTimestamp())));
            }
        } catch (Exception e) {
            // Handle any exceptions to prevent crashes
            holder.reportText.setText("Error displaying report");
            holder.reportDate.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return reports != null ? reports.size() : 0;
    }

    public void setReports(List<MoodReport> reports) {
        this.reports = reports != null ? reports : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class MoodReportViewHolder extends RecyclerView.ViewHolder {
        TextView reportText;
        TextView reportDate;

        MoodReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reportText = itemView.findViewById(R.id.reportText);
            reportDate = itemView.findViewById(R.id.reportDate);
        }
    }
} 