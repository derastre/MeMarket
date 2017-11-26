package com.example.android.memarket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.android.memarket.components.BaseActivity;
import com.example.android.memarket.models.Product;
import com.example.android.memarket.models.Sale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;


import static com.example.android.memarket.SplashActivity.USER_EMAIL;
import static com.example.android.memarket.SplashActivity.USER_EMAIL_VERIFICATION;
import static com.example.android.memarket.SplashActivity.USER_ID;
import static com.example.android.memarket.SplashActivity.USER_PICTURE;
import static com.example.android.memarket.SplashActivity.USER_NAME;


public class MainActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private String mUserId;
    private String mUserName;
    private String mUserEmail;
    private Boolean mEmailVerified;
    private Toolbar mainToolbar;
    private ActionBarDrawerToggle mainActionBarDrawerToggle;
    private DrawerLayout mainDrawerLayout;
    private NavigationView mainNavigationView;
    private String pictureUri;
    private FloatingActionButton mainFab;
    private Query myOfferQuery;
    private ValueEventListener myOfferListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting Toolbar and Navigation Drawer
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mainDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        setSupportActionBar(mainToolbar);
        mainActionBarDrawerToggle = new ActionBarDrawerToggle(this, mainDrawerLayout, mainToolbar, R.string.open, R.string.close);
        mainDrawerLayout.addDrawerListener(mainActionBarDrawerToggle);
        mainActionBarDrawerToggle.syncState();
        mainNavigationView = (NavigationView) findViewById(R.id.main_navigation);
        mainNavigationView.setNavigationItemSelectedListener(this);

        //Setting FAB Button
        mainFab = (FloatingActionButton) findViewById(R.id.main_floating_button);
        mainFab.setOnClickListener(this);
        mainFab.show();

        //Setting button listeners
        findViewById(R.id.got_it_button).setOnClickListener(this);


        //Check that there is information on the file
        TextView textView;

        //Get user name and picture then show it on Navigation drawer.
        mUserId = getIntent().getStringExtra(USER_ID);
        mUserName = getIntent().getStringExtra(USER_NAME);
        mUserEmail = getIntent().getStringExtra(USER_EMAIL);
        mEmailVerified = getIntent().getBooleanExtra(USER_EMAIL_VERIFICATION, true);
        pictureUri = getIntent().getStringExtra(USER_PICTURE);

        if (mUserId == null) getUserFirebaseData();
        View header = mainNavigationView.getHeaderView(0);
        textView = header.findViewById(R.id.userName);
        ImageView profileAvatar = header.findViewById(R.id.profilePicture);
        if (pictureUri != null) {
            Glide.with(this)
                    .load(pictureUri)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.error)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .animate(R.anim.fade_in)
                    .centerCrop()
                    .transform(new CircleTransform(this))
                    .into(profileAvatar);
        } else profileAvatar.setVisibility(View.GONE);

        //Setting name on navigation side panel
        String displayName;
        if (mUserName != null) displayName = mUserName;
        else displayName = mUserEmail;
        if (mEmailVerified) {
            textView.setText(displayName);
        } else {
            textView.setText(displayName + "\n" + getString(R.string.verify_email));
        }
        readOffersFromFirebase();
    }

    public void searchProducts() {
        startActivity(new Intent(this, BarcodeReader.class).putExtra(USER_ID, mUserId));
    }

    public void gotoStores() {
        startActivity(new Intent(this, CompaniesActivity.class));
    }

    public void gotoMyProfile() {
        startActivity(new Intent(this, MyProfileActivity.class));
    }

    public void readOffersFromFirebase() {

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myOfferRef;


        Calendar dateToday = Calendar.getInstance();
        dateToday.add(Calendar.DATE, -1); //Yesterday
        dateToday.set(Calendar.HOUR_OF_DAY, 0);
        dateToday.set(Calendar.MINUTE, 0);
        Long dateStart = dateToday.getTimeInMillis();

        myOfferRef = mDatabase.getReference().child("sales_history");
        myOfferQuery = myOfferRef.orderByKey().startAt(dateStart.toString());
        myOfferListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {
                Sale mSale = null;
                ArrayList<Sale> sales = new ArrayList<>();
                if (dataSnapshot.getChildren() != null) {

                    for (DataSnapshot offerSnapshot : dataSnapshot.getChildren()) {
                        if (offerSnapshot.getValue() != null) {
                            mSale = offerSnapshot.getValue(Sale.class);
                            sales.add(mSale);
                        }
                    }
                }
                Toast.makeText(getApplicationContext(), mSale.userId,Toast.LENGTH_LONG).show();
                //updateProductPriceOfferUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        myOfferQuery.addValueEventListener(myOfferListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (myOfferListener != null) {
            myOfferQuery.removeEventListener(myOfferListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mainFab.hide();
    }

    @Override
    public void onResume() {
        super.onResume();
        mainFab.show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.main_floating_button) {
            searchProducts();
        } else if (i == R.id.got_it_button) {
            findViewById(R.id.welcome_card).setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //AppBar onClick method
        int i = item.getItemId();

        switch (i) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Toast.makeText(this, "Settings clicked!", Toast.LENGTH_LONG).show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mainActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int i = item.getItemId();
        switch (i) {
            case R.id.my_profile_button:
                gotoMyProfile();
                break;
            case R.id.select_stores_button:
                gotoStores();
                break;
        }
        return true;
    }

    public void getUserFirebaseData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserId = currentUser.getUid();
            mUserName = currentUser.getDisplayName();
            mUserEmail = currentUser.getEmail();
            mEmailVerified = currentUser.isEmailVerified();
            Uri uri = currentUser.getPhotoUrl();
            if (uri != null) pictureUri = uri.toString();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}
