package com.example.clipshot;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomappbar.BottomAppBar;

import java.util.Objects;

public class FeedActivity extends AppCompatActivity {

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Call TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        // Set Elevation to TopBar and NavBar
        getSupportActionBar().setElevation(50f); // Float == px
        BottomAppBar bottomAppBar = findViewById(R.id.navigation);
        bottomAppBar.setElevation(50f); // Float == px
    }

    // Go To Profile (NavBar Button)
    public void goToProfile(View v) {
        Intent accessProfile = new Intent(this, ProfileActivity.class);
        startActivity(accessProfile);
    }
}