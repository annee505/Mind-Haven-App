package com.example.mindhaven;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class BreathingExerciseActivity extends AppCompatActivity {

    private ImageView ivBreathingCircle;
    private TextView tvInstructions;
    private TextView tvTimer;
    private Button btnStart;
    private Button btnStop;
    private ConstraintLayout breathingLayout;

    private String breathingType;
    private boolean isExerciseRunning = false;
    private CountDownTimer exerciseTimer;
    private ValueAnimator circleAnimator;
    
    private final long EXERCISE_DURATION_MS = 120000;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_exercise);
        
        // Get breathing type from intent
        breathingType = getIntent().getStringExtra("BREATHING_TYPE");
        if (breathingType == null) {
            breathingType = "box"; // Default to box breathing
        }
        

        ivBreathingCircle = findViewById(R.id.ivBreathingCircle);
        tvInstructions = findViewById(R.id.tvInstructions);
        tvTimer = findViewById(R.id.tvTimer);
        btnStart = findViewById(R.id.btnStartBreathing);
        btnStop = findViewById(R.id.btnStopBreathing);
        breathingLayout = findViewById(R.id.breathingLayout);
        

        TextView tvTitle = findViewById(R.id.tvBreathingTitle);
        if ("box".equals(breathingType)) {
            tvTitle.setText("Box Breathing");
            tvInstructions.setText("Breathe in, hold, breathe out, hold - each for 4 counts");
        } else if ("478".equals(breathingType)) {
            tvTitle.setText("4-7-8 Breathing");
            tvInstructions.setText("Breathe in for 4, hold for 7, breathe out for 8");
        }
        

        btnStart.setOnClickListener(v -> startBreathingExercise());
        btnStop.setOnClickListener(v -> stopBreathingExercise());
        
        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
    
    private void startBreathingExercise() {
        if (isExerciseRunning) return;
        
        isExerciseRunning = true;
        btnStart.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);
        
        // Start the exercise timer
        startExerciseTimer();
        
        // Start the breathing animation
        if ("box".equals(breathingType)) {
            startBoxBreathingAnimation();
        } else if ("478".equals(breathingType)) {
            start478BreathingAnimation();
        }
    }
    
    private void stopBreathingExercise() {
        if (!isExerciseRunning) return;
        
        isExerciseRunning = false;
        btnStart.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.GONE);
        
        // Cancel timers and animations
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }
        
        if (circleAnimator != null && circleAnimator.isRunning()) {
            circleAnimator.cancel();
        }
        
        // Reset instructions
        if ("box".equals(breathingType)) {
            tvInstructions.setText("Breathe in, hold, breathe out, hold - each for 4 counts");
        } else if ("478".equals(breathingType)) {
            tvInstructions.setText("Breathe in for 4, hold for 7, breathe out for 8");
        }
        
        // Reset timer text
        tvTimer.setText("2:00");
    }
    
    private void startExerciseTimer() {
        // Create countdown timer for entire exercise
        exerciseTimer = new CountDownTimer(EXERCISE_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update timer text
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                tvTimer.setText(String.format("%d:%02d", minutes, seconds));
            }
            
            @Override
            public void onFinish() {
                // Exercise complete
                stopBreathingExercise();
            }
        }.start();
    }
    
    private void startBoxBreathingAnimation() {
        // Box breathing: 4 seconds inhale, 4 seconds hold, 4 seconds exhale, 4 seconds hold
        final String[] instructions = {
                "Breathe in...",
                "Hold...",
                "Breathe out...",
                "Hold..."
        };
        
        // Animate the circle - grow on inhale, stay on hold, shrink on exhale, stay on hold
        animateBreathingCircle(4000, 4000, 4000, 4000, instructions, true);
    }
    
    private void start478BreathingAnimation() {
        // 4-7-8 breathing: 4 seconds inhale, 7 seconds hold, 8 seconds exhale
        final String[] instructions = {
                "Breathe in...",
                "Hold...",
                "Breathe out..."
        };
        
        // Animate the circle - grow on inhale, stay on hold, shrink on exhale
        animateBreathingCircle(4000, 7000, 8000, 0, instructions, false);
    }
    
    private void animateBreathingCircle(long inhaleTime, long holdInTime, long exhaleTime, long holdOutTime, String[] instructions, boolean isBoxBreathing) {
        // Start with the inhale animation
        circleAnimator = ValueAnimator.ofFloat(1.0f, 2.0f);
        circleAnimator.setDuration(inhaleTime);
        circleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        circleAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            ivBreathingCircle.setScaleX(value);
            ivBreathingCircle.setScaleY(value);
        });
        
        // Instructions for inhale
        tvInstructions.setText(instructions[0]);
        
        // Chain animations: inhale -> hold -> exhale -> (hold if box breathing)
        circleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Hold after inhale
                tvInstructions.setText(instructions[1]);
                new CountDownTimer(holdInTime, holdInTime) {
                    @Override
                    public void onTick(long millisUntilFinished) {}
                    
                    @Override
                    public void onFinish() {
                        // Exhale animation
                        tvInstructions.setText(instructions[2]);
                        ValueAnimator exhaleAnimator = ValueAnimator.ofFloat(2.0f, 1.0f);
                        exhaleAnimator.setDuration(exhaleTime);
                        exhaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                        exhaleAnimator.addUpdateListener(anim -> {
                            float value = (float) anim.getAnimatedValue();
                            ivBreathingCircle.setScaleX(value);
                            ivBreathingCircle.setScaleY(value);
                        });
                        
                        if (isBoxBreathing) {
                            // For box breathing, add hold after exhale
                            exhaleAnimator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    // Hold after exhale (only for box breathing)
                                    tvInstructions.setText(instructions[3]);
                                    new CountDownTimer(holdOutTime, holdOutTime) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {}
                                        
                                        @Override
                                        public void onFinish() {
                                            // Repeat the cycle if still running
                                            if (isExerciseRunning) {
                                                startBoxBreathingAnimation();
                                            }
                                        }
                                    }.start();
                                }
                            });
                        } else {
                            // For 4-7-8 breathing, directly repeat the cycle
                            exhaleAnimator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    // Repeat the cycle if still running
                                    if (isExerciseRunning) {
                                        start478BreathingAnimation();
                                    }
                                }
                            });
                        }
                        
                        exhaleAnimator.start();
                    }
                }.start();
            }
        });
        
        circleAnimator.start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (exerciseTimer != null) {
            exerciseTimer.cancel();
        }
        
        if (circleAnimator != null && circleAnimator.isRunning()) {
            circleAnimator.cancel();
        }
    }
} 