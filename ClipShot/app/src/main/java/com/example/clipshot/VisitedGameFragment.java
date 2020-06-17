package com.example.clipshot;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;

public class VisitedGameFragment extends Fragment {

    private String pickedGameName;

    public VisitedGameFragment() {
        // Required empty public constructor
    }

    public static VisitedGameFragment newInstance(String param1, String param2) {
        VisitedGameFragment fragment = new VisitedGameFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setElevation(20f); // Float == px
        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setCustomView(R.layout.visited_game_action_bar_layout);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Get extras from bundle that will be used to get the visted user image and videos
        Bundle bundle = this.getArguments();
        assert bundle != null;
        pickedGameName = bundle.getString("pickedGameName");
        /*docID = bundle.getString("docID");
        visitedEmail=bundle.getString("email");*/
        assert pickedGameName != null;
        Log.d("checkItem", pickedGameName);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_visited_game, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView appBarTitle = Objects.requireNonNull(getActivity()).findViewById(R.id.appBarTitle);

        if (pickedGameName.length() > 20) {

            appBarTitle.setText(pickedGameName.substring(0, 18) + "...");
        } else {

            appBarTitle.setText(pickedGameName);
        }

    }
}