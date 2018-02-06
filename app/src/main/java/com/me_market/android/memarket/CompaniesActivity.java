package com.me_market.android.memarket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Company;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CompaniesActivity extends BaseActivity implements View.OnClickListener {

    public static final String COMPANY_DATA = "com.example.android.memarket.COMPANY_DATA";

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
        if (ab != null) {
            ab.setTitle(R.string.select_company);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        //Floating action button
        FloatingActionButton companies_fab = (FloatingActionButton) findViewById(R.id.add_company_fab);
        companies_fab.setOnClickListener(this);

        //Setting ListView
        listView = (ListView) findViewById(R.id.companyList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                startActivity(new Intent(
                        CompaniesActivity.this, StoresActivity.class)
                        .putExtra(COMPANY_DATA, (Parcelable) companyArrayList.get(position))
                );
            }
        });


    }

    public void newCompany() {
        startActivity(new Intent(this, NewCompanyActivity.class));
    }

    private void removeFirebaseListener() {
        if (companiesListener != null) {
            myRef.removeEventListener(companiesListener);
        }
    }

    public void getCompaniesListFromFirebase() {
        //Obtener lista de companias de la base de datos
        showProgressDialog(getString(R.string.loading),CompaniesActivity.this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("companies");

        companiesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                companyArrayList = new ArrayList<>();
                for (DataSnapshot companySnapshop : dataSnapshot.getChildren()) {
                    Company company = companySnapshop.getValue(Company.class);
                    if (company != null) {
                        company.setId(companySnapshop.getKey());
                        companyArrayList.add(company);
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
                new ArrayAdapter<>(context, R.layout.text_listview_layout, R.id.list_content, companiesNameList);
        listView.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        getCompaniesListFromFirebase();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeFirebaseListener();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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

