package com.me_market.android.memarket;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.me_market.android.memarket.components.BaseActivity;
import com.me_market.android.memarket.models.Product;

import java.util.ArrayList;
import java.util.Locale;

import static com.me_market.android.memarket.BarcodeReader.PRODUCT_ID;
import static com.me_market.android.memarket.ProductActivity.SELECT_UI;
import static com.me_market.android.memarket.SplashActivity.USER_ID;

public class ShoppingListActivity extends BaseActivity implements View.OnClickListener {
    private FirebaseDatabase mDatabase;
    private String mUserId;
    private ListView listView;
    private ArrayList<Product> productsList;
    private static final int RC_SELECT_PRODUCT = 9002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        //Getting extra
        mUserId = getIntent().getStringExtra(USER_ID);

        //Get Firebase Instance
        mDatabase = FirebaseDatabase.getInstance();

        //Setting FAB listener
        findViewById(R.id.shopping_list_fab).setOnClickListener(this);

        //Setting Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_cart_toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(R.string.my_cart_label);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        //Setting ListView
        listView = (ListView) findViewById(R.id.shopping_list_listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (productsList != null) {
                    startActivity(new Intent(ShoppingListActivity.this, ProductActivity.class)
                            .putExtra(PRODUCT_ID, productsList.get(position).getId())
                    );
                }
            }
        });

        //Getting the product shopping list
        if (mUserId != null) {
            getShoppingListFromFirebase();
        }

    }

    private void getShoppingListFromFirebase() {

        showProgressDialog(getString(R.string.loading));

        DatabaseReference myRef;
        ValueEventListener myListener;

        productsList=new ArrayList<>();
        myRef = mDatabase.getReference().child("shopping_list").child(mUserId);
        myListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {
                Product product;

                if (dataSnapshot.getChildren() != null) {
                    for (DataSnapshot resultSnapshot : dataSnapshot.getChildren()) {
                        if (resultSnapshot != null) {
                            product = resultSnapshot.getValue(Product.class);
                            product.setId(resultSnapshot.getKey());
                            productsList.add(product);
                        }
                    }
                }
                setShoppingListOnListView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        myRef.addListenerForSingleValueEvent(myListener);


    }

    private void setShoppingListOnListView() {
        listView = (ListView) findViewById(R.id.shopping_list_listview);
        if (productsList != null) {
            ShoppingListActivity.productArrayAdapter adapter = new ShoppingListActivity.productArrayAdapter(this, R.layout.shopping_list_listview_layout, productsList);
            listView.setAdapter(adapter);
        } else {
            listView.setAdapter(null);
        }
        hideProgressDialog();
    }

    public class ViewHolder {
        private View row;
        private CheckBox checkBox;
        private TextView name = null, type = null, qty = null;
        private Button erase_button;

        ViewHolder(View row) {
            this.row = row;
        }


        public CheckBox getCheckBox() {
            if (this.checkBox == null) {
                this.checkBox = (CheckBox) row.findViewById(R.id.shopping_list_checkBox);
            }
            return this.checkBox;
        }

        public TextView getNameText() {
            if (this.name == null) {
                this.name = (TextView) row.findViewById(R.id.shopping_list_product_name);
            }
            return this.name;
        }

        public TextView getTypeText() {
            if (this.type == null) {
                this.type = (TextView) row.findViewById(R.id.shopping_list_product_type);
            }
            return this.type;
        }


        public TextView getQtyText() {
            if (this.qty == null) {
                this.qty = (TextView) row.findViewById(R.id.shopping_list_product_qty);
            }
            return this.qty;
        }

        public Button getButton() {
            if (this.erase_button == null) {
                this.erase_button = (Button) row.findViewById(R.id.shopping_list_product_delete_button);
            }
            return this.erase_button;
        }

    }

    class productArrayAdapter extends ArrayAdapter<Product> {
        private Context context;
        private ArrayList<Product> products;

        public productArrayAdapter(Context context, int resource, ArrayList<Product> products) {
            super(context, resource, products);
            this.context = context;
            this.products = products;

        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            //get the property we are displaying
            Product product = products.get(position);

            //get the inflater and inflate the XML layout for each item
            ShoppingListActivity.ViewHolder holder = null;
            LayoutInflater inflater = getLayoutInflater();
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.shopping_list_listview_layout, null, false);
                holder = new ShoppingListActivity.ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ShoppingListActivity.ViewHolder) convertView.getTag();
            }

            holder.getNameText().setText(product.Name);
            holder.getTypeText().setText(product.Type);
            String qty = String.format(Locale.getDefault(), "%f", product.Quantity); //TODO: Check quantity
            holder.getQtyText().setText(qty);

            holder.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    eraseProduct(position);
                }
            });


            return convertView;
        }

    }

    private void eraseProduct(int position) {
        showProgressDialog(getString(R.string.loading));

        DatabaseReference myRef;
        Product product;

        product = productsList.get(position);
        myRef = mDatabase.getReference().child("shopping_list").child(mUserId).child(product.getId());
        myRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                getShoppingListFromFirebase();
            }
        });

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        switch (i) {
            case R.id.shopping_list_fab:
                addProduct();
                break;
        }

    }

    private void addProduct() {
        startActivityForResult(new Intent(ShoppingListActivity.this,ProductActivity.class) .putExtra(SELECT_UI, "Select"),RC_SELECT_PRODUCT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Returning from barcode capture activity
        if (requestCode == RC_SELECT_PRODUCT) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String mProductId = data.getStringExtra(PRODUCT_ID);
                    addProductToFirebase(mProductId);
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

    private void addProductToFirebase(String id) {
        DatabaseReference myRef;
        ValueEventListener myListener;

        myRef = mDatabase.getReference().child("products").child(id);
        myListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {
                Product product;

                product = dataSnapshot.getValue(Product.class);
                if (product != null) {
                    product.setId(dataSnapshot.getKey());
                    DatabaseReference myWriteRef = mDatabase.getReference();
                    myWriteRef.child("shopping_list").child(mUserId).child(product.getId()).setValue(product);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        myRef.addListenerForSingleValueEvent(myListener);
    }
}
