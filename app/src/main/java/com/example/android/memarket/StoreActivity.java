package com.example.android.memarket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.memarket.components.BaseActivity;

import static com.example.android.memarket.CompaniesActivity.COMPANY_ID;
import static com.example.android.memarket.CompaniesActivity.COMPANY_NAME;
import static com.example.android.memarket.StoresActivity.STORE_ADDRESS;
import static com.example.android.memarket.StoresActivity.STORE_ID;
import static com.example.android.memarket.StoresActivity.STORE_NAME;
import static com.example.android.memarket.StoresActivity.STORE_PHONE;
import static com.example.android.memarket.MainActivity.PREFS_FILE;

public class StoreActivity extends BaseActivity {

    private String companyName;
    private String storeName;
    private String companyId;
    private String storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.store_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setTitle("");
        ab.setDisplayHomeAsUpEnabled(true);

        //Getting data from Intent
        companyName = getIntent().getStringExtra(COMPANY_NAME);
        storeName = getIntent().getStringExtra(STORE_NAME);
        companyId = getIntent().getStringExtra(COMPANY_ID);
        storeId = getIntent().getStringExtra(STORE_ID);
        String storeAddress = getIntent().getStringExtra(STORE_ADDRESS);
        String storePhone = getIntent().getStringExtra(STORE_PHONE);

        //Putting data into the views
        TextView textView = (TextView) findViewById(R.id.companyName);
        textView.setText(companyName);
        textView = (TextView) findViewById(R.id.storeName);
        textView.setText(storeName);
        textView = (TextView) findViewById(R.id.storeAddress);
        textView.setText(storeAddress);
        textView = (TextView) findViewById(R.id.storePhone);
        textView.setText(storePhone);

    }

    public void selectStore (){
        //Saving selected Store
        SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("selectedCompanyName", companyName)
                .putString("selectedCompanyId", companyId)
                .putString("selectedStoreName", storeName)
                .putString("selectedStoreId", storeId);

        // Commit the edits!
        editor.commit();


        startActivity(new Intent(StoreActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));


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
                selectStore();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

}
