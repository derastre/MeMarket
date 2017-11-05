package com.example.android.memarket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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

import static com.example.android.memarket.CompaniesActivity.COMPANY_ID;
import static com.example.android.memarket.CompaniesActivity.COMPANY_NAME;
import static com.example.android.memarket.SplashActivity.USER_EMAIL_VERIFICATION;
import static com.example.android.memarket.SplashActivity.USER_EMAIL;
import static com.example.android.memarket.SplashActivity.USER_PICTURE;
import static com.example.android.memarket.StoresActivity.STORE_ID;
import static com.example.android.memarket.StoresActivity.STORE_NAME;


public class MainActivity extends BaseActivity implements View.OnClickListener,NavigationView.OnNavigationItemSelectedListener {

    public static final String FROM_MAIN = "com.example.android.memarket.FROM_MAIN";
    public static final String PREFS_FILE = "MyPrefsFile";

    private String selectedCompanyName;
    private String selectedCompanyId;
    private String selectedStoreName;
    private String selectedStoreId;
    private String mUserEmail;
    private Boolean mEmailVerified;
    private Toolbar mainToolbar;
    private ActionBarDrawerToggle mainActionBarDrawerToggle;
    private DrawerLayout mainDrawerLayout;
    private NavigationView mainNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting Toolbar and Navigation Drawer
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        mainDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        setSupportActionBar(mainToolbar);
        mainActionBarDrawerToggle = new ActionBarDrawerToggle(this,mainDrawerLayout,mainToolbar,R.string.open,R.string.close);
        mainDrawerLayout.addDrawerListener(mainActionBarDrawerToggle);
        mainActionBarDrawerToggle.syncState();
        mainNavigationView = (NavigationView) findViewById(R.id.main_navigation);
        mainNavigationView.setNavigationItemSelectedListener(this);

        //Setting button listeners
        findViewById(R.id.main_floating_button).setOnClickListener(this);


        // Restore preferences from file
        SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
        selectedCompanyName = settings.getString("selectedCompanyName",null);
        selectedStoreName = settings.getString("selectedStoreName",null);
        selectedCompanyId = settings.getString("selectedCompanyId",null);
        selectedStoreId = settings.getString("selectedStoreId",null);


        //Check that there is information on the file
        TextView textView;
        if (selectedCompanyName!=null && selectedStoreName != null) {
            //textView = (TextView) findViewById(R.id.selectedCompany);
            //textView.setText(selectedCompanyName);
            //textView = (TextView) findViewById(R.id.selectedStore);
            //textView.setText(selectedStoreName);
        } else {
            //textView = (TextView) findViewById(R.id.selectedStore);
            //textView.setText(R.string.no_store_selected);
        }

        //Get user name and picture then show it on Navigation drawer.
        mUserEmail = getIntent().getStringExtra(USER_EMAIL);
        mEmailVerified = getIntent().getBooleanExtra(USER_EMAIL_VERIFICATION,true);
        String pictureUri = getIntent().getStringExtra(USER_PICTURE);
        View header = mainNavigationView.getHeaderView(0);
        textView = header.findViewById(R.id.userName);
        ImageView profileAvatar = header.findViewById(R.id.profilePicture);
        if (mEmailVerified){
            textView.setText(mUserEmail);
            Glide.with(this)
                    .load(pictureUri)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.error)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .animate(R.anim.fade_in)
                    .centerCrop()
                    .into(profileAvatar);
        }else{
            textView.setText(mUserEmail + "\n" +  getString(R.string.verify_email));
        }

    }

    public void searchProducts(){

        if (selectedCompanyId != null && selectedStoreId !=null) {
            Intent intent = new Intent(this, ProductActivity.class);

            //FROM_MAIN para indicarle que la actividad se inicio desde MainActivity.
            intent.putExtra(FROM_MAIN, true)
                    .putExtra(COMPANY_ID, selectedCompanyId)
                    .putExtra(COMPANY_NAME, selectedCompanyName)
                    .putExtra(STORE_ID, selectedStoreId)
                    .putExtra(STORE_NAME, selectedStoreName);

            startActivity(intent);
        }else{
            Toast.makeText(this,getString(R.string.no_store_selected),Toast.LENGTH_LONG).show();
        }
    }

    public void gotoStores (){
        startActivity(new Intent(this, CompaniesActivity.class));
    }

    public void gotoMyProfile(){
        startActivity(new Intent(this, MyProfileActivity.class));
    }

    @Override
    public void onClick(View v){
        int i = v.getId();
        if (i==R.id.find_products_button){
            searchProducts();
        }else if (i==R.id.main_floating_button) {
             searchProducts();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //AppBar onClick method
        int i = item.getItemId();

        switch (i) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Toast.makeText(this,"Settings clicked!",Toast.LENGTH_LONG).show();
                return true;

            case R.id.action_scan:
                searchProducts();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.main_toolbar_menu,menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        mainActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        int i = item.getItemId();
        switch (i){
            case R.id.my_profile_button:
                gotoMyProfile();
                break;
            case R.id.select_stores_button:
                gotoStores();
                break;
        }
        return true;
    }
}
