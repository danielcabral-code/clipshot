package com.example.clipshot;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomappbar.BottomAppBar;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Call TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);

        // Set Elevation to TopBar and NavBar
        getSupportActionBar().setElevation(50f); // Float == px
        BottomAppBar bottomAppBar = findViewById(R.id.navigation);
        bottomAppBar.setElevation(50f); // Float == px

        // Opens Feed page by default
        openFragment(FeedFragment.newInstance("",""));
    }

    // Go To Feed (NavBar Button)
    public void goToFeed(View v) {
        openFragment(FeedFragment.newInstance("",""));
    }

    // Go To Profile (NavBar Button)
    public void goToProfile(View v) {
        openFragment(ProfileFragment.newInstance("",""));
    }

    // Utilitary method for opening fragments
    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.navBar, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}