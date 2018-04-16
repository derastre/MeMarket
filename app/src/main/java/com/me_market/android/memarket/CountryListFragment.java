package com.me_market.android.memarket;


import android.content.Context;
import android.os.Bundle;
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

import java.util.ArrayList;

public class CountryListFragment extends Fragment {
    private DatabaseReference myRef;
    private ValueEventListener countryListener;
    private ArrayList<String> countryNameArrayList;
    private ArrayList<String> countryCodeArrayList;
    private ListView listView;
    private BaseActivity baseActivity;
    private CountryListListener mListener;

    public CountryListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_country_list, container, false);

        //Setting ListView
        listView = myView.findViewById(R.id.countriesList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mListener.onCountrySelected(countryCodeArrayList.get(position),countryNameArrayList.get(position));
            }
        });

        return myView;
    }

    private void removeFirebaseListener() {
        if (countryListener != null) {
            myRef.removeEventListener(countryListener);
        }
    }

    public void getCountryListFromFirebase() {
        //Obtener lista de companias de la base de datos
        baseActivity = new BaseActivity();
        baseActivity.showProgressDialog(getString(R.string.loading),getActivity());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child(getString(R.string.locations_fb));

        countryListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                countryNameArrayList = new ArrayList<>();
                countryCodeArrayList = new ArrayList<>();
                for (DataSnapshot countrySnapshop : dataSnapshot.getChildren()) {
                    if (countrySnapshop.getValue()!=null){
                        try {
                            countryNameArrayList.add(countrySnapshop.child(getString(R.string.country_name_fb)).getValue().toString());
                        } catch (NullPointerException e){
                            countryNameArrayList.add(getString(R.string.empty));
                        }

                        countryCodeArrayList.add(countrySnapshop.getKey());
                    }
                }
                setCountriesNameListView();
                baseActivity.hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                baseActivity.hideProgressDialog();
            }

        };
        myRef.addValueEventListener(countryListener);

    }

    private void setCountriesNameListView() {
        //Colocar los nombres de las companias en la lista.

        Context context = getActivity();
//        ArrayList<String> companiesNameList = new ArrayList<>();
//        for (int i = 0; i < countryNameArrayList.size(); i++) {
//            companiesNameList.add(countryNameArrayList.get(i));
//        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(context, R.layout.text_listview_layout, R.id.list_content, countryNameArrayList);
        listView.setAdapter(adapter);

    }

    public interface CountryListListener {
        void onCountrySelected(String code, String name);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CountryListListener) {
            mListener = (CountryListListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CountryListListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getCountryListFromFirebase();
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
}
