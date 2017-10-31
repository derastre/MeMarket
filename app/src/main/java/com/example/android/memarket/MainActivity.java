package com.example.android.memarket;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.android.memarket.CompaniesActivity.COMPANY_ID;
import static com.example.android.memarket.CompaniesActivity.COMPANY_NAME;
import static com.example.android.memarket.SplashActivity.USER_EMAIL_VERIFICATION;
import static com.example.android.memarket.SplashActivity.USER_EMAIL;
import static com.example.android.memarket.StoresActivity.STORE_ID;
import static com.example.android.memarket.StoresActivity.STORE_NAME;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    public static final String FROM_MAIN = "com.example.android.memarket.FROM_MAIN";
    public static final String PREFS_FILE = "MyPrefsFile";

    private String selectedCompanyName;
    private String selectedCompanyId;
    private String selectedStoreName;
    private String selectedStoreId;
    private String mUserEmail;
    private Boolean mEmailVerified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting button listeners
        findViewById(R.id.find_products_button).setOnClickListener(this);
        findViewById(R.id.select_stores_button).setOnClickListener(this);
        findViewById(R.id.my_profile_button).setOnClickListener(this);

        // Restore preferences from file
        SharedPreferences settings = getSharedPreferences(PREFS_FILE, 0);
        selectedCompanyName = settings.getString("selectedCompanyName",null);
        selectedStoreName = settings.getString("selectedStoreName",null);
        selectedCompanyId = settings.getString("selectedCompanyId",null);
        selectedStoreId = settings.getString("selectedStoreId",null);


        //Check that there is information on the file
        TextView textView;
        if (selectedCompanyName!=null && selectedStoreName != null) {
            textView = (TextView) findViewById(R.id.selectedCompany);
            textView.setText(selectedCompanyName);
            textView = (TextView) findViewById(R.id.selectedStore);
            textView.setText(selectedStoreName);
        } else {
            textView = (TextView) findViewById(R.id.selectedStore);
            textView.setText(R.string.no_store_selected);
        }

        //Get user name and show it.
        mUserEmail = getIntent().getStringExtra(USER_EMAIL);
        mEmailVerified = getIntent().getBooleanExtra(USER_EMAIL_VERIFICATION,true);
        textView = (TextView) findViewById(R.id.userName);
        if (mEmailVerified){
            textView.setText(mUserEmail);
        }else{
            textView.setText(mUserEmail + "\n" + "(" + R.string.verify_email + ")");
        }

    }

    public void searchProducts(){

        if (selectedCompanyId != null && selectedStoreId !=null) {
            Intent intent = new Intent(this, ProductActivity.class);

            //FROM_MAIN para indicarle que la actividad se inicio desde MainActivity.
            intent.putExtra(FROM_MAIN, true)
                    .putExtra(COMPANY_ID, selectedCompanyId)
                    .putExtra(COMPANY_NAME, selectedCompanyName)
                    .putExtra(STORE_ID, selectedStoreId)
                    .putExtra(STORE_NAME, selectedStoreName);

            startActivity(intent);
        }else{
            Toast.makeText(this,getString(R.string.no_store_selected),Toast.LENGTH_LONG).show();
        }
    }

    public void gotoStores (){
        startActivity(new Intent(this, CompaniesActivity.class));
    }

    public void gotoMyProfile(){
        startActivity(new Intent(this, MyProfileActivity.class));
    }

    @Override
    public void onClick(View v){
        int i = v.getId();
        if (i==R.id.find_products_button){
            searchProducts();
        } else if (i==R.id.select_stores_button){
            gotoStores();
        }else if (i==R.id.my_profile_button){
            gotoMyProfile();
        }
    }

}
