package com.example.android.memarket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.memarket.components.BaseActivity;
import com.example.android.memarket.models.Company;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CompaniesActivity extends BaseActivity implements View.OnClickListener{

    public static final String COMPANY_ID = "com.example.android.memarket.COMPANY_ID";
    public static final String COMPANY_NAME = "com.example.android.memarket.COMPANY_NAME";
    public static final String COMPANY_TYPE = "com.example.android.memarket.COMPANY_TYPE";

    private DatabaseReference myRef;
    private ValueEventListener companiesListener;
    private ArrayList<Company> companyArrayList;

    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_companies);


        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.companies_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        //Floating action button
        FloatingActionButton companies_fab = (FloatingActionButton) findViewById(R.id.add_company_fab);
        companies_fab.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.companyList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                startActivity(new Intent(
                        CompaniesActivity.this, StoresActivity.class)
                        .putExtra(COMPANY_ID, companyArrayList.get(position).Id)
                        .putExtra(COMPANY_NAME, companyArrayList.get(position).Name)
                        .putExtra(COMPANY_TYPE, companyArrayList.get(position).Type)
                );
            }
        });


    }

    @Override
    protected void onStart(){
        super.onStart();
        getCompaniesListFromFirebase();
    }

    @Override
    public void onStop(){
        super.onStop();
        removeFirebaseListener();
    }

    public void newCompany() {
        startActivity(new Intent(this, NewCompany.class));
    }

    private void removeFirebaseListener(){
        if(companiesListener!=null) {
            myRef.removeEventListener(companiesListener);
        }
    }

    public void getCompaniesListFromFirebase() {
        //Obtener lista de companias de la base de datos
        showProgressDialog(getString(R.string.loading));
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("companies");

        companiesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                companyArrayList = new ArrayList<>();
                for (DataSnapshot companySnapshop : dataSnapshot.getChildren()) {
                    Company company = companySnapshop.getValue(Company.class);
                    if (company != null) {
                        companyArrayList.add(company);
                        company.Id = companySnapshop.getKey();
                    }


                }
                setCompaniesNameListView();
                hideProgressDialog();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
            }

        };
        myRef.addValueEventListener(companiesListener);

    }

    private void setCompaniesNameListView() {
        //Colocar los nombres de las companias en la lista.

        Context context = getApplicationContext();
        ArrayList<String> companiesNameList = new ArrayList<>();
        for (int i = 0; i < companyArrayList.size(); i++) {
            companiesNameList.add(companyArrayList.get(i).Name);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, companiesNameList);
        listView.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i) {
            case R.id.add_company_fab:
                newCompany();
                break;
        }

    }
}

