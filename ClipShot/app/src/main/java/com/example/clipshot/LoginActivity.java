package com.example.clipshot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener {

    //Declaring variables from Firebase and Google SignIn and progess bar
    private static final int RC_SIGN_IN = 123;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        mAuth=FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.progress_circular);

        // Configure sign-in to request the user's ID, email address, and basic profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Applying Button SingIn style
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setColorScheme(SignInButton.COLOR_LIGHT);

        //If an account is logged in Firebase the user will skip the Login Page and will go to the Feed Page (Main Activity/Feed Fragment)
        if (mAuth.getCurrentUser() != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            updateUI(user);
        }

    }


    //SignIn function that will open dialog to choose account
    private void signIn() {
        progressBar.setVisibility(View.VISIBLE);
        signOut();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


   @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Firebase Authentication with Google Account
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                } else{
                    Log.w("AUTH", "Account is NULL");
                    Toast.makeText(LoginActivity.this, "Sign-in failed, try again later.", Toast.LENGTH_LONG).show();
                }
            } catch (ApiException e) {
                Log.w("AUTH", "Google sign in failed", e);
                Toast.makeText(LoginActivity.this, "Sign-in failed, try again later.", Toast.LENGTH_LONG).show();
            }
        }
    }


    // Firebase Google Authentication Method
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        Log.d("TAG", "firebaseAuthWithGoogle: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this,task -> {

            if (task.isSuccessful()){
                progressBar.setVisibility(View.INVISIBLE);

                Log.d("TAG", "SignIn sucess");
                boolean newuser = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getAdditionalUserInfo()).isNewUser();

                if(newuser){
                    //If it is a new user it will appear the Welcome Page
                    Log.d("TAG", "new");
                    Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
                    startActivity(intent);

                }else{
                    //If it is an existing user it will appear the Feed Page
                    Log.d("TAG", "welcome back");
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                }
            }
            else{
                //If Login fails it will appear an toast
                progressBar.setVisibility(View.INVISIBLE);
                Log.w("TAG", "failure ", task.getException());
                Toast.makeText(this, "SignIn Failed!", Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        });
    }

    // Goes to Feed page method
    private void updateUI(FirebaseUser user) {

        if (user != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
        else{
            Log.d("TAG", "updateUI:");
        }
    }

    @Override
    //Sign In Button on click method
    public void onClick(View v) {

        if (v.getId() == R.id.sign_in_button) {
            signIn();
        }
    }

    //Sign Out method
    public void signOut() {
        // Firebase sign out
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }
}