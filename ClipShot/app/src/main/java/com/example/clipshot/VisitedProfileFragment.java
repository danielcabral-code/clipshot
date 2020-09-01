package com.example.clipshot;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
import com.google.firebase.firestore.CollectionReference;
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

public class VisitedProfileFragment extends Fragment {

    // Global Variables
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String pickedProfile, docID, visitedEmail;
    private FirestorePagingAdapter adapter;
    private DocumentReference documentReference;
    StorageReference storageReference;
    int count;
    int countTotalVideos;
    String userUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    public VisitedProfileFragment() {
        // Required empty public constructor
    }

    public static VisitedProfileFragment newInstance(String param1, String param2) {
        VisitedProfileFragment fragment = new VisitedProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Calls TopBar
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setElevation(20f); // Float == px
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setCustomView(R.layout.visited_profile_action_bar_layout);

        View returnView = inflater.inflate(R.layout.fragment_visited_profile, container, false);
        RecyclerView visitedVideos = returnView.findViewById(R.id.recyclerView);

        // FireStore Instance
        db = FirebaseFirestore.getInstance();

        // Get extras from bundle that will be used to get the visted user
        Bundle bundle = this.getArguments();
        assert bundle != null;
        pickedProfile = bundle.getString("pickedProfile");
        docID = bundle.getString("docID");
        visitedEmail = bundle.getString("email");
        assert pickedProfile != null;

        // Query to get all videos where the user id equals the variable from bundle and order the videos from the most recent to older
        Query queryVideo = db.collection("videos").whereEqualTo("UserID", docID).orderBy("ReleasedTime", Query.Direction.DESCENDING);
        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        // Applying query to options of FirestorePagingAdapter
        FirestorePagingOptions<VisitedProfileVideos> options = new FirestorePagingOptions.Builder<VisitedProfileVideos>()
                .setQuery(queryVideo, config, VisitedProfileVideos.class)
                .build();

        // Setting the options applied before to FirestorePagingAdapter
        adapter = new FirestorePagingAdapter<VisitedProfileVideos, VisitedProfileHolder>(options) {
            @NonNull
            @Override
            public VisitedProfileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                //Getting the layout where adapter will fill data
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.visited_profile_videos_layout, parent, false);
                return new VisitedProfileHolder(view);
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull VisitedProfileHolder holder, int position, @NonNull VisitedProfileVideos model) {

                // Storage reference to the user avatar image
                storageReference  = FirebaseStorage.getInstance().getReference().child(model.getEmail()+"/"+model.getUserID());

                // Download uri from user image folder using the storageReference inicialized at top of document
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                    // Load the image using Glide
                    Glide.with(container).load(uri).into(holder.listUserImage);

                }).addOnFailureListener(exception -> {

                    // Adds default image if none are found
                    Glide.with(container).load(R.drawable.default_avatar).into(holder.listUserImage);
                });

                // Setting the visited user username in all videos that he has
                documentReference = db.collection("users").document(model.getUserID());
                documentReference.get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {

                                String dataUser = documentSnapshot.getString("Username");
                                holder.listUsername.setText(dataUser);
                            }
                        });

                // Check if current user already liked the visited user video
                FirebaseFirestore.getInstance().collection("videos").document(model.getDocumentName()).get().addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();

                    List<String> group = (List<String>) document.get("UsersThatLiked");

                    assert group != null;
                    if (group.contains(userUid)) {
                        holder.listLikesIcon.setImageResource(R.drawable.ic_explosion);
                        holder.listLikesIcon.setTag("liked");
                    }
                });

                // Fill description, game name, likes and icon likes in the adapter
                if (model.getGameName().length() > 34) {

                    holder.listGameName.setText(model.getGameName().substring(0, 34) + "...");
                } else {

                    holder.listGameName.setText(model.getGameName());
                }

                holder.listDescription.setText(model.getDescription());
                holder.listLikes.setText(model.getLikes());

                holder.listLikesIcon.setOnClickListener(v -> {

                    if (holder.listLikesIcon.getTag().toString().equals("liked")) {

                        // If the icon tag equals "liked", when we press the icon the like will be removed or if the tag equals "noLike" the like will be added
                        String likesCount = (String) holder.listLikes.getText();
                        int likeDone = Integer.parseInt(likesCount) - 1;
                        holder.listLikes.setText(String.valueOf(likeDone));
                        holder.listLikesIcon.setImageResource(R.drawable.ic_explosion_outline);
                        holder.listLikesIcon.setTag("noLike");


                        db.collection("videos").document(model.getDocumentName()).update("Likes", holder.listLikes.getText());
                        db.collection("videos").document(model.getDocumentName()).update("UsersThatLiked", FieldValue.arrayRemove(userUid));
                    } else {

                        String likesCount = (String) holder.listLikes.getText();
                        int likeDone = Integer.parseInt(likesCount) + 1;
                        holder.listLikes.setText(String.valueOf(likeDone));
                        holder.listLikesIcon.setImageResource(R.drawable.ic_explosion);
                        holder.listLikesIcon.setTag("liked");


                        db.collection("videos").document(model.getDocumentName()).update("Likes", holder.listLikes.getText());
                        db.collection("videos").document(model.getDocumentName()).update("UsersThatLiked", FieldValue.arrayUnion(userUid));
                    }
                });

                // Showing the loading progress bar when the video isn't completed
                holder.progressBar.setVisibility(View.VISIBLE);

                // Filling the video uri from the model class that return the videos url and putting the video image with the 1st frame
                Uri uri = Uri.parse(model.getUrl());

                holder.listVideo.setVideoURI(uri);
                holder.listVideo.seekTo(1);

                holder.listVideo.setOnPreparedListener(mp -> {

                    // When the video is fully loaded the loading disappear, the heigth is setted to wrap content and the video frame background its removed too
                    holder.progressBar.setVisibility(View.INVISIBLE);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 140, 0, 0);
                    holder.listVideo.setLayoutParams(layoutParams);
                    holder.listVideo.setBackgroundResource(0);

                    // Clicking in the video it will start it
                    holder.listVideo.setOnClickListener(v -> holder.listVideo.start());

                    // When video ends it will show the 1st frame again
                    holder.listVideo.setOnCompletionListener(mp1 -> holder.listVideo.seekTo(1));
                });
            }
        };

        // Setting the videos in adapter
        visitedVideos.setLayoutManager(new LinearLayoutManager(getContext()));
        visitedVideos.setAdapter(adapter);
        visitedVideos.setNestedScrollingEnabled(false);

        return returnView;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Declaring Layout Variables
        ImageView img = Objects.requireNonNull(getActivity()).findViewById(R.id.image);
        TextView realName = getActivity().findViewById(R.id.realName);
        TextView bio = getActivity().findViewById(R.id.bio);
        TextView title = getActivity().findViewById(R.id.gamifyTitle);
        TextView numberOfVideos = getActivity().findViewById(R.id.clipsNumber);
        AppCompatImageView steamIcon = getActivity().findViewById(R.id.iconSteam);
        AppCompatImageView xboxIcon = getActivity().findViewById(R.id.iconXbox);
        AppCompatImageView originIcon = getActivity().findViewById(R.id.iconOrigin);
        AppCompatImageView psnIcon = getActivity().findViewById(R.id.iconPsn);
        AppCompatImageView nintendoIcon = getActivity().findViewById(R.id.iconNintendo);
        TextView userName = getActivity().findViewById(R.id.appBarTitle);
        Button btnFollow = getActivity().findViewById(R.id.containedButton);
        TextView followerNumber = getActivity().findViewById(R.id.followerNumber);
        TextView followingNumber =  getActivity().findViewById(R.id.followingNumber);

        // Checks if user is following this user
        FirebaseFirestore.getInstance().collection("users").document(docID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();

            List<String> group = (List<String>) document.get("UsersFollowers");

            assert group != null;
            if (group.contains(userUid)) {

                btnFollow.setText("Unfollow");
                btnFollow.setTextSize(6);
                btnFollow.setElevation(0);
                btnFollow.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                btnFollow.setTag("following");
            }
        });

        // Follow and Unfollow button
        btnFollow.setOnClickListener(v -> {

            if (btnFollow.getTag().toString().equals("following")) {

                String followingCount = (String) followerNumber.getText();
                int followDone = Integer.parseInt(followingCount) - 1;
                followerNumber.setText(String.valueOf(followDone));
                btnFollow.setTag("noFollow");
                btnFollow.setText("Follow");
                btnFollow.setBackgroundColor(getResources().getColor(R.color.colorPurple));

                db.collection("users").document(docID).update("Followers", followerNumber.getText());
                db.collection("users").document(docID).update("UsersFollowers", FieldValue.arrayRemove(userUid));

                db.collection("videos").whereEqualTo("UserID", docID).get().addOnCompleteListener(task -> {

                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                        db.collection("videos").document(document.getId()).update("UsersFollowers", FieldValue.arrayRemove(userUid));
                    }
                });

                // Setting the visited user username in all videos that he has
                documentReference = db.collection("users").document(userUid);
                documentReference.get()
                        .addOnSuccessListener(documentSnapshot -> {

                            if (documentSnapshot.exists()) {
                                String userFollowing = documentSnapshot.getString("Following");

                                assert userFollowing != null;
                                int addFollowing = Integer.parseInt(userFollowing) -1;
                                db.collection("users").document(userUid).update("Following", String.valueOf(addFollowing));
                                db.collection("users").document(userUid).update("UsersFollowing", FieldValue.arrayRemove(docID));
                            }
                        });
            } else {

                String followingCount = (String) followerNumber.getText();
                int followDone = Integer.parseInt(followingCount) + 1;
                followerNumber.setText(String.valueOf(followDone));
                btnFollow.setText("Unfollow");
                btnFollow.setTag("following");
                btnFollow.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                btnFollow.setTextSize(6);
                btnFollow.setElevation(0);

                db.collection("users").document(docID).update("Followers", followerNumber.getText());
                db.collection("users").document(docID).update("UsersFollowers", FieldValue.arrayUnion(userUid));

                db.collection("videos").whereEqualTo("UserID", docID).get().addOnCompleteListener(task -> {

                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                        db.collection("videos").document(document.getId()).update("UsersFollowers", FieldValue.arrayUnion(userUid));
                    }
                });

                // Setting the visited user username in all videos that he has
                documentReference = db.collection("users").document(userUid);
                documentReference.get()
                        .addOnSuccessListener(documentSnapshot -> {

                            if (documentSnapshot.exists()) {
                                String userFollowing = documentSnapshot.getString("Following");
                                assert userFollowing != null;
                                int addFollowing = Integer.parseInt(userFollowing) + 1;
                                db.collection("users").document(userUid).update("Following", String.valueOf(addFollowing));
                                db.collection("users").document(userUid).update("UsersFollowing", FieldValue.arrayUnion(docID));
                            }
                        });
            }
        });

        // Count amount of clips
        db.collection("videos").whereEqualTo("UserID",docID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        count = 0;
                        for (DocumentSnapshot ignored : Objects.requireNonNull(task.getResult())) {

                            count++;
                        }
                        numberOfVideos.setText(String.valueOf(count));

                        // If there are no videos to be shown, displays a message for user
                        TextView noVideosMessage = Objects.requireNonNull(getActivity()).findViewById(R.id.noVideosMessage);

                        if (count == 0) {
                            noVideosMessage.setText("This user hasn't shared any Clips yet...");
                        }

                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
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

        // Recieves data from DB for profile section
        db.collection("users")
                .whereEqualTo("Username", pickedProfile)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                            // Map that will fill our database with values
                            Map<String, Object> Userdata = document.getData();

                            String dataUsername = (String) Userdata.get("Username");
                            String dataName = (String) Userdata.get("Name");
                            String dataBio = (String) Userdata.get("Bio");
                            String gamifyTitle = (String) Userdata.get("GamifyTitle");
                            String dataSteam = (String) Userdata.get("Steam");
                            String dataOrigin = (String) Userdata.get("Origin");
                            String dataPsn = (String) Userdata.get("Psn");
                            String dataXbox = (String) Userdata.get("Xbox");
                            String dataNintendo = (String) Userdata.get("Nintendo");
                            String dataFollower = (String) Userdata.get("Followers");
                            String dataFollowing = (String) Userdata.get("Following");

                            realName.setText(dataName);
                            bio.setText(dataBio);
                            title.setText(gamifyTitle);
                            userName.setText(dataUsername);
                            followerNumber.setText(dataFollower);
                            followingNumber.setText(dataFollowing);

                            // If user has some nickname in some platform the opacity of that platform will be 1
                            assert dataSteam != null;
                            if (!dataSteam.equals("")) {

                                steamIcon.setAlpha((float) 1.0);
                            }
                            assert dataOrigin != null;
                            if (!dataOrigin.equals("")) {

                                originIcon.setAlpha((float) 1.0);
                            }
                            assert dataPsn != null;
                            if (!dataPsn.equals("")) {

                                psnIcon.setAlpha((float) 1.0);
                            }
                            assert dataXbox != null;
                            if (!dataXbox.equals("")) {

                                xboxIcon.setAlpha((float) 1.0);
                            }
                            assert dataNintendo != null;
                            if (!dataNintendo.equals("")) {

                                nintendoIcon.setAlpha((float) 1.0);
                            }

                            // Listeners to show toast with platform id if user has some nickname introduced in database for that platform
                            steamIcon.setOnClickListener(v -> {
                                if (!dataSteam.equals("")) {
                                    Toast.makeText(getContext(), "Steam ID: " + dataSteam, Toast.LENGTH_SHORT).show();
                                }
                            });

                            originIcon.setOnClickListener(v -> {
                                if (!dataOrigin.equals("")) {
                                    Toast.makeText(getContext(), "Origin ID: " + dataOrigin, Toast.LENGTH_SHORT).show();
                                }
                            });
                            psnIcon.setOnClickListener(v -> {
                                if (!dataPsn.equals("")) {
                                    Toast.makeText(getContext(), "PSN ID: " + dataPsn, Toast.LENGTH_SHORT).show();
                                }
                            });
                            xboxIcon.setOnClickListener(v -> {
                                if (!dataXbox.equals("")) {
                                    Toast.makeText(getContext(), "Xbox ID: " + dataXbox, Toast.LENGTH_SHORT).show();
                                }
                            });
                            nintendoIcon.setOnClickListener(v -> {
                                if (!dataNintendo.equals("")) {
                                    Toast.makeText(getContext(), "Switch ID: " + dataNintendo, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }
                });

        // Download uri from user image folder using the storageReference inicialized at top of document
        storageReference  = FirebaseStorage.getInstance().getReference().child(visitedEmail+"/"+ docID);

        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

            // Load the image using Glide
            Glide.with(Objects.requireNonNull(getContext())).load(uri).into(img);

        }).addOnFailureListener(exception -> {

            // Adds default image if none are found
            Glide.with(Objects.requireNonNull(getContext())).load(R.drawable.default_avatar).into(img);
        });
    }

    // visited_profile_videos_layout Variables
    private static class VisitedProfileHolder extends RecyclerView.ViewHolder {

        private ImageView listUserImage;
        private TextView listGameName;
        private TextView listDescription;
        private VideoView listVideo;
        private TextView listUsername;
        private TextView listLikes;
        private ImageView listLikesIcon;
        ProgressBar progressBar;

        public VisitedProfileHolder(@NonNull View itemView) {
            super(itemView);

            listUsername = itemView.findViewById(R.id.videosUsername);
            listGameName = itemView.findViewById(R.id.videosGameName);
            listDescription = itemView.findViewById(R.id.videosDescription);
            listVideo = itemView.findViewById(R.id.videosFrame);
            listUserImage = itemView.findViewById(R.id.videosImage);
            listLikes = itemView.findViewById(R.id.videosLikes);
            listLikesIcon = itemView.findViewById(R.id.videosLikeIcon);
            progressBar = itemView.findViewById(R.id.progress_circular);
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