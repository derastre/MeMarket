package com.me_market.android.memarket;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.me_market.android.memarket.components.BaseActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.me_market.android.memarket.StateListFragment.COUNTRY_CODE;
import static com.me_market.android.memarket.StateListFragment.COUNTRY_NAME;
import static com.me_market.android.memarket.CityListFragment.STATE_NAME;
import static com.me_market.android.memarket.CityListFragment.STATE_CODE;
import static com.me_market.android.memarket.MainActivity.SHARED_PREF;

public class SelectCityActivity extends BaseActivity implements CountryListFragment.CountryListListener,
        CityListFragment.CityListListener, StateListFragment.StateListListener,View.OnClickListener {

    private static final int RC_HANDLE_LOCATION_PERM = 3;
    private static final String TAG = "Location";
    private static final int REQUEST_CHECK_SETTINGS = 9;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "mRequestingLocationUpdates";
    private FusedLocationProviderClient mFusedLocationClient;
    private String cityName;
    private String countryName;
    private String countryCode;
    private int attemps;
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private View dialogView;

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
            ab.setTitle(R.string.select_area);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        //FAB Button
        FloatingActionButton addCityFab = (FloatingActionButton) findViewById(R.id.add_city_fab);
        addCityFab.setOnClickListener(this);

    }

    @Override
    public void onCountrySelected(String code, String name) {
        //Start Stores List Fragment and pass the selected company
        StateListFragment stateListFragment = new StateListFragment();
        Bundle args = new Bundle();
        args.putString(COUNTRY_CODE, code);
        args.putString(COUNTRY_NAME, name);

        stateListFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container_city, stateListFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onStateSelected(String stateCode, String stateName, String countryCode) {
        //Start Stores List Fragment and pass the selected company
        CityListFragment cityListFragment = new CityListFragment();
        Bundle args = new Bundle();
        args.putString(STATE_CODE, stateCode);
        args.putString(STATE_NAME, stateName);
        args.putString(COUNTRY_CODE, countryCode);

        cityListFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container_city, cityListFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCitySelected(String city, String country) {
        SharedPreferences sharedPref = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.area_pref), city);
        editor.putString(getString(R.string.country_pref), country);
        editor.commit();

        finish();
    }



    private void getlocation() {
        //Setting GPS attemps back to 0
        attemps = 0;

        showProgressDialog(getString(R.string.getting_location), this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        showProgressDialog(getString(R.string.loading), SelectCityActivity.this);
        if (ActivityCompat.checkSelfPermission(SelectCityActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare sit uations this can be null.
                            if (location != null) {
                                findCountryCityGeocode(location);
                            } else {
                                findCurrentLocation();
                            }
                        }
                    });
        } else {
            requestLocationPermission();
        }
    }

    private void findCountryCityGeocode(Location location) {
        updateProgressDialogMessage(getString(R.string.getting_area_name));
        //View where we add a new city
        dialogView = getLayoutInflater().inflate(R.layout.dialog_add_country_city, null);

        Geocoder geocoder = new Geocoder(SelectCityActivity.this, Locale.getDefault());
        try {
            List<Address> list = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
            if (list != null && list.size() > 0) {
                Address address = list.get(0);
                countryName = address.getCountryName();
                countryCode = address.getCountryCode();
                cityName = address.getAdminArea();
                if (cityName != null) {
                    TextView textView = dialogView.findViewById(R.id.country_name_textview);
                    textView.setText(countryName);
                    textView = dialogView.findViewById(R.id.country_code_textview);
                    textView.setText(countryCode);
                    textView = dialogView.findViewById(R.id.city_name_textview);
                    textView.setText(cityName);
                    hideProgressDialog();
                    add_new_city();
                } else {
                    findCurrentLocation();
                }
            }
        } catch (IOException e) {
            Log.e("Location", "Impossible to connect to Geocoder", e);
        }
    }

    private void findCurrentLocation() {
        String message = getString(R.string.getting_gps_signal) + " " + attemps;
        updateProgressDialogMessage(message);
        if (attemps == 0) {
            createLocationRequest();
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    attemps = attemps + 1;

                    Location location = locationResult.getLastLocation();
                    //for (Location location : locationResult.getLocations()) {
                    findCountryCityGeocode(location);
                    //}
                }

                ;
            };
        } else if (attemps == 5) {
            stopLocationUpdates();
            hideProgressDialog();
            Snackbar.make(findViewById(R.id.fragment_container_city), R.string.area_location_error,
                    Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void add_new_city() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Set up the buttons
        builder.setPositiveButton(R.string.add_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
        //dialog = builder.create();
        //dialog.show();
    }

    private void requestLocationPermission() {
        Log.w(TAG, "Location permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(SelectCityActivity.this, permissions, RC_HANDLE_LOCATION_PERM);
            //this.recreate();
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_LOCATION_PERM);
            }
        };

        findViewById(R.id.fragment_container_city).setOnClickListener(listener);
        Snackbar.make(findViewById(R.id.fragment_container_city), R.string.permission_location_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(SelectCityActivity.this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(SelectCityActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                if (ActivityCompat.checkSelfPermission(SelectCityActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback,
                            null /* Looper */);
                }
            }
        });

        task.addOnFailureListener(SelectCityActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(SelectCityActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.add_city_fab) {
            getlocation();
        }
    }
}
