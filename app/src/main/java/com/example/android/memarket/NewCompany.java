package com.example.android.memarket;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.android.memarket.models.Company;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewCompany extends AppCompatActivity {

    private EditText companyName;
    private Spinner companyType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_company);

        //Getting widgets ids
        companyName=(EditText) findViewById(R.id.companyName);
        companyType= (Spinner) findViewById(R.id.companyType);

        //Setting spinner
        String[] Types = getResources().getStringArray(R.array.companyTypesList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,Types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        companyType.setAdapter(adapter);

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.new_company_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);


    }

    public void addCompany(View view){
        String name = companyName.getText().toString();
        String type = companyType.getSelectedItem().toString();

        writeNewCompanyOnFirebase(name,type);

        finish();

    }

    public void writeNewCompanyOnFirebase(String name, String type){
        // Write to the database

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        Company company= new Company(name,type);
        myRef.child("companies").push().setValue(company);
    }
}
