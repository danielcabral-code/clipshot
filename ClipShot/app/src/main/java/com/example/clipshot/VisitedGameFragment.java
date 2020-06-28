package com.example.clipshot;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
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

import java.util.List;
import java.util.Objects;

public class VisitedGameFragment extends Fragment {

    // Global Variables
    private String pickedGameName;
    private FirebaseFirestore db;
    private FirestorePagingAdapter adapter;
    private DocumentReference documentReference;

    // Variables that will get the email and userId value from the user google account
    String userUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    public VisitedGameFragment() {
        // Required empty public constructor
    }

    public static VisitedGameFragment newInstance(String param1, String param2) {
        VisitedGameFragment fragment = new VisitedGameFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls TopBar
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setElevation(20f); // Float == px
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setCustomView(R.layout.visited_game_action_bar_layout);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get extras from bundle that sends the selectedGameName
        Bundle bundle = this.getArguments();
        assert bundle != null;
        pickedGameName = bundle.getString("pickedGameName");
        assert pickedGameName != null;

        View returnView = inflater.inflate(R.layout.fragment_visited_game, container, false);
        RecyclerView VisitedGameVideos = returnView.findViewById(R.id.recyclerView);

        // FireStore instance
        db = FirebaseFirestore.getInstance();

        // Query for Recycler View for searched Game videos (gets videos from game that user searched)
        Query query = db.collection("videos").whereEqualTo("GameName",pickedGameName).orderBy("ReleasedTime", Query.Direction.DESCENDING);

        // Configuration for RecyclerView adapter
        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<VisitedGameVideos> options = new FirestorePagingOptions.Builder<VisitedGameVideos>()
                .setQuery(query,config, VisitedGameVideos.class)
                .build();

        adapter = new FirestorePagingAdapter<VisitedGameVideos, GameVideosHolder>(options) {
            @NonNull
            @Override
            public GameVideosHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.visited_game_videos_layout, parent, false);
                return new GameVideosHolder(view);
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull GameVideosHolder holder, int position, @NonNull VisitedGameVideos model) {

                // Storage reference to the user avatar image
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(model.getEmail() + "/" + model.getUserID());

                // Download uri from user image folder using the storageReference inicialized at top of document
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                    // Load the image using Glide
                    Glide.with(container).load(uri).into(holder.listUserImage);

                }).addOnFailureListener(exception -> {

                    // Loads default avatar if none are found
                    Glide.with(container).load(R.drawable.default_avatar).into(holder.listUserImage);
                });

                // Gets username from user that that video belongs to
                documentReference = db.collection("users").document(model.getUserID());
                documentReference.get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String dataUser = documentSnapshot.getString("Username");
                                holder.listUsername.setText(dataUser);

                            }
                        });

                // Checks if current user has liked video or not
                FirebaseFirestore.getInstance().collection("videos").document(model.getDocumentName()).get().addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    List<String> group = (List<String>) document.get("UsersThatLiked");
                    assert group != null;
                    if (group.contains(userUid)) {

                        holder.listLikesIcon.setImageResource(R.drawable.ic_explosion);
                        holder.listLikesIcon.setTag("liked");
                    }
                });

                // Places text in holder (video descripion, gameName, Likes) for RecyclerView
                if (model.getGameName().length() > 34) {

                    holder.listGameName.setText(model.getGameName().substring(0, 34) + "...");
                } else {

                    holder.listGameName.setText(model.getGameName());
                }

                holder.listDescription.setText(model.getDescription());
                holder.listLikes.setText(model.getLikes());
                holder.listLikesIcon.setOnClickListener(v -> {

                    // Allows user to like/dislike and changes value of like in DB
                    if (holder.listLikesIcon.getTag().toString().equals("liked")) {

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

                // Places Google's loading circle
                holder.progressBar.setVisibility(View.VISIBLE);

                // Gets and loads video in RecyclerView
                Uri uri = Uri.parse(model.getUrl());
                holder.listVideo.setVideoURI(uri);
                holder.listVideo.seekTo(1);
                holder.listVideo.setOnPreparedListener(mp -> {

                    holder.progressBar.setVisibility(View.INVISIBLE);
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 140, 0, 0);
                    holder.listVideo.setLayoutParams(layoutParams);
                    holder.listVideo.setBackgroundResource(0);
                    holder.listVideo.setOnClickListener(v -> holder.listVideo.start());
                    holder.listVideo.setOnCompletionListener(mp1 -> holder.listVideo.seekTo(1));
                });
            }
        };

        // Set the adapter the the RecyclerView
        VisitedGameVideos.setLayoutManager(new LinearLayoutManager(getContext()));
        VisitedGameVideos.setAdapter(adapter);
        VisitedGameVideos.setNestedScrollingEnabled(false);

        return returnView;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView appBarTitle = Objects.requireNonNull(getActivity()).findViewById(R.id.appBarTitle);

        // Sets name of game in the TopBar
        if (pickedGameName.length() > 20) {

            appBarTitle.setText(pickedGameName.substring(0, 18) + "...");
        } else {

            appBarTitle.setText(pickedGameName);
        }
    }

    // visited_game_videos_layout Variables
    private static class GameVideosHolder extends  RecyclerView.ViewHolder {

        private ImageView listUserImage;
        private  TextView listGameName;
        private  TextView listDescription;
        private VideoView listVideo;
        private  TextView listUsername;
        private  TextView listLikes;
        private  ImageView listLikesIcon;
        ProgressBar progressBar;

        public GameVideosHolder(@NonNull View itemView) {
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