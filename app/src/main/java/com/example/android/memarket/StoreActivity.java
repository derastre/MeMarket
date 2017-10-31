package com.example.android.memarket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import static com.example.android.memarket.CompaniesActivity.COMPANY_ID;
import static com.example.android.memarket.CompaniesActivity.COMPANY_NAME;
import static com.example.android.memarket.StoresActivity.STORE_ADDRESS;
import static com.example.android.memarket.StoresActivity.STORE_ID;
import static com.example.android.memarket.StoresActivity.STORE_NAME;
import static com.example.android.memarket.StoresActivity.STORE_PHONE;
import static com.example.android.memarket.MainActivity.PREFS_FILE;

public class StoreActivity extends AppCompatActivity {

    private String companyName;
    private String storeName;
    private String companyId;
    private String storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        companyName = getIntent().getStringExtra(COMPANY_NAME);
        storeName = getIntent().getStringExtra(STORE_NAME);
        companyId = getIntent().getStringExtra(COMPANY_ID);
        storeId = getIntent().getStringExtra(STORE_ID);
        String storeAddress = getIntent().getStringExtra(STORE_ADDRESS);
        String storePhone = getIntent().getStringExtra(STORE_PHONE);

        TextView textView = (TextView) findViewById(R.id.companyName);
        textView.setText(companyName);
        TextView textView1 = (TextView) findViewById(R.id.storeName);
        textView1.setText(storeName);
        TextView textView2 = (TextView) findViewById(R.id.storeAddress);
        textView2.setText(storeAddress);
        TextView textView3 = (TextView) findViewById(R.id.storePhone);
        textView3.setText(storePhone);

    }

    public void selectStore (View view){
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

    public void cancelButton(View view){
        finish();
    }

}
