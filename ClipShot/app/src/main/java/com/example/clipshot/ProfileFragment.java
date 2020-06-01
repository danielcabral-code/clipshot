package com.example.clipshot;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {
    private FirestoreRecyclerAdapter adapter;


    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Firebase variables
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference;

        // Google variable to detect the user that is signed
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(container.getContext());


        // Variables that will get the email and userId value from the user google account
        String email = acct.getEmail().toString();
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Storage reference to the user avatar image
        StorageReference storageReference  = FirebaseStorage.getInstance().getReference().child(email+"/"+userUid);

        // Interface variables
        View returnView = inflater.inflate(R.layout.fragment_profile, container, false);
        ImageView img = returnView.findViewById(R.id.image);
        TextView realName = returnView.findViewById(R.id.realName);
        TextView bio = returnView.findViewById(R.id.bio);
        TextView title = returnView.findViewById(R.id.gamifyTitle);
        AppCompatImageView steamIcon = returnView.findViewById(R.id.iconSteam);
        AppCompatImageView xboxIcon = returnView.findViewById(R.id.iconXbox);
        AppCompatImageView originIcon = returnView.findViewById(R.id.iconOrigin);
        AppCompatImageView psnIcon = returnView.findViewById(R.id.iconPsn);
        AppCompatImageView nintendoIcon = returnView.findViewById(R.id.iconNintendo);
        RecyclerView profileVideos = returnView.findViewById(R.id.recyclerView);

        FirebaseStorage imageStorage;

        db = FirebaseFirestore.getInstance();

        // Document reference of user data that will be read to the fields in profile
        documentReference = db.collection("users").document(userUid);

        long tStart = System.currentTimeMillis();
        // Load of data
        documentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String dataName = documentSnapshot.getString("Name");
                            String dataBio = documentSnapshot.getString("Bio");
                            String dataTitle = documentSnapshot.getString("GamifyTitle");
                            String steamName = documentSnapshot.getString("Steam");
                            String originName = documentSnapshot.getString("Origin");
                            String psnName = documentSnapshot.getString("Psn");
                            String xBoxName = documentSnapshot.getString("Xbox");
                            String nintendoName = documentSnapshot.getString("Nintendo");
                            realName.setText(dataName);
                            bio.setText(dataBio);
                            title.setText(dataTitle);

                            long tEnd = System.currentTimeMillis();
                            long tDelta = tEnd - tStart;
                            double elapsedSeconds = tDelta / 1000.0;
                            Log.d("TAG", String.valueOf(elapsedSeconds));

                            //If user has some nickname in some platform the opacity of that platform will be 1
                            if (!documentSnapshot.getString("Steam").equals("")) {

                                steamIcon.setAlpha((float) 1.0);
                            }
                            if (!documentSnapshot.getString("Origin").equals("")) {

                                originIcon.setAlpha((float) 1.0);
                            }
                            if (!documentSnapshot.getString("Psn").equals("")) {

                                psnIcon.setAlpha((float) 1.0);
                            }
                            if (!documentSnapshot.getString("Xbox").equals("")) {

                                xboxIcon.setAlpha((float) 1.0);
                            }
                            if (!documentSnapshot.getString("Nintendo").equals("")) {

                                nintendoIcon.setAlpha((float) 1.0);
                            }

                            //Listeners to show toast with platform id if user has some nickname introduced in database for that platform
                            steamIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!steamName.equals("")) {
                                        Toast.makeText(getContext(),"Steam ID: "+ steamName, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            originIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!originName.equals("")) {
                                        Toast.makeText(getContext(),"Origin ID: "+ originName, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            psnIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!psnName.equals("")) {
                                        Toast.makeText(getContext(),"PSN ID: " + psnName, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            xboxIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!xBoxName.equals("")) {
                                        Toast.makeText(getContext(),"Xbox ID: "+ xBoxName, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            nintendoIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!nintendoName.equals("")) {
                                        Toast.makeText(getContext(),"Switch ID: "+ nintendoName, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
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
                Uri personPhoto = acct.getPhotoUrl();
                Glide.with(container).load(String.valueOf(personPhoto)).into(img);
                Log.d("TAG", "onFailure: error "+ exception);
            }
        });

        Query query = db.collection("videos");

        FirestoreRecyclerOptions<ProfileVideos> options = new FirestoreRecyclerOptions.Builder<ProfileVideos>()
                .setQuery(query, ProfileVideos.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ProfileVideos, ProfileVideosHolder>(options) {
            @NonNull
            @Override
            public ProfileVideosHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_videos_layout,parent,false);
                return new ProfileVideosHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ProfileVideosHolder holder, int position, @NonNull ProfileVideos model) {

                holder.listDescription.setText(model.getDescription());
                //holder.listVideo = new VideoView(getContext());
                Uri uri = Uri.parse(model.getUrl());
                holder.listVideo.setVideoURI(uri);
                holder.listVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {

                        holder.listVideo.start();
                    }
                });

            }

        };

        // profileVideos.setHasFixedSize(true);
        profileVideos.setLayoutManager(new LinearLayoutManager(getContext()));
        profileVideos.setAdapter(adapter);
        profileVideos.setNestedScrollingEnabled(false);

        // Inflate the layout for this fragment
        return returnView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private class ProfileVideosHolder extends  RecyclerView.ViewHolder{
        private  TextView listDescription;
        private  VideoView listVideo;

        public ProfileVideosHolder(@NonNull View itemView) {
            super(itemView);

            listDescription=itemView.findViewById(R.id.videosDescription);
            listVideo=itemView.findViewById(R.id.videosFrame);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }





}