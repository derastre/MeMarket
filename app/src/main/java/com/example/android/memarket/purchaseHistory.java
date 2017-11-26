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
import com.example.android.memarket.models.Purchase;
import com.example.android.memarket.models.Store;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;


import static com.example.android.memarket.BarcodeReader.PRODUCT_ID;
import static com.example.android.memarket.SplashActivity.USER_ID;

public class purchaseHistory extends BaseActivity {

    private String productId;
    private String mUserId;
    private ArrayList<String> storeIdList;
    private ArrayList<String> companiesNameList;
    private ArrayList<String> priceList;
    private ArrayList<String> storeNameList;
    private ArrayList<String> timeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_history);

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.purchase_history_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the string

        productId = getIntent().getStringExtra(PRODUCT_ID);
        mUserId = getIntent().getStringExtra(USER_ID);
        storeIdList = new ArrayList<>();
        companiesNameList = new ArrayList<>();
        priceList = new ArrayList<>();
        storeNameList = new ArrayList<>();
        timeStamp = new ArrayList<>();

        readPurchasesHistoryFromFirebase();
    }

    private void readPurchasesHistoryFromFirebase() {
        showProgressDialog(getString(R.string.loading));

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myHistoryRef = database.getReference().child("purchases").child(mUserId).child(productId);
        final DatabaseReference myStoresRef = database.getReference().child("stores");


        //Table headers
        timeStamp.add(getString(R.string.date_label));
        companiesNameList.add(getString(R.string.company_label));
        storeNameList.add(getString(R.string.store_label));
        priceList.add(getString(R.string.price_text));

        ValueEventListener historyListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot historySnapshot : dataSnapshot.getChildren()) {
                    Purchase itemPrice = historySnapshot.getValue(Purchase.class);
                    if (itemPrice != null) {
                        priceList.add(itemPrice.price.toString());
                        storeIdList.add(itemPrice.storeId);
                        timeStamp.add(historySnapshot.getKey());
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
                            allDataRead();
                        }
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
        myHistoryRef.addListenerForSingleValueEvent(historyListener);


    }

    private void allDataRead() {

        GridLayout gridLayout = (GridLayout) findViewById(R.id.purchase_history_gridlayout);
        int column = 4;
        int row = timeStamp.size();
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
                    if (r == 0) {
                        titleText.setText(timeStamp.get(r));
                    } else {
                        Long ndate = Long.parseLong(timeStamp.get(r));
                        String sdate = getDate(ndate, "dd/MM/yyyy hh:mm");
                        titleText.setText(sdate);
                    }
                    break;
                case 1:
                    titleText.setText(companiesNameList.get(r));
                    break;
                case 2:
                    titleText.setText(storeNameList.get(r));
                    break;
                case 3:
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
            param.rightMargin = 45;
            param.topMargin = 10;
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