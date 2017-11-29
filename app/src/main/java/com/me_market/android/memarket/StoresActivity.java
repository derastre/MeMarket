package com.me_market.android.memarket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Company;
import com.me_market.android.memarket.models.Store;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class StoresActivity extends BaseActivity implements View.OnClickListener {

    public static final String STORE_DATA = "com.example.android.memarket.STORE_DATA";

    private Query MyRefQuery;
    private ValueEventListener storesListener;
    private ArrayList<Store> storesArrayList;
    private ListView listView;

    private Company companyData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stores);

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.stores_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.select_store);
        ab.setDisplayHomeAsUpEnabled(true);

        //Floating action button
        FloatingActionButton stores_fab = (FloatingActionButton) findViewById(R.id.add_store_fab);
        stores_fab.setOnClickListener(this);

        companyData = getIntent().getParcelableExtra(CompaniesActivity.COMPANY_DATA);

        if (companyData == null) {
            throw new IllegalArgumentException("Must pass COMPANY_ID");
        }

        TextView textView = (TextView) findViewById(R.id.companyName);
        textView.setText(companyData.Name);
        TextView textView2 = (TextView) findViewById(R.id.companyTypeSpinner);
        textView2.setText(companyData.Type);


        listView = (ListView) findViewById(R.id.storeList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item value
                //String  itemValue    = (String) listView.getItemAtPosition(position);

                startActivity(new Intent(
                        StoresActivity.this, StoreActivity.class)
                        .putExtra(STORE_DATA,(Parcelable) storesArrayList.get(position))
                );
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        removeFirebaseListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getStoresListFromFirebase();
    }

    private void removeFirebaseListener() {
        if (storesListener != null) {
            MyRefQuery.removeEventListener(storesListener);
        }
    }

    private void getStoresListFromFirebase() {
        showProgressDialog(getString(R.string.loading));
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("stores");
        MyRefQuery = myRef.orderByChild("CompanyData/id").equalTo(companyData.getId());

        storesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                storesArrayList = new ArrayList<>();
                for (DataSnapshot storeSnapshop : dataSnapshot.getChildren()) {
                    Store store = storeSnapshop.getValue(Store.class);
                    if (store != null) {
                        store.setId(storeSnapshop.getKey());
                        store.CompanyData = companyData;
                        storesArrayList.add(store);
                    }
                }
                setStoresNameListView();
                hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
            }

        };
        MyRefQuery.addValueEventListener(storesListener);

    }

    private void setStoresNameListView() {

        Context context = getApplicationContext();
        ArrayList<String> storesNameList = new ArrayList<>();
        for (int i = 0; i < storesArrayList.size(); i++) {
            storesNameList.add(storesArrayList.get(i).Name);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(context, R.layout.companies_listview_layout, R.id.list_content, storesNameList);
        listView.setAdapter(adapter);
    }

    public void newStore() {
        startActivity(new Intent(
                StoresActivity.this, NewStore.class)
                .putExtra(CompaniesActivity.COMPANY_DATA, (Parcelable) companyData)
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i) {
            case R.id.add_store_fab:
                newStore();
                break;
        }

    }
}
