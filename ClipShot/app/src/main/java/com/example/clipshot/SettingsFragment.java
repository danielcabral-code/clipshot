package com.example.clipshot;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Declaring sign out button from Settings fragment layout and applying one click lister to it
        Button signOutButton = view.findViewById(R.id.button_sign_out);
        signOutButton.setOnClickListener(this);


    }

    //Clicking on Logout button the user disconnect from firebase and goes to Login page
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_sign_out) {
            FirebaseAuth.getInstance().signOut();
            Intent goToLogin = new Intent(getActivity(),
                    LoginActivity.class);

            startActivity(goToLogin);
        }
    }
}
