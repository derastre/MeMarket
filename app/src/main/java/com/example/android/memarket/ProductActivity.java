package com.example.android.memarket;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.memarket.components.BaseActivity;
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

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.example.android.memarket.CompaniesActivity.COMPANY_ID;
import static com.example.android.memarket.CompaniesActivity.COMPANY_NAME;
import static com.example.android.memarket.MainActivity.FROM_MAIN;
import static com.example.android.memarket.SplashActivity.USER_ID;
import static com.example.android.memarket.StoresActivity.STORE_ID;
import static com.example.android.memarket.StoresActivity.STORE_NAME;

//TODO Erase BottomNavigationView.OnNavigationItemSelectedListener if not used.
public class ProductActivity extends BaseActivity implements View.OnClickListener, BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String PRODUCT_CODE = "com.example.android.memarket.PRODUCT_CODE";
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";


    private TextView scannedCode;
    private FloatingActionButton scan_fab;
    private FloatingActionButton add_purchase_fab;
    private String mProductPrice;
    private String mProductOfferPrice;
    private String mStoreId;
    private String mCompanyId;
    private String mStoreName;
    private String mCompanyName;
    private String mProductCode;
    private String lastPurchaseDate;
    private String lastPurchasePrice;
    private Product mProduct;
    private String mUserId;
    private Boolean fromMain;

    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;
    private DatabaseReference myPriceRef;
    private DatabaseReference myOfferRef;
    private DatabaseReference myPurchasesRef;
    private Query myOfferQuery;
    private Query myPurchasesQuery;
    private ValueEventListener myPriceListener;
    private ValueEventListener myProductListener;
    private ValueEventListener myOfferListener;
    private ValueEventListener myPurchasesListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        //Firebase database instance
        mDatabase = FirebaseDatabase.getInstance();

        //Getting widgets ids
        scannedCode = (TextView) findViewById(R.id.productCode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_profile_toolbar);
        scan_fab = (FloatingActionButton) findViewById(R.id.scan_fab);
        add_purchase_fab = (FloatingActionButton) findViewById(R.id.add_purchase_fab);

        //Setting Toolbar
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        //Buttons Listeners
        scan_fab.setOnClickListener(this);
        add_purchase_fab.setOnClickListener(this);
        findViewById(R.id.update_price_button).setOnClickListener(this);
        findViewById(R.id.compare_price_button).setOnClickListener(this);
        findViewById(R.id.view_history_button).setOnClickListener(this);
        findViewById(R.id.on_sale_button).setOnClickListener(this);
//        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
//        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        mStoreId = intent.getStringExtra(STORE_ID);
        mStoreName = intent.getStringExtra(STORE_NAME);
        mCompanyId = intent.getStringExtra(COMPANY_ID);
        mCompanyName = intent.getStringExtra(COMPANY_NAME);
        mUserId = intent.getStringExtra(USER_ID);
        fromMain = intent.getBooleanExtra(FROM_MAIN, false);

        // If there is no store selected
        if (mStoreId == null) {
            startSelectStore();
        }

        // Set store name on price card
        String storename = mCompanyName + " " + mStoreName;
        TextView textView = (TextView) findViewById(R.id.selectedStoreName);
        textView.setText(storename);

        //Hide the FAB Button when scrolling
        NestedScrollView nsv = (NestedScrollView) findViewById(R.id.scroll_group_view);
        nsv.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    scan_fab.hide();
                } else {
                    scan_fab.show();
                }
            }
        });


        //If the call came from Main activity
        if (fromMain) {
            scan_barcode();
        }
    }

    private void startSelectStore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_store_instruction);

        builder.setPositiveButton(R.string.select_store, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(ProductActivity.this, CompaniesActivity.class));
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void scan_barcode() {

        // launch barcode activity.
        Intent intent = new Intent(ProductActivity.this, BarcodeReader.class);
        intent.putExtra(BarcodeReader.AutoFocus, true);
        intent.putExtra(BarcodeReader.UseFlash, false);
        intent.putExtra(FROM_MAIN, fromMain);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);

    }

    public void comparePrices() {
        startActivity(new Intent(ProductActivity.this, PricesActivity.class).putExtra(PRODUCT_CODE, mProductCode));
    }

    public void readProductFromFirebase() {

        if (mProductCode != null && mStoreId != null) {
            showProgressDialog(getString(R.string.loading));

            myRef = mDatabase.getReference().child("products").child(mProductCode);

            //Storage for product picture
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageRef = storage.getReference().child("images").child(mProductCode);

            myProductListener = new ValueEventListener() {
                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {
                    Context context = getApplicationContext();
                    mProduct = dataSnapshot.getValue(Product.class);
                    //Check if mProduct exist in database
                    if (mProduct != null) {
                        TextView textViewName = (TextView) findViewById(R.id.productName);
                        textViewName.setText(mProduct.Name);
                        TextView textViewType = (TextView) findViewById(R.id.productType);
                        textViewType.setText(mProduct.Type);
                        TextView textViewBrand = (TextView) findViewById(R.id.productBrand);
                        textViewBrand.setText(mProduct.Brand);
                        TextView textViewQty = (TextView) findViewById(R.id.productQuantity);
                        String Qty = mProduct.Quantity + " " + mProduct.Units;
                        textViewQty.setText(Qty);
                        ImageView productImage = (ImageView) findViewById(R.id.productImage);
                        Glide.with(context)
                                .using(new FirebaseImageLoader())
                                .load(storageRef)
                                .into(productImage);
                        findViewById(R.id.scroll_group_view).setVisibility(View.VISIBLE);
                    } else {
                        //if mProduct code doesn't exist in database go to activity add new mProduct
                        startActivity(new Intent(ProductActivity.this, NewProduct.class).putExtra(PRODUCT_CODE, mProductCode));
                    }
                    hideProgressDialog();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    hideProgressDialog();
                    Snackbar.make(findViewById(R.id.placeSnackBar), databaseError.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            };
            myRef.addValueEventListener(myProductListener);

            //Reading extra product data
            readProductPriceFromFirebase();
            readProductOfferFromFirebase();
            readLastPurchaseFromFirebase();
        }

    }

    public void readProductPriceFromFirebase() {
        //Check for the mProduct price in the selected store
        myPriceRef = mDatabase.getReference().child("prices").child(mProductCode).child(mStoreId);
        myPriceListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView textView = (TextView) findViewById(R.id.productPrice);

                //Check if mProduct exist in database
                if (dataSnapshot.getValue() != null) {
                    mProductPrice = dataSnapshot.getValue().toString();
                    Float number = Float.parseFloat(mProductPrice);
                    textView.setText(NumberFormat.getCurrencyInstance().format(number));
                } else {
                    //if price  does'nt exist in database
                    textView.setText(R.string.no_price);
                    mProductPrice = null;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        myPriceRef.addValueEventListener(myPriceListener);
    }

    public void readProductOfferFromFirebase() {
        //Check for offer in the selected store
        Calendar dateToday = Calendar.getInstance();
        dateToday.set(Calendar.HOUR_OF_DAY, 0);
        dateToday.set(Calendar.MINUTE, 0);
        Long dateStart = dateToday.getTimeInMillis();

        myOfferRef = mDatabase.getReference().child("offers").child(mProductCode).child(mStoreId);
        myOfferQuery = myOfferRef.orderByKey().startAt(dateStart.toString());
        myOfferListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView offer_text = (TextView) findViewById(R.id.offer_price);
                TextView price_text = (TextView) findViewById(R.id.productPrice);

                //Check if mProduct exist in database
                if (dataSnapshot.getChildren() != null) {
                    for (DataSnapshot offerSnapshot : dataSnapshot.getChildren()) {
                        findViewById(R.id.offers_layout).setVisibility(View.VISIBLE);
                        mProductOfferPrice = offerSnapshot.getValue().toString();
                        Float number = Float.parseFloat(mProductOfferPrice);
                        String text = NumberFormat.getCurrencyInstance().format(number);
                        offer_text.setText(text);
                        price_text.setPaintFlags(price_text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        //textButton.setEnabled(false);
                    }
                } else {
                    //if price  does'nt exist in database
                    findViewById(R.id.offers_layout).setVisibility(View.GONE);
                    price_text.setPaintFlags(price_text.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    mProductOfferPrice = getResources().getString(R.string.no_offer);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        myOfferQuery.addValueEventListener(myOfferListener);
    }

    public void readLastPurchaseFromFirebase() {
        //Check for last user purchase on database
        if (mUserId != null) {
            myPurchasesRef = mDatabase.getReference().child(mUserId).child("purchases").child(mProductCode);
            myPurchasesQuery = myPurchasesRef.orderByKey().limitToLast(1);
            myPurchasesListener = new ValueEventListener() {
                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {

                    //Check if mProduct exist in database
                    if (dataSnapshot.getChildren() != null) {
                        for (DataSnapshot purchasesSnapshot : dataSnapshot.getChildren()) {
                            Purchase purchase = purchasesSnapshot.getValue(Purchase.class);
                            lastPurchasePrice = purchase.price;
                            lastPurchaseDate = purchasesSnapshot.getKey();
                        }
                    } else {
                        lastPurchasePrice = null;
                        lastPurchaseDate = null;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };

            myPurchasesQuery.addValueEventListener(myPurchasesListener);
        }
    }

    public void updatePriceFirebase() {

        if (mCompanyName != null && mStoreName != null && mStoreId != null && mProduct != null && mUserId != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.update_price);
            builder.setMessage(mCompanyName + " " + mStoreName + "\n" + mProduct.Name);

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(R.string.update_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mProductPrice = input.getText().toString();
                    Long date = System.currentTimeMillis();

                    // Write to the database
                    Map<String, Object> childUpdates = new HashMap<>();
                    Map<String, Object> updateHistory = new HashMap<>();
                    updateHistory.put("price", mProductPrice);
                    updateHistory.put("user", mUserId);
                    childUpdates.put("/prices/" + mProductCode + "/" + mStoreId, mProductPrice);
                    childUpdates.put("/prices_history/" + mProductCode + "/" + mStoreId + date, updateHistory);

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

    public void addPurchaseFirebase() {
        if (mStoreId != null && mProductPrice != null && mUserId != null) {
            Purchase register_product = new Purchase(mProductPrice, mStoreId);
            final Long date = System.currentTimeMillis();

            // Write to the database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference();
            myRef.child("purchases").child(mUserId).child(mProductCode).child(date.toString()).setValue(register_product);
            Snackbar.make(findViewById(R.id.placeSnackBar), getString(R.string.purchase_added_snackbar), Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo_snackbar_button, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            myRef.child("purchases").child(mUserId).child(mProductCode).child(date.toString()).removeValue();
                        }
                    })
                    .show();
        }
    }

    public void purchasesHistoryFirebase() {
        startActivity(new Intent(ProductActivity.this, purchaseHistory.class)
                .putExtra(PRODUCT_CODE, mProductCode)
        );
    }

    public void markAsOfferFirebase() {
        if (mCompanyName != null && mStoreName != null && mStoreId != null && mProduct != null && mUserId != null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.mark_as_offer_dialog);
            builder.setMessage(mCompanyName + " " + mStoreName + "\n" + mProduct.Name + "\n\n" + getResources().getString(R.string.offer_price_text));

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(R.string.update_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mProductOfferPrice = input.getText().toString();
                    Long timestamp = System.currentTimeMillis();

                    // Write to the database
                    // Write to the database
                    Map<String, Object> childUpdates = new HashMap<>();
                    Map<String, Object> updateHistory = new HashMap<>();
                    updateHistory.put("price", mProductOfferPrice);
                    updateHistory.put("user", mUserId);
                    childUpdates.put("/offers/" + mProductCode + "/" + mStoreId + "/" + timestamp, mProductOfferPrice);
                    childUpdates.put("/offers_history/" + mProductCode + "/" + mStoreId + timestamp, updateHistory);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Returning from barcode capture activity
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String barcode = data.getStringExtra(BarcodeReader.BarcodeObject);
                    Log.d(TAG, "Barcode read: " + barcode);
                    mProductCode = barcode;
                    scannedCode.setText(barcode);
                    readProductFromFirebase();
                    fromMain = false;
                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                    if (fromMain) finish();
                }
            } else {
                scannedCode.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (myProductListener != null) {
            myRef.removeEventListener(myProductListener);
        }
        if (myPriceListener != null) {
            myPriceRef.removeEventListener(myPriceListener);
        }
        if (myOfferListener != null) {
            myOfferQuery.removeEventListener(myOfferListener);
        }
        if (myPurchasesListener != null) {
            myPurchasesQuery.removeEventListener(myPurchasesListener);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i) {
            case R.id.scan_fab:
                scan_barcode();
                break;
            case R.id.add_purchase_fab:
                addPurchaseFirebase();
                break;
            case R.id.update_price_button:
                updatePriceFirebase();
                break;
            case R.id.compare_price_button:
                comparePrices();
                break;
            case R.id.view_history_button:
                purchasesHistoryFirebase();
                break;
            case R.id.on_sale_button:
                markAsOfferFirebase();
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.product_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //AppBar onClick method
        int i = item.getItemId();

        switch (i) {
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public boolean onNavigationItemSelected(MenuItem item) {
        //TODO Erase this method if bottom navigation not used.
        int i = item.getItemId();
        switch (i) {
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
