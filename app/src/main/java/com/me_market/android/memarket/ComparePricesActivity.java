package com.me_market.android.memarket;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.TextView;

import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Store;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;

import static com.me_market.android.memarket.BarcodeReader.PRODUCT_ID;

public class ComparePricesActivity extends BaseActivity {

    private String productId;
    private ArrayList<String> storeIdList;
    private ArrayList<String> companiesNameList;
    private ArrayList<String> priceList;
    private ArrayList<String> storeNameList;
    private String mCityCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prices);

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.prices_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab!=null) ab.setDisplayHomeAsUpEnabled(true);

        storeIdList = new ArrayList<>();
        companiesNameList = new ArrayList<>();
        priceList = new ArrayList<>();
        storeNameList = new ArrayList<>();
        productId = getIntent().getStringExtra(PRODUCT_ID);

        //Getting the selected city
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        mCityCode = sharedPref.getString(getString(R.string.city_pref), null);

        readPricesFromFirebase();

    }

    private void readPricesFromFirebase() {
        showProgressDialog(getString(R.string.loading),ComparePricesActivity.this);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myPriceRef = database.getReference().child(mCityCode).child(getString(R.string.prices)).child(productId);
        final DatabaseReference myStoresRef = database.getReference().child(mCityCode).child(getString(R.string.stores));
        companiesNameList.add(getString(R.string.company_label));
        storeNameList.add(getString(R.string.store_label));
        priceList.add(getString(R.string.price_text));
        ValueEventListener priceListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot pricesSnapshot : dataSnapshot.getChildren()) {
                    if (pricesSnapshot.getValue() != null) {
                        priceList.add(pricesSnapshot.getValue().toString());
                        storeIdList.add(pricesSnapshot.getKey());
                    }
                }
                ValueEventListener storeListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Store store;
                        for (int i = 0; i < storeIdList.size(); i++) {
                            store = dataSnapshot.child(storeIdList.get(i)).getValue(Store.class);
                            if (store != null) {
                                storeNameList.add(store.Name);
                                companiesNameList.add(store.CompanyData.Name);
                            }
                        }
                        allDataRead();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        hideProgressDialog();
                    }
                };
                myStoresRef.addListenerForSingleValueEvent(storeListener);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
            }
        };
        myPriceRef.addListenerForSingleValueEvent(priceListener);


    }

    private void allDataRead() {

        GridLayout gridLayout = (GridLayout) findViewById(R.id.prices_gridlayout);
        int column = 3;
        int row = companiesNameList.size();
        int total = column * row;
        gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        gridLayout.setColumnCount(column);
        gridLayout.setRowCount(row + 1);
        TextView titleText;


        for (int r = 0, c = 0, i = 0; i < total; r++, i++) {
            if (r == row) {
                r = 0;
                c++;
            }
            titleText = new TextView(this);
            switch (c) {
                case 0:
                    titleText.setText(companiesNameList.get(r));
                    break;
                case 1:
                    titleText.setText(storeNameList.get(r));
                    break;
                case 2:
                    if (r == 0) {
                        titleText.setText(priceList.get(r));
                    } else {
                        Float number = Float.parseFloat(priceList.get(r));
                        titleText.setText(NumberFormat.getCurrencyInstance().format(number));
                    }
                    break;
            }

            gridLayout.addView(titleText, i);
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
            param.rightMargin = 100;
            param.topMargin = 20;
            param.setGravity(Gravity.CENTER);
            param.columnSpec = GridLayout.spec(c);
            param.rowSpec = GridLayout.spec(r);
            if (r == 0) {
                titleText.setTextColor(getResources().getColor(android.R.color.black));
                titleText.setAllCaps(true);
                titleText.setTypeface(Typeface.DEFAULT_BOLD);

            }
            titleText.setLayoutParams(param);
        }
        hideProgressDialog();
    }
}
