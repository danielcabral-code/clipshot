package com.example.clipshot;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.Objects;

public class UploadVideoActivity extends AppCompatActivity {

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        // Call Upload TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.upload_action_bar);

        //Get extras from Main Activity
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Uri myUri = Uri.parse(bundle.getString("video"));
            String id = bundle.getString("userID");
            Log.d("RES", "onCreate: " + myUri +"/" + id);
        }



    }
}
