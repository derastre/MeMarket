package com.example.android.memarket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.memarket.models.Store;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewStore extends AppCompatActivity {

    private EditText storeName;
    private EditText storeAddress;
    private EditText storePhone;
    private String companyId;
    private String companyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_store);


        storeName = (EditText) findViewById(R.id.storeName);
        storeAddress = (EditText) findViewById(R.id.storeAddress);
        storePhone = (EditText) findViewById(R.id.storePhone);

        companyId = getIntent().getStringExtra(CompaniesActivity.COMPANY_ID);
        companyName = getIntent().getStringExtra(CompaniesActivity.COMPANY_NAME);
        if (companyId == null) {
            throw new IllegalArgumentException("Must pass COMPANY_ID");
        }

        TextView textView = (TextView) findViewById(R.id.companyName);
        textView.setText(companyName);
    }

    public void addStore(View view){
        String name = storeName.getText().toString();
        String address = storeAddress.getText().toString();
        String phone = storePhone.getText().toString();

        writeNewStoreOnFirebase(companyId,name,address, phone);

        finish();

    }

    public void writeNewStoreOnFirebase(String companyId, String name, String address, String phone){
        // Write to the database

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        Store store= new Store(name,address,phone,companyId);

        String key = myRef.child("stores").push().getKey();

        myRef.child("stores").child(key).setValue(store);
        myRef.child("companies").child(companyId).child("stores").child(key).setValue(true);

    }

    public void cancelButton(View view){
        finish();
    }
}

