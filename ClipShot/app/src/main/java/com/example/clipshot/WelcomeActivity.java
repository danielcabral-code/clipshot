package com.example.clipshot;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.Objects;

public class WelcomeActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Call TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.welcome_action_bar);

        ImageView img = findViewById(R.id.image);
        EditText name = findViewById(R.id.realName);
        //Button btn = findViewById(R.id.btn);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        Object mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //Listener do botao signOut que vai ser transferido para outra página
        /*btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    // ...
                    case R.id.signOut:
                        signOut();
                        break;
                }
            }
        });*/
        

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {

            String personName = acct.getDisplayName();

            Uri personPhoto = acct.getPhotoUrl();

            name.setText(personName);


            Glide.with(this).load(String.valueOf(personPhoto)).into(img);
        }
    }

    public void goToFeeed(View v){

        Intent goToFeed = new Intent(this,MainActivity.class);
        startActivity(goToFeed);

    }



    //função signOut para ser transferida para outra página
   /* private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("TAG", "onComplete: disconnected");
                        finish();

                    }
                });
*/
    }







