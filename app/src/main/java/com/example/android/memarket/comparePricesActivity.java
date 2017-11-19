package com.example.android.memarket;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.TextView;

import com.example.android.memarket.components.BaseActivity;
import com.example.android.memarket.models.Company;
import com.example.android.memarket.models.Store;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;

import static com.example.android.memarket.ProductActivity.PRODUCT_ID;

public class comparePricesActivity extends BaseActivity {

    private String productId;
    private ArrayList<String> storeIdList;
    private ArrayList<String> companiesIdList;
    private ArrayList<String> companiesNameList;
    private ArrayList<String> priceList;
    private ArrayList<String> storeNameList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prices);

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.prices_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        storeIdList = new ArrayList<>();
        companiesIdList = new ArrayList<>();
        companiesNameList = new ArrayList<>();
        priceList = new ArrayList<>();
        storeNameList = new ArrayList<>();
        productId = getIntent().getStringExtra(PRODUCT_ID);

        readPricesFromFirebase();

    }

    private void readPricesFromFirebase() {
        showProgressDialog(getString(R.string.loading));
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myPriceRef = database.getReference().child("prices").child(productId);
        final DatabaseReference myStoresRef = database.getReference().child("stores");
        final DatabaseReference myCompaniesRef = database.getReference().child("companies");
        companiesNameList.add(getString(R.string.company_label));
        storeNameList.add(getString(R.string.store_label));
        priceList.add(getString(R.string.price_text));
        ValueEventListener priceListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot pricesSnapshot : dataSnapshot.getChildren()) {
                    priceList.add(pricesSnapshot.getValue().toString());
                    storeIdList.add(pricesSnapshot.getKey());
                }
                ValueEventListener storeListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Store store;
                        for (int i = 0; i < storeIdList.size(); i++) {
                            store = dataSnapshot.child(storeIdList.get(i)).getValue(Store.class);
                            storeNameList.add(store.Name);
                            companiesIdList.add(store.CompanyId);
                        }
                        ValueEventListener companyListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Company company;
                                for (int i = 0; i < companiesIdList.size(); i++) {
                                    company = dataSnapshot.child(companiesIdList.get(i)).getValue(Company.class);
                                    companiesNameList.add(company.Name);
                                }
                                allDataRead();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                hideProgressDialog();
                            }
                        };
                        myCompaniesRef.addListenerForSingleValueEvent(companyListener);
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
//        Toast.makeText(this,"Success!",Toast.LENGTH_LONG).show();
//        TextView textView = (TextView) findViewById(R.id.pricesText);
//        textView.setText(productId + "\n");
//        for (int i=0;i<companiesNameList.size();i++){
//            textView.setText(textView.getText() + companiesNameList.get(i) + " " +
//            storeNameList.get(i) + " " + priceList.get(i) + "\n");
//        }
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
                        Long number = Long.parseLong(priceList.get(r));
                        titleText.setText(NumberFormat.getCurrencyInstance().format(number));
                    }
                    break;
            }

            gridLayout.addView(titleText, i);
            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
            param.rightMargin = 15;
            param.topMargin = 10;
            param.setGravity(Gravity.CENTER);
            param.columnSpec = GridLayout.spec(c);
            param.rowSpec = GridLayout.spec(r);
            if (r == 0) {
                titleText.setTextColor(getResources().getColor(R.color.secondaryTextColor));
                titleText.setAllCaps(true);
                titleText.setTypeface(Typeface.DEFAULT_BOLD);
            }
            titleText.setLayoutParams(param);
        }

//        for (int r = 0; r < row; r++) {
//            int c = 0;
//            titleText = new TextView(this);
//            titleText.setText(companiesNameList.get(r));
//            gridLayout.addView(titleText, r);
//
//            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
//            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
//            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
//            param.rightMargin = 5;
//            param.topMargin = 5;
//            param.setGravity(Gravity.CENTER);
//            param.columnSpec = GridLayout.spec(c);
//            param.rowSpec = GridLayout.spec(r);
//            titleText.setLayoutParams(param);
//        }
//
//        for (int r = 0; r < row; r++) {
//            int c = 1;
//            int j = r + row * c;
//            titleText = new TextView(this);
//            titleText.setText(storeNameList.get(r));
//            gridLayout.addView(titleText, j);
//
//            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
//            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
//            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
//            param.rightMargin = 5;
//            param.topMargin = 5;
//            param.setGravity(Gravity.CENTER);
//            param.columnSpec = GridLayout.spec(c);
//            param.rowSpec = GridLayout.spec(r);
//            titleText.setLayoutParams(param);
//        }
//
//        for (int r = 0; r < row; r++) {
//            int c = 2;
//            int j = r + row * c;
//            titleText = new TextView(this);
//            Long number = Long.parseLong(priceList.get(r));
//            titleText.setText(NumberFormat.getCurrencyInstance().format(number));
//            gridLayout.addView(titleText, j);
//
//            GridLayout.LayoutParams param = new GridLayout.LayoutParams();
//            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
//            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
//            param.rightMargin = 5;
//            param.topMargin = 5;
//            param.setGravity(Gravity.CENTER);
//            param.columnSpec = GridLayout.spec(c);
//            param.rowSpec = GridLayout.spec(r);
//            titleText.setLayoutParams(param);
//        }

        hideProgressDialog();
    }
}
