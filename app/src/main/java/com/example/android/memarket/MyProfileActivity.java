package com.example.android.memarket;


import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.android.memarket.components.BaseActivity;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MyProfileActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "MyProfile";

    private TextView mUserNameTexView;
    private TextView mEmailTexView;
    private Button mEmailVerifiedButton;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        //Getting widgets ids
        mUserNameTexView = (TextView) findViewById(R.id.profileUserName);
        mEmailTexView = (TextView) findViewById(R.id.profileEmail);
        mEmailVerifiedButton = (Button) findViewById(R.id.verify_email_button);

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_profile_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        //Firebase authentication
        mAuth = FirebaseAuth.getInstance();

        //Setting listeners
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        mEmailVerifiedButton.setOnClickListener(this);
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // [END on_start_check_user]


    private void sendEmailVerification() {
        // Disable button
        findViewById(R.id.verify_email_button).setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // [START_EXCLUDE]
                            // Re-enable button
                            findViewById(R.id.verify_email_button).setEnabled(true);

                            if (task.isSuccessful()) {
                                Toast.makeText(MyProfileActivity.this,
                                        "Verification email sent to " + user.getEmail(),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "sendEmailVerification", task.getException());
                                Toast.makeText(MyProfileActivity.this,
                                        "Failed to send verification email.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            // [END_EXCLUDE]
                        }
                    });
            // [END send_email_verification]
        }
    }

    private void signOut() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null) {
            List providers = user.getProviders();
            if (providers != null) {
                String provider = providers.get(0).toString();
                String google = "google.com";
                String facebook = "facebook.com";
                if (google.equals(provider)) {

                    // [START config_signin]
                    // Configure Google Sign In
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();
                    // [END config_signin]
                    GoogleApiClient mGoogleApiClient;
                    mGoogleApiClient =
                            new GoogleApiClient.Builder(this)
                                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                    .build();

                    mGoogleApiClient.connect();
                    // Google sign out
                    if (mGoogleApiClient.isConnected())
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                } else if (facebook.equals(provider)) {
                    LoginManager.getInstance().logOut();
                }
            }
            mAuth.signOut();


            goToLogin();
        }
    }

    private void updateUI(FirebaseUser user){
        hideProgressDialog();
        if (user != null) {
            String email = user.getEmail();
            String name = user.getDisplayName();
            Boolean vemail = user.isEmailVerified();
            Uri pictureUri = user.getPhotoUrl();
            ImageView profileAvatar = (ImageView) findViewById(R.id.profilePicture);
            if (pictureUri!=null) {
                profileAvatar.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(pictureUri)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.error)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .animate(R.anim.fade_in)
                        .centerCrop()
                        .into(profileAvatar);
            }else profileAvatar.setVisibility(View.GONE);
            Boolean emailVerification = user.isEmailVerified();

            if(name!=null) mUserNameTexView.setText(name);

            mEmailTexView.setText(email);
            if (vemail){
                mEmailVerifiedButton.setVisibility(View.GONE);
            }else{
                mEmailVerifiedButton.setVisibility(View.VISIBLE);
            }
                
            findViewById(R.id.verify_email_button).setEnabled(!emailVerification);

        }else{
            goToLogin();
        }
    }

    private void goToLogin(){
        startActivity(new Intent(this, SplashActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
    @Override
    public void onClick(View v){
        int i = v.getId();
        if (i == R.id.sign_out_button) {
            signOut();
        } else if (i == R.id.verify_email_button) {
            sendEmailVerification();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }



}
