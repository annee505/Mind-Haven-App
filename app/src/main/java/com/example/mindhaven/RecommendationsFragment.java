package com.example.mindhaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Fragment for showing personalized recommendations options
 * with improved animations and user experience
 */
public class RecommendationsFragment extends Fragment {

    private CardView booksCard, musicCard, moviesCard;
    private FloatingActionButton favoritesButton;
    private TextView welcomeText;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recommendations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize views
        booksCard = view.findViewById(R.id.card_books);
        musicCard = view.findViewById(R.id.card_music);
        moviesCard = view.findViewById(R.id.card_movies);
        favoritesButton = view.findViewById(R.id.favorites_button);
        welcomeText = view.findViewById(R.id.welcome_text);

        // Set welcome text with user name if available
        if (currentUser != null && currentUser.getDisplayName() != null) {
            welcomeText.setText("Hello, " + currentUser.getDisplayName() + "!\nWhat would you like to discover today?");
        }

        // Start entrance animations
        startEntranceAnimations();

        // Set up click listeners for the cards with animations
        booksCard.setOnClickListener(v -> {
            animateCardClick(booksCard);
            Intent intent = new Intent(getActivity(), BooksActivity.class);
            startActivity(intent);
        });

        musicCard.setOnClickListener(v -> {
            animateCardClick(musicCard);
            Intent intent = new Intent(getActivity(), MusicActivity.class);
            startActivity(intent);
        });

        moviesCard.setOnClickListener(v -> {
            animateCardClick(moviesCard);
            Intent intent = new Intent(getActivity(), MoviesActivity.class);
            startActivity(intent);
        });

        // Set up favorites button with animation
        favoritesButton.setOnClickListener(v -> {
            favoritesButton.animate()
                    .rotation(360f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        favoritesButton.setRotation(0f);
                        Intent intent = new Intent(getActivity(), FavoritesActivity.class);
                        startActivity(intent);
                    }).start();
        });
    }

    /**
     * Start entrance animations for cards
     */
    private void startEntranceAnimations() {
        // Animate welcome text
        welcomeText.setAlpha(0f);
        welcomeText.animate()
                .alpha(1f)
                .setDuration(500)
                .start();

        // Stagger card animations
        booksCard.setTranslationX(-1000f);
        musicCard.setTranslationX(-1000f);
        moviesCard.setTranslationX(-1000f);

        booksCard.animate()
                .translationX(0f)
                .setDuration(400)
                .setStartDelay(100)
                .start();

        musicCard.animate()
                .translationX(0f)
                .setDuration(400)
                .setStartDelay(200)
                .start();

        moviesCard.animate()
                .translationX(0f)
                .setDuration(400)
                .setStartDelay(300)
                .start();

        // Animate FAB
        favoritesButton.setScaleX(0f);
        favoritesButton.setScaleY(0f);
        favoritesButton.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setStartDelay(600)
                .start();
    }

    /**
     * Simple animation for card clicks
     */
    private void animateCardClick(CardView card) {
        card.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() ->
                        card.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start())
                .start();
    }
}