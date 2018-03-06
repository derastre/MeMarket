package com.me_market.android.memarket;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.me_market.android.memarket.components.BaseActivity;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.me_market.android.memarket.CityListFragment.COUNTRY_CODE;
import static com.me_market.android.memarket.MainActivity.SHARED_PREF;

public class SelectCityActivity extends BaseActivity implements CountryListFragment.CountryListListener,
        CityListFragment.CityListListener{

    private FusedLocationProviderClient mFusedLocationClient;
    private TextView cityName;
    private TextView countryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);

        if (findViewById(R.id.fragment_container_city) != null) {

            if (savedInstanceState != null) {
                return;
            }

            CountryListFragment countryListFragment = new CountryListFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            //// companiesListFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container_city, countryListFragment).commit();
        }

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.select_city_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.select_city);
            ab.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public void onCountrySelected(String s){
        //Start Stores List Fragment and pass the selected company
        CityListFragment cityListFragment = new CityListFragment();
        Bundle args = new Bundle();
        args.putString(COUNTRY_CODE,s);
        cityListFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container_city, cityListFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCitySelected(String city, String country){
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.city_pref), city);
        editor.putString(getString(R.string.country_pref), country);
        editor.commit();

        finish();
    }

    private void locationFromGPS() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        showProgressDialog(getString(R.string.loading), this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {

                                Geocoder geocoder = new Geocoder(SelectCityActivity.this, Locale.getDefault());
                                try {
                                    List<Address> list = geocoder.getFromLocation(
                                            location.getLatitude(), location.getLongitude(), 1);
                                    if (list != null && list.size() > 0) {
                                        Address address = list.get(0);
                                        String text = "Country Code " + address.getCountryCode() + "\n"
                                                + "Country Name " + address.getCountryName() + "\n"
                                                + "Feature Name " + address.getFeatureName() + "\n"
                                                + "Locality " + address.getLocality() + "\n"
                                                + "Locale " + address.getLocale() + "\n"
                                                + "Admin area " + address.getAdminArea() + "\n"
                                                + "Phone " + address.getPhone() + "\n"
                                                + "Postal code " + address.getPostalCode() + "\n"
                                                + "Premises " + address.getPremises() + "\n"
                                                + "Sub admin Area " + address.getSubAdminArea() + "\n"
                                                + "Sub locality " + address.getSubLocality() + "\n";
                                        cityName.setText(text);
                                        countryName.setText(address.getAddressLine(0));
                                        hideProgressDialog();
                                    }
                                } catch (IOException e) {
                                    Log.e("Location", "Impossible to connect to Geocoder", e);
                                }
                            }
                        }
                    });
        }
    }
}
