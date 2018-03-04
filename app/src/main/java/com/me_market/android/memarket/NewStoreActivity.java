package com.me_market.android.memarket;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.me_market.android.memarket.models.Company;
import com.me_market.android.memarket.models.Store;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.me_market.android.memarket.StoresListFragment.COMPANY_DATA;

public class NewStoreActivity extends AppCompatActivity {

    private EditText storeName;
    private EditText storeAddress;
    private EditText storePhone;
    private Company companyData;
    private String mCityCode;

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
        if (ab!=null)ab.setDisplayHomeAsUpEnabled(true);

        companyData = getIntent().getParcelableExtra(COMPANY_DATA);

        if (companyData == null) {
            throw new IllegalArgumentException("Must pass COMPANY_ID");
        }

        //Getting the selected city
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        mCityCode = sharedPref.getString(getString(R.string.city_pref), null);


        TextView textView = (TextView) findViewById(R.id.companyName);
        textView.setText(companyData.Name);
    }

    public void addStore(){
        String name = storeName.getText().toString();
        String address = storeAddress.getText().toString();
        String phone = storePhone.getText().toString();

        writeNewStoreOnFirebase(name,address, phone,companyData);

        finish();

    }

    public void writeNewStoreOnFirebase(String name, String address, String phone,Company companyData){
        // Write to the database

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child(mCityCode);
        Store store= new Store(name,address,phone,companyData);
        String key = myRef.child(getString(R.string.stores)).push().getKey();
        myRef.child(getString(R.string.stores)).child(key).setValue(store);

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

