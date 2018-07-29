package com.me_market.android.memarket;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Product;
import com.me_market.android.memarket.models.Purchase;
import com.me_market.android.memarket.models.Sale;
import com.me_market.android.memarket.models.Store;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import static com.me_market.android.memarket.BarcodeReader.PRODUCT_ID;
import static com.me_market.android.memarket.MainActivity.SHARED_PREF;
import static com.me_market.android.memarket.SelectStoreActivity.PREFS_FILE;
import static com.me_market.android.memarket.SplashActivity.USER_ID;


public class ProductActivity extends BaseActivity implements View.OnClickListener {

    public static final String SELECT_UI = "com.me_market.android.memarket.SELECT_UI";
    public static final String filename = "myPurchases.save";
    private static final int RC_BARCODE_CAPTURE = 9001;
    public static final String PRODUCT_DATA = "com.me_market.android.memarket.PRODUCT_DATA";


    private FloatingActionButton scan_fab;
    private FloatingActionButton add_purchase_fab;
    private String selectUi;
    private String mProductId;
    private Product mProduct;
    private Product mProductUnit;
    private Boolean scan_barcode_called;
    private Store mStore;
    private String mUserId;
    private String lastPurchaseDate;
    private Float lastPurchasePrice;
    private String mCityCode;
    private String mCountryCode;
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;
    private DatabaseReference myPriceRef;
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_profile_toolbar);
        scan_fab = (FloatingActionButton) findViewById(R.id.scan_fab);
        add_purchase_fab = (FloatingActionButton) findViewById(R.id.add_purchase_fab);

        //Setting Toolbar
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        //Buttons Listeners
        scan_fab.setOnClickListener(this);
        add_purchase_fab.setOnClickListener(this);
        findViewById(R.id.update_price_button).setOnClickListener(this);
        findViewById(R.id.compare_price_button).setOnClickListener(this);
        findViewById(R.id.view_history_button).setOnClickListener(this);
        findViewById(R.id.on_sale_button).setOnClickListener(this);
        findViewById(R.id.view_unit_detail_button).setOnClickListener(this);


        //Getting the selected city
        SharedPreferences sharedPref = this.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        mCityCode = sharedPref.getString(getString(R.string.area_pref), null);
        mCountryCode = sharedPref.getString(getString(R.string.country_pref), null);

        // Get the Intent that started this activity and extract the string
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            mUserId = intent.getStringExtra(USER_ID);
            mProductId = intent.getStringExtra(PRODUCT_ID);
            selectUi = getIntent().getStringExtra(SELECT_UI);

        } else {
            mUserId = savedInstanceState.getString("mUserId");
            mProductId = savedInstanceState.getString("mProductId");
            selectUi = savedInstanceState.getString("selectUi");
            scan_barcode_called = savedInstanceState.getBoolean("scan_barcode_called");
        }

        // If there is no product id.
        if (mProductId == null) {
            //startActivity(new Intent(this, BarcodeReader.class).putExtra(USER_ID, mUserId));

            scan_barcode();
        }
        //If we lost the UserID
        if (mUserId == null) getUserFirebaseData();

        // Get selected Store
        mStore = getSelectedStore();

        //Set the UI
        if (selectUi == null) selectUi = "";
        switch (selectUi) {
            case "Select":
                scan_fab.setVisibility(View.INVISIBLE);
                add_purchase_fab.setVisibility(View.INVISIBLE);
                break;
            default:
                scan_fab.show();
                add_purchase_fab.show();

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
                break;
        }

        // Set store name on price card
        TextView textView;
        String storename;
        if (mStore != null) {
            storename = mStore.CompanyData.Name + " " + mStore.Name;
        } else {
            storename = getString(R.string.select_store_instruction);
        }

        textView = (TextView) findViewById(R.id.selectedStoreName);
        textView.setText(storename);
    }

    private void askToSelectStore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_store_instruction);

        builder.setPositiveButton(R.string.select_store, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startSelectStore();
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

    private void startSelectStore() {
        startActivity(new Intent(ProductActivity.this, SelectStoreActivity.class));
        mStore = getSelectedStore();
        readProductFromFirebase();
    }

    public void scan_barcode() {

        // launch barcode activity.
        if (scan_barcode_called == null) {
            Intent intent = new Intent(ProductActivity.this, BarcodeReader.class);
            intent.putExtra(BarcodeReader.AutoFocus, true);
            intent.putExtra(BarcodeReader.UseFlash, false);
            intent.putExtra(SELECT_UI, false);
            scan_barcode_called = true;
            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }
        scan_barcode_called = null;
    }

    public void readProductFromFirebase() {

        if (mProductId != null) {
            showProgressDialog(getString(R.string.loading), ProductActivity.this);

            myRef = mDatabase.getReference().child(mCountryCode).child(getString(R.string.products_fb)).child(mProductId);

            myProductListener = new ValueEventListener() {
                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {

                    mProduct = dataSnapshot.getValue(Product.class);
                    if (mProduct != null) {
                        mProduct.setId(dataSnapshot.getKey());
                        if (mProduct.hasChild!= null) {
                            if (mProductUnit.hasChild) mProductUnit = mProduct.getProductChild();
                        } else mProductUnit = null;
                        updateProductUI();
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

    public void readProductPriceFromFirebase() {
        //Check for the mProduct price in the selected store
        String id = mProduct.getId();
        if (id != null && mStore != null) {
            findViewById(R.id.cardview_product_prices).setVisibility(View.VISIBLE);
            findViewById(R.id.no_store_selected_text).setVisibility(View.GONE);
            myPriceRef = mDatabase.getReference().child(mCountryCode).child(mCityCode).child(getString(R.string.prices_fb)).child(id).child(mStore.getId());
            myPriceListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //Check if mProduct exist in database
                    if (dataSnapshot.getValue() != null) {
                        Float price = Float.parseFloat(dataSnapshot.getValue().toString());
                        mProduct.setCurrentPrice(price);
                        mProduct.setOffer(false);
                    } else {
                        mProduct.setCurrentPrice(null);
                        mProduct.setOffer(false);
                    }
                    updateProductPriceOfferUI();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            myPriceRef.addValueEventListener(myPriceListener);
        } else {
            findViewById(R.id.cardview_product_prices).setVisibility(View.GONE);
            findViewById(R.id.no_store_selected_text).setVisibility(View.VISIBLE);
        }
    }

    public void readProductOnSalePriceFromFirebase() {
        //Check for offer in the selected store
        String id = mProduct.getId();
        DatabaseReference myOfferRef;

        if (id != null && mStore != null) {
            Calendar dateToday = Calendar.getInstance();
            dateToday.set(Calendar.HOUR_OF_DAY, 0);
            dateToday.set(Calendar.MINUTE, 0);
            Long dateStart = dateToday.getTimeInMillis();
            mProduct.setCurrentOffer(null);
            mProduct.setOffer(false);
            myOfferRef = mDatabase.getReference().child(mCountryCode).child(mCityCode).child(getString(R.string.sales_fb)).child(id).child(mStore.getId());
            myOfferQuery = myOfferRef.orderByKey().startAt(dateStart.toString());
            myOfferListener = new ValueEventListener() {
                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {

                    //Check if mProduct exist in database
                    if (dataSnapshot.getChildren() != null) {
                        for (DataSnapshot offerSnapshot : dataSnapshot.getChildren()) {
                            if (offerSnapshot.getValue() != null) {
                                Float offerPrice = Float.parseFloat(offerSnapshot.getValue().toString());
                                mProduct.setCurrentOffer(offerPrice);
                                mProduct.setOffer(true);
                            }
                        }
                    } else {
                        mProduct.setCurrentOffer(null);
                        mProduct.setOffer(false);
                    }
                    updateProductPriceOfferUI();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            myOfferQuery.addValueEventListener(myOfferListener);
        }
    }

    public void readLastPurchaseFromFirebase() {
        //Check for last user purchase on database
        String id = mProduct.getId();
        DatabaseReference myPurchasesRef;
        lastPurchasePrice = null;
        lastPurchaseDate = null;
        if (mUserId != null && id != null) {
            myPurchasesRef = mDatabase.getReference().child(mCountryCode).child(mCityCode).child(getString(R.string.purchases_fb)).child(mUserId).child(id);
            myPurchasesQuery = myPurchasesRef.orderByKey().limitToLast(1);
            myPurchasesListener = new ValueEventListener() {
                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {

                    //Check if mProduct exist in database
                    if (dataSnapshot.getChildren() != null) {
                        for (DataSnapshot purchasesSnapshot : dataSnapshot.getChildren()) {
                            Purchase purchase = purchasesSnapshot.getValue(Purchase.class);
                            if (purchase != null) {
                                if (purchase.isOffer) {
                                    lastPurchasePrice = purchase.offerPrice;
                                } else {
                                    lastPurchasePrice = purchase.price;
                                }
                                lastPurchaseDate = purchasesSnapshot.getKey();
                            }
                        }
                        updateProductLastPurchaseUI();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            myPurchasesQuery.addValueEventListener(myPurchasesListener);
        }
    }

    public void updateProductUI() {

        String id = mProduct.getId();
        if (id != null) {
            //Storage for product picture
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageRef = storage.getReference().child(mCountryCode).child(getString(R.string.images_fb)).child(id);
            TextView textView;
            //Set product barcode.
            textView = (TextView) findViewById(R.id.productCode);
            textView.setText(mProduct.Barcode);
            textView = (TextView) findViewById(R.id.product_name_TextView);
            textView.setText(mProduct.Name);
            textView = (TextView) findViewById(R.id.product_description_TextView);
            textView.setText(mProduct.Type);
            textView = (TextView) findViewById(R.id.product_brand_TextView);
            textView.setText(mProduct.Brand);
            textView = (TextView) findViewById(R.id.product_qty_TextView);
            String Qty = mProduct.Quantity + " " + mProduct.Units;
            textView.setText(Qty);
            ImageView productImage = (ImageView) findViewById(R.id.product_image_ImageView);
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(storageRef)
                    .into(productImage);
            findViewById(R.id.scroll_group_view).setVisibility(View.VISIBLE);
            findViewById(R.id.view_unit_detail_button).setVisibility(View.GONE);
            findViewById(R.id.product_unit_detail_layout).setVisibility(View.GONE);

            //Getting unit detail info
            if (mProductUnit != null) {
                if (mProductUnit.getId() != null) {
                    findViewById(R.id.view_unit_detail_button).setVisibility(View.VISIBLE);
                    final StorageReference storageRef2 = storage.getReference().child(mCountryCode).child(getString(R.string.images_fb)).child(mProductUnit.getId());
                    ImageView productUnitImage = (ImageView) findViewById(R.id.product_unit_image);
                    Glide.with(this)
                            .using(new FirebaseImageLoader())
                            .load(storageRef2)
                            .into(productUnitImage);
                    productUnitImage.setVisibility(View.VISIBLE);
                }
                findViewById(R.id.product_unit_detail_layout).setVisibility(View.VISIBLE);
                textView = (TextView) findViewById(R.id.product_unit_name_TextView);
                textView.setText(mProductUnit.Name);
                textView = (TextView) findViewById(R.id.product_unit_description_TextView);
                textView.setText(mProductUnit.Type);
                textView = (TextView) findViewById(R.id.product_unit_quantity_TextView);
                Qty = mProductUnit.Quantity + " " + mProductUnit.Units;
                textView.setText(Qty);
            }


            hideProgressDialog();

            //Getting the extra info
            readProductPriceFromFirebase();
            readProductOnSalePriceFromFirebase();
            readLastPurchaseFromFirebase();
        }
    }

    public void updateProductPriceOfferUI() {
        String id = mProduct.getId();
        if (id != null && mStore != null) {

            TextView price_text = (TextView) findViewById(R.id.productPrice);
            Float price = mProduct.getCurrentPrice();
            if (price != null) {
                price_text.setText(NumberFormat.getCurrencyInstance().format(price));
            } else {
                price_text.setText(R.string.no_price);
            }

            //Offer
            TextView offer_text = (TextView) findViewById(R.id.offer_price);
            Button update = (Button) findViewById(R.id.update_price_button);
            Float offerPrice = mProduct.getCurrentOffer();
            if (offerPrice != null) {
                findViewById(R.id.offers_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.on_sale_button).setVisibility(View.GONE);
                update.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                String text = NumberFormat.getCurrencyInstance().format(offerPrice);
                offer_text.setText(text);
                price_text.setPaintFlags(price_text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                findViewById(R.id.offers_layout).setVisibility(View.GONE);
                findViewById(R.id.on_sale_button).setVisibility(View.VISIBLE);
                update.setTextColor(getResources().getColor(R.color.primaryTextColor));
                price_text.setPaintFlags(price_text.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }

    public void updateProductLastPurchaseUI() {
        //Las Purchase
        String id = mProduct.getId();
        if (id != null) {
            CardView cardView = (CardView) findViewById(R.id.cardview_purchase_history);
            TextView last_price = (TextView) findViewById(R.id.last_purchase_price);
            TextView last_date = (TextView) findViewById(R.id.last_purchase_date);
            cardView.setVisibility(View.GONE);

            if (lastPurchaseDate != null && lastPurchasePrice != null) {
                cardView.setVisibility(View.VISIBLE);
                last_price.setText(NumberFormat.getCurrencyInstance().format(lastPurchasePrice));
                Long ndate = Long.parseLong(lastPurchaseDate);
                String sdate = getDate(ndate, "dd/MM/yyyy hh:mm");
                last_date.setText(sdate);
            }

            hideProgressDialog();
        }
    }

    public void comparePrices() {
        startActivity(new Intent(ProductActivity.this, ComparePricesActivity.class).putExtra(PRODUCT_ID, mProduct.getId()));
    }

    public void startPurchasesHistory() {
        String id = mProduct.getId();
        if (id != null && mUserId != null) {

            startActivity(new Intent(ProductActivity.this, PurchaseHistoryActivity.class)
                    .putExtra(PRODUCT_ID, id)
                    .putExtra(USER_ID, mUserId)
            );
        } else
            Snackbar.make(findViewById(R.id.placeSnackBar), getString(R.string.missing_info), Snackbar.LENGTH_SHORT).show();
    }

    public void updatePriceFirebase() {

        if (mStore != null && mProduct != null && mUserId != null) {
            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.update_price);
            builder.setMessage(mProduct.Name);

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
                    childUpdates.put("/" + getString(R.string.prices_fb) + "/" + id + "/" + mStore.getId(), price);
                    childUpdates.put("/" + getString(R.string.prices_history_fb) + "/" + id + "/" + mStore.getId() + "/" + date, updateHistory);

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference().child(mCountryCode).child(mCityCode);
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

        } else
            Snackbar.make(findViewById(R.id.placeSnackBar), getString(R.string.update_error), Snackbar.LENGTH_SHORT).show();

    }

    public void markAsOnSaleFirebase() {
        final String id = mProduct.getId();
        if (mStore != null && mProduct != null && mUserId != null && id != null) {
            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.offer_text);
            builder.setMessage(mProduct.Name);

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton(R.string.update_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Float onSalePrice = Float.parseFloat(input.getText().toString());
                    Sale sale = new Sale(onSalePrice, mUserId, mStore.getId(), mProduct.getId());
                    Long timestamp = System.currentTimeMillis();

                    // Write to the database
                    // Write to the database
                    Map<String, Object> childUpdates = new HashMap<>();

                    childUpdates.put("/" + getString(R.string.sales_fb) + "/" + id + "/" + mStore.getId() + "/" + timestamp, onSalePrice);
                    childUpdates.put("/" + getString(R.string.sales_history_fb) + "/" + "/" + timestamp, sale); //TODO: use push instead of timestamp????

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference().child(mCountryCode).child(mCityCode);
                    myRef.updateChildren(childUpdates);
                    readProductOnSalePriceFromFirebase();

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
        if (mUserId != null && mProduct != null) {
            if (mStore != null) {
                if (mProduct.getCurrentPrice() != null) {
                    // Write to the local database

                    AlertDialog dialog;
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    final View view = getLayoutInflater().inflate(R.layout.add_purchase_dialog, null);
                    builder.setView(view);

                    // Set up the buttons
                    builder.setPositiveButton(R.string.add_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText input = view.findViewById(R.id.purchase_quantity);
                            Float qty;
                            try {
                                qty = Float.parseFloat(input.getText().toString());
                            } catch (Exception e) {
                                qty = 0f;
                                Snackbar.make(findViewById(R.id.placeSnackBar), getString(R.string.update_error), Snackbar.LENGTH_LONG).show();
                                dialog.cancel();
                            }

                            Long date = System.currentTimeMillis();
                            Purchase purchase =
                                    new Purchase(mProduct.getId(),
                                            mProduct.Name, mProduct.Type,
                                            mStore.getId(),
                                            date,
                                            qty,
                                            mProduct.getCurrentPrice(),
                                            mProduct.getCurrentOffer(),
                                            mProduct.getOffer());
                            saveRegisterProductLocally(purchase);
                            Snackbar.make(findViewById(R.id.placeSnackBar), getString(R.string.purchase_added_snackbar), Snackbar.LENGTH_LONG)
                                    .setAction(R.string.undo_snackbar_button, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            removeLastRegisterLocally();
                                        }
                                    })
                                    .show();

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
                } else {
                    Snackbar.make(findViewById(R.id.placeSnackBar),
                            getString(R.string.update_price_instruction),
                            Snackbar.LENGTH_SHORT)
                            .setAction(R.string.update_button, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    updatePriceFirebase();
                                }
                            })
                            .show();
                }
            } else {
                Snackbar.make(findViewById(R.id.placeSnackBar),
                        getString(R.string.select_store_instruction),
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.select_button, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                askToSelectStore();
                            }
                        })
                        .show();
            }
        } else {
            Snackbar.make(findViewById(R.id.placeSnackBar),
                    getString(R.string.missing_info),
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private void removeLastRegisterLocally() {
        ArrayList purchases = new ArrayList();
        try {
            purchases = readObjectsFromFile(filename, getApplicationContext());

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
        }
        if (purchases != null) {
            if (purchases.size() > 0) {
                int i = purchases.size() - 1;
                purchases.remove(i);
                try {
                    writeObjectsToFile(filename, purchases, getApplicationContext());
                } catch (IOException e) {
                    System.out.println("Error initializing stream");
                }
            }
        }
    }

    private void saveRegisterProductLocally(Purchase purchase) {
        ArrayList products = new ArrayList();
        try {
            products = readObjectsFromFile(filename, getApplicationContext());

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
        }
        products.add(purchase);
        try {
            writeObjectsToFile(filename, products, getApplicationContext());
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        }
    }

    @Nullable
    private Store getSelectedStore() {
        ArrayList<Store> stores;
        Store store;
        try {
            stores = readObjectsFromFile(PREFS_FILE, this);
            store = stores.get(0);
            return store;
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return null;
        } catch (IOException e) {
            System.out.println("Error initializing stream");
            return null;
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found");
            return null;
        }
    }

    public void getUserFirebaseData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mUserId = currentUser.getUid();
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        readProductFromFirebase();
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
                } else markAsOnSaleFirebase();
                break;
            case R.id.compare_price_button:
                comparePrices();
                break;
            case R.id.view_history_button:
                startPurchasesHistory();
                break;
            case R.id.on_sale_button:
                markAsOnSaleFirebase();
                break;
            case R.id.view_unit_detail_button:
                startActivity(new Intent(this, ProductActivity.class)
                        .putExtra(PRODUCT_ID, mProductUnit.getId()));
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (selectUi) {
            case "Select":
                getMenuInflater().inflate(R.menu.add_item_toolbar_menu, menu);
                break;
            default:
                getMenuInflater().inflate(R.menu.product_toolbar_menu, menu);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //AppBar onClick method
        int i = item.getItemId();

        switch (i) {
            case R.id.my_cart_button:
                if (mUserId != null) {
                    startActivity(new Intent(this, MyCartActivity.class).putExtra(USER_ID, mUserId));
                }
                return true;
            case R.id.select_store_button:
                startSelectStore();
                return true;
            case R.id.select_button:
                Intent data = new Intent();
                data.putExtra(PRODUCT_DATA, (Parcelable) mProduct);
                data.putExtra(PRODUCT_ID, mProductId);
                setResult(CommonStatusCodes.SUCCESS, data);
                finish();
                return true;
            case R.id.edit_product_button:
                if (mProductId != null) {
                    startActivity(new Intent(this, EditProductActivity.class).putExtra(PRODUCT_ID, mProductId));
                }
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString("mProductId", mProductId);
        savedInstanceState.putString("mUserId", mUserId);
        savedInstanceState.putString("selectUi", selectUi);
        if (scan_barcode_called != null)
            savedInstanceState.putBoolean("scan_barcode_called", scan_barcode_called);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Returning from barcode capture activity

        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    mProductId = data.getStringExtra(PRODUCT_ID);
                    readProductFromFirebase();
                } else {
                    finish();
                }
            } else {
                Snackbar.make(findViewById(R.id.placeSnackBar),
                        String.format(getString(R.string.barcode_error), CommonStatusCodes.getStatusCodeString(resultCode)),
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}

