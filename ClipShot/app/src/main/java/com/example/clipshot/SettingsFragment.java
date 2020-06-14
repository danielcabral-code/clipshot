package com.example.clipshot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private static final int PICK_IMAGE = 1;

    Uri imageUri;
    FirebaseStorage imageStorage;
    StorageReference storageReference;
    GoogleSignInAccount acct;
    String email;
    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    ImageView profileImage;
    private String dataUsername;
    private String dataName;
    private String dataBio;
    private String steamName;
    private String originName;
    private String psnName;
    private String xBoxName;
    private String nintendoName;
    AppCompatImageView iconDoneSettings;

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
        String email = acct.getEmail().toString();
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Storage reference to the user avatar image
        StorageReference storageReference  = FirebaseStorage.getInstance().getReference().child(email+"/"+userUid);

        documentReference = db.collection("users").document(userUid);

        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {

                            dataUsername = documentSnapshot.getString("Username");
                            dataName = documentSnapshot.getString("Name");
                            dataBio = documentSnapshot.getString("Bio");
                            steamName = documentSnapshot.getString("Steam");
                            originName = documentSnapshot.getString("Origin");
                            psnName = documentSnapshot.getString("Psn");
                            xBoxName = documentSnapshot.getString("Xbox");
                            nintendoName = documentSnapshot.getString("Nintendo");

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
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "onFailure:" + e);
            }
        });

        // Download uri from user image folder using the storageReference inicialized at top of document
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                // Load the image using Glide
                Glide.with(container).load(uri).into(img);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.d("TAG", "onFailure: error "+ exception);
            }
        });

        // Download uri from user image folder using the storageReference inicialized at top of document
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                // Load the image using Glide
                Glide.with(container).load(uri).into(img);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Uri personPhoto = acct.getPhotoUrl();
                Glide.with(container).load(String.valueOf(personPhoto)).into(img);
                Log.d("TAG", "onFailure: error "+ exception);
            }
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

        iconDoneSettings = Objects.requireNonNull(getActivity()).findViewById(R.id.iconDone);

        // Listener to call method to pick an image from gallery
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pickImageFromGallery();
            }
        });

        iconDoneSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String dataName, dataUsername,dataBio,dataSteam,dataOrigin,dataPsn,dataXbox,dataNintendo,dataGamifyTitle, email;
                FirebaseFirestore db;

                // Email gets the user google email that will create a collection with that email
                email = acct.getEmail().toString();

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
                dataGamifyTitle = "Expert";

                CollectionReference usersRef = db.collection("users");
                Query query = usersRef.whereEqualTo("Username", dataUsername);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                       if (task.isSuccessful()) {
                           for (DocumentSnapshot documentSnapshot : task.getResult()) {
                               String user = documentSnapshot.getString("Username");

                               if (user.equals(dataUsername)) {
                                   Log.d("TAG", "User Exists");

                                   errorUsername.setVisibility(View.VISIBLE);

                               }
                           }
                       }
                       if (task.getResult().size() == 0) {
                           Log.d("TAG", "User not Exists");
                           errorUsername.setVisibility(View.INVISIBLE);

                           // Map that will fill our database with values
                           Map<String, String> Userdata = new HashMap<>();
                           Userdata.put("Username", dataUsername);
                           Userdata.put("Name", dataName);
                           Userdata.put("Bio", dataBio);
                           Userdata.put("Steam", dataSteam);
                           Userdata.put("Origin", dataOrigin);
                           Userdata.put("Psn", dataPsn);
                           Userdata.put("Xbox", dataXbox);
                           Userdata.put("Nintendo", dataNintendo);
                           Userdata.put("GamifyTitle", dataGamifyTitle);

                           // Call the method to upload image
                           uploadImage(email);

                           // On success data is inserted in database and user go to MainActivity
                           db.collection("users").document(userUid).set(Userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {
                                   ((MainActivity) Objects.requireNonNull(getActivity())).goToProfile(view);
                               }
                           });
                       }
                   }

                });



               /* // Map that will fill our database with values
                Map<String,String> Userdata = new HashMap<>();
                Userdata.put("Username",dataUsername);
                Userdata.put("Name", dataName);
                Userdata.put("Bio", dataBio);
                Userdata.put("Steam", dataSteam);
                Userdata.put("Origin", dataOrigin);
                Userdata.put("Psn", dataPsn);
                Userdata.put("Xbox", dataXbox);
                Userdata.put("Nintendo", dataNintendo);
                Userdata.put("GamifyTitle",dataGamifyTitle);

                // Call the method to upload image
                uploadImage(email);

                // On success data is inserted in database and user go to MainActivity
                db.collection("users").document(userUid).set(Userdata).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        ((MainActivity) Objects.requireNonNull(getActivity())).goToProfile(view);
                    }
                });*/
            }
        });

        // Listener that will check if displayName has been altered, and make check icon visible is so
        displayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().equals(dataUsername)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if realName has been altered, and make check icon visible is so
        realName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().equals(dataName)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if bio has been altered, and make check icon visible is so
        bio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().equals(dataBio)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if steamInput has been altered, and make check icon visible is so
        steamInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().equals(steamName)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if originInput has been altered, and make check icon visible is so
        originInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().equals(originName)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if psnInput has been altered, and make check icon visible is so
        psnInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().equals(psnName)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if xboxInput has been altered, and make check icon visible is so
        xboxInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().equals(xBoxName)) {
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if switchInput has been altered, and make check icon visible is so
        switchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

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
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });
        }
    }
}