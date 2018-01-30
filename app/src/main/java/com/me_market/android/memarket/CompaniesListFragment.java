package com.me_market.android.memarket;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.me_market.android.memarket.models.Company;

import java.util.ArrayList;

public class CompaniesListFragment extends Fragment implements View.OnClickListener{

    public static final String COMPANY_DATA = "com.example.android.memarket.COMPANY_DATA";


    private DatabaseReference myRef;
    private ValueEventListener companiesListener;
    private ArrayList<Company> companyArrayList;
    private ListView listView;

    private OnFragmentInteractionListener mListener;

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

            }
        });

        return myView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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
