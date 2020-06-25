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

    private String pickedGameName;
    private FirebaseFirestore db;
    StorageReference storageReference;
    private FirestorePagingAdapter adapter;
    private DocumentReference documentReference;

    // Variables that will get the email and userId value from the user google account
    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

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

        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setElevation(20f); // Float == px
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setCustomView(R.layout.visited_game_action_bar_layout);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Get extras from bundle that will be used to get the visted user image and videos
        Bundle bundle = this.getArguments();
        assert bundle != null;
        pickedGameName = bundle.getString("pickedGameName");
        /*docID = bundle.getString("docID");
        visitedEmail=bundle.getString("email");*/
        assert pickedGameName != null;
        Log.d("checkItem", pickedGameName);


        View returnView = inflater.inflate(R.layout.fragment_visited_game, container, false);
        RecyclerView VisitedGameVideos = returnView.findViewById(R.id.recyclerView);


        db = FirebaseFirestore.getInstance();

        Query query = db.collection("videos").whereEqualTo("GameName",pickedGameName).orderBy("ReleasedTime", Query.Direction.DESCENDING);
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

            @Override
            protected void onBindViewHolder(@NonNull GameVideosHolder holder, int position, @NonNull VisitedGameVideos model) {

                // Storage reference to the user avatar image
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(model.getEmail() + "/" + model.getUserID());
                Log.d("TAG", "onBindViewHolder: " + model.getEmail());

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
                        Log.d("TAG", "onFailure: error " + exception);
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
                        if (group.contains(userUid)) {
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
                    }
                });

                holder.progressBar.setVisibility(View.VISIBLE);

                Uri uri = Uri.parse(model.getUrl());
                holder.listVideo.setVideoURI(uri);
                holder.listVideo.seekTo(1);
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

                                holder.listVideo.seekTo(1);
                            }
                        });

                    }

                });
            }
        };

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

        if (pickedGameName.length() > 20) {

            appBarTitle.setText(pickedGameName.substring(0, 18) + "...");
        } else {

            appBarTitle.setText(pickedGameName);
        }
    }

    private class GameVideosHolder extends  RecyclerView.ViewHolder{

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