package com.me_market.android.memarket;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Product;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static com.me_market.android.memarket.MainActivity.SHARED_PREF;
import static com.me_market.android.memarket.SplashActivity.USER_ID;

public class BarcodeHistory extends BaseActivity implements View.OnClickListener {

    public static final String BARCODE_HISTORY_SAVE = "barcodeHistory.save";
    private FirebaseDatabase mDatabase;
    private String mUserId;
    private ListView listView;
    private String mCountryCode;
    private ArrayList<Product> productListHistory;
    private static final int RC_SELECT_BARCODE = 9009;
    private int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_history);

        //Getting extra
        mUserId = getIntent().getStringExtra(USER_ID);

        //Get Firebase Instance
        mDatabase = FirebaseDatabase.getInstance();

        //Getting the selected city
        SharedPreferences sharedPref = this.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        mCountryCode = sharedPref.getString(getString(R.string.country_pref), null);

        //Setting Toolbar
        Toolbar toolbar = findViewById(R.id.barcode_history_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.barcode_history_label);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        //Setting ListView
        listView = findViewById(R.id.barcode_history_listview);

        //Getting the product shopping list
        //if (mUserId != null) {
        readProductArrayFromFirebase();
        //}


    }


    private void readProductArrayFromFirebase() {
        final ArrayList<String> productIdListHistory = readBarcodeHistoryArrayLocally();

        if (productIdListHistory != null) {
            showProgressDialog(getString(R.string.loading), BarcodeHistory.this);

            DatabaseReference myRef;
            ValueEventListener myListener;
            counter = 0;
            productListHistory = new ArrayList<>();
            for (String id : productIdListHistory) {

                myRef = mDatabase.getReference().child(mCountryCode).child("products").child(id);
                myListener = new ValueEventListener() {
                    @Override

                    public void onDataChange(DataSnapshot dataSnapshot) {
                        counter++;
                        Product mProduct = dataSnapshot.getValue(Product.class);
                        if (mProduct != null) {
                            productListHistory.add(mProduct);
                        }
                        if (counter == productIdListHistory.size()) {
                            if (!productListHistory.isEmpty()) {
                                setBarcodeHistoryList();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };

                myRef.addListenerForSingleValueEvent(myListener);

            }
        }
    }

    private void setBarcodeHistoryList() {

        listView = findViewById(R.id.barcode_history_listview);
        if (productListHistory != null) {
            barcodeHistoryArrayAdapter adapter = new barcodeHistoryArrayAdapter(this, R.layout.listview_my_cart, productListHistory);
            //adapter.notifyDataSetInvalidated();
            listView.setAdapter(adapter);

        } else {
            listView.setAdapter(null);
        }
        hideProgressDialog();

    }

    @Override
    public void onClick(View v) {

    }

    @Nullable
    private ArrayList<String> readBarcodeHistoryArrayLocally() {
        ArrayList productsId;
        try {
            productsId = readObjectsFromFile(BARCODE_HISTORY_SAVE, this);
            return productsId;
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

    public class ViewHolder {
        private View row;
        private TextView name = null, type = null, code = null;
//        private ImageView image;


        ViewHolder(View row) {
            this.row = row;
        }

        public TextView getNameText() {
            if (this.name == null) {
                this.name = (TextView) row.findViewById(R.id.barcode_history_product_name);
            }
            return this.name;
        }

        public TextView getTypeText() {
            if (this.type == null) {
                this.type = (TextView) row.findViewById(R.id.barcode_history_product_type);
            }
            return this.type;
        }

        public TextView getCodeText() {
            if (this.code == null) {
                this.code = (TextView) row.findViewById(R.id.barcode_history_code);
            }
            return this.code;
        }

//        public ImageView getImage() {
//            if (this.image == null) {
//                this.image = (ImageView) row.findViewById(R.id.barcode_history_product_image);
//            }
//            return this.image;
//        }

    }

    class barcodeHistoryArrayAdapter extends ArrayAdapter<Product> {
        private Context context;
        private ArrayList<Product> products;

        public barcodeHistoryArrayAdapter(Context context, int resource, ArrayList<Product> products) {
            super(context, resource, products);
            this.context = context;
            this.products = products;

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            //get the property we are displaying
            Product product = products.get(position);

            //get the inflater and inflate the XML layout for each item
            BarcodeHistory.ViewHolder holder = null;
            LayoutInflater inflater = getLayoutInflater();
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.listview_barcode_history, null, false);
                holder = new BarcodeHistory.ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (BarcodeHistory.ViewHolder) convertView.getTag();
            }

            holder.getNameText().setText(product.Name);
            holder.getTypeText().setText(product.Type);
            holder.getCodeText().setText(product.Barcode);


            return convertView;
        }

    }


}
