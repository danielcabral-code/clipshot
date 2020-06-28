package com.example.clipshot;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jaredrummler.materialspinner.MaterialSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    // Global Variables
    Uri videoUri;
    StorageReference storageReference;
    String userUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    ArrayList gameNameArray = new ArrayList();
    public Timer timer = new Timer();
    final long DELAY = 1000; // milliseconds
    String myUrl;
    MaterialSpinner spinner;
    String game,name, email;
    int descriptionIsEmpty = 1;
    int gameNameIsEmpty = 1;
    AppCompatImageView iconDone;
    ProgressBar progressBar;
    TextView uploadLabel;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        assert acct != null;
        email= acct.getEmail();

        // Call Upload TopBar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.upload_action_bar);

        // AppCompatImageView iconDone = findViewById(R.id.iconDoneUpload);
        EditText description = findViewById(R.id.description);
        EditText gameName = findViewById(R.id.gameName);
        spinner = findViewById(R.id.spinner);
        VideoView videoSelected = findViewById(R.id.videoToBeUploaded);
        iconDone = findViewById(R.id.iconDoneUpload);
        progressBar=findViewById(R.id.progress_circular);

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy  HH:mm:ss");
        String formattedDate = df.format(c);
        Log.d("TAG", "onCreate: "+ formattedDate);

        // Get extras from Main Activity
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            videoUri = Uri.parse(bundle.getString("video"));
            String id = bundle.getString("userID");
            Log.d("RES", "onCreate: " + videoUri + "/" + id);
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        // Use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(this, Uri.parse(String.valueOf(videoUri)));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        retriever.release();

        if (timeInMillisec > 60000){

            Toast.makeText(this,"Your Clip exceeds the 60s max permitted!" , Toast.LENGTH_LONG).show();
            Intent goToFeed = new Intent(UploadVideoActivity.this,MainActivity.class);
            startActivity(goToFeed);
        }
        else {

            videoSelected.setVideoURI(videoUri);
            videoSelected.setOnPreparedListener(mp -> {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 20, 0, 0);
                videoSelected.setLayoutParams(layoutParams);
                videoSelected.start();
            });
            videoSelected.setOnCompletionListener(mp -> videoSelected.seekTo(1));

            // Listener that will check if username is not empty, if not the check button will appear and allow user go to feed page
            description.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d("TAG", "onTextChanged: mudou");

                    if (s.toString().trim().length() ==0 ){

                        descriptionIsEmpty = 1;


                    }
                    else descriptionIsEmpty = 0;

                    // limit to 3 lines
                    if (description.getLayout().getLineCount() > 3)
                        description.getText().delete(description.getText().length() - 1, description.getText().length());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    showIconDone();
                }
            });

            iconDone.setOnClickListener(v -> {
                progressBar.setVisibility(View.VISIBLE);
                iconDone.setVisibility(View.INVISIBLE);
                uploadVideo();
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

            spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

                @Override
                public void onItemSelected(MaterialSpinner view, int position, long id, String item) {

                    game = item;
                    gameName.setText(item);
                    gameNameIsEmpty=0;
                    showIconDone();
                }
            });
        }
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
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                        final String[][] arrayFollowers = {new String[0]};

                        db.collection("videos").whereEqualTo("UserID", userUid).get().addOnCompleteListener(task -> {

                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                List<String> group = (List<String>) document.get("UsersFollowers");

                                assert group != null;
                                arrayFollowers[0] = group.toArray(new String[0]);
                            }

                            Date date = new Date();

                            // Map that will fill our database with values
                            Map<String, Object> Userdata = new HashMap<>();
                            Userdata.put("GameName", game);
                            Userdata.put("Description", videoDescription);
                            Userdata.put("UserID", userUid);
                            Userdata.put("Url", uri.toString());
                            Userdata.put("Likes","0");
                            String[] arrayLikes = new String[0];
                            Userdata.put("UsersThatLiked", Arrays.asList(arrayLikes));
                            Userdata.put("UsersFollowers", Arrays.asList(arrayFollowers[0]));
                            Userdata.put("ReleasedTime", new Timestamp(date.getTime()).toString());
                            Userdata.put("DocumentName",randomUUID);
                            Userdata.put ("Email", email);

                            db.collection("videos").document(randomUUID).set(Userdata);
                            Intent goToFeed = new Intent(UploadVideoActivity.this, MainActivity.class);
                            startActivity(goToFeed);
                        });
                    }));
        }
    }

    @SuppressLint("StaticFieldLeak")
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

                    if (!gameNameArray.contains(name)) {
                        gameNameArray.add(name);
                    }
                }

                String[] array = new String[gameNameArray.size()];
                array = (String[]) gameNameArray.toArray(array);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(UploadVideoActivity.this, R.layout.spinner_layout, array);
                spinner.setAdapter(adapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void showIconDone(){

        if (descriptionIsEmpty == 0 && gameNameIsEmpty == 0) {

            iconDone.setVisibility(View.VISIBLE);

        } else {

            iconDone.setVisibility(View.INVISIBLE);
        }
    }
}