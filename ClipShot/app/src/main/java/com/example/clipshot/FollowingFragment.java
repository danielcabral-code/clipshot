package com.example.clipshot;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import android.widget.TextView;

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

import java.util.Objects;

public class FollowingFragment extends Fragment {

    // Global Variables
    private FirestorePagingAdapter adapter;
    private FirebaseFirestore db;
    private DocumentReference documentReference;

    public FollowingFragment() {
        // Required empty public constructor
    }

    public static FollowingFragment newInstance(String param1, String param2) {
        FollowingFragment fragment = new FollowingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Gets current user
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(container.getContext());

        // Variables that will get the email and userId value from the user google account
        String userUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        assert acct != null;

        // Layout variables
        View returnView = inflater.inflate(R.layout.fragment_following, container, false);
        RecyclerView usersList = returnView.findViewById(R.id.recyclerViewFollowing);

        // FireStore Instance
        db = FirebaseFirestore.getInstance();

        // Query that gets users that user follows
        Query query = db.collection("users").whereArrayContains("UsersFollowers", userUid);

        // Configuration for RecyclerView adapter
        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();

        FirestorePagingOptions<FollowersUsers> options = new FirestorePagingOptions.Builder<FollowersUsers>()
                .setQuery(query,config, FollowersUsers.class)
                .build();

        adapter = new FirestorePagingAdapter<FollowersUsers, FollowingHolder>(options) {

            @NonNull
            @Override
            public FollowingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.following_layout,parent,false);
                return new FollowingHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FollowingHolder holder, int position, @NonNull FollowersUsers model) {


                // Gets UserID from users collection and places username in following list
                documentReference = db.collection("users").document(model.getUserUID());
                Log.d("TAG", "d"+ model.getUserUID());

                documentReference.get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {

                                String dataUser = documentSnapshot.getString("Username");
                                holder.listUsername.setText(dataUser);
                            }
                        });

                // Storage reference to the user avatar image
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(model.getEmail() + "/" + model.getUserUID());

                // Download uri from user image folder using the storageReference inicialized at top of document
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                    // Load the image using Glide
                    Glide.with(container).load(uri).into(holder.listUserImage);

                }).addOnFailureListener(exception -> {

                    // When image can't load, app loads default avatar
                    Glide.with(container).load(R.drawable.default_avatar).into(holder.listUserImage);
                });

                // Button to unfollow
                holder.listButton.setOnClickListener(view -> {

                    // Modal confirmation message for user
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setTitle("Are you sure you want to UNFOLLOW " + model.getUsername() + "?");

                    // If removed, changes data in DB
                    alert.setPositiveButton("UNFOLLOW", (dialog, whichButton) -> {

                        db.collection("users").document(userUid).get().addOnCompleteListener(task -> {
                            task.getResult();

                            // Counts users following
                            int followingCount;
                            followingCount= Integer.parseInt(Objects.requireNonNull(task.getResult().getString("Following")));
                            followingCount = followingCount -1;

                            // Counts followers of user that is being unfollowed
                            int followersCount = Integer.parseInt(model.getFollowers());
                            followersCount = followersCount-1;

                            // Where data is altered and refreshes RecyclerView
                            db.collection("users").document(userUid).update("Following",String.valueOf(followingCount) );
                            db.collection("users").document(userUid).update("UsersFollowing", FieldValue.arrayRemove(model.getUserUID()));
                            db.collection("users").document(model.getUserUID()).update("UsersFollowers", FieldValue.arrayRemove(userUid));
                            db.collection("users").document(model.getUserUID()).update("Followers",String.valueOf(followersCount) );
                            Task<QuerySnapshot> querySnapshot = db.collection("videos").whereEqualTo("UserID", model.getUserUID()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d("TAG", document.getId() + " => " + document.getData());

                                        db.collection("videos").document(document.getId()).update("UsersFollowers", FieldValue.arrayRemove(userUid));
                                    }

                                }
                            });
                            adapter.refresh();
                        });

                    });

                    // Sets the negative (cancel) action of the modal
                    alert.setNegativeButton("Cancel",
                            (dialog, whichButton) -> {
                            });
                    alert.show();
                });
            }
        };

        // Set the adapter the the RecyclerView
        usersList.setLayoutManager(new LinearLayoutManager(getContext()));
        usersList.setAdapter(adapter);
        usersList.setNestedScrollingEnabled(false);

        return returnView;
    }

    // following_layout Variables
    private static class FollowingHolder extends  RecyclerView.ViewHolder{

        private ImageView listUserImage;
        private  TextView listUsername;
        private Button listButton;

        public FollowingHolder(@NonNull View itemView) {
            super(itemView);

            listUsername =itemView.findViewById(R.id.username);
            listUserImage= itemView.findViewById(R.id.userImage);
            listButton =itemView.findViewById(R.id.removeUser);
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