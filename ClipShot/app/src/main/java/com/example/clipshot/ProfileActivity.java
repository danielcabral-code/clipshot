package com.example.clipshot;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;

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

        Transition fade = new Fade();
        fade.excludeTarget(R.layout.action_bar_layout, true);
        fade.excludeTarget(R.id.navBar, true);
        getWindow().setExitTransition(fade);
        getWindow().setEnterTransition(fade);
    }
}