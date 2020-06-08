package com.example.clipshot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import java.util.Objects;

public class FeedFragment extends Fragment {

    private AppCompatImageView iconSearch;
    private EditText searchQuery;
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Trying to make this close keyboard when changing to this fragment from SearchFragment (not working yet)
        final InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.hideSoftInputFromWindow(Objects.requireNonNull(view).getWindowToken(), 0);

        iconSearch = Objects.requireNonNull(getActivity()).findViewById(R.id.iconSearch);

        Log.d("checkClick", String.valueOf(SEARCHBAR_VISIBILITY));

        iconSearch.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View v) {

                View searchBarContainer = Objects.requireNonNull(getActivity()).findViewById(R.id.searchBarContainer);
                View feedContents = Objects.requireNonNull(getActivity()).findViewById(R.id.feedContents);

                if (SEARCHBAR_VISIBILITY == 1) {
                    searchBarContainer.setVisibility(View.INVISIBLE);
                    Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setElevation(20f); // Float == px

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, -180, 0, 0);
                    feedContents.setLayoutParams(layoutParams);

                    SEARCHBAR_VISIBILITY = 0;
                } else {
                    searchBarContainer.setVisibility(View.VISIBLE);
                    Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setElevation(0f); // Float == px

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, 10, 0, 0);
                    feedContents.setLayoutParams(layoutParams);

                    SEARCHBAR_VISIBILITY = 1;
                }
            }
        });

        searchQuery = Objects.requireNonNull(getActivity()).findViewById(R.id.searchQuery);
        searchQuery.setOnFocusChangeListener(focusListener);
    }

    private View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {

            if (hasFocus){
                ((MainActivity) Objects.requireNonNull(getActivity())).openFragment(SearchFragment.newInstance("",""));
            }
        }
    };
}