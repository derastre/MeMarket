package com.example.android.memarket;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.memarket.models.Product;
import com.example.android.memarket.models.Purchase;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.example.android.memarket.CompaniesActivity.COMPANY_ID;
import static com.example.android.memarket.CompaniesActivity.COMPANY_NAME;
import static com.example.android.memarket.MainActivity.FROM_MAIN;
import static com.example.android.memarket.SplashActivity.USER_ID;
import static com.example.android.memarket.StoresActivity.STORE_ID;
import static com.example.android.memarket.StoresActivity.STORE_NAME;


public class ProductActivity extends BaseActivity implements View.OnClickListener,BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String PRODUCT_CODE = "com.example.android.memarket.PRODUCT_CODE";
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";


    private TextView productCode;
    private String productPrice;
    private String productOfferPrice;
    private String mStoreId;
    private String mCompanyId;
    private String storeName;
    private String companyName;
    private String productId;
    private String lastPurchaseDate;
    private String lastPurchasePrice;
    private Product product;
    private String mUserId;
    private Boolean fromMain;

    private DatabaseReference myRef;
    private DatabaseReference myPriceRef;
    private DatabaseReference myOfferRef;
    private DatabaseReference myPurchasesRef;
    private Query myOfferQuery;
    private Query myPurchasesQuery;
    private ValueEventListener priceListener;
    private ValueEventListener productListener;
    private ValueEventListener offerListener;
    private ValueEventListener purchasesListener;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        //Getting widgets ids
        productCode = (TextView) findViewById(R.id.productCode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //Setting Toolbar
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        //Buttons Listeners
        findViewById(R.id.scan_button).setOnClickListener(this);
        findViewById(R.id.update_price_button).setOnClickListener(this);
        findViewById(R.id.compare_price_button).setOnClickListener(this);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        mStoreId = intent.getStringExtra(STORE_ID);
        storeName= intent.getStringExtra(STORE_NAME);
        mCompanyId = intent.getStringExtra(COMPANY_ID);
        companyName= intent.getStringExtra(COMPANY_NAME);
        mUserId = intent.getStringExtra(USER_ID);
        fromMain = intent.getBooleanExtra(FROM_MAIN,false);

        // Set store name on price card
        String storename = companyName + " " + storeName;
        TextView textView = (TextView) findViewById(R.id.selectedStoreName);
        textView.setText(storename);

        if (fromMain ){
            scan_barcode();
        }
    }

    public void scan_barcode(){

        // launch barcode activity.
        Intent intent = new Intent(ProductActivity.this, BarcodeReader.class);
        intent.putExtra(BarcodeReader.AutoFocus,true);
        intent.putExtra(BarcodeReader.UseFlash, false);
        intent.putExtra(FROM_MAIN,fromMain);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);

    }

    public void readProductFromFirebase(final String code) {
        showProgressDialog(getString(R.string.loading));
        //Creating instance of Firebase database
        //Creating a reference the product code and Price in database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("products").child(code);

        //Creating instance of Firebase Storage
        // Create a storage reference of the product
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference().child("images").child(code);

        //Listener para leer los datos del producto

        productListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {
                Context context = getApplicationContext();
                product = dataSnapshot.getValue(Product.class);
                //Check if product exist in database
                if (product != null) {
                    TextView textViewName = (TextView) findViewById(R.id.productName);
                    textViewName.setText(product.Name);
                    TextView textViewType = (TextView) findViewById(R.id.productType);
                    textViewType.setText(product.Type);
                    TextView textViewBrand = (TextView) findViewById(R.id.productBrand);
                    textViewBrand.setText(product.Brand);
                    TextView textViewQty = (TextView) findViewById(R.id.productQuantity);
                    String Qty = product.Quantity + " " + product.Units;
                    textViewQty.setText(Qty);
                    ImageView productImage = (ImageView) findViewById(R.id.productImage);
                    Glide.with(context)
                            .using(new FirebaseImageLoader())
                            .load(storageRef)
                            .into(productImage);
                    findViewById(R.id.scroll_group_view).setVisibility(View.VISIBLE);
                } else {
                    //if product code doesn't exist in database go to activity add new product
                    startActivity(new Intent(ProductActivity.this, NewProduct.class).putExtra(PRODUCT_CODE,code));
                }
                hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
                Snackbar.make(findViewById(R.id.placeSnackBar),databaseError.getMessage(),Snackbar.LENGTH_SHORT).show();
            }
        };

        myRef.addValueEventListener(productListener);

        //Check for the product price in the selected store

        myPriceRef = database.getReference().child("prices").child(code).child(mStoreId);
        priceListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView textView = (TextView) findViewById(R.id.productPrice);

                //Check if product exist in database
                if (dataSnapshot.getValue() != null) {
                    productPrice = dataSnapshot.getValue().toString();
                    textView.setText(productPrice);
                } else {
                    //if price  does'nt exist in database
                    textView.setText(R.string.no_price);
                    productPrice = null;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        myPriceRef.addValueEventListener(priceListener);

        //Check for offer in the selected store
        Calendar dateToday = Calendar.getInstance();
        dateToday.set(Calendar.HOUR_OF_DAY,0);
        dateToday.set(Calendar.MINUTE,0);
        Long dateStart = dateToday.getTimeInMillis();

        myOfferRef = database.getReference().child("offers").child(code).child(mStoreId);
        myOfferQuery = myOfferRef.orderByKey().startAt(dateStart.toString());
        offerListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView offer_text = (TextView) findViewById(R.id.offer_price);

                //Check if product exist in database
                if (dataSnapshot.getChildren() != null) {
                    for (DataSnapshot offerSnapshot:dataSnapshot.getChildren()) {
                        offer_text.setVisibility(View.VISIBLE);
                        productOfferPrice = offerSnapshot.getValue().toString();
                        String text = getResources().getString(R.string.offer_button_dialog_2) + " " + productOfferPrice;
                        offer_text.setText(text);
                        //textButton.setEnabled(false);
                    }
                } else {
                    //if price  does'nt exist in database
                    offer_text.setVisibility(View.GONE);
                    productOfferPrice = getResources().getString(R.string.no_offer);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        myOfferQuery.addValueEventListener(offerListener);

        //Check for last user purchase on database

        myPurchasesRef = database.getReference().child("purchases").child(code);
        myPurchasesQuery = myPurchasesRef.orderByKey().limitToLast(1);
        purchasesListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {


                //Check if product exist in database
                if (dataSnapshot.getChildren() != null) {
                    for (DataSnapshot purchasesSnapshot:dataSnapshot.getChildren()) {
                        Purchase purchase = purchasesSnapshot.getValue(Purchase.class);
                        lastPurchasePrice = purchase.price;
                        lastPurchaseDate = purchasesSnapshot.getKey();
                    }
                } else {
                    lastPurchasePrice = null;
                    lastPurchaseDate=null;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        myPurchasesQuery.addValueEventListener(purchasesListener);


    }

    public void updatePriceFirebase(){

        if (companyName!=null && storeName != null && mCompanyId !=null && mStoreId != null && product!=null && mUserId!=null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.update_price);
            builder.setMessage(companyName + " " + storeName + "\n" + product.Name);

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(R.string.update_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    productPrice = input.getText().toString();
                    Long date = System.currentTimeMillis();

                    // Write to the database
                    Map<String, Object> childUpdates = new HashMap<>();
                    Map<String, Object> updateHistory = new HashMap<>();
                    updateHistory.put("price",productPrice);
                    updateHistory.put("user",mUserId);
                    childUpdates.put("/prices/" + productId + "/" + mStoreId, productPrice);
                    childUpdates.put("/prices_history/" + productId + "/" + mStoreId + date , updateHistory);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();
                    myRef.updateChildren(childUpdates);

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

    }

    public void comparePricesFirebase(){
        startActivity(new Intent(ProductActivity.this,PricesActivity.class).putExtra(PRODUCT_CODE,productId));
    }

    public void addPurchaseFirebase(){
        if (mStoreId != null && product!=null && productPrice!=null && mUserId!=null) {
            Purchase register_product = new Purchase(productPrice, mStoreId);
            final Long date = System.currentTimeMillis();

            // Write to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference();
            myRef.child("purchases").child(mUserId).child(productId).child(date.toString()).setValue(register_product);
            Snackbar.make(findViewById(R.id.placeSnackBar),getString(R.string.purchase_added_snackbar),Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo_snackbar_button, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            myRef.child("purchases").child(mUserId).child(productId).child(date.toString()).removeValue();
                        }
                    })
                    .show();
        }
    }

    public void purchasesHistoryFirebase(){
        startActivity(new Intent(ProductActivity.this,purchaseHistory.class)
                .putExtra(PRODUCT_CODE,productId)
        );
    }

    public void markAsOfferFirebase(){
        if (companyName!=null && storeName != null && mCompanyId !=null && mStoreId != null && product!=null && mUserId!=null ) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.mark_as_offer_dialog);
            builder.setMessage(companyName + " " + storeName + "\n" + product.Name + "\n\n" + getResources().getString(R.string.offer_price_text));

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(R.string.update_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    productOfferPrice = input.getText().toString();
                    Long timestamp = System.currentTimeMillis();

                    // Write to the database
                    // Write to the database
                    Map<String, Object> childUpdates = new HashMap<>();
                    Map<String, Object> updateHistory = new HashMap<>();
                    updateHistory.put("price",productOfferPrice);
                    updateHistory.put("user",mUserId);
                    childUpdates.put("/offers/" + productId + "/" + mStoreId + "/" +  timestamp, productOfferPrice);
                    childUpdates.put("/offers_history/" + productId + "/" + mStoreId + timestamp , updateHistory);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();
                    myRef.updateChildren(childUpdates);

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
    }

   private void showProductPriceOptions() {
        if (companyName!=null && storeName != null && mCompanyId !=null && mStoreId != null && productId!=null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.product_price_dialog,null);
            builder.setView(dialogView);
            builder.setTitle(R.string.product_price_text);

            Button update =  dialogView.findViewById(R.id.update_price_button);
            Button compare = dialogView.findViewById(R.id.compare_price_button);
            Button purchases = dialogView.findViewById(R.id.purchase_history_button);
            TextView price = dialogView.findViewById(R.id.actual_price_text);
            TextView offer = dialogView.findViewById(R.id.offer_price_text);
            TextView last_purchase = dialogView.findViewById(R.id.last_purchase__price_text);
            String last_purchase_text = lastPurchaseDate + " " + lastPurchasePrice;
            price.setText(productPrice);
            offer.setText(productOfferPrice);
            last_purchase.setText(last_purchase_text);

            update.setOnClickListener(this);
            compare.setOnClickListener(this);
            purchases.setOnClickListener(this);

            builder.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Returning from barcode capture activity
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String barcode = data.getStringExtra(BarcodeReader.BarcodeObject);
                    Log.d(TAG, "Barcode read: " + barcode);
                    productId = barcode;
                    productCode.setText(barcode);
                    readProductFromFirebase(barcode);
                    fromMain = false;
                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                    if (fromMain) finish();
                }
            } else {
                productCode.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void  onStop(){
        super.onStop();
        if (productListener!=null) {
            myRef.removeEventListener(productListener);
        }
        if (priceListener!=null) {
            myPriceRef.removeEventListener(priceListener);
        }
        if (offerListener!=null) {
            myOfferQuery.removeEventListener(offerListener);
        }
        if (purchasesListener!=null) {
            myPurchasesQuery.removeEventListener(purchasesListener);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i){
            case R.id.scan_button:
                scan_barcode();
                break;
            case R.id.update_price_button:
                updatePriceFirebase();
                break;
            case R.id.compare_price_button:
                comparePricesFirebase();
                break;

        }

    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.product_toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //AppBar onClick method
        int i = item.getItemId();

        switch (i) {
            case R.id.action_scan:
                scan_barcode();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public boolean onNavigationItemSelected(MenuItem item){
        int i = item.getItemId();
        switch (i){
            case R.id.offer_button:
                markAsOfferFirebase();
                break;
            case R.id.add_purchase_button:
                addPurchaseFirebase();
                break;
            case R.id.purchase_history_button:
                purchasesHistoryFirebase();
                break;
        }
        return true;
    }
}
