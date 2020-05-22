package com.example.clipshot;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

public class FeedFragment extends Fragment {

    private AppCompatImageView iconSearch;
    private int SEARCHBAR_VISIBILITY = 0;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        iconSearch = Objects.requireNonNull(getActivity()).findViewById(R.id.iconSearch);

        Log.d("checkClick", String.valueOf(SEARCHBAR_VISIBILITY));

        iconSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View searchBarContainer = Objects.requireNonNull(getActivity()).findViewById(R.id.searchBarContainer);

                if (SEARCHBAR_VISIBILITY == 1) {
                    searchBarContainer.setVisibility(View.INVISIBLE);
                    Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setElevation(20f); // Float == px
                    SEARCHBAR_VISIBILITY = 0;
                } else {
                    searchBarContainer.setVisibility(View.VISIBLE);
                    Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setElevation(0f); // Float == px
                    SEARCHBAR_VISIBILITY = 1;
                }
            }
        });
    }
}