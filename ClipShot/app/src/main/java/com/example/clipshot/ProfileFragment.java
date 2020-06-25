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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



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
        TextView numberOfVideos = returnView.findViewById(R.id.clipsNumber);
        TextView followingNumber = returnView.findViewById(R.id.followingNumber);
        TextView followersNumber = returnView.findViewById(R.id.followerNumber);
        AppCompatImageView steamIcon = returnView.findViewById(R.id.iconSteam);
        AppCompatImageView xboxIcon = returnView.findViewById(R.id.iconXbox);
        AppCompatImageView originIcon = returnView.findViewById(R.id.iconOrigin);
        AppCompatImageView psnIcon = returnView.findViewById(R.id.iconPsn);
        AppCompatImageView nintendoIcon = returnView.findViewById(R.id.iconNintendo);
        RecyclerView profileVideos = returnView.findViewById(R.id.recyclerView);


        //FirebaseStorage imageStorage;

        db = FirebaseFirestore.getInstance();

        db.collection("videos").whereEqualTo("UserID",userUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            countTotalVideos = 0;
                            for (DocumentSnapshot document : task.getResult()) {

                                countTotalVideos++;
                                Log.d("TAG", "onComplete: " + countTotalVideos);
                                Log.d("TAG", "onComplete: "+ task.getResult().toString());


                            }
                            numberOfVideos.setText(String.valueOf(countTotalVideos));

                            TextView noVideosMessage = Objects.requireNonNull(getActivity()).findViewById(R.id.noVideosMessage);
                            if (countTotalVideos == 0) {
                                noVideosMessage.setText("You haven't shared any Clips yet...");
                            }

                        } else {

                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }



                    }
                });



        Task<QuerySnapshot> querySnapshot = db.collection("videos").whereEqualTo("UserID",userUid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                int countTotalLikes =0;

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d("TAG", document.getId() + " => " + document.getData());


                    countTotalLikes = countTotalLikes + Integer.parseInt(document.getString("Likes"));

                    //db.collection("videos").document(document.getId()).update("UsersFollowers", FieldValue.arrayRemove(userUid));
                }
                Log.d("TAG", "Likes: " + countTotalLikes+ " " +  countTotalVideos);

                if (countTotalLikes>-1   && countTotalVideos>0 && countTotalVideos<10){
                    Log.d("TAG", "begginer: ");
                    db.collection("users").document(userUid).update("GamifyTitle","Beginner");
                }
                else if (countTotalLikes>99  && countTotalVideos>10 && countTotalVideos<24){
                    Log.d("TAG", "Rookie: ");
                    db.collection("users").document(userUid).update("GamifyTitle","Rookie");
                }
                else if (countTotalLikes>250 && countTotalVideos>24 && countTotalVideos<50){
                    Log.d("TAG", "Intermediate: ");
                    db.collection("users").document(userUid).update("GamifyTitle","Intermediate");
                }
                else if (countTotalLikes>500 && countTotalVideos>49 && countTotalVideos<100){
                    Log.d("TAG", "Trained: ");
                    db.collection("users").document(userUid).update("GamifyTitle","Trained");
                }
                else if (countTotalLikes>1000 && countTotalVideos>99 && countTotalVideos<150){
                    Log.d("TAG", "Gamer: ");
                    db.collection("users").document(userUid).update("GamifyTitle","Gamer");
                }
                else if (countTotalLikes>2000 && countTotalVideos>149 && countTotalVideos<200){
                    Log.d("TAG", "Expert: ");
                    db.collection("users").document(userUid).update("GamifyTitle","Expert");
                }
                else if (countTotalLikes>3000 && countTotalVideos>199 && countTotalVideos<300){
                    Log.d("TAG", "Veteran: ");
                    db.collection("users").document(userUid).update("GamifyTitle","Veteran");
                }
                else if (countTotalLikes>5000 && countTotalVideos>299 && countTotalVideos<400){
                    Log.d("TAG", "Legend: ");
                    db.collection("users").document(userUid).update("GamifyTitle","Legend");
                }
                else if (countTotalLikes>8000 && countTotalVideos>400){
                    Log.d("TAG", "Clip Master: ");
                    db.collection("users").document(userUid).update("GamifyTitle","ClipMaster");
                }


            }
        });

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
                            String dataFollowingNumber = documentSnapshot.getString("Following");
                            String dataFollowersNumber = documentSnapshot.getString("Followers");
                            realName.setText(dataName);
                            bio.setText(dataBio);
                            title.setText(dataTitle);
                            followingNumber.setText(dataFollowingNumber);
                            followersNumber.setText(dataFollowersNumber);


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
                Glide.with(getContext()).load(R.drawable.default_avatar).into(img);
                Log.d("TAG", "onFailure: error "+ exception);
            }
        });



        Query query = db.collection("videos").whereEqualTo("UserID",userUid).orderBy("ReleasedTime", Query.Direction.DESCENDING);
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
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // Load the image using Glide
                        Glide.with(container).load(uri).into(holder.listUserImage);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Glide.with(container).load(R.drawable.default_avatar).into(holder.listUserImage);
                        Log.d("TAG", "onFailure: error "+ exception);
                    }
                });


                documentReference = db.collection("users").document(model.getUserID());

                documentReference.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String dataUser = documentSnapshot.getString("Username");
                                    holder.listUsername.setText(dataUser);

                                }
                            }
                        });


                FirebaseFirestore.getInstance().collection("videos").document(model.getDocumentName()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        List<String> group = (List<String>) document.get("UsersThatLiked");
                        assert group != null;
                        if (group.contains(userUid)){
                            //Log.d("TAG", "onComplete: existe ");
                            holder.listLikesIcon.setImageResource(R.drawable.ic_explosion);
                            holder.listLikesIcon.setTag("liked");
                        }
                    }
                });

                holder.listDescription.setText(model.getDescription());
                holder.listGameName.setText(model.getGameName());
                holder.listLikes.setText(model.getLikes());
                holder.listLikesIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (holder.listLikesIcon.getTag().toString().equals("liked"))
                        {

                            String likesCount = (String) holder.listLikes.getText();
                            int likeDone= Integer.parseInt(likesCount)-1;
                            holder.listLikes.setText(String.valueOf(likeDone));
                            holder.listLikesIcon.setImageResource(R.drawable.ic_explosion_outline);
                            holder.listLikesIcon.setTag("noLike");



                            db.collection("videos").document(model.getDocumentName()).update("Likes",holder.listLikes.getText());
                            db.collection("videos").document(model.getDocumentName()).update("UsersThatLiked", FieldValue.arrayRemove(userUid));
                        }
                        else
                        {

                            String likesCount = (String) holder.listLikes.getText();
                            int likeDone= Integer.parseInt(likesCount)+1;
                            holder.listLikes.setText(String.valueOf(likeDone));
                            holder.listLikesIcon.setImageResource(R.drawable.ic_explosion);
                            holder.listLikesIcon.setTag("liked");


                            db.collection("videos").document(model.getDocumentName()).update("Likes",holder.listLikes.getText());
                            db.collection("videos").document(model.getDocumentName()).update("UsersThatLiked", FieldValue.arrayUnion(userUid));
                        }








                    }
                });

                holder.progressBar.setVisibility(View.VISIBLE);
                Uri uri = Uri.parse(model.getUrl());
                holder.listVideo.setVideoURI(uri);
                holder.listVideo.seekTo( 1);
                holder.listVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        holder.progressBar.setVisibility(View.INVISIBLE);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(0, 140, 0, 0);
                        holder.listVideo.setLayoutParams(layoutParams);
                        holder.listVideo.setBackgroundResource(0);

                        holder.listVideo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                holder.listVideo.start();
                            }
                        });

                        holder.listVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {

                                holder.listVideo.seekTo( 1);
                            }
                        });

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