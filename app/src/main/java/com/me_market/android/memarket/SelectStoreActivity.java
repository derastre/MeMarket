package com.me_market.android.memarket;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Company;
import com.me_market.android.memarket.models.Store;

import java.io.IOException;
import java.util.ArrayList;

public class SelectStoreActivity extends BaseActivity implements CompaniesListFragment.CompaniesListListener,
        StoresListFragment.StoresListListener,
        StoreDetailFragment.StoreDetailListener
{

    public static final String PREFS_FILE = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_store);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            CompaniesListFragment companiesListFragment = new CompaniesListFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            //// companiesListFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, companiesListFragment).commit();
        }

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.select_store_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.select_company);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onCompanySelected(Company c){

        //Start Stores List Fragment and pass the selected company
        StoresListFragment storesListFragment = new StoresListFragment();
        Bundle args = new Bundle();
        args.putParcelable(StoresListFragment.COMPANY_DATA,c);
        storesListFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, storesListFragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    @Override
    public void onStoreSelected(Store s){

        //Start Stores List Fragment and pass the selected company
        StoreDetailFragment storeDetailFragment = new StoreDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(StoreDetailFragment.STORE_DATA,s);
        storeDetailFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, storeDetailFragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    @Override
    public void onStoreConfirmed(Store s){

        ArrayList<Store> stores = new ArrayList<>();
        stores.add(s);
        try {
            writeObjectsToFile(PREFS_FILE, stores, getApplicationContext());
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        }

        finish();

    }


}
