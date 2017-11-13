package com.example.android.memarket;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

        //Getting reference ids
        storeName = (EditText) findViewById(R.id.storeName);
        storeAddress = (EditText) findViewById(R.id.storeAddress);
        storePhone = (EditText) findViewById(R.id.storePhone);

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.new_store_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);


        companyId = getIntent().getStringExtra(CompaniesActivity.COMPANY_ID);
        companyName = getIntent().getStringExtra(CompaniesActivity.COMPANY_NAME);
        if (companyId == null) {
            throw new IllegalArgumentException("Must pass COMPANY_ID");
        }

        TextView textView = (TextView) findViewById(R.id.companyName);
        textView.setText(companyName);
    }

    public void addStore(){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_item_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //AppBar onClick method
        int i = item.getItemId();

        switch (i) {
            case R.id.select_button:
                addStore();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}

