package com.me_market.android.memarket;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Product;
import com.me_market.android.memarket.models.Sale;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.ads.MobileAds;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.me_market.android.memarket.BarcodeReader.PRODUCT_ID;
import static com.me_market.android.memarket.SplashActivity.USER_EMAIL;
import static com.me_market.android.memarket.SplashActivity.USER_EMAIL_VERIFICATION;
import static com.me_market.android.memarket.SplashActivity.USER_ID;
import static com.me_market.android.memarket.SplashActivity.USER_PICTURE;
import static com.me_market.android.memarket.SplashActivity.USER_NAME;


public class MainActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    public static final String SHARED_PREF = "com.example.android.memarket.SHARED_PREF";
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
    private AdView mAdView;
    private String mCityCode;


    //TODO: Poner ciertas cosas bajo PAIS (product_units,product_keys, etc)
    //Precios y ofertas si deberian estar bajo ciudad.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting the Ads
        MobileAds.initialize(this, "ca-app-pub-8262098314220863~2165729690");
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

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
                    //.error(R.drawable.error)
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

        //Getting the selected city
        SharedPreferences sharedPref = this.getSharedPreferences(SHARED_PREF,Context.MODE_PRIVATE);
        mCityCode = sharedPref.getString(getString(R.string.area_pref),null);
        if (mCityCode == null) {
            gotoSelectCity();
        } else {
            readOffersFromFirebase();
        }

    }

    public void searchProducts() {
        //startActivity(new Intent(this, BarcodeReader.class).putExtra(USER_ID, mUserId));
        startActivity(new Intent(this, ProductActivity.class)
                .putExtra(USER_ID, mUserId)
        );
    }

    public void gotoStores() {
        startActivity(new Intent(this, SelectStoreActivity.class));
    }

    public void gotoMyProfile() {
        startActivity(new Intent(this, MyProfileActivity.class));
    }

    public void gotoShoppingList() {
        if (mUserId != null) {
            startActivity(new Intent(this, ShoppingListActivity.class)
                    .putExtra(USER_ID, mUserId)
            );
        }
    }

    public void gotoSelectCity() {
        startActivity(new Intent(this, SelectCityActivity.class));
    }

    public void readOffersFromFirebase() {

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myOfferRef;

        //Setting the time to yesterday midnight
        Calendar dateToday = Calendar.getInstance();
        dateToday.add(Calendar.DATE, -1); //Yesterday
        dateToday.set(Calendar.HOUR_OF_DAY, 0);
        dateToday.set(Calendar.MINUTE, 0);
        Long dateStart = dateToday.getTimeInMillis();

        myOfferRef = mDatabase.getReference().child(mCityCode).child(getString(R.string.sales_history_fb));
        myOfferQuery = myOfferRef.orderByKey().startAt(dateStart.toString());
        myOfferListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {
                Sale mSale;
                ArrayList<Sale> sales = new ArrayList<>();
                if (dataSnapshot.getChildren() != null) {

                    for (DataSnapshot offerSnapshot : dataSnapshot.getChildren()) {
                        if (offerSnapshot.getValue() != null) {
                            mSale = offerSnapshot.getValue(Sale.class);
                            sales.add(mSale);
                        }
                    }
                }
                setSalesRecyclerView(sales);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        myOfferQuery.addValueEventListener(myOfferListener);
    }

    private void setSalesRecyclerView(ArrayList<Sale> sales) {
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.on_sale_recyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        salesArrayAdapter mAdapter = new salesArrayAdapter(sales);
        mRecyclerView.setAdapter(mAdapter);

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

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);

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
            case R.id.action_settings:
                //gotoSettings();
                break;
            case R.id.action_shopping_list:
                gotoShoppingList();
                break;
            case R.id.select_city_button:
                gotoSelectCity();
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

    class salesArrayAdapter extends RecyclerView.Adapter<salesArrayAdapter.ViewHolder> {

        private ArrayList<Sale> sales;

        public salesArrayAdapter(ArrayList<Sale> sales) {
            this.sales = sales;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private View row;
            private TextView name = null, type = null, price = null;
            private ImageView image;

            public ViewHolder(View row) {
                super(row);
                this.row = row;
            }

            public TextView getNameText() {
                if (this.name == null) {
                    this.name = (TextView) row.findViewById(R.id.on_sale_product_name);
                }
                return this.name;
            }

            public TextView getTypeText() {
                if (this.type == null) {
                    this.type = (TextView) row.findViewById(R.id.on_sale_product_type);
                }
                return this.type;
            }

            public TextView getPriceText() {
                if (this.price == null) {
                    this.price = (TextView) row.findViewById(R.id.on_sale_product_price);
                }
                return this.price;
            }

            public ImageView getImage() {
                if (this.image == null) {
                    this.image = (ImageView) row.findViewById(R.id.on_sale_product_image);
                }
                return this.image;
            }

        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.on_sale_recyclerview_layout, parent, false);


            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            //get the property we are displaying
            final Sale sale = sales.get(position);
            if (sale.productId != null) {
                FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
                DatabaseReference myRef = mDatabase.getReference().child("products").child(sale.productId);

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference().child("images").child(sale.productId);
                Glide.with(MainActivity.this)
                        .using(new FirebaseImageLoader())
                        .load(storageRef)
                        .into(holder.getImage());

                ValueEventListener myProductListener = new ValueEventListener() {
                    @Override

                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Product mProduct = dataSnapshot.getValue(Product.class);
                        if (mProduct != null) {
                            mProduct.setId(dataSnapshot.getKey());
                            holder.getNameText().setText(mProduct.Name);
                            holder.getTypeText().setText(mProduct.Type);
                            holder.getPriceText().setText(NumberFormat.getCurrencyInstance().format(sale.onSalePrice));

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        hideProgressDialog();
                        Snackbar.make(findViewById(R.id.main_drawer_layout), databaseError.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                };

                myRef.addListenerForSingleValueEvent(myProductListener);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MainActivity.this, ProductActivity.class)
                                .putExtra(PRODUCT_ID, sale.productId)
                                .putExtra(USER_ID, mUserId));
                    }
                });

            }
        }

        @Override
        public int getItemCount() {
            return sales.size();
        }

    }

}
