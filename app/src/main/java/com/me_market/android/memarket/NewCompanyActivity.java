package com.me_market.android.memarket;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Company;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NewCompanyActivity extends BaseActivity {

    private EditText companyName;
    private Spinner companyTypeSpinner;
    private String mCityCode;
    private DatabaseReference myRef;
    private ValueEventListener companiesTypesListener;
    private ArrayList<String> companyTypesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_company);

        //Getting widgets ids
        companyName=(EditText) findViewById(R.id.companyName);
        companyTypeSpinner = (Spinner) findViewById(R.id.companyTypeSpinner);

        //Getting the selected city
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        mCityCode = sharedPref.getString(getString(R.string.city_pref), null);

        //Setting spinner
        getCompaniesTypesListFromFirebase();

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.new_company_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab!=null) ab.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void onStop(){
        super.onStop();
        myRef.removeEventListener(companiesTypesListener);
    }

    public void addCompany(){
        String name = companyName.getText().toString();
        String type = companyTypeSpinner.getSelectedItem().toString();

        writeNewCompanyOnFirebase(name,type);

        finish();

    }

    public void writeNewCompanyOnFirebase(String name, String type){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child(mCityCode);
        Company company= new Company(name,type);
        myRef.child(getString(R.string.companies)).push().setValue(company);

    }

    public void getCompaniesTypesListFromFirebase() {

        showProgressDialog(getString(R.string.loading),NewCompanyActivity.this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child(mCityCode).child(getString(R.string.companies_types));

        companiesTypesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                companyTypesArrayList = new ArrayList<>();
                companyTypesArrayList.add("");
                for (DataSnapshot companySnapshop : dataSnapshot.getChildren()) {
                    String companyType = companySnapshop.getValue().toString();
                    if (companyType != null) {
                        companyTypesArrayList.add(companyType);
                    }
                }
                companyTypesArrayList.add(getString(R.string.add_new_company_type));
                setCompaniesTypeSpinner();
                companyTypeSpinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        int j = companyTypeSpinner.getAdapter().getCount()-1;
                        if (i==j) addNewCompanyTypeFirebase();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
            }

        };
        myRef.addValueEventListener(companiesTypesListener);

    }

    private void setCompaniesTypeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,companyTypesArrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        companyTypeSpinner.setAdapter(adapter);
    }

    private void addNewCompanyTypeFirebase() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_new_company_type);


        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.add_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String type = input.getText().toString();

                // Write to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference().child(mCityCode);
                myRef.child(getString(R.string.companies_types)).push().setValue(type);
                getCompaniesTypesListFromFirebase();

            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
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
                addCompany();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}
