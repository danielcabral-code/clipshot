package com.example.clipshot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class FeedFragment extends Fragment {

    // Global Variables
    private int SEARCHBAR_VISIBILITY = 0;
    private FirestorePagingAdapter adapter;
    private FirebaseFirestore db;
    private DocumentReference documentReference;

    public FeedFragment() {
        // Required empty public constructor
    }

    public static FeedFragment newInstance(String param1, String param2) {
        FeedFragment fragment = new FeedFragment();
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
        assert acct != null;

        // Variables that will get the email and userId value from the user google account
        String userUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Layout Variables
        View returnView = inflater.inflate(R.layout.fragment_feed, container, false);
        RecyclerView feedVideos = returnView.findViewById(R.id.recyclerView);
        TextView message = returnView.findViewById(R.id.noVideosMessage);

        // FireStore Instance
        db = FirebaseFirestore.getInstance();

        // Query for Recycler View for feed videos (gets videos from users that this user follows)
        Query query = db.collection("videos").whereArrayContains("UsersFollowers",userUid).orderBy("ReleasedTime", Query.Direction.DESCENDING);

        // Configuration for RecyclerView adapter
        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<FeedVideos> options = new FirestorePagingOptions.Builder<FeedVideos>()
                .setQuery(query,config, FeedVideos.class)
                .build();

        adapter = new FirestorePagingAdapter<FeedVideos, FeedVideosHolder>(options) {
            @NonNull
            @Override
            public FeedVideosHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_videos_layout, parent, false);
                return new FeedVideosHolder(view);
            }

            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull FeedVideosHolder holder, int position, @NonNull FeedVideos model) {

                // Storage reference to the user avatar image
                StorageReference storageReference  = FirebaseStorage.getInstance().getReference().child(model.getEmail()+"/"+model.getUserID());

                // Download uri from user image folder using the storageReference inicialized at top of document
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                    // Load the image using Glide
                    Glide.with(container).load(uri).into(holder.listUserImage);

                }).addOnFailureListener(exception -> {

                    // When image can't load, app loads default avatar
                    Glide.with(container).load(R.drawable.default_avatar).into(holder.listUserImage);
                });

                // Gets username from each video
                documentReference = db.collection("users").document(model.getUserID());
                documentReference.get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {

                                String dataUser = documentSnapshot.getString("Username");
                                holder.listUsername.setText(dataUser);
                            }
                        });

                // Checks if user has liked each individual video
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

                // Allows user to like/dislike and changes value of like in DB
                holder.listLikesIcon.setOnClickListener(v -> {

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

                    // Onclick that visually starts video for user
                    holder.listVideo.setOnClickListener(v -> holder.listVideo.start());

                    // Gives each video a "thumbnail" (always the 1st frame of video)
                    holder.listVideo.setOnCompletionListener(mp1 -> holder.listVideo.seekTo(1));
                });
            }
        };

        // Set the adapter the the RecyclerView
        feedVideos.setLayoutManager(new LinearLayoutManager(getContext()));
        feedVideos.setAdapter(adapter);
        feedVideos.setNestedScrollingEnabled(false);

        // When there are no videos to be shown, shows message
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                int totalNumberOfItems = adapter.getItemCount();
                if(totalNumberOfItems == 0) {

                    message.setVisibility(View.VISIBLE);
                }
                else message.setVisibility(View.INVISIBLE);
            }
        });
        return returnView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Declaring Variables
        AppCompatImageView iconSearch = Objects.requireNonNull(getActivity()).findViewById(R.id.iconSearch);

        // Shows or hides SearchBar when user presses serach Icon
        iconSearch.setOnClickListener(v -> {

            View searchBarContainer = Objects.requireNonNull(getActivity()).findViewById(R.id.searchBarContainer);
            View feedContents = Objects.requireNonNull(getActivity()).findViewById(R.id.feedContents);

            if (SEARCHBAR_VISIBILITY == 1) {
                searchBarContainer.setVisibility(View.INVISIBLE);
                Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setElevation(20f); // Float == px

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, -180, 0, 0);
                feedContents.setLayoutParams(layoutParams);

                SEARCHBAR_VISIBILITY = 0;

            } else {
                searchBarContainer.setVisibility(View.VISIBLE);
                Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setElevation(0f); // Float == px

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 10, 0, 0);
                feedContents.setLayoutParams(layoutParams);

                SEARCHBAR_VISIBILITY = 1;
            }
        });

        EditText searchQuery = Objects.requireNonNull(getActivity()).findViewById(R.id.searchQuery);
        searchQuery.setOnFocusChangeListener(focusListener);
    }

    // When user focuses on searchBar editText, changes fragment to start performing search
    private View.OnFocusChangeListener focusListener = (v, hasFocus) -> {

        if (hasFocus){
            ((MainActivity) Objects.requireNonNull(getActivity())).openFragment(SearchFragment.newInstance("",""));
        }
    };

    // feed_videos_layout Variables
    private static class FeedVideosHolder extends RecyclerView.ViewHolder {

        private ImageView listUserImage;
        private TextView listGameName;
        private  TextView listDescription;
        private VideoView listVideo;
        private  TextView listUsername;
        private  TextView listLikes;
        private  ImageView listLikesIcon;
        ProgressBar progressBar;

        public FeedVideosHolder(@NonNull View itemView) {
            super(itemView);

            listUsername =itemView.findViewById(R.id.videosUsernameFeed);
            listGameName =itemView.findViewById(R.id.videosGameNameFeed);
            listDescription=itemView.findViewById(R.id.videosDescriptionFeed);
            listVideo=itemView.findViewById(R.id.videosFrameFeed);
            listUserImage= itemView.findViewById(R.id.videosImageFeed);
            listLikes=itemView.findViewById(R.id.videosLikesFeed);
            listLikesIcon=itemView.findViewById(R.id.videosLikeIconFeed);
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