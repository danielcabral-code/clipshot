package com.example.clipshot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class UploadVideoActivity extends AppCompatActivity {

    Uri videoUri;
    StorageReference storageReference;
    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String name;
    ArrayList gameName = new ArrayList();

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        // Call Upload TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.upload_action_bar);

        AppCompatImageView iconDone = findViewById(R.id.iconDone);
        EditText descrtiption = findViewById(R.id.description);

        for (int i = 1; i <= 19838 ; i++) {
            new GetIp().execute("https://api.rawg.io/api/games?page="+i);

        }


        //Get extras from Main Activity
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            videoUri = Uri.parse(bundle.getString("video"));
            String id = bundle.getString("userID");
            Log.d("RES", "onCreate: " + videoUri + "/" + id);
        }

        iconDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVideo();
            }
        });


    }

    public void uploadVideo(){

        if (videoUri != null) {

            // Firestore instance
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            EditText descrtiption = findViewById(R.id.description);
            // Declaring variables that will be inserted in Firestore
            String videoDescription = descrtiption.getText().toString();

            String randomUUID = UUID.randomUUID().toString();

            // Map that will fill our database with values
            Map<String,String> Userdata = new HashMap<>();
            Userdata.put("Description",videoDescription);
            Userdata.put("UserID",userUid);
            Userdata.put("Url", randomUUID);

            // On success data is inserted in database and user go to MainActivity
            db.collection("videos").document(randomUUID).set(Userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    storageReference = FirebaseStorage.getInstance().getReference("videos/" + randomUUID);
                    storageReference.putFile(videoUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.d("TAG", "onSuccess: uploaded");

                                }
                            });
                }
            });


        }
    }
    public class GetIp extends AsyncTask<String,String,String> {
        @Override

        protected  String doInBackground(String ... fileUrl){
            StringBuilder stringBuilder = new StringBuilder();
            try {
                URL url = new URL(fileUrl[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.connect();
                InputStream in = connection.getInputStream();

                stringBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine())!=null){
                    stringBuilder.append(line);
                }

            }
            catch (Exception e){
                Log.e("ERROR", "onCreate" + e );
            }
            return stringBuilder.toString();


        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);


                    try {
                        JSONObject jsonResponse = new JSONObject(result);
                        JSONArray results = jsonResponse.getJSONArray("results");

                        //Log.d("TAG", "onPostExecute: " + results.toString());




                        for (int i = 0; i < results.length(); i++) {
                            JSONObject c = results.getJSONObject(i);

                            double rating =c.getDouble("rating");
                            if (rating>=4) {
                                 name = c.getString("name");

                            }
                            if (!gameName.contains(name)) {
                                gameName.add(name);
                            }
                        }
                        Log.d("TAG", "onPostExecute: "+ gameName);


        } catch (JSONException e) {
                        e.printStackTrace();
                    }
        }
    }
}
