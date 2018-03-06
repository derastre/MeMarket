package com.me_market.android.memarket;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Company;
import com.me_market.android.memarket.models.Store;

import java.util.ArrayList;

import static com.me_market.android.memarket.MainActivity.SHARED_PREF;


/**
 * A simple {@link Fragment} subclass.
 */
public class StoresListFragment extends Fragment implements View.OnClickListener {
    public static final String COMPANY_DATA = "com.example.android.memarket.COMPANY_DATA";

    private Query MyRefQuery;
    private ValueEventListener storesListener;
    private ArrayList<Store> storesArrayList;
    private ListView listView;
    private Company companyData;
    private StoresListListener mListener;
    private BaseActivity baseActivity;
    private String mCityCode;
    private String mCountryCode;
    public StoresListFragment() {
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
        View myView = inflater.inflate(R.layout.fragment_stores_list, container, false);

        //Floating action button
        FloatingActionButton stores_fab = myView.findViewById(R.id.add_store_fab);
        stores_fab.setOnClickListener(this);

        //Getting the selected city
        SharedPreferences sharedPref = getActivity().getSharedPreferences(SHARED_PREF,Context.MODE_PRIVATE);
        mCityCode = sharedPref.getString(getString(R.string.city_pref),null);
        mCountryCode= sharedPref.getString(getString(R.string.country_pref),null);

        companyData = getArguments().getParcelable(COMPANY_DATA);

        if (companyData == null) {
            throw new IllegalArgumentException("Must pass COMPANY_ID");
        }

        TextView textView = myView.findViewById(R.id.companyNameText);
        textView.setText(companyData.Name);
        TextView textView2 = myView.findViewById(R.id.companyTypeText);
        textView2.setText(companyData.Type);


        listView = myView.findViewById(R.id.storeList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mListener.onStoreSelected(storesArrayList.get(position));
            }
        });

        return myView;
    }

    private void removeFirebaseListener() {
        if (storesListener != null) {
            MyRefQuery.removeEventListener(storesListener);
        }
    }

    private void getStoresListFromFirebase() {
        baseActivity = new BaseActivity();
        baseActivity.showProgressDialog(getString(R.string.loading),getActivity());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child(mCountryCode).child(mCityCode).child(getString(R.string.stores));
        MyRefQuery = myRef.orderByChild(getString(R.string.companydata_id)).equalTo(companyData.getId());

        storesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                storesArrayList = new ArrayList<>();
                for (DataSnapshot storeSnapshop : dataSnapshot.getChildren()) {
                    Store store = storeSnapshop.getValue(Store.class);
                    if (store != null) {
                        store.setId(storeSnapshop.getKey());
                        store.CompanyData = companyData;
                        storesArrayList.add(store);
                    }
                }
                setStoresNameListView();
                baseActivity.hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                baseActivity.hideProgressDialog();
            }

        };
        MyRefQuery.addValueEventListener(storesListener);

    }

    private void setStoresNameListView() {

        Context context = getActivity();
        ArrayList<String> storesNameList = new ArrayList<>();
        for (int i = 0; i < storesArrayList.size(); i++) {
            storesNameList.add(storesArrayList.get(i).Name);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(context, R.layout.text_listview_layout, R.id.list_content, storesNameList);
        listView.setAdapter(adapter);
    }

    public void newStore() {
        startActivity(new Intent(
                getActivity(), NewStoreActivity.class)
                .putExtra(COMPANY_DATA, (Parcelable) companyData)
        );
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StoresListListener) {
            mListener = (StoresListListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement StoresListListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getStoresListFromFirebase();
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

    public interface StoresListListener {
        void onStoreSelected(Store s);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i) {
            case R.id.add_store_fab:
                newStore();
                break;
        }

    }
}
