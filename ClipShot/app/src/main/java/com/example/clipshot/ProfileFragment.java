package com.example.clipshot;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    // Global Variables
    private FirestorePagingAdapter adapter;
    private FirebaseFirestore db;
    private DocumentReference documentReference;
    int countTotalVideos;

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

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Google variable to detect the user that is signed
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(container.getContext());

        // Variables that will get the email and userId value from the user google account
        assert acct != null;
        String email = acct.getEmail();
        String userUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Storage reference to the user avatar image
        StorageReference storageReference  = FirebaseStorage.getInstance().getReference().child(email+"/"+userUid);

        // Interface variables
        View returnView = inflater.inflate(R.layout.fragment_profile, container, false);
        ImageView img = returnView.findViewById(R.id.image);
        TextView realName = returnView.findViewById(R.id.realName);
        TextView bio = returnView.findViewById(R.id.bio);
        TextView title = returnView.findViewById(R.id.gamifyTitle);
        TextView numberOfVideos = returnView.findViewById(R.id.clipsNumber);
        TextView followingNumber = returnView.findViewById(R.id.followingNumber);
        TextView followersNumber = returnView.findViewById(R.id.followerNumber);
        AppCompatImageView steamIcon = returnView.findViewById(R.id.iconSteam);
        AppCompatImageView xboxIcon = returnView.findViewById(R.id.iconXbox);
        AppCompatImageView originIcon = returnView.findViewById(R.id.iconOrigin);
        AppCompatImageView psnIcon = returnView.findViewById(R.id.iconPsn);
        AppCompatImageView nintendoIcon = returnView.findViewById(R.id.iconNintendo);
        RecyclerView profileVideos = returnView.findViewById(R.id.recyclerView);

        // FireStore Instance
        db = FirebaseFirestore.getInstance();

        // Gets user's videos
        db.collection("videos").whereEqualTo("UserID",userUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        countTotalVideos = 0;

                        for (DocumentSnapshot ignored : Objects.requireNonNull(task.getResult())) {

                            countTotalVideos++;
                        }
                        numberOfVideos.setText(String.valueOf(countTotalVideos));

                        // If there are no videos, shows message to user
                        TextView noVideosMessage = Objects.requireNonNull(getActivity()).findViewById(R.id.noVideosMessage);

                        if (countTotalVideos == 0) {
                            noVideosMessage.setText("You haven't shared any Clips yet...");
                        }
                    }
                });

        // Gamification System (checks amount of user videos uploaded and likes recieved)
        db.collection("videos").whereEqualTo("UserID",userUid).get().addOnCompleteListener(task -> {
            int countTotalLikes =0;

            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                countTotalLikes = countTotalLikes + Integer.parseInt(Objects.requireNonNull(document.getString("Likes")));
            }

            if (countTotalLikes>-1   && countTotalVideos>0 && countTotalVideos<10){
                db.collection("users").document(userUid).update("GamifyTitle","Beginner");

            } else if (countTotalLikes>99  && countTotalVideos>9 && countTotalVideos<25){
                db.collection("users").document(userUid).update("GamifyTitle","Rookie");

            } else if (countTotalLikes>249 && countTotalVideos>24 && countTotalVideos<50){
                db.collection("users").document(userUid).update("GamifyTitle","Intermediate");

            } else if (countTotalLikes>499 && countTotalVideos>49 && countTotalVideos<100){
                db.collection("users").document(userUid).update("GamifyTitle","Trained");

            } else if (countTotalLikes>999 && countTotalVideos>99 && countTotalVideos<150){
                db.collection("users").document(userUid).update("GamifyTitle","Gamer");

            } else if (countTotalLikes>1999 && countTotalVideos>149 && countTotalVideos<200){
                db.collection("users").document(userUid).update("GamifyTitle","Expert");

            } else if (countTotalLikes>2999 && countTotalVideos>199 && countTotalVideos<300){
                db.collection("users").document(userUid).update("GamifyTitle","Veteran");

            } else if (countTotalLikes>4999 && countTotalVideos>299 && countTotalVideos<400){
                db.collection("users").document(userUid).update("GamifyTitle","Legend");

            } else if (countTotalLikes>7999 && countTotalVideos>399){
                db.collection("users").document(userUid).update("GamifyTitle","ClipMaster");
            }
        });

        // Document reference of user data that will be read to the fields in profile
        documentReference = db.collection("users").document(userUid);

        // Load of data
        documentReference.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String dataName = documentSnapshot.getString("Name");
                        String dataBio = documentSnapshot.getString("Bio");
                        String dataTitle = documentSnapshot.getString("GamifyTitle");
                        String steamName = documentSnapshot.getString("Steam");
                        String originName = documentSnapshot.getString("Origin");
                        String psnName = documentSnapshot.getString("Psn");
                        String xBoxName = documentSnapshot.getString("Xbox");
                        String nintendoName = documentSnapshot.getString("Nintendo");
                        String dataFollowingNumber = documentSnapshot.getString("Following");
                        String dataFollowersNumber = documentSnapshot.getString("Followers");
                        realName.setText(dataName);
                        bio.setText(dataBio);
                        title.setText(dataTitle);
                        followingNumber.setText(dataFollowingNumber);
                        followersNumber.setText(dataFollowersNumber);

                        // If user has some nickname in some platform the opacity of that platform will be 1
                        if (!Objects.equals(documentSnapshot.getString("Steam"), "")) {

                            steamIcon.setAlpha((float) 1.0);
                        }
                        if (!Objects.equals(documentSnapshot.getString("Origin"), "")) {

                            originIcon.setAlpha((float) 1.0);
                        }
                        if (!Objects.equals(documentSnapshot.getString("Psn"), "")) {

                            psnIcon.setAlpha((float) 1.0);
                        }
                        if (!Objects.equals(documentSnapshot.getString("Xbox"), "")) {

                            xboxIcon.setAlpha((float) 1.0);
                        }
                        if (!Objects.equals(documentSnapshot.getString("Nintendo"), "")) {

                            nintendoIcon.setAlpha((float) 1.0);
                        }

                        // Listeners to show toast with platform id if user has some nickname introduced in database for that platform
                        steamIcon.setOnClickListener(v -> {
                            assert steamName != null;
                            if (!steamName.equals("")) {
                                Toast.makeText(getContext(),"Steam ID: "+ steamName, Toast.LENGTH_SHORT).show();
                            }
                        });

                        originIcon.setOnClickListener(v -> {
                            assert originName != null;
                            if (!originName.equals("")) {
                                Toast.makeText(getContext(),"Origin ID: "+ originName, Toast.LENGTH_SHORT).show();
                            }
                        });

                        psnIcon.setOnClickListener(v -> {
                            assert psnName != null;
                            if (!psnName.equals("")) {
                                Toast.makeText(getContext(),"PSN ID: " + psnName, Toast.LENGTH_SHORT).show();
                            }
                        });

                        xboxIcon.setOnClickListener(v -> {
                            assert xBoxName != null;
                            if (!xBoxName.equals("")) {
                                Toast.makeText(getContext(),"Xbox ID: "+ xBoxName, Toast.LENGTH_SHORT).show();
                            }
                        });

                        nintendoIcon.setOnClickListener(v -> {
                            assert nintendoName != null;
                            if (!nintendoName.equals("")) {
                                Toast.makeText(getContext(),"Switch ID: "+ nintendoName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(e -> Log.d("TAG", "onFailure:" + e));

        // Download uri from user image folder using the storageReference inicialized at top of document
        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

            // Load the image using Glide
            Glide.with(container).load(uri).into(img);

        }).addOnFailureListener(exception -> {

            // When image can't load, app loads default avatar
            Glide.with(Objects.requireNonNull(getContext())).load(R.drawable.default_avatar).into(img);
        });

        // Query to order users videos by released time
        Query query = db.collection("videos").whereEqualTo("UserID",userUid).orderBy("ReleasedTime", Query.Direction.DESCENDING);

        // Configuration for RecyclerView adapter
        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<ProfileVideos> options = new FirestorePagingOptions.Builder<ProfileVideos>()
                .setQuery(query,config, ProfileVideos.class)
                .build();

        adapter = new FirestorePagingAdapter<ProfileVideos, ProfileVideosHolder>(options) {
            @NonNull
            @Override
            public ProfileVideosHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_videos_layout,parent,false);
                return new ProfileVideosHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ProfileVideosHolder holder, int position, @NonNull ProfileVideos model) {

                // Download uri from user image folder using the storageReference inicialized at top of document
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                    // Load the image using Glide
                    Glide.with(container).load(uri).into(holder.listUserImage);

                }).addOnFailureListener(exception -> {

                    // When image can't load, app loads default avatar
                    Glide.with(container).load(R.drawable.default_avatar).into(holder.listUserImage);
                });

                // Gets username
                documentReference = db.collection("users").document(model.getUserID());
                documentReference.get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {

                                // Places username
                                String dataUser = documentSnapshot.getString("Username");
                                holder.listUsername.setText(dataUser);
                            }
                        });

                // Gets and places likes on user's videos
                FirebaseFirestore.getInstance().collection("videos").document(model.getDocumentName()).get().addOnCompleteListener(task -> {

                    DocumentSnapshot document = task.getResult();
                    List<String> group = (List<String>) document.get("UsersThatLiked");

                    assert group != null;
                    if (group.contains(userUid)){

                        holder.listLikesIcon.setImageResource(R.drawable.ic_explosion);
                        holder.listLikesIcon.setTag("liked");
                    }
                });

                // Places text in holder (video descripion, gameName, Likes) for RecyclerView
                holder.listDescription.setText(model.getDescription());
                holder.listLikes.setText(model.getLikes());

                if (model.getGameName().length() > 34) {

                    holder.listGameName.setText(model.getGameName().substring(0, 34) + "...");
                } else {

                    holder.listGameName.setText(model.getGameName());
                }

                // Allows user to like / dislike and changes value of like in DB
                holder.listLikesIcon.setOnClickListener(v -> {

                    if (holder.listLikesIcon.getTag().toString().equals("liked")) {

                        String likesCount = (String) holder.listLikes.getText();
                        int likeDone= Integer.parseInt(likesCount)-1;
                        holder.listLikes.setText(String.valueOf(likeDone));
                        holder.listLikesIcon.setImageResource(R.drawable.ic_explosion_outline);
                        holder.listLikesIcon.setTag("noLike");

                        db.collection("videos").document(model.getDocumentName()).update("Likes",holder.listLikes.getText());
                        db.collection("videos").document(model.getDocumentName()).update("UsersThatLiked", FieldValue.arrayRemove(userUid));
                    } else {

                        String likesCount = (String) holder.listLikes.getText();
                        int likeDone= Integer.parseInt(likesCount)+1;
                        holder.listLikes.setText(String.valueOf(likeDone));
                        holder.listLikesIcon.setImageResource(R.drawable.ic_explosion);
                        holder.listLikesIcon.setTag("liked");

                        db.collection("videos").document(model.getDocumentName()).update("Likes",holder.listLikes.getText());
                        db.collection("videos").document(model.getDocumentName()).update("UsersThatLiked", FieldValue.arrayUnion(userUid));
                    }
                });

                // Places Google's loading circle
                holder.progressBar.setVisibility(View.VISIBLE);

                // Gets and loads video in RecyclerView
                Uri uri = Uri.parse(model.getUrl());
                holder.listVideo.setVideoURI(uri);
                holder.listVideo.seekTo( 1);
                holder.listVideo.setOnPreparedListener(mp -> {

                    holder.progressBar.setVisibility(View.INVISIBLE);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 140, 0, 0);
                    holder.listVideo.setLayoutParams(layoutParams);
                    holder.listVideo.setBackgroundResource(0);

                    // Onclick that visually starts video for user
                    holder.listVideo.setOnClickListener(v -> holder.listVideo.start());

                    // Gives each video a "thumbnail" (always the 1st frame of video)
                    holder.listVideo.setOnCompletionListener(mp1 -> holder.listVideo.seekTo( 1));
                });
            }
        };

        // Set the adapter the the RecyclerView
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

    // profile_videos_layout Variables
    private static class ProfileVideosHolder extends  RecyclerView.ViewHolder{

        private  ImageView listUserImage;
        private  TextView listGameName;
        private  TextView listDescription;
        private  VideoView listVideo;
        private  TextView listUsername;
        private  TextView listLikes;
        private  ImageView listLikesIcon;
        ProgressBar progressBar;

        public ProfileVideosHolder(@NonNull View itemView) {
            super(itemView);

            listUsername =itemView.findViewById(R.id.videosUsername);
            listGameName =itemView.findViewById(R.id.videosGameName);
            listDescription=itemView.findViewById(R.id.videosDescription);
            listVideo=itemView.findViewById(R.id.videosFrame);
            listUserImage= itemView.findViewById(R.id.videosImage);
            listLikes=itemView.findViewById(R.id.videosLikes);
            listLikesIcon=itemView.findViewById(R.id.videosLikeIcon);
            progressBar =itemView.findViewById(R.id.progress_circular);
        }
    }

    // Allows adapter to start and stop recieving data
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