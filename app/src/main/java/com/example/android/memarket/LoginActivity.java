package com.example.android.memarket;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.example.android.memarket.components.BaseActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static com.example.android.memarket.SplashActivity.USER_EMAIL;
import static com.example.android.memarket.SplashActivity.USER_EMAIL_VERIFICATION;
import static com.example.android.memarket.SplashActivity.USER_ID;
import static com.example.android.memarket.SplashActivity.USER_PICTURE;

public class LoginActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener,View.OnClickListener {

    private static final String TAG = "Login";
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mUserNameField;
    private GoogleApiClient mGoogleApiClient; //Google
    private CallbackManager mCallbackManager; //Facebook

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Views
        mUserNameField = (EditText) findViewById(R.id.field_user_name);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
        findViewById(R.id.create_account_button).setOnClickListener(this);
        findViewById(R.id.cancel_button).setOnClickListener(this);
        findViewById(R.id.google_sign_in_button).setOnClickListener(this);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]


        /*
        // [START initialize_fblogin]
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        });
        // [END initialize_fblogin]
        */
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

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            String userId = user.getUid();
            String email = user.getEmail();
            Boolean emailVerification = user.isEmailVerified();
            Uri pictureUri = user.getPhotoUrl();
            String stringUri = pictureUri.toString();
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra(USER_ID,userId)
                    .putExtra(USER_EMAIL,email)
                    .putExtra(USER_EMAIL_VERIFICATION,emailVerification)
                    .putExtra(USER_PICTURE,stringUri)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        } else {
            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
            findViewById(R.id.providers_login).setVisibility(View.VISIBLE);
            findViewById(R.id.create_account_buttons).setVisibility(View.GONE);
            findViewById(R.id.field_user_name).setVisibility(View.GONE);

        }
    }

    //[START EMAIL AUTHENTICATION]
    private void createAccount(final String name, String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm(true)) {
            return;
        }

        showProgressDialog(getString(R.string.loading));

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d(TAG, "User account created.");
                            Toast.makeText(LoginActivity.this, "Account created!. " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            try {
                                Toast.makeText(LoginActivity.this, "Authentication failed. \n" + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }catch (Exception ie){
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private void emailSignIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm(false)) {
            return;
        }

        showProgressDialog(getString(R.string.loading));

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "FIREBASE Sign in Error.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private boolean validateForm(Boolean create) {
        boolean valid = true;

        if (create) {
            String username = mUserNameField.getText().toString();
            if (TextUtils.isEmpty(username)) {
                mUserNameField.setError("Required.");
                valid = false;
            } else {
                mUserNameField.setError(null);
            }
        }

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void showCreateAccountFields(Boolean show){

        EditText editText;
        if (show) {
            editText = (EditText) findViewById(R.id.field_user_name);
            editText.setVisibility(View.VISIBLE);
            editText.requestFocus();
            editText = (EditText) findViewById(R.id.field_email);
            editText.setText(null);
            editText = (EditText) findViewById(R.id.field_password);
            editText.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
            findViewById(R.id.create_account_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.providers_login).setVisibility(View.GONE);

        } else {
            editText = (EditText) findViewById(R.id.field_user_name);
            editText.setVisibility(View.GONE);
            editText = (EditText) findViewById(R.id.field_email);
            editText.setText(null);
            editText = (EditText) findViewById(R.id.field_password);
            editText.setText(null);

            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.create_account_buttons).setVisibility(View.GONE);
            findViewById(R.id.providers_login).setVisibility(View.VISIBLE);
        }
    }
    //[END EMAIL AUTHENTICATION]

    // [START GOOGLE AUTHENTICATION]
    private void googleSignIn() {
        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog(getString(R.string.loading));
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "Google onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
    // [END GOOGLE AUTHENTICATION]

    // [START FACEBOOK AUTHENTICATION]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        // [START_EXCLUDE silent]
        showProgressDialog(getString(R.string.loading));
        // [END_EXCLUDE]

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END FACEBOOK AUTHENTICATION]

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        showProgressDialog(getString(R.string.loading));
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                updateUI(null);
                Toast.makeText(this,"Login failed.",Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        } else {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
        hideProgressDialog();
    }
    // [END onactivityresult]

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            showCreateAccountFields(true);
        } else if (i == R.id.email_sign_in_button) {
            emailSignIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.create_account_button){
            createAccount(mUserNameField.getText().toString(),mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i==R.id.cancel_button){
            showCreateAccountFields(false);
        } else if (i==R.id.google_sign_in_button){
            googleSignIn();
        }
    }
}
