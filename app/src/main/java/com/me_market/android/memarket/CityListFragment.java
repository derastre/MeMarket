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
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.me_market.android.memarket.components.BaseActivity;

import java.util.ArrayList;

import static com.me_market.android.memarket.StateListFragment.COUNTRY_CODE;


/**
 * A simple {@link Fragment} subclass.
 */
public class CityListFragment extends Fragment {

    public static final String STATE_NAME = "com.me_market.android.memarket.STATE_NAME";
    private DatabaseReference myRef;
    private ValueEventListener cityListener;
    private ArrayList<String> cityNameArrayList;
    private ArrayList<String> cityCodeArrayList;
    private String selectedStateName;
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
        View myView = inflater.inflate(R.layout.fragment_city_list, container, false);
        assert getArguments() != null;
        selectedCountryCode = getArguments().getString(COUNTRY_CODE);
        selectedStateName = getArguments().getString(STATE_NAME);
        TextView textView = myView.findViewById(R.id.stateNameText);
        textView.setText(selectedStateName);
        //Setting ListView
        listView = myView.findViewById(R.id.citiesList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mListener.onCitySelected(cityCodeArrayList.get(position), selectedCountryCode);
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
        baseActivity.showProgressDialog(getString(R.string.loading), getActivity());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child(getString(R.string.locations_fb)).child(selectedCountryCode).child(getString(R.string.states_fb)).child(selectedStateName).child(getString(R.string.cities_fb));

        cityListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cityNameArrayList = new ArrayList<>();
                cityCodeArrayList = new ArrayList<>();
                for (DataSnapshot countrySnapshop : dataSnapshot.getChildren()) {
                    if (countrySnapshop.getValue() != null) {
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
        Context context = getActivity();
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(context, R.layout.listview_text, R.id.list_content, cityNameArrayList);
        listView.setAdapter(adapter);

    }

    public interface CityListListener {
        void onCitySelected(String city, String country);
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
