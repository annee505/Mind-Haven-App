package com.example.mindhaven.data;

import android.os.Handler;
import android.os.Looper;

import com.example.mindhaven.model.adapter.EducationalResource;
import com.example.mindhaven.model.adapter.PracticalExercise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repository class for fetching mental health data
 * In a real application, this would make network requests to a backend API
 */
public class DataRepository {

    // Interface for handling async data callbacks
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }

    /**
     * Fetches educational resources from reliable mental health sources
     * Sources include: National Institute of Mental Health (NIMH), World Health Organization (WHO),
     * American Psychological Association (APA), and more
     */
    public void getEducationalResources(DataCallback<List<EducationalResource>> callback) {
        // Simulate network delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                List<EducationalResource> resources = getEducationalResourcesData();
                callback.onSuccess(resources);
            } catch (Exception e) {
                callback.onError(e);
            }
        }, 1000);
    }

    /**
     * Fetches practical exercises for mental health
     * These exercises are based on evidence-based practices like CBT, mindfulness,
     * and other therapeutic approaches
     */
    public void getPracticalExercises(DataCallback<List<PracticalExercise>> callback) {
        // Simulate network delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                List<PracticalExercise> exercises = getPracticalExercisesData();
                callback.onSuccess(exercises);
            } catch (Exception e) {
                callback.onError(e);
            }
        }, 1000);
    }

    /**
     * Create real educational resources data from reputable sources
     */
    private List<EducationalResource> getEducationalResourcesData() {
        List<EducationalResource> resources = new ArrayList<>();

        // Data from National Institute of Mental Health (NIMH)
        resources.add(new EducationalResource(
                1,
                "Understanding Depression",
                "Learn about the symptoms, causes, and treatments for depression from the National Institute of Mental Health.",
                "Mental Health Basics",
                "National Institute of Mental Health",
                "https://www.nimh.nih.gov/health/topics/depression",
                "rain"
        ));

        resources.add(new EducationalResource(
                2,
                "Anxiety Disorders",
                "Comprehensive guide to different types of anxiety disorders, their symptoms, and evidence-based treatments.",
                "Disorders",
                "National Institute of Mental Health",
                "https://www.nimh.nih.gov/health/topics/anxiety-disorders",
                "flash"
        ));

        // Data from World Health Organization (WHO)
        resources.add(new EducationalResource(
                3,
                "Stress Management Strategies",
                "Evidence-based approaches to managing stress and improving mental wellbeing in daily life.",
                "Coping Skills",
                "World Health Organization",
                "https://www.who.int/news-room/questions-and-answers/item/stress",
                "shield"
        ));

        // Data from American Psychological Association (APA)
        resources.add(new EducationalResource(
                4,
                "Mindfulness for Mental Health",
                "Research on how mindfulness practices can help manage stress, anxiety, and depression.",
                "Wellness Practices",
                "American Psychological Association",
                "https://www.apa.org/topics/mindfulness",
                "sun"
        ));

        // Data from Harvard Health
        resources.add(new EducationalResource(
                5,
                "Sleep and Mental Health",
                "The relationship between sleep and mental health, plus strategies for improving sleep quality.",
                "Physical Health",
                "Harvard Health Publishing",
                "https://www.health.harvard.edu/newsletter_article/sleep-and-mental-health",
                "moon"
        ));

        // More data from American Psychological Association
        resources.add(new EducationalResource(
                6,
                "Cognitive Behavioral Therapy",
                "Introduction to CBT, one of the most effective evidence-based psychotherapies for various mental health conditions.",
                "Treatments",
                "American Psychological Association",
                "https://www.apa.org/ptsd-guideline/patients-and-families/cognitive-behavioral",
                "edit"
        ));

        return resources;
    }

    /**
     * Create practical exercises data based on evidence-based mental health practices
     */
    private List<PracticalExercise> getPracticalExercisesData() {
        List<PracticalExercise> exercises = new ArrayList<>();

        // Mindfulness exercise
        exercises.add(new PracticalExercise(
                1,
                "5-Minute Mindful Breathing",
                "A quick mindfulness exercise focusing on breathing to reduce stress and improve focus.",
                "Mindfulness",
                "Easy",
                "5 minutes",
                "wind",
                Arrays.asList(
                        "Find a comfortable seated position with your back straight.",
                        "Close your eyes or maintain a soft gaze downward.",
                        "Breathe naturally, focusing your attention on the sensation of breath entering and leaving your body.",
                        "When your mind wanders (which is normal), gently bring your attention back to your breath.",
                        "Continue for 5 minutes, ending with a deep breath and moment of gratitude."
                )
        ));

        // Relaxation exercise
        exercises.add(new PracticalExercise(
                2,
                "Progressive Muscle Relaxation",
                "Technique to reduce physical tension by systematically tensing and relaxing muscle groups.",
                "Relaxation",
                "Moderate",
                "15 minutes",
                "battery",
                Arrays.asList(
                        "Lie down or sit in a comfortable position where you can fully relax.",
                        "Starting with your feet, tense the muscles as tightly as you can for 5 seconds.",
                        "Release the tension and notice the feeling of relaxation for 10-15 seconds.",
                        "Move progressively upward through your body: calves, thighs, abdomen, hands, arms, shoulders, neck, and face.",
                        "Focus on the contrast between tension and relaxation.",
                        "After completing all muscle groups, take a few deep breaths and notice how your body feels."
                )
        ));

        // Positivity exercise
        exercises.add(new PracticalExercise(
                3,
                "Gratitude Journaling",
                "Structured writing exercise to cultivate appreciation and positive emotions.",
                "Positivity",
                "Easy",
                "10 minutes",
                "edit",
                Arrays.asList(
                        "Set aside 10 minutes in a quiet space with your journal.",
                        "Reflect on three specific things you're grateful for today.",
                        "For each item, write in detail why you're grateful and how it affects your life.",
                        "Try to find different things to appreciate each day, from small pleasures to meaningful relationships.",
                        "Notice any positive feelings that arise during this practice.",
                        "Continue regularly to build a habit of noticing positive aspects of life."
                )
        ));

        // Cognitive exercise (CBT-based)
        exercises.add(new PracticalExercise(
                4,
                "Thought Record Challenge",
                "CBT-based exercise to identify and reframe negative thought patterns.",
                "Cognitive Techniques",
                "Challenging",
                "20 minutes",
                "clipboard",
                Arrays.asList(
                        "Identify a situation that triggered difficult emotions.",
                        "Write down the automatic thoughts that came to mind during the situation.",
                        "Rate how strongly you believed these thoughts (0-100%).",
                        "Identify cognitive distortions in your thinking (e.g., catastrophizing, black-and-white thinking).",
                        "Generate more balanced alternative thoughts based on evidence.",
                        "Rate your belief in the alternative thoughts and notice any change in emotional state."
                )
        ));

        // Anxiety management exercise
        exercises.add(new PracticalExercise(
                5,
                "3-3-3 Grounding Exercise",
                "Quick grounding technique to manage anxiety and bring attention to the present moment.",
                "Anxiety Management",
                "Easy",
                "3 minutes",
                "anchor",
                Arrays.asList(
                        "Look around and name three things you can see.",
                        "Listen carefully and name three things you can hear.",
                        "Move three parts of your body: wiggle your fingers, rotate your ankles, roll your shoulders.",
                        "Take a deep breath after completing each step.",
                        "Notice how your anxiety level may have changed after this brief exercise."
                )
        ));

        // Self-discovery exercise
        exercises.add(new PracticalExercise(
                6,
                "Values Clarification Exercise",
                "Reflective activity to identify core personal values and assess life alignment.",
                "Self-Discovery",
                "Moderate",
                "30 minutes",
                "compass",
                Arrays.asList(
                        "Review a list of common values (e.g., honesty, creativity, health, family).",
                        "Select 10 values that resonate most strongly with you.",
                        "Narrow down to your top 5 most essential values.",
                        "For each core value, write what it means to you personally.",
                        "Reflect on how well your current life aligns with these values.",
                        "Identify one small action you can take this week to better honor each value."
                )
        ));

        return exercises;
    }
}