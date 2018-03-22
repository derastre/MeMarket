package com.me_market.android.memarket;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.components.MySuggestionProvider;
import com.me_market.android.memarket.models.Product;

import java.util.ArrayList;

import static com.me_market.android.memarket.BarcodeReader.PRODUCT_ID;
import static com.me_market.android.memarket.MainActivity.SHARED_PREF;

public class SearchActivity extends BaseActivity {

    private ArrayList<Product> products;
    private ListView listView;
    private String mCityCode;
    private String mCountryCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        handleIntent(getIntent());

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.results);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        //Getting the selected city
        SharedPreferences sharedPref = this.getSharedPreferences(SHARED_PREF,Context.MODE_PRIVATE);
        mCityCode = sharedPref.getString(getString(R.string.area_pref),null);
        mCountryCode= sharedPref.getString(getString(R.string.country_pref),null);

        //Setting ListView
        listView = (ListView) findViewById(R.id.results_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (products != null) {
                    startActivity(new Intent(SearchActivity.this, ProductActivity.class)
                            .putExtra(PRODUCT_ID, products.get(position).getId())
                    );
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setContentView(R.layout.activity_search);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            doMySearchOnFirebase(query);
        }
    }

    private void doMySearchOnFirebase(String query) {
        showProgressDialog(getString(R.string.loading),SearchActivity.this);
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mySearchRef;
        Query mySearchQuery;
        ValueEventListener mySearchListener;

        mySearchRef = mDatabase.getReference().child(mCountryCode).child(getString(R.string.products_fb));
        mySearchQuery = mySearchRef.orderByChild("Name").startAt(query).endAt(query + "\uF8FF");
        mySearchListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {
                Product product;

                if (dataSnapshot.getChildren() != null) {
                    products = new ArrayList<>();
                    for (DataSnapshot resultSnapshot : dataSnapshot.getChildren()) {
                        if (resultSnapshot.getValue() != null) {
                            product = resultSnapshot.getValue(Product.class);
                            products.add(product);
                        }
                    }
                }
                setSearchResults();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mySearchQuery.addListenerForSingleValueEvent(mySearchListener);
    }

    private void setSearchResults() {
        hideProgressDialog();
        Context context = getApplicationContext();
        ArrayList<String> resultsList = new ArrayList<>();
        if (products != null) {
            for (int i = 0; i < products.size(); i++) {
                resultsList.add(products.get(i).Name);
            }

            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(context, R.layout.text_listview_layout, R.id.list_content, resultsList);
            listView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
