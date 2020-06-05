package com.example.clipshot;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View v) {

                View searchBarContainer = Objects.requireNonNull(getActivity()).findViewById(R.id.searchBarContainer);
                EditText searchQuery = Objects.requireNonNull(getActivity()).findViewById(R.id.searchQuery);
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

                    searchQuery.setOnTouchListener(new View.OnTouchListener() {
                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {

                            ((MainActivity) getActivity()).openFragment(SearchFragment.newInstance("",""));

                            return true;
                        }
                    });
                }
            }
        });
    }
}