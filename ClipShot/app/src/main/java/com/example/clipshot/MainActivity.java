package com.example.clipshot;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Call Feed TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.feed_action_bar_layout);

        // Set Elevation to TopBar and NavBar
        getSupportActionBar().setElevation(50f); // Float == px
        BottomAppBar bottomAppBar = findViewById(R.id.navigation);
        bottomAppBar.setElevation(50f); // Float == px

        // Opens Feed page by default
        openFragment(FeedFragment.newInstance("",""));
        // Opacity changes on Bottom Bar Icon depending on what page is selected
        AppCompatImageView iconHome = findViewById(R.id.iconHome);
        iconHome.setAlpha((float) 1.0);
        AppCompatImageView iconProfile = findViewById(R.id.iconProfile);
        iconProfile.setAlpha((float) 0.45);
    }

    // Go To Feed (NavBar Button)
    @SuppressLint("WrongConstant")
    public void goToFeed(View v) {

        // Call Feed TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.feed_action_bar_layout);

        openFragment(FeedFragment.newInstance("",""));
        // Opacity changes on Bottom Bar Icon depending on what page is selected
        AppCompatImageView iconHome = findViewById(R.id.iconHome);
        iconHome.setAlpha((float) 1.0);
        AppCompatImageView iconProfile = findViewById(R.id.iconProfile);
        iconProfile.setAlpha((float) 0.45);
    }

    // Go To Profile (NavBar Button)
    @SuppressLint("WrongConstant")
    public void goToProfile(View v) {

        // Call Profile TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.profile_action_bar_layout);

        openFragment(ProfileFragment.newInstance("",""));
        // Opacity changes on Bottom Bar Icon depending on what page is selected
        AppCompatImageView iconProfile = findViewById(R.id.iconProfile);
        iconProfile.setAlpha((float) 1.0);
        AppCompatImageView iconHome = findViewById(R.id.iconHome);
        iconHome.setAlpha((float) 0.45);
    }

    // Go To Settings (TopBar Button)
    @SuppressLint("WrongConstant")
    public void goToSettings(View v) {

        // Call Settings TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.settings_action_bar_layout);

        openFragment(SettingsFragment.newInstance("",""));
        // Opacity changes on Bottom Bar Icon depending on what page is selected
        AppCompatImageView iconProfile = findViewById(R.id.iconProfile);
        iconProfile.setAlpha((float) 1.0);
        AppCompatImageView iconHome = findViewById(R.id.iconHome);
        iconHome.setAlpha((float) 0.45);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);

        if (acct!= null) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String email = acct.getEmail().toString();

            db.collection(email).document(Userdata)
        }
    }

    // Utilitary method for opening fragments
    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}