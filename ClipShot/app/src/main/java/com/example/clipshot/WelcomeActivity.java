package com.example.clipshot;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class WelcomeActivity extends AppCompatActivity {


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Call TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.welcome_action_bar);

        //Declaring interface components
        ImageView img = findViewById(R.id.image);
        EditText name = findViewById(R.id.realName);
        EditText username = findViewById(R.id.displayName);
        EditText bio = findViewById(R.id.bio);
        EditText steamInput = findViewById(R.id.steamInput);
        EditText originInput = findViewById(R.id.originInput);
        EditText psnInput = findViewById(R.id.psnInput);
        EditText xboxInput = findViewById(R.id.xboxInput);
        EditText nintendoInput = findViewById(R.id.switchInput);
        AppCompatImageView iconHome = findViewById(R.id.iconDone);

        //Automatically fill avatar with Google Account Image and real name
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {

            String personName = acct.getDisplayName();

            Uri personPhoto = acct.getPhotoUrl();

            name.setText(personName);


            Glide.with(this).load(String.valueOf(personPhoto)).into(img);
        }

        //Listener that will check if username is not empty, if not the check button will appear and allow user go to feed page
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().length()==0){

                    iconHome.setVisibility(View.INVISIBLE);
                }
                else iconHome.setVisibility(View.VISIBLE);

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });

    }

    //Method to go to main feed
    public void goToMainFeed(View v){

        Intent goToFeed = new Intent(this,MainActivity.class);
        startActivity(goToFeed);

    }

}







