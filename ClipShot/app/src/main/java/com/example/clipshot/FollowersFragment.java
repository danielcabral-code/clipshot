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
import android.widget.ProgressBar;
import android.widget.TextView;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FollowersFragment extends Fragment {

    private FirestorePagingAdapter adapter;
    private FirebaseFirestore db;
    private DocumentReference documentReference;

    public FollowersFragment() {
        // Required empty public constructor
    }

    public static FollowersFragment newInstance(String param1, String param2) {
        FollowersFragment fragment = new FollowersFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(container.getContext());

        // Variables that will get the email and userId value from the user google account
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String email = acct.getEmail().toString();

        View returnView = inflater.inflate(R.layout.fragment_followers, container, false);
        RecyclerView usersList = returnView.findViewById(R.id.recyclerViewFollowers);
        Log.d("TAG", "email: "+ email);

        db = FirebaseFirestore.getInstance();

        Query query = db.collection("users").whereArrayContains("UsersFollowing", userUid);

        PagedList.Config config = new PagedList.Config.Builder()
                .setInitialLoadSizeHint(10)
                .setPageSize(3)
                .build();


       FirestorePagingOptions<FollowersUsers> options = new FirestorePagingOptions.Builder<FollowersUsers>()
                .setQuery(query,config, FollowersUsers.class)
                .build();

        adapter = new FirestorePagingAdapter<FollowersUsers, FollowersHolder>(options) {

            @NonNull
            @Override
            public FollowersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.followers_layout,parent,false);
                return new FollowersHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FollowersHolder holder, int position, @NonNull FollowersUsers model) {




                documentReference = db.collection("users").document(model.getUserUID());
                Log.d("TAG", "onBindViewHolder: "+ model.UserUID);

                documentReference.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String dataUser = documentSnapshot.getString("Username");
                                    Log.d("TAG", "onSuccess: "+ dataUser);
                                    holder.listUsername.setText(dataUser);



                                }
                            }
                        });


                // Storage reference to the user avatar image
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(model.getEmail() + "/" + model.getUserUID());

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


                holder.listButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle("Are you sure you want to REMOVE " + model.getUsername() + "?");
                        // alert.setMessage("Message");

                        alert.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //Your action here


                                Task<DocumentSnapshot> myFollowers = db.collection("users").document(userUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        task.getResult();

                                        int followingCount;

                                        followingCount= Integer.parseInt(model.getFollowing());
                                        Log.d("TAG", "ver following: " + String.valueOf(followingCount));
                                        followingCount = followingCount -1;
                                        Log.d("TAG", "ver following depois de deixar de sguir " + String.valueOf(followingCount));

                                        Log.d("TAG", "meus followers: "+ task.getResult().getString("Followers"));
                                        int followersCount = Integer.parseInt(task.getResult().getString("Followers"));
                                        Log.d("TAG", "ver followers: "+ String.valueOf(followersCount));
                                        followersCount = followersCount-1;
                                        Log.d("TAG", "followers depois de remover: "+ String.valueOf(followersCount));


                                        db.collection("users").document(model.getUserUID()).update("Following",String.valueOf(followingCount) );
                                        db.collection("users").document(model.getUserUID()).update("UsersFollowing", FieldValue.arrayRemove(userUid));
                                        db.collection("users").document(userUid).update("UsersFollowers", FieldValue.arrayRemove(model.getUserUID()));
                                        db.collection("users").document(userUid).update("Followers",String.valueOf(followersCount) );
                                        adapter.refresh();


                                    }
                                });


                            }
                        });

                        alert.setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                });

                        alert.show();


                    }
                });

            }
        };

        usersList.setLayoutManager(new LinearLayoutManager(getContext()));
        usersList.setAdapter(adapter);
        usersList.setNestedScrollingEnabled(false);



        return returnView;
    }


    private class FollowersHolder extends  RecyclerView.ViewHolder{

        private ImageView listUserImage;
        private  TextView listUsername;
        private Button listButton;


        public FollowersHolder(@NonNull View itemView) {
            super(itemView);

            listUsername =itemView.findViewById(R.id.username);
            listUserImage= itemView.findViewById(R.id.userImage);
            listButton =itemView.findViewById(R.id.removeUser);

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