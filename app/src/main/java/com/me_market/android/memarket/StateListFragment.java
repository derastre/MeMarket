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

public class StateListFragment extends Fragment {
    public static final String COUNTRY_CODE = "com.example.android.memarket.COUNTRY_CODE";
    public static final String COUNTRY_NAME = "com.example.android.memarket.COUNTRY_NAME";
    private DatabaseReference myRef;
    private ValueEventListener stateListener;
    private ArrayList<String> stateNameArrayList;
    private ArrayList<String> stateCodeArrayList;
    private String selectedCountryCode;
    private ListView listView;
    private BaseActivity baseActivity;
    private StateListListener mListener;

    public StateListFragment() {
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
        View myView = inflater.inflate(R.layout.fragment_state_list, container, false);

        selectedCountryCode = getArguments().getString(COUNTRY_CODE);
        String name = getArguments().getString(COUNTRY_NAME);
        TextView textView = myView.findViewById(R.id.countryNameText);
        textView.setText(name);

        //Setting ListView
        listView = myView.findViewById(R.id.statesList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mListener.onStateSelected(stateCodeArrayList.get(position),stateNameArrayList.get(position),selectedCountryCode);
            }
        });

        return myView;
    }

    private void removeFirebaseListener() {
        if (stateListener != null) {
            myRef.removeEventListener(stateListener);
        }
    }

    public void getStateListFromFirebase() {
        //Obtener lista de estados de la base de datos
        baseActivity = new BaseActivity();
        baseActivity.showProgressDialog(getString(R.string.loading),getActivity());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child(getString(R.string.locations_fb)).child(selectedCountryCode).child(getString(R.string.states_fb));

        stateListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                stateNameArrayList = new ArrayList<>();
                stateCodeArrayList = new ArrayList<>();
                for (DataSnapshot stateSnapshop : dataSnapshot.getChildren()) {
                    if (stateSnapshop.getValue()!=null){
                        try {
                            stateNameArrayList.add(stateSnapshop.child(getString(R.string.state_name_fb)).getValue().toString());
                        } catch (NullPointerException e){
                            stateNameArrayList.add(getString(R.string.empty));
                        }

                        stateCodeArrayList.add(stateSnapshop.getKey());
                    }
                }
                setStatesNameListView();
                baseActivity.hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                baseActivity.hideProgressDialog();
            }

        };
        myRef.addValueEventListener(stateListener);

    }

    private void setStatesNameListView() {
        //Colocar los nombres de los estados en la lista.

        Context context = getActivity();
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(context, R.layout.listview_text, R.id.list_content, stateNameArrayList);
        listView.setAdapter(adapter);

    }

    public interface StateListListener {
        void onStateSelected(String stateCode, String stateName, String countryCode);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StateListListener) {
            mListener = (StateListListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement StateListListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getStateListFromFirebase();
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
