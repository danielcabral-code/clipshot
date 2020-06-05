package com.example.clipshot;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class SearchFragment extends Fragment {

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
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
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText searchQuery = Objects.requireNonNull(getActivity()).findViewById(R.id.searchQuery);

        searchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Uri imageUri;
                FirebaseStorage imageStorage;
                StorageReference storageReference;

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                ArrayList<String> usernames = new ArrayList<>();

                db.collection("users")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                    Map<String, Object> findUsernames = document.getData();

                                    if (Objects.requireNonNull(findUsernames.get("Username")).toString().contains(s.toString().toLowerCase()) && s.toString().toLowerCase().length() >= 2) {

                                        usernames.add((String) findUsernames.get("Username"));
                                        Log.d("checkTAG", String.valueOf(usernames));
                                    }
                                }
                            } else {
                                Log.d("checkTAG", "Error getting documents: ", task.getException());
                            }
                        });
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}