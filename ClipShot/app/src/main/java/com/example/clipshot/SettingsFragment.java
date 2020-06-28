package com.example.clipshot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    // Global Variables
    private static final int PICK_IMAGE = 1;
    private int usernameChanged = 0;
    Uri imageUri;
    FirebaseStorage imageStorage;
    StorageReference storageReference;
    GoogleSignInAccount acct;
    String email;
    String userUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    ImageView profileImage;
    private String dataUsername,dataName,dataBio,steamName,originName,psnName,xBoxName,nintendoName,followers,following;
    String[] followersArray = new String[0];
    String[] followingArray = new String[0];
    AppCompatImageView iconDoneSettings;
    ProgressBar progressBar;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Calling Firebase Instances
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference;
        acct = GoogleSignIn.getLastSignedInAccount(container.getContext());

        // Calling XML variables
        View returnView = inflater.inflate(R.layout.fragment_settings, container, false);
        ImageView img = returnView.findViewById(R.id.image);
        EditText displayName = returnView.findViewById(R.id.displayName);
        EditText realName = returnView.findViewById(R.id.realName);
        EditText bio = returnView.findViewById(R.id.bio);
        EditText steam = returnView.findViewById(R.id.steamInput);
        EditText origin = returnView.findViewById(R.id.originInput);
        EditText psn = returnView.findViewById(R.id.psnInput);
        EditText xbox = returnView.findViewById(R.id.xboxInput);
        EditText nintendo = returnView.findViewById(R.id.switchInput);

        // Retrieving Firebase FireStore data
        String email = acct.getEmail();
        String userUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Storage reference to the user avatar image
        StorageReference storageReference  = FirebaseStorage.getInstance().getReference().child(email+"/"+userUid);

        // Gets user's data and displays in settingsFragment
        documentReference = db.collection("users").document(userUid);
        documentReference.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        dataUsername = documentSnapshot.getString("Username");
                        dataName = documentSnapshot.getString("Name");
                        dataBio = documentSnapshot.getString("Bio");
                        steamName = documentSnapshot.getString("Steam");
                        originName = documentSnapshot.getString("Origin");
                        psnName = documentSnapshot.getString("Psn");
                        xBoxName = documentSnapshot.getString("Xbox");
                        nintendoName = documentSnapshot.getString("Nintendo");
                        followers=documentSnapshot.getString("Followers");
                        following=documentSnapshot.getString("Following");

                        displayName.setText(dataUsername);
                        realName.setText(dataName);
                        bio.setText(dataBio);
                        steam.setText(steamName);
                        origin.setText(originName);
                        psn.setText(psnName);
                        xbox.setText(xBoxName);
                        nintendo.setText(nintendoName);

                    } else {
                        Log.d("TAG", "doesnt exist");
                    }
                }).addOnFailureListener(e -> Log.d("TAG", "onFailure:" + e));

        // Download uri from user image folder using the storageReference inicialized at top of document
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

            // Load the image using Glide
            Glide.with(container).load(uri).into(img);

        }).addOnFailureListener(exception -> {

            // Handle any errors
            Log.d("TAG", "onFailure: error "+ exception);
        });

        // Download uri from user image folder using the storageReference inicialized at top of document
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

            // Load the image using Glide
            Glide.with(container).load(uri).into(img);

        }).addOnFailureListener(exception -> {

            // When image can't load, app loads default avatar
            Glide.with(container).load(R.drawable.default_avatar).into(img);
        });

        // Inflate the layout for this fragment
        return returnView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Declaring variables to later check if they are altered in settings page
        profileImage = Objects.requireNonNull(getView()).findViewById(R.id.image);
        EditText displayName = Objects.requireNonNull(getActivity()).findViewById(R.id.displayName);
        EditText realName = getActivity().findViewById(R.id.realName);
        EditText bio = getActivity().findViewById(R.id.bio);
        EditText steamInput = getActivity().findViewById(R.id.steamInput);
        EditText originInput = getActivity().findViewById(R.id.originInput);
        EditText psnInput = getActivity().findViewById(R.id.psnInput);
        EditText xboxInput = getActivity().findViewById(R.id.xboxInput);
        EditText switchInput = getActivity().findViewById(R.id.switchInput);
        TextView errorUsername = getActivity().findViewById(R.id.labelErrorUsername);

        // Only allows user to input 3 lines into bio
        bio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // limit to 3 lines
                if (bio.getLayout().getLineCount() > 3)
                    bio.getText().delete(bio.getText().length() - 1, bio.getText().length());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        iconDoneSettings = Objects.requireNonNull(getActivity()).findViewById(R.id.iconDone);
        progressBar= Objects.requireNonNull(getActivity()).findViewById(R.id.progress_circular);

        // Listener to call method to pick an image from gallery
        profileImage.setOnClickListener(v -> pickImageFromGallery());

        // Button to accept changes and update FireStore
        iconDoneSettings.setOnClickListener(v -> {

            progressBar.setVisibility(View.VISIBLE);
            iconDoneSettings.setVisibility(View.INVISIBLE);

            String dataName, dataUsername,dataBio,dataSteam,dataOrigin,dataPsn,dataXbox,dataNintendo, email;
            FirebaseFirestore db;

            // Email gets the user google email that will create a collection with that email
            email = acct.getEmail();

            // Firestore instance
            db = FirebaseFirestore.getInstance();
            imageStorage = FirebaseStorage.getInstance();
            storageReference = imageStorage.getReference();

            // Declaring variables that will be inserted in Firestore
            dataUsername = displayName.getText().toString().toLowerCase();
            dataName = realName.getText().toString();
            dataBio = bio.getText().toString();
            dataSteam = steamInput.getText().toString();
            dataOrigin = originInput.getText().toString();
            dataPsn = psnInput.getText().toString();
            dataXbox = xboxInput.getText().toString();
            dataNintendo = switchInput.getText().toString();

            // Update FireStore
            CollectionReference usersRef = db.collection("users");

            Query query = usersRef.whereEqualTo("Username", dataUsername);

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                        String user = documentSnapshot.getString("Username");

                        assert user != null;
                        if (user.equals(dataUsername) && usernameChanged == 1) {

                            errorUsername.setVisibility(View.VISIBLE);
                        }

                        if (user.equals(dataUsername) && usernameChanged == 0) {

                            errorUsername.setVisibility(View.INVISIBLE);

                            db.collection("users").document(userUid).update("Username", dataUsername);
                            db.collection("users").document(userUid).update("Name", dataName);
                            db.collection("users").document(userUid).update("Bio", dataBio);
                            db.collection("users").document(userUid).update("Steam", dataSteam);
                            db.collection("users").document(userUid).update("Origin", dataOrigin);
                            db.collection("users").document(userUid).update("Psn", dataPsn);
                            db.collection("users").document(userUid).update("Xbox", dataXbox);
                            db.collection("users").document(userUid).update("Nintendo", dataNintendo);
                            db.collection("users").document(userUid).update("Email", email);
                            db.collection("users").document(userUid).update("UserUID", userUid);
                            db.collection("users").document(userUid).update("Followers", followers);
                            db.collection("users").document(userUid).update("Following", following);

                            // Call the method to upload image
                            uploadImage(email);

                            new Handler().postDelayed(() -> ((MainActivity) Objects.requireNonNull(getActivity())).goToProfile(view), 5000);
                        }
                    }

                    if (task.getResult().size() == 0) {

                        errorUsername.setVisibility(View.INVISIBLE);

                        db.collection("users").document(userUid).update("Username", dataUsername);
                        db.collection("users").document(userUid).update("Name", dataName);
                        db.collection("users").document(userUid).update("Bio", dataBio);
                        db.collection("users").document(userUid).update("Steam", dataSteam);
                        db.collection("users").document(userUid).update("Origin", dataOrigin);
                        db.collection("users").document(userUid).update("Psn", dataPsn);
                        db.collection("users").document(userUid).update("Xbox", dataXbox);
                        db.collection("users").document(userUid).update("Nintendo", dataNintendo);
                        db.collection("users").document(userUid).update("Email", email);
                        db.collection("users").document(userUid).update("UserUID", userUid);
                        db.collection("users").document(userUid).update("Followers", followers);
                        db.collection("users").document(userUid).update("Following", following);

                        // Call the method to upload image
                        uploadImage(email);

                        new Handler().postDelayed(() -> ((MainActivity) Objects.requireNonNull(getActivity())).goToProfile(view), 5000);
                    }
                }
            });
        });

        // Listener that will check if displayName has been altered, and makes checkIcon visible
        displayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().equals(dataUsername)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else {
                    iconDoneSettings.setVisibility(View.VISIBLE);
                    usernameChanged = 1;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if realName has been altered, and makes checkIcon visible
        realName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().equals(dataName)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if bio has been altered, and makes checkIcon visible
        bio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().equals(dataBio)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if steamInput has been altered, and makes checkIcon visible
        steamInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().equals(steamName)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if originInput has been altered, and makes checkIcon visible
        originInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().equals(originName)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if psnInput has been altered, and makes checkIcon visible
        psnInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().equals(psnName)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if xboxInput has been altered, and makes checkIcon visible
        xboxInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().equals(xBoxName)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if switchInput has been altered, and makes checkIcon visible
        switchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().equals(nintendoName)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Declaring sign out button from Settings fragment layout and applying OnClick listener to it
        Button signOutButton = view.findViewById(R.id.button_sign_out);
        signOutButton.setOnClickListener(this);
    }

    // Clicking on Logout button; the user disconnects from Firebase and goes to Login page
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button_sign_out) {

            FirebaseAuth.getInstance().signOut();

            Intent goToLogin = new Intent(getActivity(), LoginActivity.class);
            startActivity(goToLogin);
        }
    }

    // Method to go to gallery
    private void pickImageFromGallery(){

        Intent gallery = new Intent(Intent.ACTION_PICK);
        gallery.setType("image/*");
        startActivityForResult(Intent.createChooser(gallery, "Select Picture"), PICK_IMAGE);
    }

    // Fill welcome avatar image with another from gallery
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {

            imageUri = data.getData();
            iconDoneSettings.setVisibility(View.VISIBLE);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Create a folder in Firebase Storage with the user email and upload the image from gallery
    public void uploadImage(String email){

        if (imageUri != null){
            StorageReference ref = storageReference.child(email+"/" + userUid);
            ref.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                    });
        }
    }
}