package com.example.clipshot;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.View;

import com.google.android.material.bottomappbar.BottomAppBar;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Call TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        // Set Elevation to TopBar and NavBar
        getSupportActionBar().setElevation(50f); // Float == px
        BottomAppBar bottomAppBar = findViewById(R.id.navigation);
        bottomAppBar.setElevation(50f); // Float == px
    }

    // Go To Feed (NavBar Button)
    public void goToFeed(View v) {
        Intent accessFeed = new Intent(this, FeedActivity.class);
        startActivity(accessFeed);
    }
}