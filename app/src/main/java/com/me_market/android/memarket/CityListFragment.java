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


/**
 * A simple {@link Fragment} subclass.
 */
public class CityListFragment extends Fragment {

    public static final String COUNTRY_CODE = "com.example.android.memarket.COUNTRY_CODE";
    private DatabaseReference myRef;
    private ValueEventListener cityListener;
    private ArrayList<String> cityNameArrayList;
    private ArrayList<String> cityCodeArrayList;
    private String selectedCountryCode;
    private ListView listView;
    private BaseActivity baseActivity;
    private CityListListener mListener;

    public CityListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_city_list, container, false);

        selectedCountryCode = getArguments().getString(COUNTRY_CODE);

        //Setting ListView
        listView = myView.findViewById(R.id.countriesList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mListener.onCitySelected(cityCodeArrayList.get(position));
            }
        });

        return myView;

    }

    private void removeFirebaseListener() {
        if (cityListener != null) {
            myRef.removeEventListener(cityListener);
        }
    }

    public void getCitiesListFromFirebase() {
        //Obtener lista de companias de la base de datos
        baseActivity = new BaseActivity();
        baseActivity.showProgressDialog(getString(R.string.loading),getActivity());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child(getString(R.string.locations)).child(selectedCountryCode).child(getString(R.string.cities));

        cityListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cityNameArrayList = new ArrayList<>();
                cityCodeArrayList = new ArrayList<>();
                for (DataSnapshot countrySnapshop : dataSnapshot.getChildren()) {
                    if (countrySnapshop.getValue()!=null){
                        cityNameArrayList.add(countrySnapshop.getValue().toString());
                        cityCodeArrayList.add(countrySnapshop.getKey());
                    }
                }
                setCitiesNameListView();
                baseActivity.hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                baseActivity.hideProgressDialog();
            }

        };
        myRef.addValueEventListener(cityListener);

    }

    private void setCitiesNameListView() {
        //Colocar los nombres de las companias en la lista.

        Context context = getActivity();
//        ArrayList<String> companiesNameList = new ArrayList<>();
//        for (int i = 0; i < countryNameArrayList.size(); i++) {
//            companiesNameList.add(countryNameArrayList.get(i));
//        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(context, R.layout.text_listview_layout, R.id.list_content, cityNameArrayList);
        listView.setAdapter(adapter);

    }

    public interface CityListListener {
        void onCitySelected(String c);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CityListListener) {
            mListener = (CityListListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement CityListListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getCitiesListFromFirebase();
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
