package com.example.android.memarket;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

//import static com.example.android.memarket.CompaniesActivity.COMPANY_ID;
import static com.example.android.memarket.CompaniesActivity.COMPANY_NAME;
import static com.example.android.memarket.MainActivity.FROM_MAIN;
import static com.example.android.memarket.SplashActivity.USER_ID;
import static com.example.android.memarket.StoresActivity.STORE_ID;
import static com.example.android.memarket.StoresActivity.STORE_NAME;


public class ProductActivity extends BaseActivity implements View.OnClickListener {

    public static final String PRODUCT_ID = "com.example.android.memarket.PRODUCT_ID";
    public static final String PRODUCT_BARCODE = "com.example.android.memarket.PRODUCT_BARCODE";
    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    private TextView scannedCode;
    private FloatingActionButton scan_fab;
    private FloatingActionButton add_purchase_fab;
    private String mBarcode;
    private Product mProduct;
    private String mStoreId;
    private String mStoreName;
    private String mCompanyName;
    private String lastPurchaseDate;
    private Float lastPurchasePrice;
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

        // Get the Intent that started this activity and extract the string
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            mStoreId = intent.getStringExtra(STORE_ID);
            mStoreName = intent.getStringExtra(STORE_NAME);
            mCompanyName = intent.getStringExtra(COMPANY_NAME);
            mUserId = intent.getStringExtra(USER_ID);
            fromMain = intent.getBooleanExtra(FROM_MAIN, false);
        } else {
            mStoreId = savedInstanceState.getString("mStoreId");
            mStoreName = savedInstanceState.getString("mStoreName");
            mCompanyName = savedInstanceState.getString("mCompanyName");
            mUserId = savedInstanceState.getString("mUserId");
        }

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
                    add_purchase_fab.hide();
                } else {
                    scan_fab.show();
                    add_purchase_fab.show();
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

    public void readProductFromFirebase() {

        if (mBarcode != null && mStoreId != null) {
            showProgressDialog(getString(R.string.loading));

            myRef = mDatabase.getReference().child("products").child(mBarcode);

            myProductListener = new ValueEventListener() {
                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {

                    ArrayList<Product> productArrayList = new ArrayList<>();
                    ArrayList<String> productKeyArrayList = new ArrayList<>();
                    if (dataSnapshot.getChildrenCount() == 1) {
                        for (DataSnapshot productSnapshop : dataSnapshot.getChildren()) {
                            mProduct = productSnapshop.getValue(Product.class);

                            if (mProduct != null) {
                                mProduct.setId(productSnapshop.getKey());
                                readProductPriceFromFirebase(); //updateProductUI();
                            }
                        }
                    } else if (dataSnapshot.getChildrenCount() == 0) {
                        //if mProduct code doesn't exist in database go to activity add new mProduct
                        startActivity(new Intent(ProductActivity.this, NewProduct.class).putExtra(PRODUCT_BARCODE, mBarcode));
                    } else if (dataSnapshot.getChildrenCount() > 1) {
                        for (DataSnapshot productSnapshop : dataSnapshot.getChildren()) {
                            mProduct = productSnapshop.getValue(Product.class);
                            String key = productSnapshop.getKey();
                            productArrayList.add(mProduct);
                            productKeyArrayList.add(key);
                        }
                        selectProductDialog(productArrayList, productKeyArrayList);

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

    private void selectProductDialog(final ArrayList<Product> productArrayList, final ArrayList<String> productKeyArrayList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_product);
        builder.setMessage(R.string.select_product_instructions);

        ArrayList<String> productNameList = new ArrayList<>();
        for (int i = 0; i < productArrayList.size(); i++) {
            productNameList.add(productArrayList.get(i).Name);
        }
        CharSequence[] cs = productNameList.toArray(new CharSequence[productNameList.size()]);
        builder.setItems(cs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mProduct = productArrayList.get(i);
                //mProductId = productKeyArrayList.get(i);
                mProduct.setId(productKeyArrayList.get(i));
                readProductPriceFromFirebase(); //updateProductUI();

            }
        });
        builder.show();
    }

    public void updateProductUI() {
        //Storage for product picture

        String id = mProduct.getId();
        if (id != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageRef = storage.getReference().child("images").child(id);
            TextView textView;
            textView = (TextView) findViewById(R.id.productName);
            textView.setText(mProduct.Name);
            textView = (TextView) findViewById(R.id.productType);
            textView.setText(mProduct.Type);
            textView = (TextView) findViewById(R.id.productBrand);
            textView.setText(mProduct.Brand);
            textView = (TextView) findViewById(R.id.productQuantity);
            String Qty = mProduct.Quantity + " " + mProduct.Units;
            textView.setText(Qty);
            ImageView productImage = (ImageView) findViewById(R.id.productImage);
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageRef)
                    .into(productImage);
            findViewById(R.id.scroll_group_view).setVisibility(View.VISIBLE);

            //Reading extra product data
            //Price
            TextView price_text = (TextView) findViewById(R.id.productPrice);
            Float price = mProduct.getCurrentPrice();
            if (price!=null) {
                price_text.setText(NumberFormat.getCurrencyInstance().format(price));
            }else {
                price_text.setText(R.string.no_price);
            }

            //Offer
            TextView offer_text = (TextView) findViewById(R.id.offer_price);
            Button update = (Button) findViewById(R.id.update_price_button);
            Float offerPrice = mProduct.getCurrentOffer();
            if (offerPrice!=null) {
                findViewById(R.id.offers_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.on_sale_button).setVisibility(View.GONE);
                update.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                String text = NumberFormat.getCurrencyInstance().format(offerPrice);
                offer_text.setText(text);
                price_text.setPaintFlags(price_text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }else {
                findViewById(R.id.offers_layout).setVisibility(View.GONE);
                findViewById(R.id.on_sale_button).setVisibility(View.VISIBLE);
                update.setTextColor(getResources().getColor(R.color.primaryTextColor));
                price_text.setPaintFlags(price_text.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            //Las Purchase
            CardView cardView = (CardView) findViewById(R.id.cardview_purchase_history);
            TextView last_price = (TextView) findViewById(R.id.last_purchase_price);
            TextView last_date = (TextView) findViewById(R.id.last_purchase_date);

            if (lastPurchaseDate != null && lastPurchasePrice != null) {
                cardView.setVisibility(View.VISIBLE);
                last_price.setText(NumberFormat.getCurrencyInstance().format(lastPurchasePrice));
                Long ndate = Long.parseLong(lastPurchaseDate);
                String sdate = getDate(ndate, "dd/MM/yyyy hh:mm");
                last_date.setText(sdate);
            }else {
                cardView.setVisibility(View.GONE);
            }

            hideProgressDialog();
            //readProductPriceFromFirebase();
            //readProductOfferFromFirebase();
            // readLastPurchaseFromFirebase();
        }
    }

    public void readProductPriceFromFirebase() {
        //Check for the mProduct price in the selected store
        String id = mProduct.getId();
        if (id != null) {
            myPriceRef = mDatabase.getReference().child("prices").child(id).child(mStoreId);
            myPriceListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //TextView textView = (TextView) findViewById(R.id.productPrice);

                    //Check if mProduct exist in database
                    if (dataSnapshot.getValue() != null) {
                        Float price = Float.parseFloat(dataSnapshot.getValue().toString());
                        mProduct.setCurrentPrice(price);
                        mProduct.setOffer(false);
                        //textView.setText(NumberFormat.getCurrencyInstance().format(price));
                    } else {
                        //if price  does'nt exist in database
                        //textView.setText(R.string.no_price);
                        mProduct.setCurrentPrice(null);
                        mProduct.setOffer(false);
                    }
                    readProductOfferFromFirebase();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    readProductOfferFromFirebase();
                }
            };

            myPriceRef.addValueEventListener(myPriceListener);
        }
    }

    public void readProductOfferFromFirebase() {
        //Check for offer in the selected store
        String id = mProduct.getId();
        if (id != null) {
            Calendar dateToday = Calendar.getInstance();
            dateToday.set(Calendar.HOUR_OF_DAY, 0);
            dateToday.set(Calendar.MINUTE, 0);
            Long dateStart = dateToday.getTimeInMillis();
            mProduct.setCurrentOffer(null);
            mProduct.setOffer(false);
            myOfferRef = mDatabase.getReference().child("offers").child(id).child(mStoreId);
            myOfferQuery = myOfferRef.orderByKey().startAt(dateStart.toString());
            myOfferListener = new ValueEventListener() {
                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {

                    //TextView offer_text = (TextView) findViewById(R.id.offer_price);
                    //TextView price_text = (TextView) findViewById(R.id.productPrice);
                    //Button update = (Button) findViewById(R.id.update_price_button);

                    //Check if mProduct exist in database
                    if (dataSnapshot.getChildren() != null) {
                        for (DataSnapshot offerSnapshot : dataSnapshot.getChildren()) {
//                            findViewById(R.id.offers_layout).setVisibility(View.VISIBLE);
//                            findViewById(R.id.on_sale_button).setVisibility(View.GONE);
                            //update.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            if (offerSnapshot.getValue()!=null) {
                                Float offerPrice = Float.parseFloat(offerSnapshot.getValue().toString());
                                mProduct.setCurrentOffer(offerPrice);
                                mProduct.setOffer(true);
                            }

//                            String text = NumberFormat.getCurrencyInstance().format(offerPrice);
//                            offer_text.setText(text);
//                            price_text.setPaintFlags(price_text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        }
                    } else {
                        //if offer  does'nt exist in database
//                        findViewById(R.id.offers_layout).setVisibility(View.GONE);
//                        findViewById(R.id.offer_button).setVisibility(View.VISIBLE);
//                        update.setTextColor(getResources().getColor(R.color.primaryTextColor));
//                        price_text.setPaintFlags(price_text.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        mProduct.setCurrentOffer(null);
                        mProduct.setOffer(false);
                    }
                    readLastPurchaseFromFirebase();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    readLastPurchaseFromFirebase();
                }
            };

            myOfferQuery.addValueEventListener(myOfferListener);
        }
    }

    public void readLastPurchaseFromFirebase() {
        //Check for last user purchase on database
        String id = mProduct.getId();
        lastPurchasePrice = null;
        lastPurchaseDate = null;
        if (mUserId != null && id != null) {
            myPurchasesRef = mDatabase.getReference().child("purchases").child(mUserId).child(id);
            myPurchasesQuery = myPurchasesRef.orderByKey().limitToLast(1);
            myPurchasesListener = new ValueEventListener() {
                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {
//                    CardView cardView = (CardView) findViewById(R.id.cardview_purchase_history);
//                    TextView price = (TextView) findViewById(R.id.last_purchase_price);
//                    TextView date = (TextView) findViewById(R.id.last_purchase_date);
//                    cardView.setVisibility(View.GONE);
                    //Check if mProduct exist in database
                    if (dataSnapshot.getChildren() != null) {
                        for (DataSnapshot purchasesSnapshot : dataSnapshot.getChildren()) {
                            Purchase purchase = purchasesSnapshot.getValue(Purchase.class);
                            lastPurchasePrice = purchase.price;
                            lastPurchaseDate = purchasesSnapshot.getKey();
//                            cardView.setVisibility(View.VISIBLE);
                        }
//                        if (lastPurchaseDate != null && lastPurchasePrice != null) {
//                            price.setText(NumberFormat.getCurrencyInstance().format(lastPurchasePrice));
//                            Long ndate = Long.parseLong(lastPurchaseDate);
//                            String sdate = getDate(ndate, "dd/MM/yyyy hh:mm");
//                            date.setText(sdate);
//                        }
                    }
                    updateProductUI();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    updateProductUI();
                }
            };

            myPurchasesQuery.addValueEventListener(myPurchasesListener);
        }
    }

    public void comparePrices() {
        startActivity(new Intent(ProductActivity.this, comparePricesActivity.class).putExtra(PRODUCT_ID, mProduct.getId()));
    }

    public void purchasesHistoryFirebase() {
        String id = mProduct.getId();
        if (id != null && mUserId != null) {

            startActivity(new Intent(ProductActivity.this, purchaseHistory.class)
                    .putExtra(PRODUCT_ID, id)
                    .putExtra(USER_ID, mUserId)
            );
        } else
            Snackbar.make(findViewById(R.id.placeSnackBar), getString(R.string.missing_info), Snackbar.LENGTH_SHORT).show();
    }

    public void updatePriceFirebase() {

        if (mCompanyName != null && mStoreName != null && mStoreId != null && mProduct != null && mUserId != null) {
            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.update_price);
            builder.setMessage(mCompanyName + " " + mStoreName + "\n" + mProduct.Name);

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(R.string.update_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String id = mProduct.getId();
                    Float price = Float.parseFloat(input.getText().toString());
                    Long date = System.currentTimeMillis();

                    // Write to the database
                    Map<String, Object> childUpdates = new HashMap<>();
                    Map<String, Object> updateHistory = new HashMap<>();
                    updateHistory.put("price", price);
                    updateHistory.put("user", mUserId);
                    childUpdates.put("/prices/" + id + "/" + mStoreId, price);
                    childUpdates.put("/prices_history/" + id + "/" + mStoreId + date, updateHistory);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();
                    myRef.updateChildren(childUpdates);
                    readProductPriceFromFirebase();

                }
            });
            builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialog = builder.create();
            dialog.show();
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            ;
        } else
            Snackbar.make(findViewById(R.id.placeSnackBar), getString(R.string.update_error), Snackbar.LENGTH_SHORT).show();

    }

    public void markAsOfferFirebase() {
        final String id = mProduct.getId();
        if (mCompanyName != null && mStoreName != null && mStoreId != null && mProduct != null && mUserId != null && id != null) {
            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.mark_as_offer_dialog);
            builder.setMessage(mCompanyName + " " + mStoreName + "\n" + mProduct.Name + "\n\n" + getResources().getString(R.string.offer_price_text));

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(R.string.update_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Float offerPrice = Float.parseFloat(input.getText().toString());
                    Long timestamp = System.currentTimeMillis();

                    // Write to the database
                    // Write to the database
                    Map<String, Object> childUpdates = new HashMap<>();
                    Map<String, Object> updateHistory = new HashMap<>();
                    updateHistory.put("price", offerPrice);
                    updateHistory.put("user", mUserId);
                    childUpdates.put("/offers/" + id + "/" + mStoreId + "/" + timestamp, offerPrice);
                    childUpdates.put("/offers_history/" + id + "/" + mStoreId + timestamp, updateHistory);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference();
                    myRef.updateChildren(childUpdates);
                    readProductOfferFromFirebase();

                }
            });
            builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialog = builder.create();
            dialog.show();
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else
            Snackbar.make(findViewById(R.id.placeSnackBar), getString(R.string.missing_info), Snackbar.LENGTH_SHORT).show();
    }

    public void addPurchase() {
        if (mStoreId != null  && mUserId != null  && mProduct != null) {
            // Write to the local database
            saveRegisterProductLocally(mProduct);

            Snackbar.make(findViewById(R.id.placeSnackBar), getString(R.string.purchase_added_snackbar), Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo_snackbar_button, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            removeLastRegisterLocally();
                        }
                    })
                    .show();
        } else
            Snackbar.make(findViewById(R.id.placeSnackBar), getString(R.string.missing_info), Snackbar.LENGTH_SHORT).show();
    }

    private void removeLastRegisterLocally() {
        ArrayList products = new ArrayList();
        try {
            products = readObjectsFromFile("myProducts.txt", getApplicationContext());

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
        }
        if (products != null) {
            if (products.size() > 0) {
                int i = products.size() - 1;
                products.remove(i);
                try {
                    writeObjectsToFile("myProducts.txt", products, getApplicationContext());
                } catch (IOException e) {
                    System.out.println("Error initializing stream");
                }
            }
        }
    }

    private void saveRegisterProductLocally(Product product) {
        ArrayList products = new ArrayList();
        try {
            products = readObjectsFromFile("myProducts.txt", getApplicationContext());

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
        }
        products.add(product);
        try {
            writeObjectsToFile("myProducts.txt", products, getApplicationContext());
        } catch (IOException e) {
            System.out.println("Error initializing stream");
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
                    mBarcode = barcode;
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
                addPurchase();
                break;
            case R.id.update_price_button:
                if (findViewById(R.id.on_sale_button).getVisibility() == View.VISIBLE) {
                    updatePriceFirebase();
                } else markAsOfferFirebase();
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
            case R.id.my_cart_button:
                startActivity(new Intent(this, MyCart.class));
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString("mStoreId", mStoreId);
        savedInstanceState.putString("mStoreName", mStoreName);
        savedInstanceState.putString("mCompanyName", mCompanyName);
        savedInstanceState.putString("mUserId", mUserId);


        super.onSaveInstanceState(savedInstanceState);
    }

}
