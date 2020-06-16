package com.example.clipshot;

import android.annotation.SuppressLint;
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

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String pickedProfile, docID, visitedEmail;
    private FirestorePagingAdapter adapter;
    private DocumentReference documentReference;
    String userVisitedUid;
    CollectionReference usersRef;
    StorageReference storageReference;

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


        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setElevation(20f); // Float == px
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setCustomView(R.layout.visited_profile_action_bar_layout);


        /*// Google variable to detect the user that is signed
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(container.getContext());
        Log.d("TAG", "onCreateView: "+ acct.getEmail());*/

        /*String email = acct.getEmail().toString();*/
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        View returnView = inflater.inflate(R.layout.fragment_visited_profile, container, false);
        RecyclerView visitedVideos = returnView.findViewById(R.id.recyclerView);

        db = FirebaseFirestore.getInstance();

        //Get extras from bundle that will be used to get the visted user image and videos
        Bundle bundle = this.getArguments();
        assert bundle != null;
        pickedProfile = bundle.getString("pickedProfile");
        docID = bundle.getString("docID");
        visitedEmail=bundle.getString("email");
        assert pickedProfile != null;
        Log.d("TAG", pickedProfile);


        //Query to get all videos where the user id equals the variable from bundle and order the videos from the most recent to older
        Query queryVideo = db.collection("videos").whereEqualTo("UserID", docID).orderBy("ReleasedTime", Query.Direction.DESCENDING);
        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        //Applying query to options of FirestorePagingAdapter
        FirestorePagingOptions<VisitedProfileVideos> options = new FirestorePagingOptions.Builder<VisitedProfileVideos>()
                .setQuery(queryVideo, config, VisitedProfileVideos.class)
                .build();

        //Setting the options applied before to FirestorePaginAdapter
        adapter = new FirestorePagingAdapter<VisitedProfileVideos, VisitedProfileHolder>(options) {
            @NonNull
            @Override
            public VisitedProfileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                //Getting the layout where adapter will fill data
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.visited_profile_videos_layout, parent, false);
                return new VisitedProfileHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull VisitedProfileHolder holder, int position, @NonNull VisitedProfileVideos model) {
                // Storage reference to the user avatar image
                storageReference  = FirebaseStorage.getInstance().getReference().child(model.getEmail()+"/"+model.getUserID());

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

                //Setting the visited user username in all videos that he got
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

                //Check if "we" already liked the visited user video
                FirebaseFirestore.getInstance().collection("videos").document(model.getDocumentName()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        List<String> group = (List<String>) document.get("UsersThatLiked");
                        assert group != null;
                        if (group.contains(userUid)) {
                            holder.listLikesIcon.setImageResource(R.drawable.ic_explosion);
                            holder.listLikesIcon.setTag("liked");
                        }
                    }
                });

                //Fill description, game name, likes and icon likes in the adapter
                holder.listDescription.setText(model.getDescription());
                holder.listGameName.setText(model.getGameName());
                holder.listLikes.setText(model.getLikes());
                holder.listLikesIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (holder.listLikesIcon.getTag().toString().equals("liked")) {

                            //If the icon tag equals "liked", when we press the icon the like will be removed or if the tag equals "noLike" the like will be added
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

                //Showing the loading progress bar when the video isnt completed
                holder.progressBar.setVisibility(View.VISIBLE);

                //Filling the video uri from the model class that return the videos url and putting the video image with the 1st frame
                Uri uri = Uri.parse(model.getUrl());
                holder.listVideo.setVideoURI(uri);
                holder.listVideo.seekTo(1);
                holder.listVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        //When the video is fully loaded the loading disappear, the heigth is setted to wrap content and the video frame background its removed too
                        holder.progressBar.setVisibility(View.INVISIBLE);
                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(0, 200, 0, 0);
                        holder.listVideo.setLayoutParams(layoutParams);
                        holder.listVideo.setBackgroundResource(0);

                        //Clicking in the video it will start it
                        holder.listVideo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                holder.listVideo.start();
                            }
                        });

                        //When video ends it will show the 1st frame again
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

        //setting the videos in adapter
        visitedVideos.setLayoutManager(new LinearLayoutManager(getContext()));
        visitedVideos.setAdapter(adapter);
        visitedVideos.setNestedScrollingEnabled(false);

      return  returnView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


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

        db.collection("users")
                .whereEqualTo("Username", pickedProfile)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot document : task.getResult()) {

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

                            realName.setText(dataName);
                            bio.setText(dataBio);
                            title.setText(gamifyTitle);
                            userName.setText(dataUsername);

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

                            //Listeners to show toast with platform id if user has some nickname introduced in database for that platform
                            steamIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!dataSteam.equals("")) {
                                        Toast.makeText(getContext(), "Steam ID: " + dataSteam, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            originIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!dataOrigin.equals("")) {
                                        Toast.makeText(getContext(), "Origin ID: " + dataOrigin, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            psnIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!dataPsn.equals("")) {
                                        Toast.makeText(getContext(), "PSN ID: " + dataPsn, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            xboxIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!dataXbox.equals("")) {
                                        Toast.makeText(getContext(), "Xbox ID: " + dataXbox, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            nintendoIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!dataNintendo.equals("")) {
                                        Toast.makeText(getContext(), "Switch ID: " + dataNintendo, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }
                });



        storageReference  = FirebaseStorage.getInstance().getReference().child(visitedEmail+"/"+ docID);
        // Download uri from user image folder using the storageReference inicialized at top of document
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                // Load the image using Glide
                Glide.with(getContext()).load(uri).into(img);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Glide.with(getContext()).load(R.drawable.default_avatar).into(img);
                Log.d("TAG", "onFailure: error imagem grande "+ exception);
            }
        });


    }


    private class VisitedProfileHolder extends RecyclerView.ViewHolder {

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


