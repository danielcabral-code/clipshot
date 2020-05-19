package com.example.clipshot;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.Executor;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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

        View returnView = inflater.inflate(R.layout.fragment_settings, container, false);
        EditText realName = returnView.findViewById(R.id.realName);
        EditText bio = returnView.findViewById(R.id.bio);
        EditText title = returnView.findViewById(R.id.gamifyTitle);

        String email = acct.getEmail().toString();
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Declaring variables to later check if they are altered in settings page
        // (Yet to find a way to check an image alteration)
        ImageView profileImage = Objects.requireNonNull(getView()).findViewById(R.id.image);

        EditText displayName = Objects.requireNonNull(getActivity()).findViewById(R.id.displayName);
        EditText realName = getActivity().findViewById(R.id.realName);
        EditText bio = getActivity().findViewById(R.id.bio);
        EditText steamInput = getActivity().findViewById(R.id.steamInput);
        EditText originInput = getActivity().findViewById(R.id.originInput);
        EditText psnInput = getActivity().findViewById(R.id.psnInput);
        EditText xboxInput = getActivity().findViewById(R.id.xboxInput);
        EditText switchInput = getActivity().findViewById(R.id.switchInput);

        AppCompatImageView iconDoneSettings = Objects.requireNonNull(getActivity()).findViewById(R.id.iconDone);

        // Listener that will check if displayName has been altered, and make check icon visible is so
        displayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().length()==0){
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if realName has been altered, and make check icon visible is so
        realName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().length()==0){
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if bio has been altered, and make check icon visible is so
        bio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().length()==0){
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if steamInput has been altered, and make check icon visible is so
        steamInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().length()==0){
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if originInput has been altered, and make check icon visible is so
        originInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().length()==0){
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if psnInput has been altered, and make check icon visible is so
        psnInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().length()==0){
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if xboxInput has been altered, and make check icon visible is so
        xboxInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().length()==0){
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Listener that will check if switchInput has been altered, and make check icon visible is so
        switchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged: mudou");

                if (s.toString().trim().length()==0){
                    iconDoneSettings.setVisibility(View.INVISIBLE);
                }
                else iconDoneSettings.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Declaring sign out button from Settings fragment layout and applying OnClick listener to it
        Button signOutButton = view.findViewById(R.id.button_sign_out);
        signOutButton.setOnClickListener(this);
    }

    // Clicking on Logout button; the user disconnects from Firebase and goes to Login page
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button_sign_out) {

            FirebaseAuth.getInstance().signOut();

            Intent goToLogin = new Intent(getActivity(), LoginActivity.class);
            startActivity(goToLogin);
        }
    }
}