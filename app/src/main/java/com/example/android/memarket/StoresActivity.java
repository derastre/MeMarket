package com.example.android.memarket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.memarket.components.BaseActivity;
import com.example.android.memarket.models.Store;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.android.memarket.CompaniesActivity.COMPANY_ID;
import static com.example.android.memarket.CompaniesActivity.COMPANY_NAME;
import static com.example.android.memarket.CompaniesActivity.COMPANY_TYPE;

public class StoresActivity extends BaseActivity implements View.OnClickListener{

    public static final String STORE_ID = "com.example.android.memarket.STORE_ID";
    public static final String STORE_NAME = "com.example.android.memarket.STORE_NAME";
    public static final String STORE_ADDRESS = "com.example.android.memarket.STORE_ADDRESS";
    public static final String STORE_PHONE = "com.example.android.memarket.STORE_PHONE";

    private Query MyRefQuery;
    private ValueEventListener storesListener;
    private ArrayList<Store> storesArrayList;
    private ListView listView;

    private String companyId;
    private String companyName;
    private String companyType;


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

        companyId = getIntent().getStringExtra(COMPANY_ID);
        companyName = getIntent().getStringExtra(COMPANY_NAME);
        companyType = getIntent().getStringExtra(COMPANY_TYPE);

        if (companyId == null) {
            throw new IllegalArgumentException("Must pass COMPANY_ID");
        }

        TextView textView = (TextView) findViewById(R.id.companyName);
        textView.setText(companyName);
        TextView textView2 = (TextView) findViewById(R.id.companyTypeSpinner);
        textView2.setText(companyType);


        listView =  (ListView) findViewById(R.id.storeList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item value
                //String  itemValue    = (String) listView.getItemAtPosition(position);

                startActivity(new Intent(
                        StoresActivity.this, StoreActivity.class)
                        .putExtra(STORE_ID, storesArrayList.get(position).Id)
                        .putExtra(STORE_NAME, storesArrayList.get(position).Name)
                        .putExtra(STORE_ADDRESS, storesArrayList.get(position).Address)
                        .putExtra(STORE_PHONE, storesArrayList.get(position).Phone)
                        .putExtra(COMPANY_NAME,companyName)
                        .putExtra(COMPANY_ID,companyId)
                );
            }
        });
    }

    @Override
    public void onStop(){
        super.onStop();
        removeFirebaseListener();
    }

    @Override
    protected void onStart(){
        super.onStart();
        getStoresListFromFirebase();
    }

    private void removeFirebaseListener(){
        if(storesListener!=null) {
            MyRefQuery.removeEventListener(storesListener);
        }
    }

    private void getStoresListFromFirebase(){
       showProgressDialog(getString(R.string.loading));
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("stores");
        MyRefQuery = myRef.orderByChild("CompanyId").equalTo(companyId);

        storesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                storesArrayList = new ArrayList<>();
                for (DataSnapshot storeSnapshop : dataSnapshot.getChildren()) {
                    Store store = storeSnapshop.getValue(Store.class);
                    if (store != null) {
                        store.Id = storeSnapshop.getKey();
                        storesArrayList.add(store);
                    }
                    setStoresNameListView();
                    hideProgressDialog();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
            }

        };
        MyRefQuery.addValueEventListener(storesListener);

    }

    private void setStoresNameListView() {
        //Colocar los nombres de las tiendas en la lista.

        Context context = getApplicationContext();
        ArrayList<String> storesNameList = new ArrayList<>();
        for (int i = 0; i < storesArrayList.size(); i++) {
            storesNameList.add(storesArrayList.get(i).Name);
        }
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(context, R.layout.listview_layout, R.id.list_content, storesNameList);
        listView.setAdapter(adapter);
    }

    public void newStore() {
        startActivity(new Intent(
                StoresActivity.this, NewStore.class)
                .putExtra(COMPANY_ID,companyId)
                .putExtra(COMPANY_NAME,companyName)
                .putExtra(COMPANY_TYPE,companyType)
        );
    }

    @Override
    public boolean onSupportNavigateUp(){
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
