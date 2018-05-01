package com.me_market.android.memarket;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static com.me_market.android.memarket.BarcodeReader.PRODUCT_BARCODE;
import static com.me_market.android.memarket.BarcodeReader.PRODUCT_ID;
import static com.me_market.android.memarket.MainActivity.SHARED_PREF;
import static com.me_market.android.memarket.ProductActivity.PRODUCT_DATA;
import static com.me_market.android.memarket.ProductActivity.SELECT_UI;


public class NewProductActivity extends BaseActivity implements View.OnClickListener {

    private static final int CAMERA_REQUEST = 1888;
    private static final int PICTURE_SELECT = 1889;
    private static final int RC_SELECT_PRODUCT = 9003;
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
    private Button productUnitScanCodeButton;
    private Button productUnitEditNameButton;
    private Button productUnitEditDescriptionButton;
    private CardView unitDetail;
    private String mCityCode;
    private String mCountryCode;
    private boolean uploadComplete;
    private ArrayList<String> unitsArrayList;
    //private ArrayList<String> unitsUnitArrayList;
    private String mProductUnitId;
    private Product mProductUnit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String code = intent.getStringExtra(PRODUCT_BARCODE);
        //mUserId = intent.getStringExtra(USER_ID);

        //Get references to layout views
        productCode = (TextView) findViewById(R.id.productCode);
        productName = (TextInputEditText) findViewById(R.id.productNameInput);
        productType = (TextInputEditText) findViewById(R.id.productTypeInput);
        productBrand = (TextInputEditText) findViewById(R.id.productBrandInput);
        productQty = (TextInputEditText) findViewById(R.id.productQuantityInput);
        productUnitsSpinner = (Spinner) findViewById(R.id.spinner_unit);
        productImage = (ImageView) findViewById(R.id.productImageInput);
        addPhoto = (Button) findViewById(R.id.add_photo_button);
        selectPicture = (Button) findViewById(R.id.select_picture_button);

        unitDetail = (CardView) findViewById(R.id.new_product_unit_detail_cardview);
        productUnitCode = (TextInputEditText) findViewById(R.id.product_unit_code_edittext);
        productUnitName = (TextInputEditText) findViewById(R.id.product_unit_name_edittext);
        productUnitDescription = (TextInputEditText) findViewById(R.id.product_description_unit_edittext);
        productUnitQty = (TextInputEditText) findViewById(R.id.product_unit_quantity_input);
        productUnitScanCodeButton = (Button) findViewById(R.id.product_unit_barcode_button);
        productUnitEditNameButton = (Button) findViewById(R.id.product_unit_edit_name_button);
        productUnitEditDescriptionButton = (Button) findViewById(R.id.product_unit_edit_description_button);
        productUnitCheckbox = (CheckBox) findViewById(R.id.product_unit_checkbox);
        productUnitUnitsSpinner = (Spinner) findViewById(R.id.product_unit_spinner);

        //Listeners
        addPhoto.setOnClickListener(this);
        selectPicture.setOnClickListener(this);
        productImage.setOnClickListener(this);
        productUnitScanCodeButton.setOnClickListener(this);
        productUnitEditNameButton.setOnClickListener(this);
        productUnitEditDescriptionButton.setOnClickListener(this);
        productUnitCheckbox.setOnClickListener(this);

        // Capture the layout's EditText and set the string as its text
        productCode.setText(code);

        //Getting the selected city
        SharedPreferences sharedPref = this.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        mCityCode = sharedPref.getString(getString(R.string.area_pref), null);
        mCountryCode = sharedPref.getString(getString(R.string.country_pref), null);

        //Setting the spinner
        getProductUnitsListFromFirebase();

        //Setting the unit detail card view
        initializeUnitDetailCardView();

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.new_product_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);


    }

    private void initializeUnitDetailCardView() {
        unitDetail.setVisibility(View.GONE);
        productUnitName.setVisibility(View.GONE);
        productUnitEditNameButton.setVisibility(View.GONE);
        productUnitDescription.setVisibility(View.GONE);
        productUnitEditDescriptionButton.setVisibility(View.GONE);
        productUnitQty.setVisibility(View.GONE);
        productUnitUnitsSpinner.setVisibility(View.GONE);

        productUnitName.setEnabled(false);
        productUnitDescription.setEnabled(false);
    }

    public void addProduct() {
        //Add button OnClick event
        String code = productCode.getText().toString();
        String name = productName.getText().toString();
        String type = productType.getText().toString();
        String brand = productBrand.getText().toString();
        Float quantity = 0f;
        if(productQty.getText()!=null) {
            quantity = Float.parseFloat(productQty.getText().toString());
        }
        String units = productUnitsSpinner.getSelectedItem().toString();

        productImage.setDrawingCacheEnabled(true);
        productImage.buildDrawingCache();
        Bitmap bitmap = productImage.getDrawingCache();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        int i = productUnitsSpinner.getSelectedItemPosition();

        if (i == 1) {
            if (productUnitCheckbox.isChecked()) {
                String unitName = productUnitName.getText().toString();
                String unitDescription = productUnitDescription.getText().toString();
                Float unitQty = Float.parseFloat(productUnitQty.getText().toString());
                String unitUnits = productUnitUnitsSpinner.getSelectedItem().toString();
                writeNewProductWithUnitNoBarcodeOnFirebase(code, name, type, brand, quantity, units, imageData, unitName, unitDescription, unitQty, unitUnits);
            } else {
                if (mProductUnitId!=null) {
                    writeNewProductWithUnitBarcodeOnFirebase(code, name, type, brand, quantity, units, imageData, mProductUnitId, mProductUnit );
                }
            }
        } else writeNewProductOnFirebase(code, name, type, brand, quantity, units, imageData);

        //finish();

    }

    private void writeNewProductWithUnitBarcodeOnFirebase(final String ProductCode,
                                                          String name,
                                                          String type,
                                                          String brand,
                                                          Float quantity,
                                                          String units,
                                                          byte[] image,
                                                          String unitId,
                                                          Product productUnit) {
        // Write to the database
        uploadComplete = false;
        showProgressDialog(getString(R.string.saving_new_product), NewProductActivity.this);
        HashMap<String, Object> childsUpdate = new HashMap<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child(mCountryCode);
        Product Product = new Product(ProductCode, name, type, brand, quantity, units, true);
        //Product unitProduct = new Product(productUnit.Barcode, productUnit.Name, productUnit.Type, productUnit.Quantity, productUnit.Units);
        productUnit.setId(unitId);
        Product.setProductChild(productUnit);

        final String key = myRef.child(getString(R.string.products_keys_fb)).child(ProductCode).push().getKey();
        childsUpdate.put("/" + getString(R.string.products_fb) + "/" + key, Product);
        childsUpdate.put("/" + getString(R.string.products_keys_fb) + "/" + ProductCode + "/" + key, name);
        //childsUpdate.put("/" + getString(R.string.products_fb) + "/" + key + "/" + getString(R.string.unitChild_fb) + "/" + unitId, productUnit);


        myRef.updateChildren(childsUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (uploadComplete) {
                    hideProgressDialog();
                    startProductActivity(key, ProductCode);
                } else {
                    uploadComplete = true;
                    updateProgressDialogMessage(getString(R.string.loading_picture));
                }
            }
        });

        //Write picture
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference productImagesRef = storageRef.child(mCountryCode).child(getString(R.string.images_fb) + "/" + key);

        InputStream stream = new ByteArrayInputStream(image);
        UploadTask uploadTask = productImagesRef.putStream(stream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (uploadComplete) {
                    hideProgressDialog();
                    startProductActivity(key, ProductCode);
                } else {
                    uploadComplete = true;
                    updateProgressDialogMessage(getString(R.string.loading_product_data));
                }
            }
        });

    }

    private void writeNewProductWithUnitNoBarcodeOnFirebase(final String ProductCode,
                                                            String name,
                                                            String type,
                                                            String brand,
                                                            Float quantity,
                                                            String units,
                                                            byte[] image,
                                                            String unitName,
                                                            String unitDescription,
                                                            Float unitQty,
                                                            String unitUnits) {
        // Write to the database
        uploadComplete = false;
        showProgressDialog(getString(R.string.saving_new_product), NewProductActivity.this);
        HashMap<String, Object> childsUpdate = new HashMap<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child(mCountryCode);
        Product Product = new Product(ProductCode, name, type, brand, quantity, units , true);
        Product unitProduct = new Product(null, unitName, unitDescription, unitQty, unitUnits);
        Product.setProductChild(unitProduct);

        final String key = myRef.child(getString(R.string.products_keys_fb)).child(ProductCode).push().getKey();
        childsUpdate.put("/" + getString(R.string.products_fb) + "/" + key, Product);
        childsUpdate.put("/" + getString(R.string.products_keys_fb) + "/" + ProductCode + "/" + key, name);

        myRef.updateChildren(childsUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (uploadComplete) {
                    hideProgressDialog();
                    startProductActivity(key, ProductCode);
                } else {
                    uploadComplete = true;
                    updateProgressDialogMessage(getString(R.string.loading_picture));
                }
            }
        });

        //Write picture
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference productImagesRef = storageRef.child(mCountryCode).child(getString(R.string.images_fb) + "/" + key);

        InputStream stream = new ByteArrayInputStream(image);
        UploadTask uploadTask = productImagesRef.putStream(stream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (uploadComplete) {
                    hideProgressDialog();
                    startProductActivity(key, ProductCode);
                } else {
                    uploadComplete = true;
                    updateProgressDialogMessage(getString(R.string.loading_product_data));
                }
            }
        });

    }


    public void writeNewProductOnFirebase(final String ProductCode, String name, String type, String brand, Float quantity, String units, byte[] image) {
        // Write to the database
        uploadComplete = false;
        showProgressDialog(getString(R.string.saving_new_product), NewProductActivity.this);
        HashMap<String, Object> childsUpdate = new HashMap<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child(mCountryCode);
        Product Product = new Product(ProductCode, name, type, brand, quantity, units,false);
        final String key = myRef.child(getString(R.string.products_keys_fb)).child(ProductCode).push().getKey();
        childsUpdate.put("/" + getString(R.string.products_fb) + "/" + key, Product);
        childsUpdate.put("/" + getString(R.string.products_keys_fb) + "/" + ProductCode + "/" + key, name);
        //childsUpdate.put("/" + getString(R.string.products_fb) + "/" + key + "/" + getString(R.string.hasUnit_fb), false);
        myRef.updateChildren(childsUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (uploadComplete) {
                    hideProgressDialog();
                    startProductActivity(key, ProductCode);
                } else {
                    uploadComplete = true;
                    updateProgressDialogMessage(getString(R.string.loading_picture));
                }
            }
        });

        //Write picture
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference productImagesRef = storageRef.child(mCountryCode).child(getString(R.string.images_fb) + "/" + key);

        InputStream stream = new ByteArrayInputStream(image);
        UploadTask uploadTask = productImagesRef.putStream(stream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (uploadComplete) {
                    hideProgressDialog();
                    startProductActivity(key, ProductCode);
                } else {
                    uploadComplete = true;
                    updateProgressDialogMessage(getString(R.string.loading_product_data));
                }
            }
        });

    }

    private void startProductActivity(String id, String code) {
        startActivity(new Intent(this, ProductActivity.class)
                .putExtra(PRODUCT_ID, id)
                .putExtra(PRODUCT_BARCODE, code)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
        );
    }

    public void takePicture() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    public void selectPicture(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , PICTURE_SELECT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            productImage.setImageBitmap(photo);
            productImage.setVisibility(View.VISIBLE);
            addPhoto.setVisibility(View.GONE);
        }

        if (requestCode == PICTURE_SELECT && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                productImage.setImageBitmap(photo);
                productImage.setVisibility(View.VISIBLE);
                addPhoto.setVisibility(View.GONE);
//                If the picture if to big:
//                BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(myStream, false);
//                Bitmap region = decoder.decodeRegion(new Rect(10, 10, 50, 50), null);
            }catch (IOException e){
                Snackbar.make(findViewById(R.id.new_product_scroll_layout),
                        String.format(getString(R.string.picture_error), CommonStatusCodes.getStatusCodeString(resultCode)),
                        Snackbar.LENGTH_SHORT)
                        .show();
            }


        }

        //Returning from barcode capture activity
        if (requestCode == RC_SELECT_PRODUCT) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    mProductUnit = data.getParcelableExtra(PRODUCT_DATA);
                    mProductUnitId = data.getStringExtra(PRODUCT_ID);
                    productUnitName.setText(mProductUnit.Name);
                    productUnitDescription.setText(mProductUnit.Type);
                    productUnitCode.setText(mProductUnit.Barcode);
                    productUnitQty.setText(String.format("%f", mProductUnit.Quantity));
                    productUnitUnitsSpinner.setSelection(((ArrayAdapter) productUnitsSpinner.getAdapter()).getPosition(mProductUnit.Units));
                    showProductUnitCardViewDetails(true);
                    productUnitCheckbox.setEnabled(false);
                } else {
                    finish();
                }
            } else {
                Snackbar.make(findViewById(R.id.new_product_scroll_layout),
                        String.format(getString(R.string.barcode_error), CommonStatusCodes.getStatusCodeString(resultCode)),
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }


    }

    public void getProductUnitsListFromFirebase() {
        //Obtener lista de unidades de la base de datos
        DatabaseReference myRef;
        ValueEventListener unitsListener;

        showProgressDialog(getString(R.string.loading), NewProductActivity.this);
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
//        adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, unitsUnitArrayList);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        productUnitUnitsSpinner.setAdapter(adapter);
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

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i) {
            case R.id.add_photo_button:
                takePicture();
                break;
            case R.id.productImageInput:
                //takePicture();
                break;
            case R.id.product_unit_checkbox:
                if (((CheckBox) v).isChecked()) {
                    productUnitScanCodeButton.setEnabled(false);
                    showProductUnitCardViewDetails(true);
                } else {
                    productUnitScanCodeButton.setEnabled(true);
                    showProductUnitCardViewDetails(false);
                }
                break;
            case R.id.product_unit_barcode_button:
                startGetBarcode();
                break;
            case R.id.product_unit_edit_name_button:
                productUnitName.setEnabled(true);
                break;
            case R.id.product_unit_edit_description_button:
                productUnitDescription.setEnabled(true);
                break;
            case R.id.select_picture_button:
                selectPicture();
                break;

        }

    }

    private void startGetBarcode() {
        startActivityForResult(new Intent(NewProductActivity.this, ProductActivity.class)
                .putExtra(SELECT_UI, "Select"), RC_SELECT_PRODUCT);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_item_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //AppBar onClick method
        int i = item.getItemId();

        switch (i) {
            case R.id.select_button:
                addProduct();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

}
