package com.example.clipshot;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class VisitedProfileFragment extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String pickedProfile;

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

        Bundle bundle = this.getArguments();
        assert bundle != null;
        pickedProfile = bundle.getString("pickedProfile");
        assert pickedProfile != null;
        Log.d("checkItem", pickedProfile);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_visited_profile, container, false);
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
                                        Toast.makeText(getContext(),"Steam ID: "+ dataSteam, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            originIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!dataOrigin.equals("")) {
                                        Toast.makeText(getContext(),"Origin ID: "+ dataOrigin, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            psnIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!dataPsn.equals("")) {
                                        Toast.makeText(getContext(),"PSN ID: " + dataPsn, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            xboxIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!dataXbox.equals("")) {
                                        Toast.makeText(getContext(),"Xbox ID: "+ dataXbox, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            nintendoIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!dataNintendo.equals("")) {
                                        Toast.makeText(getContext(),"Switch ID: "+ dataNintendo, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }
                });

        /*db.collection("videos")
                .whereEqualTo("Username", pickedProfile)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            int count = 0;
                            for (DocumentSnapshot document : task.getResult()) {

                                count++;
                                Log.d("TAG", "onComplete: " + count);
                                Log.d("TAG", "onComplete: "+ task.getResult().toString());
                            }
                            numberOfVideos.setText(String.valueOf(count));

                            TextView noVideosMessage = Objects.requireNonNull(getActivity()).findViewById(R.id.noVideosMessage);
                            if (count == 0) {
                                noVideosMessage.setText("You haven't shared any Clips yet...");
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });*/
    }
}