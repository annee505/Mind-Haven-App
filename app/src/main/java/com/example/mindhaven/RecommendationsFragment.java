package com.example.mindhaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RecommendationsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recommendations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up click listeners for the cards
        view.findViewById(R.id.card_books).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BooksActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.card_music).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MusicActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.card_movies).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MoviesActivity.class);
            startActivity(intent);
        });
    }
}
