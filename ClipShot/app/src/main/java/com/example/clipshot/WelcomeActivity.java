package com.example.clipshot;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.IOException;
import java.util.Objects;

public class WelcomeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE =1;
    private static final int PERMISSION_CODE =2;

    Uri imageUri;
    ImageView img;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Declaring interface components
        img = findViewById(R.id.image);
        EditText name = findViewById(R.id.realName);
        EditText username = findViewById(R.id.displayName);

        // (Yet to be used)
        EditText bio = findViewById(R.id.bio);
        EditText steamInput = findViewById(R.id.steamInput);
        EditText originInput = findViewById(R.id.originInput);
        EditText psnInput = findViewById(R.id.psnInput);
        EditText xboxInput = findViewById(R.id.xboxInput);
        EditText nintendoInput = findViewById(R.id.switchInput);
        AppCompatImageView iconHome = findViewById(R.id.iconDone);

        // Call TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.welcome_action_bar);
        // Set Elevation to Top Bar
        getSupportActionBar().setElevation(50f); // Float == px



        // Automatically fill avatar with Google Account Image and real name
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {

            String personName = acct.getDisplayName();

            Uri personPhoto = acct.getPhotoUrl();

            name.setText(personName);

            Glide.with(this).load(String.valueOf(personPhoto)).into(img);
        }

        // Listener that will check if username is not empty, if not the check button will appear and allow user go to feed page
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

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {

                    pickImageFromGallery();
                }
            }
        });

    }


     private void pickImageFromGallery(){

        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setType("image/*");
        startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {

            imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                img.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // Method to go to Main Feed
    public void goToMainFeed(View v){

        Intent goToFeed = new Intent(this,MainActivity.class);
        startActivity(goToFeed);
    }
}