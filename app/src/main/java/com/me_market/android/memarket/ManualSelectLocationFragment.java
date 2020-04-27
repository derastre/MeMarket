package com.me_market.android.memarket;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Objects;

public class ManualSelectLocationFragment extends Fragment implements View.OnClickListener {

    private String mCountryCode;
    private String mCountryName;
    private String mStateName;
    private String mCityName;
    private EditText editText_city;
    private Spinner spinner_Country;
    private Spinner spinner_State;
    private Button add_city_button;
    private String[] country_Codes;
    private String[] country_Names;
    private String[] states_array;
    private ManualCityListener manualCityListener;

    public ManualSelectLocationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_manual_select_location, container, false);
        spinner_Country = myView.findViewById(R.id.spinner_Country);
        spinner_State = myView.findViewById(R.id.spinner_State);
        editText_city = (EditText) myView.findViewById(R.id.editText_city);
        editText_city.setVisibility(View.INVISIBLE);
        add_city_button = myView.findViewById(R.id.add_manual_city_button);
        add_city_button.setVisibility(View.INVISIBLE);
        add_city_button.setOnClickListener(this);
        //FAB Button
        //FloatingActionButton addCityFab = (FloatingActionButton) myView.findViewById(R.id.add_city_fab);
        //addCityFab.hide();


        //Reading resource with countries and states names
        country_Codes = getResources().getStringArray(R.array.countries_codes);
        country_Names = getResources().getStringArray(R.array.countries_names);
        states_array = getResources().getStringArray(R.array.countries_States);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(Objects.requireNonNull(getActivity()),
                R.array.countries_names, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner_Country.setAdapter(adapter);
        spinner_Country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mCountryCode = country_Codes[i];
                mCountryName = country_Names[i];
                editText_city.setVisibility(View.INVISIBLE);
                add_city_button.setVisibility(View.INVISIBLE);
                String[] state_array = states_array[i].split(",");
                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), android.R.layout.simple_spinner_item, state_array);
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_State.setAdapter(adapter1);
                spinner_State.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        editText_city.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Aparecer boton agregar city luego de que en EditText escriban 3 caracteres
        editText_city.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()>=3) add_city_button.setVisibility(View.VISIBLE);
                if(editable.length()<3) add_city_button.setVisibility(View.INVISIBLE);
            }
        });

        add_city_button.setVisibility(View.VISIBLE);

        return myView;

    }

    public void onClick(View view) {
        int i = view.getId();
        switch (i) {
            case R.id.add_manual_city_button:
                manualCityListener.onManualCitySelected(mCountryName,mCountryCode,mStateName,editText_city.getText().toString());
                break;
        }
    }

    public interface ManualCityListener {
        void onManualCitySelected(String countryName, String countryCode, String stateName, String cityName);
    }

    public interface ManualSelectLocationListener {
        void onManualSeletedLocation(String contryCode, String countreName, String stateName, String cityName);
    }
}
