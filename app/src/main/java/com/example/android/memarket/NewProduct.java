package com.example.android.memarket;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.memarket.components.BaseActivity;
import com.example.android.memarket.models.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class NewProduct extends BaseActivity implements View.OnClickListener {

    private static final int CAMERA_REQUEST = 1888;
    private ImageView productImage;
    private TextView productCode;
    private EditText productType;
    private EditText productBrand;
    private EditText productQty;
    private EditText productName;
    private Spinner productUnits;
    private Button addPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(ProductActivity.PRODUCT_CODE);

        //Get references to layout views
        productCode = (TextView) findViewById(R.id.productCode);
        productName = (EditText) findViewById(R.id.productNameInput);
        productType = (EditText) findViewById(R.id.productTypeInput);
        productBrand = (EditText) findViewById(R.id.productBrandInput);
        productQty = (EditText) findViewById(R.id.productQuantityInput);
        productUnits = (Spinner) findViewById(R.id.spinner_unit);
        productImage = (ImageView) findViewById(R.id.productImageInput);
        addPhoto = (Button) findViewById(R.id.add_photo_button);

        //Listeners
        addPhoto.setOnClickListener(this);
        productImage.setOnClickListener(this);

        // Capture the layout's EditText and set the string as its text
        productCode.setText(message);

        //Setting the spinner
        String[] units = getResources().getStringArray(R.array.units_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productUnits.setAdapter(adapter);

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
        String quantity = productQty.getText().toString();
        String units = productUnits.getSelectedItem().toString();

        productImage.setDrawingCacheEnabled(true);
        productImage.buildDrawingCache();
        Bitmap bitmap = productImage.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        writeNewProductOnFirebase(code,name,type,brand,quantity,units,imageData);

        finish();
    }

    public void writeNewProductOnFirebase(String ProductCode, String name, String type, String brand, String quantity , String units, byte[] image){
        // Write to the database

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        Product Product= new Product(name,type,brand,quantity,units);
        myRef.child("products").child(ProductCode).setValue(Product);

        //Write picture
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference productImagesRef = storageRef.child("images/"+ProductCode);

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
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                //Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });

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
