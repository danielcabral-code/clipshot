package com.example.clipshot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.autofill.UserData;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.Extension;
import java.sql.Array;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class UploadVideoActivity extends AppCompatActivity {

    Uri videoUri;
    StorageReference storageReference;
    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String name;
    ArrayList gameNameArray = new ArrayList();
    public Timer timer = new Timer();
    final long DELAY = 1000; // milliseconds
    String myUrl;
    MaterialSpinner spinner;
    String game;


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
        EditText gameName = findViewById(R.id.gameName);
        spinner = findViewById(R.id.spinner);

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy  HH:mm:ss");
        String formattedDate = df.format(c);
        Log.d("TAG", "onCreate: "+ formattedDate);


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

        // Listener that will check if username is not empty, if not the check button will appear and allow user go to feed page
        gameName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                Uri.Builder builder = new Uri.Builder();
                                builder.scheme("https")
                                        .authority("api.rawg.io")
                                        .appendPath("api")
                                        .appendPath("games")
                                        .appendQueryParameter("ordering", "-rating")
                                        .appendQueryParameter("search", s.toString());
                                Log.d("TAG", "afterTextChanged: " + s.toString());


                                myUrl = builder.build().toString();
                                new GetIp().execute(myUrl);
                            }
                        },
                        DELAY
                );


            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });
        /*spinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "onClick: "+ spinner.getSelectedIndex());
            }
        });*/

        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                Log.d("TAG", "onItemSelected: " + item);
                game = item;
                gameName.setText(item);
            }
        });


    }

    public void uploadVideo() {

        if (videoUri != null) {

            // Firestore instance
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            EditText descrtiption = findViewById(R.id.description);
            // Declaring variables that will be inserted in Firestore
            String videoDescription = descrtiption.getText().toString();

            String randomUUID = UUID.randomUUID().toString();


            // Add video to folder videos in Firebase Storage and after upload it create a file in database with video details
            storageReference = FirebaseStorage.getInstance().getReference("videos/" + randomUUID);
            storageReference.putFile(videoUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Date date = new Date();
                                    // Map that will fill our database with values
                                    Map<String, Object> Userdata = new HashMap<>();
                                    Userdata.put("GameName", game);
                                    Userdata.put("Description", videoDescription);
                                    Userdata.put("UserID", userUid);
                                    Userdata.put("Url", uri.toString());
                                    Userdata.put("Likes","0");
                                    String[] array = new String[0];
                                    Userdata.put("UsersThatLiked", Arrays.asList(array));
                                    Userdata.put("ReleasedTime", new Timestamp(date.getTime()).toString());
                                    Userdata.put("DocumentName",randomUUID);


                                    db.collection("videos").document(randomUUID).set(Userdata);
                                    Intent goToFeed = new Intent(UploadVideoActivity.this, MainActivity.class);
                                    startActivity(goToFeed);

                                }
                            });


                        }
                    });
        }


    }

    class GetIp extends AsyncTask<String, String, String> {
        @Override

        protected String doInBackground(String... fileUrl) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                URL url = new URL(fileUrl[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.connect();
                InputStream in = connection.getInputStream();

                stringBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

            } catch (Exception e) {
                Log.e("ERROR", "onCreate" + e);
            }
            return stringBuilder.toString();


        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            gameNameArray.clear();


            try {
                JSONObject jsonResponse = new JSONObject(result);
                JSONArray results = jsonResponse.getJSONArray("results");


                for (int i = 0; i < results.length(); i++) {
                    JSONObject c = results.getJSONObject(i);
                    name = c.getString("name");
/*
                double rating =c.getDouble("rating");
                if (rating >= 3) {

                }
*/

                    if (!gameNameArray.contains(name)) {
                        gameNameArray.add(name);

                    }
                }
                Log.d("TAG", "onPostExecute: " + gameNameArray);

                String[] array = new String[gameNameArray.size()];
                array = (String[]) gameNameArray.toArray(array);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(UploadVideoActivity.this, R.layout.spinner_layout, array);
                spinner.setAdapter(adapter);


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }




    }


}