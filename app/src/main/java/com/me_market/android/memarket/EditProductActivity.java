package com.me_market.android.memarket;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Product;

import java.util.ArrayList;

import static com.me_market.android.memarket.BarcodeReader.PRODUCT_BARCODE;
import static com.me_market.android.memarket.BarcodeReader.PRODUCT_ID;
import static com.me_market.android.memarket.MainActivity.SHARED_PREF;

public class EditProductActivity extends BaseActivity implements View.OnClickListener {

    private String mProductId;
    private Product mProduct;
    private Product mProductUnit;
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;
    private String mCityCode;
    private String mCountryCode;
    private ValueEventListener myProductListener;

    private ImageView productImage;
    private TextView productCode;
    private TextInputEditText productType;
    private TextInputEditText productBrand;
    private TextInputEditText productQty;
    private TextInputEditText productName;
    private TextInputEditText productUnitCode;
    private TextInputEditText productUnitName;
    private TextInputEditText productUnitDescription;
    private TextInputEditText productUnitQty;
    private CheckBox productUnitCheckbox;
    private Spinner productUnitsSpinner;
    private Spinner productUnitUnitsSpinner;
    private Button addPhoto;
    private Button selectPicture;
    private Button productEditImageButton;
    private Button productEditDescriptionButton;
    private Button productEditNameButton;
    private Button productEditBrandButton;
    private Button productEditQtyButton;
    private Button productUnitScanCodeButton;
    private Button productUnitEditNameButton;
    private Button productUnitEditDescriptionButton;
    private Button productUnitEditQtyButton;
    private ArrayList<String> unitsArrayList;

    private CardView unitDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        //Firebase database instance
        mDatabase = FirebaseDatabase.getInstance();

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        mProductId = intent.getStringExtra(PRODUCT_ID);

        //Get references to layout views
        //Product layouts
        productCode = (TextView) findViewById(R.id.editProduct_productCode);
        productName = (TextInputEditText) findViewById(R.id.editProduct_productNameInput);
        productType = (TextInputEditText) findViewById(R.id.editProduct_productTypeInput);
        productBrand = (TextInputEditText) findViewById(R.id.editProduct_productBrandInput);
        productQty = (TextInputEditText) findViewById(R.id.editProduct_productQuantityInput);
        productUnitsSpinner = (Spinner) findViewById(R.id.editProduct_spinner_unit);
        productImage = (ImageView) findViewById(R.id.editProduct_productImageInput);
        addPhoto = (Button) findViewById(R.id.editProduct_add_photo_button);
        selectPicture = (Button) findViewById(R.id.editProduct_select_picture_button);

        //Product unit layouts
        unitDetail = (CardView) findViewById(R.id.editProduct_newProductUnitDetailCardView);
        productUnitCode = (TextInputEditText) findViewById(R.id.editProduct_product_unit_code_edittext);
        productUnitName = (TextInputEditText) findViewById(R.id.editProduct_product_unit_name_edittext);
        productUnitDescription = (TextInputEditText) findViewById(R.id.editProduct_product_description_unit_edittext);
        productUnitQty = (TextInputEditText) findViewById(R.id.editProduct_product_unit_quantity_input);
        productUnitCheckbox = (CheckBox) findViewById(R.id.editProduct_product_unit_checkbox);
        productUnitUnitsSpinner = (Spinner) findViewById(R.id.editProduct_product_unit_spinner);

        productEditImageButton = (Button) findViewById(R.id.editProduct_product_edit_image_button);
        productEditNameButton= (Button) findViewById(R.id.editProduct_product_edit_name_button);
        productEditDescriptionButton = (Button) findViewById(R.id.editProduct_product_edit_description_button);
        productEditBrandButton = (Button) findViewById(R.id.editProduct_product_edit_brand_button);
        productEditQtyButton = (Button) findViewById(R.id.editProduct_product_edit_qty_button);
        productUnitScanCodeButton = (Button) findViewById(R.id.editProduct_product_unit_barcode_button);
        productUnitEditNameButton = (Button) findViewById(R.id.product_unit_edit_name_button);
        productUnitEditDescriptionButton = (Button) findViewById(R.id.product_unit_edit_description_button);
        productUnitEditQtyButton = (Button) findViewById(R.id.editProduct_product_unit_edit_quantity_button);


        //Listeners
        addPhoto.setOnClickListener(this);
        selectPicture.setOnClickListener(this);
        productImage.setOnClickListener(this);
        productUnitScanCodeButton.setOnClickListener(this);
        productUnitEditNameButton.setOnClickListener(this);
        productUnitEditDescriptionButton.setOnClickListener(this);
        productUnitCheckbox.setOnClickListener(this);
        productEditImageButton.setOnClickListener(this);
        productEditNameButton.setOnClickListener(this);
        productEditDescriptionButton.setOnClickListener(this);
        productEditBrandButton.setOnClickListener(this);
        productEditQtyButton.setOnClickListener(this);
        productUnitEditQtyButton.setOnClickListener(this);


        //Getting the selected city
        SharedPreferences sharedPref = this.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        mCityCode = sharedPref.getString(getString(R.string.area_pref), null);
        mCountryCode = sharedPref.getString(getString(R.string.country_pref), null);

        //Setting the spinner
        getProductUnitsListFromFirebase();

        readProductFromFirebase();

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_product_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

    }


    public void readProductFromFirebase() {

        if (mProductId != null) {
            showProgressDialog(getString(R.string.loading), EditProductActivity.this);

            myRef = mDatabase.getReference().child(mCountryCode).child(getString(R.string.products_fb)).child(mProductId);

            myProductListener = new ValueEventListener() {
                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {

                    mProduct = dataSnapshot.getValue(Product.class);
                    if (mProduct != null) {
                        mProduct.setId(dataSnapshot.getKey());
                        if (mProduct.hasChild != null) {
                            if (mProductUnit.hasChild) mProductUnit = mProduct.getProductChild();
                        } else mProductUnit = null;
                        updateEditProductUI();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    hideProgressDialog();
                    Snackbar.make(findViewById(R.id.placeSnackBar), databaseError.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            };

            myRef.addValueEventListener(myProductListener);

        }
    }

    private void updateEditProductUI() {
        productCode.setText(mProduct.Barcode);
        productName.setText(mProduct.Name);
        productType.setText(mProduct.Type);
        productBrand.setText(mProduct.Brand);
        productQty.setText(mProduct.Quantity.toString());
        productUnitsSpinner.setSelection(((ArrayAdapter) productUnitsSpinner.getAdapter()).getPosition(mProduct.Units));
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference().child(mCountryCode).child(getString(R.string.images_fb)).child(mProduct.getId());
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(storageRef)
                .into(productImage);

        unitDetail.setVisibility(View.GONE);
        if (mProductUnit!=null){
            unitDetail.setVisibility(View.VISIBLE);

            productUnitName.setText(mProductUnit.Name);
            productUnitDescription.setText(mProductUnit.Type);
            productUnitQty.setText(String.format("%f", mProductUnit.Quantity));
            productUnitUnitsSpinner.setSelection(((ArrayAdapter) productUnitsSpinner.getAdapter()).getPosition(mProductUnit.Units));
            if(mProductUnit.Barcode!=null){
                productUnitCheckbox.setChecked(false);
                productUnitCode.setText(mProductUnit.Barcode);
            }else {
                productUnitCheckbox.setChecked(true);
                productUnitCode.setText(R.string.no_barcode);
            }

            productUnitName.setEnabled(false);
            productUnitDescription.setEnabled(false);
            productUnitQty.setEnabled(false);
            productUnitUnitsSpinner.setEnabled(false);
        }

        productCode.setEnabled(false);
        productName.setEnabled(false);
        productType.setEnabled(false);
        productBrand.setEnabled(false);
        productQty.setEnabled(false);
        productUnitsSpinner.setEnabled(false);
    }

    public void getProductUnitsListFromFirebase() {
        //Obtener lista de unidades de la base de datos
        DatabaseReference myRef;
        ValueEventListener unitsListener;

        showProgressDialog(getString(R.string.loading), EditProductActivity.this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child(mCountryCode).child(getString(R.string.product_units_fb));

        //Initializing arrays
        unitsArrayList = new ArrayList<>();
        unitsArrayList.add("");
        unitsArrayList.add(getString(R.string.unit_count));

        //unitsUnitArrayList = new ArrayList<>();
        //unitsUnitArrayList.add("");

        unitsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot unitsSnapshop : dataSnapshot.getChildren()) {
                    String unitName = unitsSnapshop.getValue().toString();
                    if (unitName != null) {
                        unitsArrayList.add(unitName);
                        //              unitsUnitArrayList.add(unitName);
                    }
                }
                unitsArrayList.add(getString(R.string.add_new_unit_type));
                //    unitsUnitArrayList.add(getString(R.string.add_new_unit_type));

                setUnitsTypesSpinner();

                productUnitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        int j = productUnitsSpinner.getAdapter().getCount() - 1;
                        if (i == j) addNewProductUnitFirebase();
                        if (i == 1) unitDetail.setVisibility(View.VISIBLE);
                        else unitDetail.setVisibility(View.GONE);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                productUnitUnitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        int j = productUnitUnitsSpinner.getAdapter().getCount() - 1;
                        if (i == j) addNewProductUnitFirebase();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
            }

        };
        myRef.addValueEventListener(unitsListener);

    }

    private void setUnitsTypesSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, unitsArrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productUnitsSpinner.setAdapter(adapter);
        productUnitUnitsSpinner.setAdapter(adapter);
    }

    private void addNewProductUnitFirebase() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_new_unit_type);


        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.add_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String type = input.getText().toString();

                // Write to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference().child(mCountryCode);
                myRef.child(getString(R.string.product_units_fb)).push().setValue(type);
                getProductUnitsListFromFirebase();

            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showProductUnitCardViewDetails(boolean b) {
        if (b) {
            productUnitName.setVisibility(View.VISIBLE);
            productUnitEditNameButton.setVisibility(View.VISIBLE);
            productUnitDescription.setVisibility(View.VISIBLE);
            productUnitEditDescriptionButton.setVisibility(View.VISIBLE);
            productUnitQty.setVisibility(View.VISIBLE);
            productUnitUnitsSpinner.setVisibility(View.VISIBLE);
            productUnitCode.setEnabled(false);
        } else {
            productUnitName.setVisibility(View.GONE);
            productUnitEditNameButton.setVisibility(View.GONE);
            productUnitDescription.setVisibility(View.GONE);
            productUnitEditDescriptionButton.setVisibility(View.GONE);
            productUnitQty.setVisibility(View.GONE);
            productUnitUnitsSpinner.setVisibility(View.GONE);
            productUnitCode.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i){
            case R.id.editProduct_product_edit_image_button:
                addPhoto.setVisibility(View.VISIBLE);
                selectPicture.setVisibility(View.VISIBLE);
                findViewById(R.id.editText_or_text).setVisibility(View.VISIBLE);
                productEditImageButton.setVisibility(View.GONE);
                break;

            case (R.id.editProduct_product_edit_name_button):
                productName.setEnabled(true);
                productEditNameButton.setVisibility(View.GONE);
                break;

            case (R.id.editProduct_product_edit_description_button):
                productType.setEnabled(true);
                productEditDescriptionButton.setVisibility(View.GONE);
                break;

            case (R.id.editProduct_product_edit_brand_button):
                productBrand.setEnabled(true);
                productEditBrandButton.setVisibility(View.GONE);
                break;

            case (R.id.editProduct_product_edit_qty_button):
                productQty.setEnabled(true);
                productUnitsSpinner.setEnabled(true);
                productEditQtyButton.setVisibility(View.GONE);
                break;

            case (R.id.editProduct_product_unit_barcode_button):

                break;

            case (R.id.product_unit_edit_name_button):
                productUnitName.setEnabled(true);
                productUnitEditNameButton.setVisibility(View.GONE);
                break;

            case (R.id.product_unit_edit_description_button):
                productUnitDescription.setEnabled(true);
                productUnitEditDescriptionButton.setVisibility(View.GONE);
                break;

            case (R.id.editProduct_product_unit_edit_quantity_button):
                productUnitQty.setEnabled(true);
                productUnitUnitsSpinner.setEnabled(true);
                productUnitEditQtyButton.setVisibility(View.GONE);
                break;
        }
    }
}