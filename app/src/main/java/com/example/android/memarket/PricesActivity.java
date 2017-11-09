package com.example.android.memarket;


import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.memarket.components.BaseActivity;
import com.example.android.memarket.models.Company;
import com.example.android.memarket.models.Store;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.android.memarket.ProductActivity.PRODUCT_CODE;

public class PricesActivity extends BaseActivity {

    private String productId;
    private ArrayList<String> storeIdList ;
    private ArrayList<String> companiesIdList ;
    private ArrayList<String> companiesNameList;
    private ArrayList<String> priceList;
    private ArrayList<String> storeNameList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prices);


        storeIdList = new ArrayList<>();
        companiesIdList = new ArrayList<>();
        companiesNameList = new ArrayList<>();
        priceList = new ArrayList<>();
        storeNameList = new ArrayList<>();
        productId = getIntent().getStringExtra(PRODUCT_CODE);

        readPricesFromFirebase();

    }

    private void readPricesFromFirebase(){
        showProgressDialog(getString(R.string.loading));
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myPriceRef = database.getReference().child("prices").child(productId);
        final DatabaseReference myStoresRef = database.getReference().child("stores");
        final DatabaseReference myCompaniesRef = database.getReference().child("companies");

        ValueEventListener priceListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot pricesSnapshot:dataSnapshot.getChildren()) {
                    priceList.add(pricesSnapshot.getValue().toString());
                    storeIdList.add(pricesSnapshot.getKey());
                }
                ValueEventListener storeListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Store store;
                        for (int i=0;i<storeIdList.size();i++){
                            store = dataSnapshot.child(storeIdList.get(i)).getValue(Store.class);
                            storeNameList.add(store.Name);
                            companiesIdList.add(store.CompanyId);
                        }
                        ValueEventListener companyListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Company company;
                                for (int i=0;i<companiesIdList.size();i++){
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

    private void allDataRead(){
        Toast.makeText(this,"Success!",Toast.LENGTH_LONG).show();
        TextView textView = (TextView) findViewById(R.id.pricesText);
        textView.setText(productId + "\n");
        for (int i=0;i<companiesNameList.size();i++){
            textView.setText(textView.getText() + companiesNameList.get(i) + " " +
            storeNameList.get(i) + " " + priceList.get(i) + "\n");
        }
        hideProgressDialog();
    }
}
