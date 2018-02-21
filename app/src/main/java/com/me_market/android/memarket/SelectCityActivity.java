package com.me_market.android.memarket;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class SelectCityActivity extends BaseActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    private TextView cityName;
    private TextView countryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        cityName = (TextView) findViewById(R.id.city_name);
        countryName = (TextView) findViewById(R.id.country_name);
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
