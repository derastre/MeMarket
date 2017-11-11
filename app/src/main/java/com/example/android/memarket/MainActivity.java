package com.example.android.memarket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.android.memarket.CompaniesActivity.COMPANY_ID;
import static com.example.android.memarket.CompaniesActivity.COMPANY_NAME;
import static com.example.android.memarket.SplashActivity.USER_EMAIL_VERIFICATION;
import static com.example.android.memarket.SplashActivity.USER_ID;
import static com.example.android.memarket.SplashActivity.USER_PICTURE;
import static com.example.android.memarket.StoresActivity.STORE_ID;
import static com.example.android.memarket.StoresActivity.STORE_NAME;
import static com.example.android.memarket.SplashActivity.USER_NAME;


public class MainActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    public static final String FROM_MAIN = "com.example.android.memarket.FROM_MAIN";
    public static final String PREFS_FILE = "MyPrefsFile";

    private String selectedCompanyName;
    private String selectedCompanyId;
    private String selectedStoreName;
    private String selectedStoreId;
    private String mUserId;
    private String mUserName;
    private Boolean mEmailVerified;
    private Toolbar mainToolbar;
    private ActionBarDrawerToggle mainActionBarDrawerToggle;
    private DrawerLayout mainDrawerLayout;
    private NavigationView mainNavigationView;
    private String pictureUri;

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

        //Setting button listeners
        findViewById(R.id.main_floating_button).setOnClickListener(this);
        findViewById(R.id.got_it_button).setOnClickListener(this);

        // Restore preferences from file
        SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
        selectedCompanyName = settings.getString("selectedCompanyName", null);
        selectedStoreName = settings.getString("selectedStoreName", null);
        selectedCompanyId = settings.getString("selectedCompanyId", null);
        selectedStoreId = settings.getString("selectedStoreId", null);


        //Check that there is information on the file
        TextView textView;

        //Get user name and picture then show it on Navigation drawer.
        mUserId = getIntent().getStringExtra(USER_ID);
        mUserName = getIntent().getStringExtra(USER_NAME);
        mEmailVerified = getIntent().getBooleanExtra(USER_EMAIL_VERIFICATION, true);
        pictureUri = getIntent().getStringExtra(USER_PICTURE);

        if (mUserId == null) getUserFirebaseData();
        View header = mainNavigationView.getHeaderView(0);
        textView = header.findViewById(R.id.userName);
        ImageView profileAvatar = header.findViewById(R.id.profilePicture);
        Glide.with(this)
                .load(pictureUri)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .animate(R.anim.fade_in)
                .centerCrop()
                .transform(new CircleTransform(this))
                .into(profileAvatar);

        if (mEmailVerified) {
            textView.setText(mUserName);
        } else {
            textView.setText(mUserName + "\n" + getString(R.string.verify_email));
        }

    }

    public void searchProducts() {

        //if (selectedCompanyId != null && selectedStoreId !=null) {
        Intent intent = new Intent(this, ProductActivity.class);

        //FROM_MAIN para indicarle que la actividad se inicio desde MainActivity.
        intent.putExtra(FROM_MAIN, true)
                .putExtra(COMPANY_ID, selectedCompanyId)
                .putExtra(COMPANY_NAME, selectedCompanyName)
                .putExtra(STORE_ID, selectedStoreId)
                .putExtra(STORE_NAME, selectedStoreName)
                .putExtra(USER_ID, mUserId);

        startActivity(intent);
        //}else{
        //    Toast.makeText(this,getString(R.string.no_store_selected),Toast.LENGTH_LONG).show();
        //}
    }

    public void gotoStores() {
        startActivity(new Intent(this, CompaniesActivity.class));
    }

    public void gotoMyProfile() {
        startActivity(new Intent(this, MyProfileActivity.class));
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
            mEmailVerified = currentUser.isEmailVerified();
            Uri uri = currentUser.getPhotoUrl();
            pictureUri = uri.toString();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
}
