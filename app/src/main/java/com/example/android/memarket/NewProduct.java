package com.example.android.memarket;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.memarket.components.BaseActivity;
import com.example.android.memarket.models.Product;
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
import java.io.InputStream;
import java.util.ArrayList;

import static com.example.android.memarket.BarcodeReader.PRODUCT_BARCODE;
import static com.example.android.memarket.BarcodeReader.PRODUCT_ID;
import static com.example.android.memarket.SplashActivity.USER_ID;


public class NewProduct extends BaseActivity implements View.OnClickListener {

    private static final int CAMERA_REQUEST = 1888;
    private String mUserId;
    private ImageView productImage;
    private TextView productCode;
    private EditText productType;
    private EditText productBrand;
    private EditText productQty;
    private EditText productName;
    private Spinner productUnitsSpinner;
    private Button addPhoto;

    private ArrayList<String> unitsArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String code = intent.getStringExtra(PRODUCT_BARCODE);
        mUserId = intent.getStringExtra(USER_ID);

        //Get references to layout views
        productCode = (TextView) findViewById(R.id.productCode);
        productName = (EditText) findViewById(R.id.productNameInput);
        productType = (EditText) findViewById(R.id.productTypeInput);
        productBrand = (EditText) findViewById(R.id.productBrandInput);
        productQty = (EditText) findViewById(R.id.productQuantityInput);
        productUnitsSpinner = (Spinner) findViewById(R.id.spinner_unit);
        productImage = (ImageView) findViewById(R.id.productImageInput);
        addPhoto = (Button) findViewById(R.id.add_photo_button);

        //Listeners
        addPhoto.setOnClickListener(this);
        productImage.setOnClickListener(this);

        // Capture the layout's EditText and set the string as its text
        productCode.setText(code);

        //Setting the spinner
        getProductUnitsListFromFirebase();

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.new_product_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);


    }

    public void addProduct(){
        //Add button OnClick event
        String code = productCode.getText().toString();
        String name = productName.getText().toString();
        String type = productType.getText().toString();
        String brand = productBrand.getText().toString();
        Float quantity = Float.parseFloat(productQty.getText().toString());
        String units = productUnitsSpinner.getSelectedItem().toString();

        productImage.setDrawingCacheEnabled(true);
        productImage.buildDrawingCache();
        Bitmap bitmap = productImage.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        writeNewProductOnFirebase(code,name,type,brand,quantity,units,imageData);

        finish();
    }

    public void writeNewProductOnFirebase(final String ProductCode, String name, String type, String brand, Float quantity , String units, byte[] image){
        // Write to the database

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        Product Product= new Product(name,type,brand,quantity,units);
        final String key= myRef.child("products").child(ProductCode).push().getKey();
        myRef.child("products").child(ProductCode).child(key).setValue(Product).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                startProductActivity(key,ProductCode);
            }
        });

        //Write picture
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference productImagesRef = storageRef.child("images/"+key);

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

            }
        });

    }

    private void startProductActivity(String id,String code) {
        startActivity(new Intent(this, ProductActivity.class)
                .putExtra(PRODUCT_ID, id)
                .putExtra(PRODUCT_BARCODE, code)
                .putExtra(USER_ID,mUserId));
    }
    public void takePicture(){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            productImage.setImageBitmap(photo);
            productImage.setVisibility(View.VISIBLE);
            addPhoto.setVisibility(View.GONE);
        }
    }

    public void getProductUnitsListFromFirebase() {
        //Obtener lista de unidades de la base de datos
        DatabaseReference myRef;
        ValueEventListener unitsListener;

        showProgressDialog(getString(R.string.loading));
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("product_units");

        unitsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                unitsArrayList = new ArrayList<>();
                unitsArrayList.add("");
                for (DataSnapshot unitsSnapshop : dataSnapshot.getChildren()) {
                    String companyType = unitsSnapshop.getValue().toString();
                    if (companyType != null) {
                        unitsArrayList.add(companyType);
                    }
                }
                unitsArrayList.add(getString(R.string.add_new_unit_type));
                setUnitsTypesSpinner();
                productUnitsSpinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        int j = productUnitsSpinner.getAdapter().getCount()-1;
                        if (i==j) addNewProductUnit();
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,unitsArrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productUnitsSpinner.setAdapter(adapter);
    }

    private void addNewProductUnit() {

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
                DatabaseReference myRef = database.getReference();
                myRef.child("product_units").push().setValue(type);
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
                takePicture();
                break;
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
