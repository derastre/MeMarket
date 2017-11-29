package com.me_market.android.memarket;

import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Store;

import java.io.IOException;
import java.util.ArrayList;

public class StoreActivity extends BaseActivity implements View.OnClickListener{

    public static final String PREFS_FILE = "MyPrefsFile";

    private Store storeData;

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
        storeData = getIntent().getParcelableExtra(StoresActivity.STORE_DATA);

        //Putting data into the views
        TextView textView = (TextView) findViewById(R.id.companyName);
        textView.setText(storeData.CompanyData.Name);
        textView = (TextView) findViewById(R.id.storeName);
        textView.setText(storeData.Name);
        textView = (TextView) findViewById(R.id.storeAddress);
        textView.setText(storeData.Address);
        textView = (TextView) findViewById(R.id.storePhone);
        textView.setText(storeData.Phone);

        //Setting listener
        findViewById(R.id.select_this_store_button).setOnClickListener(this);
    }

    public void selectStore (){
        //Saving selected Store
//        SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putString("selectedCompanyName", storeData.CompanyData.Name)
//                .putString("selectedCompanyId", storeData.CompanyData.getId())
//                .putString("selectedStoreName", storeData.Name)
//                .putString("selectedStoreId", storeData.getId());
//
//        // Commit the edits!
//        editor.commit();
        ArrayList<Store> stores = new ArrayList<>();
        stores.add(storeData);
        try {
            writeObjectsToFile(PREFS_FILE, stores, getApplicationContext());
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        }

        finish();
        //startActivity(new Intent(StoreActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));


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

    @Override
    public void onClick(View v){
        int i = v.getId();
        switch (i) {
            case R.id.select_this_store_button:
                selectStore();
                break;
        }
    }
}
