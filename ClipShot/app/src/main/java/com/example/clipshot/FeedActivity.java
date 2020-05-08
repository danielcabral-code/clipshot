package com.example.clipshot;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

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

        Transition fade = new Fade();
        fade.excludeTarget(R.layout.action_bar_layout, true);
        fade.excludeTarget(R.id.navBar, true);
        getWindow().setExitTransition(fade);
        getWindow().setEnterTransition(fade);
    }

    public void goToProfile(View v) {
        Intent accessProfile = new Intent(this, ProfileActivity.class);
        startActivity(accessProfile);
    }
}