package com.example.clipshot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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

public class FeedFragment extends Fragment {

    private AppCompatImageView iconSearch;
    private EditText searchQuery;
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
        Log.d("TAG", "onCreateView: "+ acct.getEmail());

        // Variables that will get the email and userId value from the user google account
        String email = acct.getEmail().toString();
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();



        View returnView = inflater.inflate(R.layout.fragment_feed, container, false);
        RecyclerView feedVideos = returnView.findViewById(R.id.recyclerView);

        db = FirebaseFirestore.getInstance();
        Log.d("TAG", userUid);

        Query query = db.collection("videos").whereArrayContains("UsersFollowers",userUid);

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

            @Override

            protected void onBindViewHolder(@NonNull FeedVideosHolder holder, int position, @NonNull FeedVideos model) {
                // Storage reference to the user avatar image
                StorageReference storageReference  = FirebaseStorage.getInstance().getReference().child(model.getEmail()+"/"+model.getUserID());
                Log.d("TAG", "onBindViewHolder: "+ model.getEmail());

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

        feedVideos.setLayoutManager(new LinearLayoutManager(getContext()));
        feedVideos.setAdapter(adapter);
        feedVideos.setNestedScrollingEnabled(false);

        return returnView;

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Trying to make this close keyboard when changing to this fragment from SearchFragment (not working yet)
        final InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(Objects.requireNonNull(view).getWindowToken(), 0);

        iconSearch = Objects.requireNonNull(getActivity()).findViewById(R.id.iconSearch);

        Log.d("checkClick", String.valueOf(SEARCHBAR_VISIBILITY));

        iconSearch.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View v) {

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
            }
        });

        searchQuery = Objects.requireNonNull(getActivity()).findViewById(R.id.searchQuery);
        searchQuery.setOnFocusChangeListener(focusListener);
    }

    private View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {

            if (hasFocus){
                ((MainActivity) Objects.requireNonNull(getActivity())).openFragment(SearchFragment.newInstance("",""));
            }
        }
    };

    private class FeedVideosHolder extends  RecyclerView.ViewHolder{

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