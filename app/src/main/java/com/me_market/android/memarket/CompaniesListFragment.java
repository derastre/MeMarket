package com.me_market.android.memarket;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Company;

import java.util.ArrayList;

public class CompaniesListFragment extends Fragment implements View.OnClickListener {

    private DatabaseReference myRef;
    private ValueEventListener companiesListener;
    private ArrayList<Company> companyArrayList;
    private ListView listView;
    private BaseActivity baseActivity;

    private CompaniesListListener mListener;

    public CompaniesListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View myView = inflater.inflate(R.layout.fragment_companies_list, container, false);

        //Floating action button
        FloatingActionButton companies_fab = myView.findViewById(R.id.add_company_fab);
        companies_fab.setOnClickListener(this);

        //Setting ListView
        listView = myView.findViewById(R.id.companyList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mListener.onCompanySelected(companyArrayList.get(position));
            }
        });

        return myView;
    }

    public void newCompany() {
        startActivity(new Intent(getActivity(), NewCompanyActivity.class));
    }

    private void removeFirebaseListener() {
        if (companiesListener != null) {
            myRef.removeEventListener(companiesListener);
        }
    }

    public void getCompaniesListFromFirebase() {
        //Obtener lista de companias de la base de datos
        baseActivity = new BaseActivity();
        baseActivity.showProgressDialog(getString(R.string.loading),getActivity());
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
                baseActivity.hideProgressDialog();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                baseActivity.hideProgressDialog();
            }

        };
        myRef.addValueEventListener(companiesListener);

    }

    private void setCompaniesNameListView() {
        //Colocar los nombres de las companias en la lista.

        Context context = getActivity();
        ArrayList<String> companiesNameList = new ArrayList<>();
        for (int i = 0; i < companyArrayList.size(); i++) {
            companiesNameList.add(companyArrayList.get(i).Name);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(context, R.layout.text_listview_layout, R.id.list_content, companiesNameList);
        listView.setAdapter(adapter);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CompaniesListListener) {
            mListener = (CompaniesListListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CompaniesListListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getCompaniesListFromFirebase();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeFirebaseListener();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface CompaniesListListener {
        void onCompanySelected(Company c);
    }

    public void setCompaniesListListener(CompaniesListListener listener) {
        this.mListener = listener;
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
