package com.example.clipshot;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

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

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference;
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(container.getContext());

        View returnView = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView realName = returnView.findViewById(R.id.realName);
        TextView bio = returnView.findViewById(R.id.bio);
        TextView title = returnView.findViewById(R.id.gamifyTitle);
        AppCompatImageView steamIcon = returnView.findViewById(R.id.iconSteam);
        AppCompatImageView xboxIcon = returnView.findViewById(R.id.iconXbox);
        AppCompatImageView originIcon = returnView.findViewById(R.id.iconOrigin);
        AppCompatImageView psnIcon = returnView.findViewById(R.id.iconPsn);
        AppCompatImageView nintendoIcon = returnView.findViewById(R.id.iconNintendo);

        String email = acct.getEmail().toString();
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // StorageReference storageReference = null;

        documentReference = db.collection(email).document(userUid);

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
                            realName.setText(dataName);
                            bio.setText(dataBio);
                            title.setText(dataTitle);

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

                            steamIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getContext(), steamName, Toast.LENGTH_SHORT).show();
                                }
                            });

                            originIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getContext(), originName, Toast.LENGTH_SHORT).show();
                                }
                            });
                            psnIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getContext(), psnName, Toast.LENGTH_SHORT).show();
                                }
                            });
                            xboxIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getContext(), xBoxName, Toast.LENGTH_SHORT).show();
                                }
                            });
                            nintendoIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getContext(), nintendoName, Toast.LENGTH_SHORT).show();
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

        // Inflate the layout for this fragment
        return returnView;
    }
}