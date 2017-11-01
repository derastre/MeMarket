package com.example.android.memarket;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends Activity  {

    public static final String USER_EMAIL = "com.example.android.memarket.USER_EMAIL";
    public static final String USER_EMAIL_VERIFICATION = "com.example.android.memarket.USER_EMAIL_VERIFICATION";
    public static final String USER_PICTURE = "com.example.android.memarket.PICTURE";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                checkUser(currentUser);
            }
        }, 2000);
    }

    private void checkUser(FirebaseUser user){
        if (user != null) {
            String email = user.getEmail();
            Boolean emailVerification = user.isEmailVerified();
            Uri pictureUri = user.getPhotoUrl();
            stringUri = pictureUri.toString();
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra(USER_EMAIL,email)
                    .putExtra(USER_EMAIL_VERIFICATION,emailVerification)
                    .putExtra(USER_PICTURE,stringUri)      
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));


        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }


}
