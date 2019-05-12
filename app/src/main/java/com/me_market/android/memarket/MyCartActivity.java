package com.me_market.android.memarket;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Purchase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static com.me_market.android.memarket.MainActivity.SHARED_PREF;
import static com.me_market.android.memarket.ProductActivity.MY_PURCHASES_SAVE;
import static com.me_market.android.memarket.SplashActivity.USER_ID;


public class MyCartActivity extends BaseActivity implements View.OnClickListener {

    private String mUserId;
    private BottomSheetBehavior behavior;
    private GridLayout gridLayout;
    private ArrayList<Purchase> purchaseArrayList;
    private ListView listView;
    private String mCityCode;
    private String mCountryCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_cart);

        //Getting extra
        mUserId = getIntent().getStringExtra(USER_ID);

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_cart_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.my_cart_label);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        //Buttons listeners
        findViewById(R.id.my_cart_gridlayout).setOnClickListener(this);

        //Setting bottom sheet
        gridLayout = (GridLayout) findViewById(R.id.my_cart_gridlayout);
        behavior = BottomSheetBehavior.from(gridLayout);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                switch (i) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        hideBottomItems();
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        showBottomItems();
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        showBottomItems();
                        break;
                }

            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        //Getting the selected city
        SharedPreferences sharedPref = this.getSharedPreferences(SHARED_PREF,Context.MODE_PRIVATE);
        mCityCode = sharedPref.getString(getString(R.string.area_pref),null);
        mCountryCode= sharedPref.getString(getString(R.string.country_pref),null);


        //Getting the product list
        setListProducts();

    }

    private void showTotalPrice() {
        Float price = 0f, offerPrice = 0f, totalPrice = 0f, subTotal = 0f, discount = 0f, qty = 0f;
        Purchase purchase;
        if (purchaseArrayList != null) {
            for (int i = 0; i < purchaseArrayList.size(); i++) {
                purchase = purchaseArrayList.get(i);
                price = purchase.price;
                offerPrice = purchase.offerPrice;
                qty = purchase.quantity;

                if (price != null) {
                    subTotal = subTotal + price * qty;
                    if (purchase.isOffer) {
                        if (offerPrice != null) totalPrice = totalPrice + offerPrice * qty;
                    } else {
                        totalPrice = totalPrice + price * qty;
                    }
                    discount = subTotal - totalPrice;
                }
            }
            //Total price
            TextView textView = (TextView) findViewById(R.id.total_number);
            String text = NumberFormat.getCurrencyInstance().format(totalPrice);
            textView.setText(text);
            //Subtotal price
            textView = (TextView) findViewById(R.id.subtotal_number);
            text = NumberFormat.getCurrencyInstance().format(subTotal);
            textView.setText(text);
            //Discount price
            textView = (TextView) findViewById(R.id.discount_number);
            text = NumberFormat.getCurrencyInstance().format(discount);
            textView.setText(text);
        }

    }

    private void setListProducts() {
        purchaseArrayList = readRegisterProductLocally();
        listView = (ListView) findViewById(R.id.my_cart_listview);
        if (purchaseArrayList != null) {
            productArrayAdapter adapter = new productArrayAdapter(this, R.layout.listview_my_cart, purchaseArrayList);
            listView.setAdapter(adapter);
            showTotalPrice();
        } else {
            listView.setAdapter(null);
        }
    }

    private void showBottomItems() {
        findViewById(R.id.subtotal_text).setVisibility(View.VISIBLE);
        findViewById(R.id.subtotal_number).setVisibility(View.VISIBLE);
        findViewById(R.id.discount_text).setVisibility(View.VISIBLE);
        findViewById(R.id.discount_number).setVisibility(View.VISIBLE);
    }

    private void hideBottomItems() {
        findViewById(R.id.subtotal_text).setVisibility(View.GONE);
        findViewById(R.id.subtotal_number).setVisibility(View.GONE);
        findViewById(R.id.discount_text).setVisibility(View.GONE);
        findViewById(R.id.discount_number).setVisibility(View.GONE);
    }

    private void expandBottomSheet() {
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void addPurchaseToFirebase() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.add_all_purchases_question));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int j) {
                HashMap<String, Object> childUpdates = new HashMap<>();
                Purchase register_product;

                if (purchaseArrayList != null) {
                    for (int i = 0; i < purchaseArrayList.size(); i++) {
                        register_product = purchaseArrayList.get(i);
                        childUpdates.put("/" + getString(R.string.purchases_fb) + "/" + mUserId + "/" + register_product.productId + "/" + register_product.timeStamp, register_product);
                    }
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference().child(mCountryCode).child(mCityCode);
                    myRef.updateChildren(childUpdates);
                    clearCart();
                    Snackbar.make(findViewById(R.id.my_cart_list_layout), getString(R.string.purchase_added_snackbar), Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();

    }

    public void clearCart() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.clear_cart_question));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int j) {
                try {
                    purchaseArrayList = null;
                    emptyObjectFile(MY_PURCHASES_SAVE, getApplicationContext());
                    setListProducts();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();

    }

    private void eraseProduct(int position) {
        purchaseArrayList.remove(position);
        try {
            writeObjectsToFile(MY_PURCHASES_SAVE, purchaseArrayList, getApplicationContext());
        } catch (IOException e) {
            System.out.println("Error initializing stream");
        }
        setListProducts();
    }

    public class ViewHolder {
        private View row;
        private TextView name = null, type = null, price = null, qty = null;
        private Button erase_button;

        ViewHolder(View row) {
            this.row = row;
        }

        public TextView getNameText() {
            if (this.name == null) {
                this.name = (TextView) row.findViewById(R.id.cart_product_name);
            }
            return this.name;
        }

        public TextView getTypeText() {
            if (this.type == null) {
                this.type = (TextView) row.findViewById(R.id.cart_product_type);
            }
            return this.type;
        }

        public TextView getPriceText() {
            if (this.price == null) {
                this.price = (TextView) row.findViewById(R.id.cart_product_price);
            }
            return this.price;
        }

        public TextView getQtyText() {
            if (this.qty == null) {
                this.qty = (TextView) row.findViewById(R.id.cart_product_qty);
            }
            return this.qty;
        }

        public Button getButton() {
            if (this.erase_button == null) {
                this.erase_button = (Button) row.findViewById(R.id.cart_product_delete_button);
            }
            return this.erase_button;
        }

    }

    class productArrayAdapter extends ArrayAdapter<Purchase> {
        private Context context;
        private ArrayList<Purchase> purchases;

        public productArrayAdapter(Context context, int resource, ArrayList<Purchase> purchases) {
            super(context, resource, purchases);
            this.context = context;
            this.purchases = purchases;

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            //get the property we are displaying
            Purchase purchase = purchases.get(position);

            //get the inflater and inflate the XML layout for each item
            ViewHolder holder = null;
            LayoutInflater inflater = getLayoutInflater();
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.listview_my_cart, null, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.getNameText().setText(purchase.productName);
            holder.getTypeText().setText(purchase.productType);
            String qty = String.format(Locale.getDefault(), "%f", purchase.quantity);
            holder.getQtyText().setText(qty);
            if (purchase.isOffer) {
                holder.getPriceText().setText(NumberFormat.getCurrencyInstance().format(purchase.offerPrice));
            } else {
                holder.getPriceText().setText(NumberFormat.getCurrencyInstance().format(purchase.price));
            }
            holder.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    eraseProduct(position);
                }
            });


            return convertView;
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i) {
            case R.id.my_cart_gridlayout:
                expandBottomSheet();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_cart_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //AppBar onClick method
        int i = item.getItemId();

        switch (i) {
            case R.id.done_button:
                addPurchaseToFirebase();
                return true;

            case R.id.clear_button:
                clearCart();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    private ArrayList<Purchase> readRegisterProductLocally() {
        ArrayList purchases;
        try {
            purchases = readObjectsFromFile(MY_PURCHASES_SAVE, this);
            return purchases;
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

}
