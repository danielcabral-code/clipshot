package com.example.clipshot;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class SearchFragment extends Fragment {

    // Declaring Variables
    String userUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String currentUsername;

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

        // Call text focus and keyboard for smooth transtion from feed fragment
        EditText searchQuery = Objects.requireNonNull(getActivity()).findViewById(R.id.searchQuery);
        searchQuery.requestFocus();

        // Shows keyboard when search is focused
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);

        AppCompatImageView iconSearch = Objects.requireNonNull(getActivity()).findViewById(R.id.iconSearch);

        // Return to feed fragment if iconSearch is pressed
        iconSearch.setOnClickListener(v -> {

            ((MainActivity) Objects.requireNonNull(getActivity())).openFragment(FeedFragment.newInstance("",""));
            Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setElevation(20f); // Float == px
        });

        // Document reference of user data that will read user data
        DocumentReference documentReference = db.collection("users").document(userUid);

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                assert document != null;
                if (document.exists()) {

                    Map<String, Object> findCurrentUsername = document.getData();
                    assert findCurrentUsername != null;
                    currentUsername = (String) findCurrentUsername.get("Username");
                }
            }
        });

        // Search Functionality
        searchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // Declaring Arrays
                ArrayList<String> usernames = new ArrayList<>();
                ArrayList<String> gameNames = new ArrayList<>();

                // Gets Usernames according to user search parameters
                db.collection("users")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                    Map<String, Object> findUsernames = document.getData();

                                    // Allows user to search with Caps or non-Caps and still shows all results
                                    if (Objects.requireNonNull(findUsernames.get("Username")).toString().toLowerCase().contains(s.toString().toLowerCase()) && s.toString().toLowerCase().length() > 0) {

                                        Log.d("checkMe", String.valueOf(Objects.requireNonNull(findUsernames.get("Username")).toString().contains(s.toString().toLowerCase())));

                                        // Permits a max of 4 usernames per search to account for layout separation for GameSearch
                                        if (usernames.size() < 4) {

                                            usernames.add((String) findUsernames.get("Username"));
                                        }

                                        Log.d("checkMe", String.valueOf(usernames));

                                        // Removes ability to search for yourself
                                        if (currentUsername.contains(s.toString().toLowerCase())) {
                                            usernames.remove(currentUsername);
                                        }

                                        // Sets adapter to ListView
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.list_view_items, usernames);
                                        ListView lvData = Objects.requireNonNull(getActivity()).findViewById(R.id.lvData);
                                        lvData.setAdapter(adapter);

                                        // When user presses a profile, app gets user's data from DB fragment changes to visit that profile
                                        lvData.setClickable(true);
                                        lvData.setOnItemClickListener((arg0, arg1, position, arg3) -> {

                                            String pickedProfile = lvData.getItemAtPosition(position).toString();

                                            CollectionReference usersRef = db.collection("users");
                                            Query queryUser = usersRef.whereEqualTo("Username", pickedProfile);
                                            queryUser.get().addOnCompleteListener(task1 -> {

                                                String userVisitedUid ="";
                                                String userVisitedEmail ="";

                                                if (task1.isSuccessful()) {
                                                    for (DocumentSnapshot documentSnapshot : Objects.requireNonNull(task1.getResult())) {
                                                        String user = documentSnapshot.getString("Username");

                                                        assert user != null;
                                                        if (user.equals(pickedProfile)) {

                                                            userVisitedUid = documentSnapshot.getId();
                                                            userVisitedEmail= documentSnapshot.getString("Email");
                                                        }
                                                    }
                                                }

                                                // Sends data to VisitedProfileFragment
                                                Bundle args = new Bundle();
                                                args.putString("pickedProfile", pickedProfile);
                                                args.putString("docID",userVisitedUid);
                                                args.putString("email",userVisitedEmail);

                                                VisitedProfileFragment fragment = new VisitedProfileFragment();
                                                fragment.setArguments(args);

                                                assert getFragmentManager() != null;
                                                getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();

                                            });
                                        });
                                    } else {

                                        // Fills ListView with nothing if there are no results
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.list_view_items, usernames);
                                        ListView lvData = Objects.requireNonNull(getActivity()).findViewById(R.id.lvData);
                                        lvData.setAdapter(adapter);
                                    }
                                }
                            } else {
                                Log.d("checkTAG", "Error getting documents: ", task.getException());
                            }
                        });

                // Gets GameNames according to user search parameters
                db.collection("videos")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                    Map<String, Object> findGameNames = document.getData();

                                    if (Objects.requireNonNull(findGameNames.get("GameName")).toString().contains(s.toString()) && s.toString().length() > 0) {

                                        gameNames.add((String) findGameNames.get("GameName"));

                                        // Searches for all games but only shows each game once
                                        for (int i = 0; i < gameNames.size(); i++) {
                                            for (int j = i + 1; j < gameNames.size(); j++) {
                                                if (gameNames.get(i).equals(gameNames.get(j))) {

                                                    gameNames.remove(i);
                                                }
                                            }
                                        }

                                        // Sets adapter to listView
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.list_view_items, gameNames);
                                        ListView lvData2 = Objects.requireNonNull(getActivity()).findViewById(R.id.lvDataGames);
                                        lvData2.setAdapter(adapter);

                                        // When user presses a profile, app gets user's data from DB fragment changes to visit that profile
                                        lvData2.setClickable(true);
                                        lvData2.setOnItemClickListener((arg0, arg1, position, arg3) -> {

                                            String pickedGameName = lvData2.getItemAtPosition(position).toString();

                                            // Sends data to VisitedGameFragment
                                            Bundle args = new Bundle();
                                            args.putString("pickedGameName", pickedGameName);
                                            VisitedGameFragment fragment = new VisitedGameFragment();
                                            fragment.setArguments(args);

                                            assert getFragmentManager() != null;
                                            getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
                                        });
                                    } else {

                                        // Fills ListView with nothing if there are no results
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.list_view_items, gameNames);
                                        ListView lvData2 = Objects.requireNonNull(getActivity()).findViewById(R.id.lvDataGames);
                                        lvData2.setAdapter(adapter);
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