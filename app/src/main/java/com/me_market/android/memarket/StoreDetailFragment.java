package com.me_market.android.memarket;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Store;

import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class StoreDetailFragment extends Fragment implements View.OnClickListener{


    public static final String STORE_DATA = "com.example.android.memarket.STORE_DATA";

    private Store storeData;
    private StoreDetailListener mListener;

    public StoreDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View myView = inflater.inflate(R.layout.fragment_store_detail, container, false);

        //Getting data from Intent
        storeData = getArguments().getParcelable(STORE_DATA);

        //Putting data into the views
        TextView textView = (TextView) myView.findViewById(R.id.companyName);
        textView.setText(storeData.CompanyData.Name);
        textView = (TextView) myView.findViewById(R.id.storeName);
        textView.setText(storeData.Name);
        textView = (TextView) myView.findViewById(R.id.storeAddress);
        textView.setText(storeData.Address);
        textView = (TextView) myView.findViewById(R.id.storePhone);
        textView.setText(storeData.Phone);

        //Setting listener
        myView.findViewById(R.id.select_this_store_button).setOnClickListener(this);
        // Inflate the layout for this fragment
        return myView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StoreDetailListener) {
            mListener = (StoreDetailListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement StoreDetailListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface StoreDetailListener {
        void onStoreConfirmed(Store s);
    }

    @Override
    public void onClick(View v){
        int i = v.getId();
        switch (i) {
            case R.id.select_this_store_button:
                mListener.onStoreConfirmed(storeData);
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        inflater.inflate(R.menu.add_item_toolbar_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //AppBar onClick method
        int i = item.getItemId();

        switch (i) {
            case R.id.select_button:
                mListener.onStoreConfirmed(storeData);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}
