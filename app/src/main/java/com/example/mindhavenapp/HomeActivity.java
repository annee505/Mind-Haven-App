package com.example.mindhavenapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class HomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageButton chatButton;
    private ImageButton settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        drawerLayout = findViewById(R.id.drawerLayout);
        chatButton = findViewById(R.id.chat);
        settingsButton = findViewById(R.id.settings);


        settingsButton.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.END);
        });


        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AnonymousChatActivity.class);
            startActivity(intent);
        });
    }
}
