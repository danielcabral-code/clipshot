package com.example.clipshot;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

public class VisitedProfileFragment extends Fragment {


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
        String pickedProfile = bundle.getString("pickedProfile");
        assert pickedProfile != null;
        Log.d("checkItem", pickedProfile);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_visited_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}